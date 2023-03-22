package org.wallentines.hideandseek.common;

import com.google.common.collect.ImmutableSet;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.*;
import org.wallentines.hideandseek.common.game.map.MapImpl;
import org.wallentines.hideandseek.common.game.map.PlayerClassImpl;
import org.wallentines.hideandseek.common.game.map.RoleDataImpl;
import org.wallentines.hideandseek.common.gametype.ClassicGameType;
import org.wallentines.hideandseek.common.integration.Requirements;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightlib.Version;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Set;
import java.util.regex.Pattern;

public final class Constants {

    private static final PermissionCache GAME_CACHE = new PermissionCache(false, "hideandseek.command", "hideandseek.command.leave");
    private static final PermissionCache VIEW_CACHE = new PermissionCache(false, "hideandseek.command", "hideandseek.command.leave");
    private static final PermissionCache EDIT_CACHE = new PermissionCache(true, "hideandseek.command", "hideandseek.command.leave", "hideandseek.command.map", "minecraft.command.gamemode", "minecraft.debug_stick");

    public static final Version VERSION = Version.SERIALIZER.deserialize("1.0.0");

    public static final String EMPTY_RESOURCE_PACK = "https://github.com/M1dnightNinja/HideAndSeek/blob/master/empty.zip?raw=true";
    public static final String EMPTY_RESOURCE_PACK_HASH = "F8CC3481867628951AD312B9FB886223856F7AB0";

    public static final InlineSerializer<Identifier> ID_SERIALIZER = Identifier.serializer(HideAndSeekAPI.DEFAULT_NAMESPACE);

    public static final ConfigSection CONFIG_DEFAULTS = new ConfigSection()
            .with("enable_anti_cheat", false)
            .with("permissions", new ConfigSection()
                    .with("game", PermissionCache.SERIALIZER.serialize(ConfigContext.INSTANCE, GAME_CACHE).getOrThrow())
                    .with("viewing", PermissionCache.SERIALIZER.serialize(ConfigContext.INSTANCE, VIEW_CACHE).getOrThrow())
                    .with("editing", PermissionCache.SERIALIZER.serialize(ConfigContext.INSTANCE, EDIT_CACHE).getOrThrow())
            )
            .with("roles", new ConfigSection()
                    .with("hider", RoleDataImpl.SERIALIZER.serialize(ConfigContext.INSTANCE, BuiltinRoles.DEFAULT_HIDER_DATA).getOrThrow())
                    .with("seeker", RoleDataImpl.SERIALIZER.serialize(ConfigContext.INSTANCE, BuiltinRoles.DEFAULT_SEEKER_DATA).getOrThrow())
            );

    public static final ConfigSection LOBBY_CONFIG_DEFAULTS = new ConfigSection()
            .with("lobbies", new ConfigList());

    public static final ConfigSection CLASS_CONFIG_DEFAULTS = new ConfigSection()
            .with("classes", new ConfigList());

    public static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");
    public static final Set<String> RESERVED_WORDS = ImmutableSet.of("editing", "viewing");

    public static void registerDefaults() {

        ContentRegistryImpl.INSTANCE.registerGameType(new ClassicGameType());

        ContentRegistryImpl.INSTANCE.registerRole(BuiltinRoles.HIDER);
        ContentRegistryImpl.INSTANCE.registerRole(BuiltinRoles.SEEKER);
        ContentRegistryImpl.INSTANCE.registerRole(BuiltinRoles.MAIN_HIDER);
        ContentRegistryImpl.INSTANCE.registerRole(BuiltinRoles.MAIN_SEEKER);

        Requirements.register(Registries.REQUIREMENT_REGISTRY, HideAndSeekAPI.DEFAULT_NAMESPACE);
    }

    public static void registerInlinePlaceholder(PlaceholderManager manager, String id, PlaceholderSupplier<String> supplier) {

        manager.getInlinePlaceholders().register(HideAndSeekAPI.DEFAULT_NAMESPACE + "_" + id, supplier);
    }

    public static void registerPlaceholder(PlaceholderManager manager, String id, PlaceholderSupplier<MComponent> supplier) {

        manager.getPlaceholders().register(HideAndSeekAPI.DEFAULT_NAMESPACE + "_" + id, supplier);
    }

    public static void registerPlaceholders(PlaceholderManager manager) {

        LobbyImpl.registerPlaceholders(manager);
        MapImpl.registerPlaceholders(manager);
        PlayerClassImpl.registerPlaceholders(manager);
        AbstractGameSession.registerPlaceholders(manager);

        // RoleData
        registerInlinePlaceholder(manager, "role_color", PlaceholderSupplier.create(RoleDataImpl.class, dt -> dt.getColor().toHex()));
        registerPlaceholder(manager, "role_name", PlaceholderSupplier.create(RoleDataImpl.class, RoleDataImpl::getName));
        registerPlaceholder(manager, "role_name_plural", PlaceholderSupplier.create(RoleDataImpl.class, RoleDataImpl::getPluralName));
        registerPlaceholder(manager, "role_name_proper", PlaceholderSupplier.create(RoleDataImpl.class, RoleDataImpl::getProperName));


    }

}
