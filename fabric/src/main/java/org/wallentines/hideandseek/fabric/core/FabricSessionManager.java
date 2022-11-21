package org.wallentines.hideandseek.fabric.core;

import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.core.AbstractSessionManager;
import org.wallentines.hideandseek.fabric.game.FabricGameSession;
import org.wallentines.hideandseek.fabric.game.FabricEditSession;
import org.wallentines.hideandseek.fabric.game.FabricViewSession;

public class FabricSessionManager extends AbstractSessionManager {

    @Override
    public GameSession createGameSession(LobbySession sess, GameType type) {
        return new FabricGameSession(sess, type);
    }

    @Override
    public EditingSession createEditingSession(Map map) {
        EditingSession sess = new FabricEditSession(map);
        getModule().registerSession(sess);
        return sess;
    }

    @Override
    public ViewingSession createViewingSession(Map map) {
        ViewingSession sess = new FabricViewSession(map);
        getModule().registerSession(sess);
        return sess;
    }
}
