package uk.oczadly.karl.csgsi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SteamUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamUtils.class);
    
    
    /** Installation folder name */
    private static final String CSGO_DIR_NAME = "Counter-Strike Global Offensive";
    /** Config path relative to game dir */
    private static final String CSGO_CONFIG_PATH = "csgo/cfg";
    
    /** Relative folder for list of game library dirs */
    private static final String STEAM_LIBRARY_FOLDERS = "steamapps/libraryfolders.vdf";
    /** Relative folder for game install dirs */
    private static final String STEAM_APPS_FOLDER = "steamapps/common";
    
    
    
    /**
     * <p>Obtains the primary installation directory for the Steam client on this computer system.</p>
     * <p>Currently this method is only supported on Windows, Linux and Macintosh operating systems due to complications
     * between the various different file systems. If an unsupported OS is detected, the method will throw a
     * {@link SteamDirectoryException} with an appropriate message.</p>
     *
     * @return the Steam installation directory
     * @throws SteamDirectoryException if a Steam installation couldn't be found or there was an error in the process
     */
    public static Path getSteamInstallDirectory() throws SteamDirectoryException {
        String os = System.getProperty("os.name").toLowerCase(); //Obtain current OS name
        String homePath = System.getProperty("user.home");
        
        Set<Path> candidatePaths = new LinkedHashSet<>(); //Ordered set of potential installation dirs
        try {
            if (os.contains("linux")) { //Linux-based, TODO untested
                candidatePaths.add(Paths.get(homePath, ".local/share/Steam"));
                candidatePaths.add(Paths.get(homePath, ".steam"));
            } else if (os.contains("win")) { //Windows
                //Attempt to read from registry
                String regVal = readWinRegValue("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath");
                if (regVal != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Obtained Steam installation dir from registry ({})", regVal);
                    }
                    
                    candidatePaths.add(Paths.get(regVal));
                }
                
                //Common installation directories
                candidatePaths.add(Paths.get("C:\\Program Files (x86)\\Steam")); //64 bit
                candidatePaths.add(Paths.get("C:\\Program Files\\Steam")); //32 bit
            } else if (os.contains("mac")) { //Mac TODO untested
                candidatePaths.add(Paths.get(homePath, "Library/Application Support/Steam"));
            } else { //Unknown OS type
                throw new SteamDirectoryException("Unknown or unsupported operating system.");
            }
        } catch (InvalidPathException e) {
            throw new SteamDirectoryException("Expected Steam path was rejected by the filesystem.", e);
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Located a total of {} possible Steam installation candidates", candidatePaths.size());
        }
        
        //Search for valid path
        boolean caughtSecurityEx = false;
        for (Path p : candidatePaths) {
            try {
                if (Files.isDirectory(p)) {
                    return p; //Exists!
                }
            } catch (SecurityException e) {
                caughtSecurityEx = true;
            }
        }
        
        //No suitable path found
        throw new SteamDirectoryException("No Steam installation directory could be located."
                + (caughtSecurityEx ? " Additionally, a SecurityException was caught during the process." : ""));
    }
    
    
    /**
     * <p>Obtains a list of Steam game installation directories configured within the client. This includes library
     * directories on differing drives from the Steam installation or operating system.</p>
     * <p>The returned directories are only the root folder; to access raw game files you will need to enter the
     * {@value #STEAM_APPS_FOLDER} relative subdirectories.</p>
     *
     * @return a list of Steam library directories
     * @throws SteamDirectoryException if no Steam installation or library directories are located
     */
    public static Set<Path> getSteamLibraries() throws SteamDirectoryException {
        Path steamDir = getSteamInstallDirectory();
        Path libFile = steamDir.resolve(STEAM_LIBRARY_FOLDERS);
    
        Set<Path> paths = new HashSet<>();
        paths.add(steamDir); //Add Steam install dir
        
        try {
            if (Files.exists(libFile)) {
                try (BufferedReader reader = Files.newBufferedReader(libFile)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("\t")) {
                        /* Values follow the format: [tab]"id"[tab][tab]"value"
                           ID is index 1 and value is index 3 */
                            
                            String[] vals = line.split("\t");
                            if (vals.length == 4) {
                                try {
                                    /* Verify first value is an integer - if not, ignore property in file
                                       Substring is to remove surrounding quotes */
                                    Integer.parseInt(vals[1].substring(1, vals[1].length() - 1));
                                    
                                    Path path = Paths.get(vals[3].substring(1, vals[3].length() - 1));
                                    try {
                                        if (Files.isDirectory(path)) {
                                            paths.add(path);
                                        }
                                    } catch (SecurityException e) {
                                        LOGGER.warn("SecurityException occured while checking Steam library path \""
                                                + path.toString() + "\"", e);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }
                    reader.close();
                    
                    return paths;
                } catch (IOException e) {
                    throw new SteamDirectoryException("Failed to read the Steam library configuration file.", e);
                }
            } else {
                throw new SteamDirectoryException("Failed to locate the Steam library configuration file.");
            }
        } catch (SecurityException e) {
            throw new SteamDirectoryException("Unable to read the Steam library configuration file.", e);
        }
    }
    
    
    /**
     * <p>Attempts to locate the installation directory for the specified application/game title. The title must match
     * the installation directory name (within the {@value #STEAM_APPS_FOLDER} subdirectory), which is not necessarily
     * the title displayed on the Steam platform or client.</p>
     *
     * @param name the installation name of the application
     * @return the path of the found directory, or null if the application can't be found
     * @throws SteamDirectoryException  if no Steam installation or library directories are located
     * @throws SecurityException        if the current security manager disallows access to the directory
     */
    public static Path findApplicationDirectoryByName(String name) throws SteamDirectoryException {
        for (Path p : getSteamLibraries()) {
            Path gamePath = p.resolve(STEAM_APPS_FOLDER).resolve(name);
            if (Files.isDirectory(gamePath)) {
                return gamePath; //File exists as valid directory, return
            }
        }
        return null; //No dir found, return null
    }
    
    
    /**
     * Attempts to locate the CS:GO configuration folder installed on this computer.
     *
     * @return the CS:GO configuration folder, or null if the game can't be found
     * @throws SteamDirectoryException  if no Steam installation or library directories are located
     * @throws SecurityException        if the current security manager disallows access to the directory
     * @see GSIProfile#createConfig(Path, GSIProfile, String)
     */
    public static Path findCsgoConfigFolder() throws SteamDirectoryException {
        Path gameDir = findApplicationDirectoryByName(CSGO_DIR_NAME);
        if (gameDir != null) {
            return gameDir.resolve(CSGO_CONFIG_PATH);
        }
        return null; //Game not found
    }
    
    
    
    /** Helper method to read Windows registry keys */
    private static String readWinRegValue(String path, String key) {
        try {
            Process proc = Runtime.getRuntime().exec("reg query \"" + path + "\" /v \"" + key + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null && !line.startsWith("    ")); //Loop until line with delimiter
            reader.close();
            proc.destroy();
            
            if (line != null)
                return line.substring(line.indexOf("REG_SZ") + 10);
        } catch (IOException | IndexOutOfBoundsException e) {
            LOGGER.warn("Failed to read registry key {} at path {}", key, path, e);
        }
        return null;
    }

}
