package uk.oczadly.karl.csgsi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.Util;

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
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamUtils.class);
    
    private static final Pattern STEAM_VDF_PATTERN = Pattern.compile("^\\s+\"\\d+\"\\s+\"(.+)\"$");
    private static final Pattern STEAM_ACF_PATTERN = Pattern.compile("^\\s+\"installdir\"\\s+\"(.+)\"$");
    
    private static volatile Path cachedCsgoConfigPath;
    
    
    /** The CS:GO Steam app ID number. */
    public static final int CSGO_STEAM_ID = 730;
    
    /** The name of the CS:GO configuration folder, relative to the game dir. */
    public static final String CSGO_CONFIG_PATH = "csgo/cfg";
    
    
    /** The relative Steam folder for game metadata. */
    public static final String STEAM_APPS_DIR = "steamapps";
    
    /** The relative Steam folder for game install directories. */
    public static final String STEAM_GAME_INSTALL_DIR = STEAM_APPS_DIR + "/common";
    
    /** The relative Steam file path for the {@code .vfd} containing a list of game library dirs. */
    public static final String STEAM_LIBRARY_FOLDERS = STEAM_APPS_DIR + "/libraryfolders.vdf";
    
    
    /**
     * <p>Obtains the primary installation directory for the Steam client on this computer system.</p>
     * <p>Currently this method is only supported on Windows, Linux and Macintosh operating systems due to
     * complications
     * between the various different file systems. If an unsupported OS is detected, the method will throw a {@link
     * GameNotFoundException} with an appropriate message.</p>
     *
     * @return the Steam installation directory
     *
     * @throws SteamNotFoundException if a Steam installation couldn't be found or there was an error in the process
     */
    public static Path getSteamInstallDirectory() throws SteamNotFoundException {
        String os = System.getProperty("os.name").toLowerCase(); // Obtain current OS name
        String homePath = System.getProperty("user.home");
        
        Set<Path> candidatePaths = new LinkedHashSet<>(); // Ordered set of potential installation dirs
        try {
            if (os.contains("linux")) { // Linux, TODO: untested
                candidatePaths.add(Paths.get(homePath, ".local/share/Steam"));
                candidatePaths.add(Paths.get(homePath, ".steam"));
            } else if (os.contains("win")) { // Windows
                // Attempt to read from registry
                String regVal = Util.readWinRegValue("HKCU\\Software\\Valve\\Steam", "SteamPath");
                if (regVal != null) {
                    LOGGER.debug("Obtained Steam installation dir from registry ({})", regVal);
                    candidatePaths.add(Paths.get(regVal));
                }
                
                // Common installation directories
                candidatePaths.add(Paths.get("C:\\Program Files (x86)\\Steam"));
                candidatePaths.add(Paths.get("C:\\Program Files\\Steam"));
            } else if (os.contains("mac")) { // Mac TODO: untested
                candidatePaths.add(Paths.get(homePath, "Library/Application Support/Steam"));
            } else { // Unknown OS type
                throw new SteamNotFoundException("Unknown or unsupported operating system.");
            }
        } catch (InvalidPathException e) {
            throw new SteamNotFoundException("Expected Steam path was rejected by the filesystem.", e);
        }
        
        LOGGER.debug("Located a total of {} possible Steam installation candidates", candidatePaths.size());
        
        // Search for valid path
        for (Path p : candidatePaths) {
            try {
                if (Files.isDirectory(p))
                    return p; // Exists!
            } catch (SecurityException e) {
                LOGGER.warn("Could not read potential Steam directory file {}", p.toAbsolutePath(), e);
            }
        }
        
        // No suitable path found by this point
        throw new SteamNotFoundException("The Steam installation directory could not be found.");
    }
    
    
    /**
     * <p>Obtains a list of Steam game installation directories configured within the client. This includes library
     * directories on differing drives from the Steam installation or operating system.</p>
     *
     * <p>The returned directories are only the root folder; to access raw game files you will need to enter the
     * {@value #STEAM_APPS_DIR} relative subdirectories.</p>
     *
     * @return a list of Steam library directories
     *
     * @throws SteamNotFoundException if no Steam installation or library directories are located
     */
    public static Set<Path> getSteamLibraries() throws SteamNotFoundException {
        Path steamDir = getSteamInstallDirectory();
        Path libFile = steamDir.resolve(STEAM_LIBRARY_FOLDERS);
        
        Set<Path> paths = new HashSet<>();
        paths.add(steamDir); //Add Steam install dir
        
        try {
            if (Files.exists(libFile)) {
                try {
                    List<Matcher> matchers = matchSteamFile(libFile, STEAM_VDF_PATTERN);
                    for (Matcher matcher : matchers) {
                        Path path = Paths.get(matcher.group(1));
                        try {
                            if (Files.isDirectory(path))
                                paths.add(path);
                        } catch (SecurityException e) {
                            LOGGER.warn("SecurityException occured while checking Steam library path \""
                                    + path.toString() + "\"", e);
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
     * Attempts to locate the installation directory for the specified application/game title.
     *
     * <p>The title must match the installation directory name (within the {@value #STEAM_GAME_INSTALL_DIR} subdirectory),
     * which is not necessarily the title displayed on the Steam platform or client.</p>
     *
     * @param name the installation name of the application
     * @return the path of the found application directory
     *
     * @throws SteamNotFoundException if the specified Steam installation could not be located
     * @throws GameNotFoundException  if the specified game installation could not be located
     * @throws SecurityException      if the security manager disallows access to the directory
     *
     * @deprecated Use of {@link #findGameDirectoryById(int)} is preferred for consistency.
     */
    @Deprecated
    public static Path findGameDirectoryByName(String name) throws GameNotFoundException {
        for (Path p : getSteamLibraries()) {
            Path gamePath = p.resolve(STEAM_GAME_INSTALL_DIR).resolve(name);
            if (Files.isDirectory(gamePath)) {
                return gamePath; //File exists as valid directory, return
            }
        }
        throw new GameNotFoundException("Could not locate game directory with name \"" + name + "\".");
    }
    
    /**
     * Attempts to locate the installation directory for the specified application/game title.
     *
     * <p>The title must match the installation directory name (within the {@value #STEAM_GAME_INSTALL_DIR} subdirectory),
     * which is not necessarily the title displayed on the Steam platform or client.</p>
     *
     * @param id the official Steam ID number of the application
     * @return the path of the found application directory
     *
     * @throws SteamNotFoundException if the specified Steam installation could not be located
     * @throws GameNotFoundException  if the specified game installation could not be located
     * @throws SecurityException      if the security manager disallows access to the directory
     */
    public static Path findGameDirectoryById(int id) throws GameNotFoundException {
        try {
            for (Path p : getSteamLibraries()) {
                Path manifest = p.resolve(STEAM_APPS_DIR).resolve("appmanifest_" + id + ".acf");
                if (Files.isRegularFile(manifest)) {
                    List<Matcher> matchers = matchSteamFile(manifest, STEAM_ACF_PATTERN);
                    if (matchers.size() == 1) {
                        String gameDirName = matchers.get(0).group(1);
                        Path gameDir = p.resolve(STEAM_GAME_INSTALL_DIR).resolve(gameDirName);
                        if (Files.isDirectory(gameDir))
                            return gameDir;
                    }
                }
            }
        } catch (IOException e) {
            throw new GameNotFoundException("Could not read game manifest with ID \"" + id + "\".", e);
        }
        throw new GameNotFoundException("Could not locate directory for game ID \"" + id + "\".");
    }
    
    
    /**
     * Attempts to locate the CS:GO configuration folder installed on this computer.
     *
     * @return the CS:GO configuration folder
     *
     * @throws GameNotFoundException if the CSGO or Steam installations could not be located
     * @throws SecurityException     if the security manager disallows access to the directory
     */
    public static Path locateCsgoConfigFolder() throws GameNotFoundException {
        if (cachedCsgoConfigPath == null || !Files.isDirectory(cachedCsgoConfigPath)) { // Not cached or no longer exists
            synchronized (SteamUtils.class) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Attempting to locate CSGO configuration directory...");
                cachedCsgoConfigPath = findGameDirectoryById(CSGO_STEAM_ID).resolve(CSGO_CONFIG_PATH);
            }
        }
        return cachedCsgoConfigPath;
    }
    
    
    private static List<Matcher> matchSteamFile(Path filePath, Pattern pattern) throws IOException {
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
