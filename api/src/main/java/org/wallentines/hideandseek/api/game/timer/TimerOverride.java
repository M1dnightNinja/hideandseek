package org.wallentines.hideandseek.api.game.timer;

import org.wallentines.midnightcore.api.text.MComponent;

public interface TimerOverride extends Comparable<TimerOverride> {

    int getStartTime();

    boolean shouldReset();

    MComponent getDisplay();

    @Override
    default int compareTo(TimerOverride other) {
        return other.getStartTime() - getStartTime();
    }
}
