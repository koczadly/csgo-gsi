package uk.oczadly.karl.csgsi.util.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class contains a set of static constants and methods to facilitate in the automated location of CSGO files.
 */
public class CsgoUtils {

    private static final Logger log = LoggerFactory.getLogger(CsgoUtils.class);

    /** The CS:GO Steam app ID number. */
    public static final int STEAM_APP_ID = 730;

    private static final String CONFIG_PATH = "csgo/cfg";

    private static volatile Path cachedGamePath;


    /**
     * Attempts to locate the path of the CS:GO game installation on this computer.
     *
     * @return the CS:GO installation directory
     *
     * @throws GameNotFoundException if the CSGO or Steam installations could not be located
     * @throws SecurityException     if the security manager disallows access to the directory
     */
    public static Path locateInstallationPath() throws GameNotFoundException {
        if (cachedGamePath == null || !Files.isDirectory(cachedGamePath)) { // Not cached or no longer exists
            synchronized (CsgoUtils.class) {
                log.debug("Attempting to locate CSGO installation directory...");
                cachedGamePath = SteamUtils.locateAppInstallation(STEAM_APP_ID);
            }
        }
        return cachedGamePath;
    }

    /**
     * Attempts to locate the CS:GO configuration folder installed on this computer.
     *
     * @return the CS:GO configuration folder
     *
     * @throws GameNotFoundException if the CSGO or Steam installations could not be located
     * @throws SecurityException     if the security manager disallows access to the directory
     */
    public static Path locateConfigDirectory() throws GameNotFoundException {
        Path path = locateInstallationPath().resolve(CONFIG_PATH);
        if (!Files.isDirectory(path))
            throw new GameNotFoundException("Found game installation, but configuration directory is missing!");
        return path;
    }

}
