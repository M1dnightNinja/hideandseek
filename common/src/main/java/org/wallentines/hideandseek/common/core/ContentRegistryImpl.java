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
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.InlineSerializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class ContentRegistryImpl implements ContentRegistry {

    private static final Logger LOGGER = LogManager.getLogger("ContentRegistry");

    private final HashMap<String, Map> maps = new HashMap<>();
    private final HashMap<String, Lobby> lobbies = new HashMap<>();
    private final HashMap<String, PlayerClass> classes = new HashMap<>();
    private final HashMap<Identifier, GameType> gameTypes = new HashMap<>();
    private final HashMap<Identifier, Role> roles = new HashMap<>();

    private final HashMap<Role, RoleData> defaultRoleData = new HashMap<>();

    private final HashMap<String, PermissionCache> permissionCaches = new HashMap<>();

    @Override
    public void registerMap(Map map) {
        if(map == null) {
            throw new IllegalArgumentException("Attempt to register a null map!");
        }
        String id = map.getId();
        if(maps.containsKey(id)) {
            throw new IllegalArgumentException("Attempt to register map with duplicate ID! \"" + id + "\"");
        }
        Matcher m = Constants.VALID_ID.matcher(id);
        if(!m.matches()) {
            throw new IllegalArgumentException("Attempt to register lobby with invalid ID! \"" + id + "\" does not match " + Constants.VALID_ID.pattern());
        }
        maps.put(map.getId(), map);
    }

    @Override
    public void registerLobby(Lobby lobby) {
        if(lobby == null) {
            throw new IllegalArgumentException("Attempt to register a null lobby!");
        }
        String id = lobby.getId();
        if(lobbies.containsKey(id)) {
            throw new IllegalArgumentException("Attempt to register lobby with duplicate ID! \"" + id + "\"");
        }
        Matcher m = Constants.VALID_ID.matcher(id);
        if(!m.matches()) {
            throw new IllegalArgumentException("Attempt to register lobby with invalid ID! \"" + id + "\" does not match " + Constants.VALID_ID.pattern());
        }
        if(Constants.RESERVED_WORDS.contains(id.toLowerCase(Locale.ROOT))) {

            throw new IllegalArgumentException("Attempt to register lobby with invalid ID! \"" + id + "\" is a reserved word!");
        }

        lobbies.put(id, lobby);
    }

    @Override
    public void registerGlobalClass(PlayerClass clazz) {
        if(clazz == null) {
            throw new IllegalArgumentException("Attempt to register a null global class!");
        }
        String id = clazz.getId();
        if(classes.containsKey(clazz.getId())) {
            throw new IllegalArgumentException("Attempt to register global class with duplicate ID! \"" + id + "\"");
        }
        Matcher m = Constants.VALID_ID.matcher(id);
        if(!m.matches()) {
            throw new IllegalArgumentException("Attempt to register global class with invalid ID! \"" + id + "\" does not match " + Constants.VALID_ID.pattern());
        }
        classes.put(clazz.getId(), clazz);
    }

    @Override
    public void registerGameType(GameType type) {
        if(type == null) {
            throw new IllegalArgumentException("Attempt to register a null game type!");
        }
        Identifier id = type.getId();
        if(gameTypes.containsKey(type.getId())) {
            throw new IllegalArgumentException("Attempt to register game type with duplicate ID! \"" + id + "\"");
        }
        gameTypes.put(type.getId(), type);
    }

    @Override
    public Role registerRole(Role role) {
        if(role == null) {
            throw new IllegalArgumentException("Attempt to register a null game type!");
        }
        Identifier id = role.getId();
        if(roles.containsKey(role.getId())) {
            throw new IllegalArgumentException("Attempt to register game type with duplicate ID! \"" + id + "\"");
        }
        return roles.put(role.getId(), role);
    }

    @Override
    public Map getMap(String id) {
        return maps.get(id);
    }

    @Override
    public Lobby getLobby(String id) {
        return lobbies.get(id);
    }

    @Override
    public PlayerClass getGlobalClass(String id) {
        return classes.get(id);
    }

    @Override
    public GameType getGameType(Identifier id) {
        return gameTypes.get(id);
    }

    @Override
    public Role getRole(Identifier id) { return roles.get(id); }

    @Override
    public Collection<Map> getMaps() {
        return maps.values();
    }

    @Override
    public Collection<Lobby> getLobbies() {
        return lobbies.values();
    }

    @Override
    public Collection<PlayerClass> getGlobalClasses() {
        return classes.values();
    }

    @Override
    public Collection<GameType> getGameTypes() {
        return gameTypes.values();
    }

    public Collection<Role> getRoles() {
        return roles.values();
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

        if(!section.has("lobbies", List.class)) return;
        for(ConfigSection sec : section.getListFiltered("lobbies", ConfigSection.class)) {
            try {
                registerLobby(LobbyImpl.SERIALIZER.deserialize(sec));
            } catch (Exception ex) {
                LOGGER.warn("An error occurred while parsing a lobby!");
                ex.printStackTrace();
            }
        }
    }

    public void clearRoleData() { defaultRoleData.clear(); }

    public void loadRoleData(ConfigSection section) {
        for(String key : section.getKeys()) {
            Role role = getRole(Constants.ID_SERIALIZER.deserialize(key));
            RoleDataImpl data = section.get(key, RoleDataImpl.class);
            setDefaultData(role, data);
        }
    }

    public void clearClasses() { classes.clear(); }

    public void loadClasses(ConfigSection section) {

        if(!section.has("classes", List.class)) return;
        for(ConfigSection sec : section.getListFiltered("classes", ConfigSection.class)) {
            try {
                registerGlobalClass(PlayerClassImpl.SERIALIZER.deserialize(sec));
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
            permissionCaches.put(s, section.get(s, PermissionCache.class));
        }
    }

    public static final ContentRegistryImpl INSTANCE = new ContentRegistryImpl();

    public static final InlineSerializer<Map> REGISTERED_MAP = InlineSerializer.of(Map::getId, INSTANCE::getMap);
    public static final InlineSerializer<Lobby> REGISTERED_LOBBY = InlineSerializer.of(Lobby::getId, INSTANCE::getLobby);
    public static final InlineSerializer<PlayerClass> REGISTERED_GLOBAL_CLASS = InlineSerializer.of(PlayerClass::getId, INSTANCE::getGlobalClass);
    public static final InlineSerializer<GameType> REGISTERED_GAME_TYPE = InlineSerializer.of(gt -> gt.getId().toString(), id -> INSTANCE.getGameType(Constants.ID_SERIALIZER.deserialize(id)));
    public static final InlineSerializer<Role> REGISTERED_ROLE = InlineSerializer.of(r -> r.getId().toString(), id -> INSTANCE.getRole(Constants.ID_SERIALIZER.deserialize(id)));

}
