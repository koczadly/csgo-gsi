package uk.oczadly.karl.csgsi.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class SteamUtils {
    
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
     * <p>Currently this method is only supported on Windows, Linux and Macintosh operating systems due to
     * complications and restrictions between the various different filesystem. If an unsupported OS is detected,
     * the method will throw a {@link SteamLibraryException}.</p>
     *
     * @return the Steam installation directory
     * @throws SteamLibraryException if a Steam installation couldn't be found or there was an error in the process
     */
    public static Path getSteamInstallDirectory() throws SteamLibraryException {
        String os = System.getProperty("os.name").toLowerCase();
        
        Path foundPath;
        try {
            if (os.contains("linux")) { //Linux-based
                foundPath = Paths.get(System.getProperty("user.home"), ".local/share/Steam"); //TODO Untested(?)
            } else if (os.contains("win")) { //Windows
                //Attempt to read from registry
                String regVal = readWinRegValue("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath");
                
                if (regVal != null) {
                    //Directory found in registry, use
                    return Paths.get(regVal);
                } else {
                    //Registry value not found, use common path
                    foundPath = Paths.get("C:\\Program Files (x86)\\Steam");
                    if (!Files.isDirectory(foundPath)) {
                        //Not found, try 32 bit version
                        foundPath = Paths.get("C:\\Program Files\\Steam");
                    }
                }
            } else if (os.contains("mac")) { //Mac
                foundPath = Paths.get(System.getProperty("user.home"), "Library/Application Support/Steam"); //TODO Untested(?)
            } else { //Unknown OS type
                throw new SteamLibraryException("Unknown or unsupported operating system \"" + os + "\".");
            }
        } catch (InvalidPathException e1) {
            throw new SteamLibraryException("Expected Steam path was rejected by the filesystem.", e1);
        }
        
        if (!Files.isDirectory(foundPath))
            throw new SteamLibraryException();
        
        return foundPath;
    }
    
    
    /**
     * <p>Obtains a list of Steam game installation directories configured within the client. This includes library
     * directories on differing drives from the Steam installation or operating system.</p>
     * <p>The returned directories are only the root folder; to access raw game files you will need to enter the
     * {@value #STEAM_APPS_FOLDER} relative subdirectories.</p>
     *
     * @return a list of Steam library directories
     * @throws SteamLibraryException if no Steam installation or library directories are located
     */
    public static Set<Path> getSteamLibraries() throws SteamLibraryException {
        Path steamDir = getSteamInstallDirectory();
        Path libFile = steamDir.resolve(STEAM_LIBRARY_FOLDERS);
        if (Files.exists(libFile)) {
            try {
                Set<Path> paths = new HashSet<>();
                
                //Add Steam install dir
                paths.add(steamDir);
                
                BufferedReader reader = Files.newBufferedReader(libFile);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("\t")) {
                        /* Values follow the format: [tab]"id"[tab][tab]"value"
                           ID is index 1 and value is index 3 */
                        
                        String[] vals = line.split("\t");
                        if (vals.length == 4) {
                            try {
                                //Verify first value is an integer - if not, ignore property in file
                                //Substring is to remove surrounding quotes
                                Integer.parseInt(vals[1].substring(1, vals[1].length() - 1));
                                
                                Path path = Paths.get(vals[3].substring(1, vals[3].length() - 1));
                                if (Files.isDirectory(path))
                                    paths.add(path);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                reader.close();
                
                return paths;
            } catch (IOException e) {
                throw new SteamLibraryException("Unable to read Steam's library file.", e);
            }
        } else {
            throw new SteamLibraryException("Failed to locate Steam's library file.");
        }
    }
    
    
    /**
     * <p>Attempts to locate the installation directory for the specified application/game title. The title must match
     * the installation directory name (within the {@value #STEAM_APPS_FOLDER} subdirectory), which is not necessarily
     * the title displayed on the Steam platform or client.</p>
     *
     * @param name the installation name of the application
     * @return the path of the found directory, or null if the application can't be found
     * @throws SteamLibraryException if no Steam installation or library directories are located
     */
    public static Path findApplicationDirectoryByName(String name) throws SteamLibraryException {
        for(Path p : getSteamLibraries()) {
            Path gamePath = p.resolve(STEAM_APPS_FOLDER).resolve(name);
            if (Files.isDirectory(gamePath)) {
                return gamePath; //File exists as valid directory, return
            }
        }
        return null;
    }
    
    
    /**
     * Attempts to locate the CS:GO configuration folder installed on this computer.
     *
     * @return the CS:GO configuration folder, or null if the game can't be found
     * @throws SteamLibraryException if no Steam installation or library directories are located
     * @see GSIProfile#createConfig(Path, GSIProfile, String)
     */
    public static Path findCsgoConfigFolder() throws SteamLibraryException {
        Path gameDir = findApplicationDirectoryByName(CSGO_DIR_NAME);
        if (gameDir != null) {
            return gameDir.resolve(CSGO_CONFIG_PATH);
        } else {
            return null; //Game not found
        }
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
            
            if (line != null) {
                return line.substring(line.indexOf("REG_SZ") + 10);
            } else {
                return null;
            }
        } catch (IOException e2) {
            return null;
        }
    }

}
