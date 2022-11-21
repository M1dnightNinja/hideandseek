package org.wallentines.hideandseek.fabric.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.common.game.timer.AbstractTimer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.fabric.MidnightCore;

import java.util.function.Consumer;
import java.util.function.Function;

public class FabricTimer extends AbstractTimer {

    public FabricTimer(Function<MPlayer, MComponent> message, int time, Consumer<Integer> onTick) {
        super(message, time, onTick);
    }

    @Override
    protected void executeTick() {
        try {
            MidnightCore.getInstance().getServer().execute(() -> onTick.accept(currentTime));
        } catch (Exception ex) {
            HideAndSeekAPI.getLogger().info("An error occurred while a timer was ticking!");
            ex.printStackTrace();
        }
    }
}
