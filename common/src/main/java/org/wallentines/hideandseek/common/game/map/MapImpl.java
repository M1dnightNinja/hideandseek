package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.ScoreboardTemplateImpl;
import org.wallentines.hideandseek.common.game.UIDisplayImpl;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;

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

        if(!folder.isDirectory()) throw new IllegalArgumentException("Unable to parse a map from " + folder.getName() + "! Not a folder!");
        FileConfig mapConfig = FileConfig.findFile(folder.listFiles(), "map");

        if(mapConfig == null) throw new IllegalArgumentException("Unable to find a map config in " + folder.getName() + "!");

        ConfigSerializer<MapImpl> serializer = createSerializer(folder);
        if(!serializer.canDeserialize(mapConfig.getRoot())) throw new IllegalArgumentException("Unable to parse " + mapConfig.getFile().getName() + " as a map! Missing required entries!");

        return serializer.deserialize(mapConfig.getRoot());
    }

    public static ConfigSerializer<MapImpl> createSerializer(File folder) {

        return ConfigSerializer.create(
                ConfigSerializer.<MapMetaImpl, MapImpl>entry(MapMetaImpl.SERIALIZER, "meta", m -> m.meta).orDefault(new MapMetaImpl(new UUID(0L,0L))),
                ConfigSerializer.<UIDisplayImpl, MapImpl>entry(UIDisplayImpl.SERIALIZER, "display", m -> m.display).optional(),
                WorldDataImpl.SERIALIZER.entry("world", m -> m.worldData),
                GameDataImpl.SERIALIZER.entry("game", m -> m.gameData),
                ConfigSerializer.<PregameDataImpl, MapImpl>entry(PregameDataImpl.SERIALIZER, "pregame", m -> m.pregameData).orDefault(new PregameDataImpl(null, 0.0d)),
                ConfigSerializer.<PlayerClassImpl, MapImpl>listEntry(PlayerClassImpl.SERIALIZER, "classes", m -> m.classes.values()).optional(),
                ConfigSerializer.<ScoreboardTemplateImpl, MapImpl>entry(ScoreboardTemplateImpl.SERIALIZER, "scoreboard", m -> m.scoreboardTemplate).optional(),
                (meta, display, world, game, pregame, classes, scoreboard) -> {

                    String id = folder.getName();
                    if(display == null) display = UIDisplayImpl.createDefault(id);

                    MapImpl out = new MapImpl(id, folder, meta, display, world, game, pregame, scoreboard);
                    for(PlayerClassImpl clz : classes) {
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

        Constants.registerInlinePlaceholder(manager, "map_author_name", PlaceholderSupplier.create(MapImpl.class, m -> MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(m.meta.getAuthor()).getUsername()));

        Constants.registerInlinePlaceholder(manager, "map_hide_time", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.getGameData().getHideTime())));
        Constants.registerInlinePlaceholder(manager, "map_seek_time", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.getGameData().getSeekTime())));
        Constants.registerInlinePlaceholder(manager, "map_resource_pack_url", PlaceholderSupplier.create(MapImpl.class, m -> Objects.toString(m.getGameData().getResourcePack().getURL())));

        Constants.registerPlaceholder(manager, "map_role_name", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getName(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getName())
        );

        Constants.registerPlaceholder(manager, "map_role_name_proper", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getProperName(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getProperName())
        );

        Constants.registerPlaceholder(manager, "map_role_name_plural", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getPluralName(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getPluralName())
        );

        Constants.registerInlinePlaceholder(manager, "map_role_color", PlaceholderSupplier.createWithParameter(MapImpl.class,
                (m, param) -> m.getGameData().getRoleData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getColor().toHex(),
                param -> ContentRegistryImpl.INSTANCE.getDefaultData(ContentRegistryImpl.REGISTERED_ROLE.deserialize(param)).getColor().toHex())
        );

    }
}
