package org.wallentines.hideandseek.common;

import com.google.common.collect.ImmutableSet;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.*;
import org.wallentines.hideandseek.common.game.map.MapImpl;
import org.wallentines.hideandseek.common.game.map.MapRegionImpl;
import org.wallentines.hideandseek.common.game.map.PlayerClassImpl;
import org.wallentines.hideandseek.common.game.map.RoleDataImpl;
import org.wallentines.hideandseek.common.gametype.ClassicGameType;
import org.wallentines.hideandseek.common.integration.Requirements;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightcore.common.Registries;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.InlineSerializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

public final class Constants {

    private static final PermissionCache GAME_CACHE = new PermissionCache(false, "hideandseek.command", "hideandseek.command.leave");
    private static final PermissionCache VIEW_CACHE = new PermissionCache(false, "hideandseek.command", "hideandseek.command.leave");
    private static final PermissionCache EDIT_CACHE = new PermissionCache(true, "hideandseek.command", "hideandseek.command.leave", "hideandseek.command.map", "minecraft.command.gamemode", "minecraft.debug_stick");

    public static final Version VERSION = Version.SERIALIZER.deserialize("1.0.0");
    public static final String DEFAULT_NAMESPACE = "hideandseek";

    public static final String EMPTY_RESOURCE_PACK = "https://github.com/M1dnightNinja/HideAndSeek/blob/master/empty.zip?raw=true";
    public static final String EMPTY_RESOURCE_PACK_HASH = "F8CC3481867628951AD312B9FB886223856F7AB0";

    public static final InlineSerializer<Identifier> ID_SERIALIZER = new Identifier.Serializer(DEFAULT_NAMESPACE);

    public static final ConfigSection CONFIG_DEFAULTS = new ConfigSection()
            .with("enable_anti_cheat", false)
            .with("permissions", new ConfigSection()
                    .with("game", PermissionCache.SERIALIZER.serialize(GAME_CACHE))
                    .with("viewing", PermissionCache.SERIALIZER.serialize(VIEW_CACHE))
                    .with("editing", PermissionCache.SERIALIZER.serialize(EDIT_CACHE))
            )
            .with("roles", new ConfigSection()
                    .with("hider", RoleDataImpl.SERIALIZER.serialize(BuiltinRoles.DEFAULT_HIDER_DATA))
                    .with("seeker", RoleDataImpl.SERIALIZER.serialize(BuiltinRoles.DEFAULT_SEEKER_DATA))
            );

    public static final ConfigSection LOBBY_CONFIG_DEFAULTS = new ConfigSection()
            .with("lobbies", new ArrayList<>());

    public static final ConfigSection CLASS_CONFIG_DEFAULTS = new ConfigSection()
            .with("classes", new ArrayList<>());

    public static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");
    public static final Set<String> RESERVED_WORDS = ImmutableSet.of("editing", "viewing");

    public static void registerDefaults() {

        ContentRegistryImpl.INSTANCE.registerGameType(new ClassicGameType());

        ConfigRegistry.INSTANCE.registerSerializer(UIDisplayImpl.class, UIDisplayImpl.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(LobbyImpl.class, LobbyImpl.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MapRegionImpl.class, MapRegionImpl.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(RoleDataImpl.class, RoleDataImpl.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(PlayerClassImpl.class, PlayerClassImpl.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(PermissionCache.class, PermissionCache.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(ScoreboardTemplateImpl.class, ScoreboardTemplateImpl.SERIALIZER);

        HideAndSeekAPI.getInstance().getContentRegistry().registerRole(BuiltinRoles.HIDER);
        HideAndSeekAPI.getInstance().getContentRegistry().registerRole(BuiltinRoles.SEEKER);
        HideAndSeekAPI.getInstance().getContentRegistry().registerRole(BuiltinRoles.MAIN_HIDER);
        HideAndSeekAPI.getInstance().getContentRegistry().registerRole(BuiltinRoles.MAIN_SEEKER);

        Requirements.register(Registries.REQUIREMENT_REGISTRY, DEFAULT_NAMESPACE);
    }

    public static void registerInlinePlaceholder(PlaceholderManager manager, String id, PlaceholderSupplier<String> supplier) {

        manager.getInlinePlaceholders().register(DEFAULT_NAMESPACE + "_" + id, supplier);
    }

    public static void registerPlaceholder(PlaceholderManager manager, String id, PlaceholderSupplier<MComponent> supplier) {

        manager.getPlaceholders().register(DEFAULT_NAMESPACE + "_" + id, supplier);
    }

    public static void registerPlaceholders(PlaceholderManager manager) {

        LobbyImpl.registerPlaceholders(manager);
        MapImpl.registerPlaceholders(manager);
        PlayerClassImpl.registerPlaceholders(manager);
        RoleDataImpl.registerPlaceholders(manager);
        AbstractGameSession.registerPlaceholders(manager);

    }

}
