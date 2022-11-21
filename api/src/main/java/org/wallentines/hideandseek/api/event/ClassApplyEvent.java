package org.wallentines.hideandseek.api.event;

import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.event.Event;

public class ClassApplyEvent extends Event {

    private final MPlayer player;
    private PlayerClass applied;

    private boolean cancelled = false;

    public ClassApplyEvent(MPlayer player, PlayerClass applied) {
        this.player = player;
        this.applied = applied;
    }

    public MPlayer getPlayer() {
        return player;
    }

    public PlayerClass getPlayerClass() {
        return applied;
    }

    public void setPlayerClass(PlayerClass applied) {
        this.applied = applied;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
