package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.ScoreboardTemplateImpl;
import org.wallentines.hideandseek.common.game.UIDisplayImpl;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;

import java.io.File;
import java.util.*;

public class MapImpl implements Map {

    private final String id;

    private final File dataFolder;
    private final File worldFolder;

    private final MapMetaImpl meta;

    private final UIDisplayImpl display;

    private final WorldDataImpl worldData;

    private final GameDataImpl gameData;

    private final PregameDataImpl pregameData;

    private final ScoreboardTemplateImpl scoreboardTemplate;

    private final HashMap<String, PlayerClassImpl> classes = new HashMap<>();


    public MapImpl(String id, File dataFolder, MapMetaImpl meta, UIDisplayImpl display, WorldDataImpl worldData, GameDataImpl gameData, PregameDataImpl pregameData, ScoreboardTemplateImpl scoreboardTemplate) {
        this.id = id;
        this.dataFolder = dataFolder;
        this.worldFolder = new File(dataFolder, "world");
        this.meta = meta;
        this.display = display;
        this.worldData = worldData;
        this.gameData = gameData;
        this.pregameData = pregameData;
        this.scoreboardTemplate = scoreboardTemplate;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public MapMeta getMeta() {
        return meta;
    }

    @Override
    public GameData getGameData() {
        return gameData;
    }

    @Override
    public UIDisplay getDisplay() {
        return display;
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public PregameData getPregameData() {
        return pregameData;
    }

    @Override
    public ScoreboardTemplate getScoreboardTemplate() {
        return scoreboardTemplate;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getWorldFolder() {
        return worldFolder;
    }

    @Override
    public PlayerClass getClass(String id) {
        return classes.get(id);
    }

    @Override
    public PlayerClass getOrGlobal(String id) {

        if(id == null) return null;
        if(!classes.containsKey(id)) return ContentRegistryImpl.INSTANCE.getGlobalClass(id);
        return getClass(id);
    }

    @Override
    public boolean canView(MPlayer player) {
        return canEdit(player) || player.hasPermission("hideandseek.map.view." + id, 3);
    }

    @Override
    public boolean canEdit(MPlayer player) {
        return meta.canEdit(player) || player.hasPermission("hideandseek.map.edit." + id, 3);
    }

    public static MapImpl fromFolder(File folder) {

        if(!folder.isDirectory()) throw new IllegalArgumentException("Unable to parse a map from " + folder.getPath() + "! Not a folder!");
        FileWrapper<ConfigObject> mapConfig = FileConfig.find("map", folder);

        if(mapConfig == null) throw new IllegalArgumentException("Unable to find a map config in " + folder.getPath() + "!");
        if(mapConfig.getRoot() == null || !mapConfig.getRoot().isSection()) throw new IllegalStateException("Map config in " + folder.getPath() + " does not contain valid map data!");

        Serializer<MapImpl> serializer = createSerializer(folder);
        return serializer.deserialize(ConfigContext.INSTANCE, mapConfig.getRoot()).getOrThrow();
    }

    public static Serializer<MapImpl> createSerializer(File folder) {

        return ObjectSerializer.create(
                MapMetaImpl.SERIALIZER.<MapImpl>entry( "meta", m -> m.meta).orElse(new MapMetaImpl(new UUID(0L,0L))),
                UIDisplayImpl.SERIALIZER.<MapImpl>entry("display", m -> m.display).optional(),
                WorldDataImpl.SERIALIZER.entry("world", m -> m.worldData),
                GameDataImpl.SERIALIZER.entry("game", m -> m.gameData),
                PregameDataImpl.SERIALIZER.<MapImpl>entry("pregame", m -> m.pregameData).orElse(new PregameDataImpl(null, 0.0d)),
                PlayerClassImpl.SERIALIZER.listOf().<MapImpl>entry("classes", m -> m.classes.values()).optional(),
                ScoreboardTemplateImpl.SERIALIZER.<MapImpl>entry("scoreboard", m -> m.scoreboardTemplate).optional(),
                (meta, display, world, game, pregame, classes, scoreboard) -> {

                    String id = folder.getName();
                    if(display == null) display = UIDisplayImpl.createDefault(id);

                    MapImpl out = new MapImpl(id, folder, meta, display, world, game, pregame, scoreboard);
                    if(classes != null) for(PlayerClassImpl clz : classes) {
                        out.classes.put(clz.getId(), clz);
                    }
                    return out;
                }
        );

    }

    public static void registerPlaceholders(PlaceholderManager manager) {

        Constants.registerInlinePlaceholder(manager, "map_id", PlaceholderSupplier.create(MapImpl.class, MapImpl::getId));

        Constants.registerPlaceholder(manager, "map_name", PlaceholderSupplier.create(MapImpl.class, m -> m.display.getName()));
        Constants.registerInlinePlaceholder(manager, "map_color", PlaceholderSupplier.create(MapImpl.class, m -> m.display.getColor().toHex()));

        Constants.registerInlinePlaceholder(manager, "map_random_time", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.worldData.hasRandomTime())));
        Constants.registerInlinePlaceholder(manager, "map_rain", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.worldData.hasRain())));
        Constants.registerInlinePlaceholder(manager, "map_thunder", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.worldData.hasThunder())));

        Constants.registerInlinePlaceholder(manager, "map_author_name", PlaceholderSupplier.create(MapImpl.class, m -> MidnightCoreAPI.getRunningServer().getPlayer(m.meta.getAuthor()).getUsername()));

        Constants.registerInlinePlaceholder(manager, "map_hide_time", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.getGameData().getHideTime())));
        Constants.registerInlinePlaceholder(manager, "map_seek_time", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.getGameData().getSeekTime())));
        Constants.registerInlinePlaceholder(manager, "map_resource_pack_url", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.getGameData().getResourcePack().getURL())));

        Constants.registerPlaceholder(manager, "map_role_name", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getName(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getName())
        );

        Constants.registerPlaceholder(manager, "map_role_name_proper", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getProperName(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getProperName())
        );

        Constants.registerPlaceholder(manager, "map_role_name_plural", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getPluralName(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getPluralName())
        );

        Constants.registerInlinePlaceholder(manager, "map_role_color", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getColor().toHex(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.readString(param)).getColor().toHex())
        );

    }
}
