package uk.oczadly.karl.csgsi.util.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.OSUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a set of static constant fields and methods to facilitate in the automated location of Steam and
 * game directories on the system.
 */
public class SteamUtils {
    
    private static final Logger log = LoggerFactory.getLogger(SteamUtils.class);

    private static final Pattern LIB_MANIFEST_PATTERN = Pattern.compile("^\\s{2}\"path\"\\s+\"(.+)\"$");
    private static final Pattern APP_MANIFEST_PATTERN = Pattern.compile("^\\s\"installdir\"\\s+\"(.+)\"$");

    /** The relative (to the Steam installation or library) folder for game metadata. */
    public static final String APPS_DIR = "steamapps";
    
    /** The relative (to the Steam installation or library) folder for game install directories. */
    public static final String APPS_INSTALL_DIR = APPS_DIR + "/common";
    
    /**
     * The relative (to the Steam installation or library) path for the manifest file containing a list of configured
     * game library directories.
     */
    public static final String LIBRARY_MANIFEST_FILE = APPS_DIR + "/libraryfolders.vdf";
    
    
    /**
     * Obtains the current installation path for the Steam client on this computer.
     *
     * <p>Currently this method is only supported on Windows, Linux and Macintosh operating systems due to
     * complications between the various different file systems. If an unsupported OS is detected, the method will
     * throw a {@link GameNotFoundException} with an appropriate message.</p>
     *
     * @return the Steam installation path on this machine
     *
     * @throws SteamNotFoundException if a Steam installation couldn't be found or there was an error in the process
     */
    public static Path locateSteamInstallation() throws SteamNotFoundException {
        String homePath = System.getProperty("user.home", "");
        Set<Path> candidatePaths = new LinkedHashSet<>(); // Ordered set of potential installation dirs
        try {
            switch (OSUtil.getSystemFamily()) {
                case WINDOWS:
                    // Attempt to read from registry
                    String regVal = OSUtil.readWinRegValue("HKCU\\Software\\Valve\\Steam", "SteamPath");
                    if (regVal != null) {
                        log.debug("Obtained Steam installation dir from registry ({})", regVal);
                        candidatePaths.add(Paths.get(regVal));
                    }
                    // Common installation directories
                    candidatePaths.add(Paths.get("C:\\Program Files (x86)\\Steam"));
                    candidatePaths.add(Paths.get("C:\\Program Files\\Steam"));
                    break;
                case UNIX:
                case LINUX: // todo: untested
                    candidatePaths.add(Paths.get(homePath, ".local/share/Steam"));
                    candidatePaths.add(Paths.get(homePath, ".steam/steam"));
                    break;
                case MAC: // todo: untested
                    candidatePaths.add(Paths.get(homePath, "Library/Application Support/Steam"));
                    break;
                default:
                    throw new SteamNotFoundException("Unrecognized or unsupported operating system.");
            }
        } catch (InvalidPathException e) {
            throw new SteamNotFoundException("Expected Steam path was rejected by the filesystem.", e);
        }
        log.debug("Located a total of {} possible Steam installation candidates", candidatePaths.size());
        
        // Search for valid path
        for (Path p : candidatePaths) {
            try {
                if (Files.isDirectory(p)) return p; // Exists!
            } catch (SecurityException e) {
                log.warn("Could not read potential Steam directory file {}", p.toAbsolutePath(), e);
            }
        }
        // No suitable path found
        throw new SteamNotFoundException("No Steam installation directory was found.");
    }
    
    
    /**
     * <p>Obtains a list of Steam game installation directories configured within the client. This includes library
     * directories on differing drives from the Steam installation or operating system.</p>
     *
     * <p>The returned directories are only the root folder; to access raw game files you will need to enter the
     * {@value #APPS_DIR} relative subdirectories.</p>
     *
     * @return a list of Steam library directories
     *
     * @throws SteamNotFoundException if no Steam installation or library directories are located
     */
    public static Set<Path> findSteamLibraryPaths() throws SteamNotFoundException {
        Path steamDir = locateSteamInstallation();
        Path manifest = steamDir.resolve(LIBRARY_MANIFEST_FILE);
        
        Set<Path> paths = new HashSet<>();
        paths.add(steamDir); // Add root Steam install dir
        try {
            if (Files.exists(manifest)) {
                try {
                    List<Matcher> matchers = matchManifestLines(manifest, LIB_MANIFEST_PATTERN);
                    for (Matcher matcher : matchers) {
                        Path path = Paths.get(matcher.group(1));
                        try {
                            if (Files.isDirectory(path))
                                paths.add(path);
                        } catch (SecurityException e) {
                            log.warn("SecurityException occurred while checking Steam library path \"{}\"", path, e);
                        }
                    }
                    return paths;
                } catch (IOException e) {
                    throw new SteamNotFoundException("Failed to read the Steam library configuration file.", e);
                }
            } else {
                throw new SteamNotFoundException("Failed to locate the Steam library configuration file.");
            }
        } catch (SecurityException e) {
            throw new SteamNotFoundException("Unable to read the Steam library configuration file.", e);
        }
    }
    

    /**
     * Attempts to locate the game/application installation directory of the specified app ID.
     *
     * @param appId the official registered Steam ID of the application
     * @return the path of the found application directory
     *
     * @throws SteamNotFoundException if the specified Steam installation could not be located
     * @throws GameNotFoundException  if the specified game installation could not be located
     * @throws SecurityException      if the security manager disallows access to the directory
     */
    public static Path locateAppInstallation(int appId) throws GameNotFoundException {
        try {
            for (Path p : findSteamLibraryPaths()) {
                Path manifest = p.resolve(APPS_DIR).resolve("appmanifest_" + appId + ".acf");
                if (Files.isRegularFile(manifest)) {
                    List<Matcher> matchers = matchManifestLines(manifest, APP_MANIFEST_PATTERN);
                    if (matchers.size() == 1) {
                        String gameDirName = matchers.get(0).group(1);
                        Path gameDir = p.resolve(APPS_INSTALL_DIR).resolve(gameDirName);
                        if (Files.isDirectory(gameDir)) {
                            return gameDir;
                        }
                    } else {
                        throw new GameNotFoundException("Found duplicate installations of the app!");
                    }
                }
            }
        } catch (IOException e) {
            throw new GameNotFoundException("Could not read game manifest with ID " + appId + ".", e);
        }
        throw new GameNotFoundException("Could not locate directory for game ID " + appId + ".");
    }
    

    private static List<Matcher> matchManifestLines(Path filePath, Pattern pattern) throws IOException {
        if (!Files.isRegularFile(filePath))
            throw new FileNotFoundException();
        
        List<Matcher> matchers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                    matchers.add(matcher);
            }
        }
        return matchers;
    }
    
}
