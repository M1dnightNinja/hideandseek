package org.wallentines.hideandseek.api.game.timer;

import org.wallentines.midnightcore.api.player.MPlayer;

public interface GameTimer {

    void run();

    void cancel();

    void addViewer(MPlayer player);

    void removeViewer(MPlayer player);

    int getTimeLeft();

    void setTimeLeft(int i);

    void reset();

    void addOverride(TimerOverride override);

}
