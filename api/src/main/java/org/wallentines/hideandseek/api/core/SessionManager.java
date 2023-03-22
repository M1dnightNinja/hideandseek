package org.wallentines.hideandseek.api.core;

import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.midnightcore.api.module.session.SessionModule;
import org.wallentines.midnightcore.api.player.MPlayer;

public interface SessionManager {

    LobbySession getLobbySession(Lobby lobby);

    LobbySession createLobbySession(Lobby lobby);

    GameSession createGameSession(LobbySession lobby, GameType type);

    EditingSession getEditingSession(Map map);

    EditingSession createEditingSession(Map map, boolean init);

    ViewingSession getViewingSession(Map map);

    ViewingSession createViewingSession(Map map);

    SessionModule getModule();

    boolean damage(MPlayer player, MPlayer attacker, String sourceId, float amount);

}
