package org.wallentines.hideandseek.fabric.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.common.game.timer.AbstractTimer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.function.Consumer;
import java.util.function.Function;

public class FabricTimer extends AbstractTimer {

    public FabricTimer(Function<MPlayer, MComponent> message, int time, Consumer<Integer> onTick) {
        super(message, time, onTick);
    }

    @Override
    protected void executeTick() {

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) return;

        server.submit(() -> {
            try {
                onTick.accept(currentTime);
            } catch (Exception ex) {
                HideAndSeekAPI.getLogger().info("An error occurred while a timer was ticking!");
                ex.printStackTrace();
            }
        });
    }
}
