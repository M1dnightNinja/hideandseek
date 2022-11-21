package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.midnightcore.api.player.MPlayer;

public interface LobbySession extends Session, DamageListener {

    Lobby getLobby();

    void startGame(MPlayer player, Map map);

    GameSession getCurrentGame();

    boolean isRunning();

}
