package org.wallentines.hideandseek.api.core;

import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.Collection;

public interface ContentRegistry {

    void registerMap(Map map);

    void registerLobby(Lobby lobby);

    void registerGlobalClass(PlayerClass clazz);

    void registerGameType(GameType type);

    Role registerRole(Role role);


    Map getMap(String id);

    Lobby getLobby(String id);

    PlayerClass getGlobalClass(String id);

    GameType getGameType(Identifier id);

    Role getRole(Identifier id);


    StringRegistry<Map> getMaps();

    StringRegistry<Lobby> getLobbies();

    StringRegistry<PlayerClass> getGlobalClasses();

    Registry<GameType> getGameTypes();

}
