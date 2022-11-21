package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.midnightcore.api.player.MPlayer;

public interface GameSession extends Session, DamageListener {

    Role getRole(MPlayer player);

    void setRole(MPlayer player, Role role);

    PlayerClass getClass(MPlayer player);

    void setClass(MPlayer player, PlayerClass clazz);

    Map getMap();

    void loadMap(Map map, Runnable onComplete);

    void startGame(MPlayer seeker);

    void endGame(EndType winner);

    LobbySession getLobbySession();

    boolean isRunning();

    GameState getState();

    class EndType {

        private final Role victor;

        private EndType(Role victor) {
            this.victor = victor;
        }

        public Role getVictor() {
            return victor;
        }


        public static EndType draw() {
            return new EndType(null);
        }

        public static EndType winner(Role r) {
            return new EndType(r);
        }
    }

    enum GameState {
        NOT_STARTED("not_started"),
        HIDING("hiding"),
        SEEKING("seeking"),
        ENDED("ended");

        final String langId;

        GameState(String langId) {
            this.langId = langId;
        }

        public String getLangId() {
            return langId;
        }
    }

}
