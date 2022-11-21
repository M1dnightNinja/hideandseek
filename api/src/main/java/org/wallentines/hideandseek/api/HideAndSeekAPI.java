package org.wallentines.hideandseek.api;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.hideandseek.api.core.ContentRegistry;
import org.wallentines.hideandseek.api.core.SessionManager;
import org.wallentines.hideandseek.api.game.map.ResourcePackData;
import org.wallentines.hideandseek.api.game.timer.GameTimer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class HideAndSeekAPI {

    protected static final Logger LOGGER = LogManager.getLogger("HideAndSeek");
    private static HideAndSeekAPI INSTANCE;

    protected HideAndSeekAPI() {
        if(INSTANCE == null) INSTANCE = this;
    }

    /**
     * Returns the main config for this mod
     * 
     * @return The main config
     */
    public abstract ConfigSection getConfig();

    /**
     * Returns the folder where this mod stores data
     * 
     * @return The data folder
     */
    public abstract File getDataFolder();


    public abstract File getMapFolder();

    /**
     * Returns the global content registry, which contains lobbies, maps, classes, game types, and roles
     *
     * @return The content registry
     */
    public abstract ContentRegistry getContentRegistry();

    /**
     * Returns the global session manager, which contains all running sessions
     *
     * @return The session manager
     */
    public abstract SessionManager getSessionManager();

    /**
     * Returns the global lang provider
     *
     * @return The lang provider
     */
    public abstract LangProvider getLangProvider();


    public abstract GameTimer createTimer(MComponent component, int time, Consumer<Integer> onTick);


    public abstract GameTimer createTimer(Function<MPlayer, MComponent> componentFunc, int time, Consumer<Integer> onTick);


    public abstract ResourcePackData getServerResourcePack();


    public abstract void reload();

    /**
     * Returns the global MidnightCoreAPI instance
     *
     * @return The global api
     */
    public static HideAndSeekAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the global MidnightCoreAPI logger
     *
     * @return The logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }


}
