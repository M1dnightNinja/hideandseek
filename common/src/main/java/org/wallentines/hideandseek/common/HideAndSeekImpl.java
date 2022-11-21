package org.wallentines.hideandseek.common;

import org.wallentines.hideandseek.api.core.ContentRegistry;
import org.wallentines.hideandseek.api.core.SessionManager;
import org.wallentines.hideandseek.api.game.map.ResourcePackData;
import org.wallentines.hideandseek.api.game.timer.GameTimer;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.common.game.timer.AbstractTimer;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.hideandseek.api.HideAndSeekAPI;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HideAndSeekImpl extends HideAndSeekAPI {

    private final FileConfig config;
    private final File dataFolder;
    private final File mapFolder;

    private final SessionManager manager;

    private LangProvider provider;
    private final AbstractTimer.TimerCreator timerCreator;
    private final Supplier<ResourcePackData> serverPack;

    private static BiConsumer<MPlayer, PlayerClass> classApplier;

    public HideAndSeekImpl(Path dataFolder, SessionManager manager, AbstractTimer.TimerCreator timerCreator, Supplier<ResourcePackData> serverPack, BiConsumer<MPlayer, PlayerClass> classApplier) {

        super();

        HideAndSeekImpl.classApplier = classApplier;

        this.dataFolder = FileUtil.tryCreateDirectory(dataFolder);
        if(this.dataFolder == null) {
            throw new IllegalStateException("Unable to create data folder!");
        }

        this.mapFolder = dataFolder.resolve("maps").toFile();
        this.manager = manager;
        this.timerCreator = timerCreator;
        this.serverPack = serverPack;
        this.config = FileConfig.findOrCreate("config", this.dataFolder, Constants.CONFIG_DEFAULTS);
        if(this.config == null) {
            throw new IllegalStateException("Unable to create config!");
        }
        this.config.save();

        Constants.registerDefaults();
    }

    public void loadContents(ConfigSection defaultLang) {

        this.provider = new LangProvider(dataFolder.toPath().resolve("lang"), defaultLang);
        Constants.registerPlaceholders(PlaceholderManager.INSTANCE);

        reload();
    }

    @Override
    public ConfigSection getConfig() {

        return config.getRoot();
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getMapFolder() {
        return mapFolder;
    }

    @Override
    public ContentRegistry getContentRegistry() {
        return ContentRegistryImpl.INSTANCE;
    }

    @Override
    public SessionManager getSessionManager() {
        return manager;
    }

    @Override
    public LangProvider getLangProvider() {
        return provider;
    }

    @Override
    public GameTimer createTimer(MComponent component, int time, Consumer<Integer> onTick) {
        return timerCreator.create(mpl -> component, time, onTick);
    }

    @Override
    public GameTimer createTimer(Function<MPlayer, MComponent> componentFunc, int time, Consumer<Integer> onTick) {
        return timerCreator.create(componentFunc, time, onTick);
    }

    @Override
    public ResourcePackData getServerResourcePack() {
        return serverPack.get();
    }

    @Override
    public void reload() {

        ContentRegistryImpl reg = ContentRegistryImpl.INSTANCE;
        getSessionManager().getModule().shutdownAll();

        reg.clearPermissions();
        reg.clearClasses();
        reg.clearLobbies();
        reg.clearMaps();
        reg.clearRoleData();

        FileConfig lobbies = FileConfig.findOrCreate("lobbies", this.dataFolder, Constants.LOBBY_CONFIG_DEFAULTS);
        FileConfig classes = FileConfig.findOrCreate("classes", this.dataFolder, Constants.CLASS_CONFIG_DEFAULTS);

        reg.loadClasses(classes.getRoot());
        reg.loadRoleData(config.getRoot().getSection("roles"));
        reg.loadMaps(mapFolder);
        reg.loadLobbies(lobbies.getRoot());
        reg.loadPermissions(config.getRoot().getSection("permissions"));

        LOGGER.info("Registered " + reg.getGlobalClasses().size() + " classes");
        LOGGER.info("Registered " + reg.getMaps().size() + " maps");
        LOGGER.info("Registered " + reg.getLobbies().size() + " lobbies");

        provider.reload();
    }

    public static void applyClass(MPlayer pl, PlayerClass cl) {
        classApplier.accept(pl, cl);
    }

}
