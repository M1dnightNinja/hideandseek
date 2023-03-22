package org.wallentines.hideandseek.common.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.hideandseek.api.core.ContentRegistry;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.game.*;
import org.wallentines.hideandseek.common.game.map.MapImpl;
import org.wallentines.hideandseek.common.game.map.PlayerClassImpl;
import org.wallentines.hideandseek.common.game.map.RoleDataImpl;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;

public class ContentRegistryImpl implements ContentRegistry {

    private static final Logger LOGGER = LogManager.getLogger("ContentRegistry");

    private final StringRegistry<Map> maps = new StringRegistry<>();
    private final StringRegistry<Lobby> lobbies = new StringRegistry<>();
    private final StringRegistry<PlayerClass> classes = new StringRegistry<>();
    private final Registry<GameType> gameTypes = new Registry<>("hideandseek");
    private final Registry<Role> roles = new Registry<>("hideandseek");

    private final HashMap<Role, RoleData> defaultRoleData = new HashMap<>();

    private final HashMap<String, PermissionCache> permissionCaches = new HashMap<>();

    @Override
    public void registerMap(Map map) {
        if(map == null) {
            throw new IllegalArgumentException("Attempt to register a null map!");
        }
        String id = map.getId();
        if(maps.hasKey(id)) {
            throw new IllegalArgumentException("Attempt to register map with duplicate ID! \"" + id + "\"");
        }
        Matcher m = Constants.VALID_ID.matcher(id);
        if(!m.matches()) {
            throw new IllegalArgumentException("Attempt to register lobby with invalid ID! \"" + id + "\" does not match " + Constants.VALID_ID.pattern());
        }
        maps.register(map.getId(), map);
    }

    @Override
    public void registerLobby(Lobby lobby) {
        if(lobby == null) {
            throw new IllegalArgumentException("Attempt to register a null lobby!");
        }
        String id = lobby.getId();
        if(lobbies.hasKey(id)) {
            throw new IllegalArgumentException("Attempt to register lobby with duplicate ID! \"" + id + "\"");
        }
        Matcher m = Constants.VALID_ID.matcher(id);
        if(!m.matches()) {
            throw new IllegalArgumentException("Attempt to register lobby with invalid ID! \"" + id + "\" does not match " + Constants.VALID_ID.pattern());
        }
        if(Constants.RESERVED_WORDS.contains(id.toLowerCase(Locale.ROOT))) {

            throw new IllegalArgumentException("Attempt to register lobby with invalid ID! \"" + id + "\" is a reserved word!");
        }

        lobbies.register(id, lobby);
    }

    @Override
    public void registerGlobalClass(PlayerClass clazz) {
        if(clazz == null) {
            throw new IllegalArgumentException("Attempt to register a null global class!");
        }
        String id = clazz.getId();
        if(classes.hasKey(clazz.getId())) {
            throw new IllegalArgumentException("Attempt to register global class with duplicate ID! \"" + id + "\"");
        }
        Matcher m = Constants.VALID_ID.matcher(id);
        if(!m.matches()) {
            throw new IllegalArgumentException("Attempt to register global class with invalid ID! \"" + id + "\" does not match " + Constants.VALID_ID.pattern());
        }
        classes.register(clazz.getId(), clazz);
    }

    @Override
    public void registerGameType(GameType type) {
        if(type == null) {
            throw new IllegalArgumentException("Attempt to register a null game type!");
        }
        Identifier id = type.getId();
        if(gameTypes.hasKey(type.getId())) {
            throw new IllegalArgumentException("Attempt to register game type with duplicate ID! \"" + id + "\"");
        }
        gameTypes.register(type.getId(), type);
    }

    @Override
    public Role registerRole(Role role) {
        if(role == null) {
            throw new IllegalArgumentException("Attempt to register a null game type!");
        }
        Identifier id = role.getId();
        if(roles.hasKey(role.getId())) {
            throw new IllegalArgumentException("Attempt to register game type with duplicate ID! \"" + id + "\"");
        }
        return roles.register(role.getId(), role);
    }

    @Override
    public Map getMap(String id) {
        if(id == null) return null;
        return maps.get(id);
    }

    @Override
    public Lobby getLobby(String id) {
        if(id == null) return null;
        return lobbies.get(id);
    }

    @Override
    public PlayerClass getGlobalClass(String id) {
        if(id == null) return null;
        return classes.get(id);
    }

    @Override
    public GameType getGameType(Identifier id) {
        if(id == null) return null;
        return gameTypes.get(id);
    }

    @Override
    public Role getRole(Identifier id) {
        if(id == null) return null;
        return roles.get(id);
    }

    @Override
    public StringRegistry<Map> getMaps() {
        return maps;
    }

    @Override
    public StringRegistry<Lobby> getLobbies() {
        return lobbies;
    }

    @Override
    public StringRegistry<PlayerClass> getGlobalClasses() {
        return classes;
    }

    @Override
    public Registry<GameType> getGameTypes() {
        return gameTypes;
    }

    public Registry<Role> getRoles() {
        return roles;
    }

    public void setDefaultData(Role role, RoleData data) {

        defaultRoleData.put(role, data);
    }

    public RoleData getDefaultData(Role role) {

        return defaultRoleData.get(role);
    }

    public PermissionCache getPermissions(String context) {

        return permissionCaches.get(context);
    }


    public void clearMaps() { maps.clear(); }

    public void loadMaps(File mapFolder) {

        if(!mapFolder.exists() || !mapFolder.isDirectory()) return;
        File[] files = mapFolder.listFiles();

        if(files == null) return;

        for(File f : files) {
            if(!f.isDirectory()) continue;
            try {
                registerMap(MapImpl.fromFolder(f));
            } catch (Exception ex) {
                LOGGER.warn("An error occurred while parsing a map! (" + f.getName() + ")");
                ex.printStackTrace();
            }
        }
    }

    public void clearLobbies() { lobbies.clear(); }

    public void loadLobbies(ConfigSection section) {

        if(!section.hasList("lobbies")) return;
        for(ConfigSection sec : section.getListFiltered("lobbies", ConfigSection.SERIALIZER)) {
            try {
                registerLobby(LobbyImpl.SERIALIZER.deserialize(ConfigContext.INSTANCE, sec).getOrThrow());
            } catch (Exception ex) {
                LOGGER.warn("An error occurred while parsing a lobby!");
                ex.printStackTrace();
            }
        }
    }

    public void clearRoleData() { defaultRoleData.clear(); }

    public void loadRoleData(ConfigSection section) {
        for(String key : section.getKeys()) {
            Role role = getRole(Constants.ID_SERIALIZER.readString(key));
            RoleDataImpl data = section.get(key, RoleDataImpl.SERIALIZER);
            setDefaultData(role, data);
        }
    }

    public void clearClasses() { classes.clear(); }

    public void loadClasses(ConfigSection section) {

        if(!section.hasList("classes")) return;
        for(ConfigSection sec : section.getListFiltered("classes", ConfigSection.SERIALIZER)) {
            try {
                registerGlobalClass(PlayerClassImpl.SERIALIZER.deserialize(ConfigContext.INSTANCE, sec).getOrThrow());
            } catch (Exception ex) {
                LOGGER.warn("An error occurred while parsing a global class!");
                ex.printStackTrace();
            }
        }
    }


    public void clearPermissions() {
        permissionCaches.clear();
    }

    public void loadPermissions(ConfigSection section) {

        for(String s : section.getKeys()) {
            permissionCaches.put(s, section.get(s, PermissionCache.SERIALIZER));
        }
    }

    public static final ContentRegistryImpl INSTANCE = new ContentRegistryImpl();

    public static final InlineSerializer<Map> REGISTERED_MAP = InlineSerializer.of(Map::getId, INSTANCE::getMap);
    public static final InlineSerializer<Lobby> REGISTERED_LOBBY = InlineSerializer.of(Lobby::getId, INSTANCE::getLobby);
    public static final InlineSerializer<PlayerClass> REGISTERED_GLOBAL_CLASS = InlineSerializer.of(PlayerClass::getId, INSTANCE::getGlobalClass);
    public static final InlineSerializer<GameType> REGISTERED_GAME_TYPE = InlineSerializer.of(gt -> gt.getId().toString(), id -> INSTANCE.getGameType(Constants.ID_SERIALIZER.readString(id)));
    public static final InlineSerializer<Role> REGISTERED_ROLE = InlineSerializer.of(r -> r.getId().toString(), id -> INSTANCE.getRole(Constants.ID_SERIALIZER.readString(id)));

}
