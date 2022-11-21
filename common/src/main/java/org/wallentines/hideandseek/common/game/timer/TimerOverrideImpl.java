package org.wallentines.hideandseek.common.game.timer;

import org.wallentines.hideandseek.api.game.timer.TimerOverride;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;

public class TimerOverrideImpl implements TimerOverride {

    private final int startTime;
    private final MComponent display;

    public TimerOverrideImpl(int startTime, MComponent display) {
        this.startTime = startTime;
        this.display = display;
    }

    @Override
    public int getStartTime() {
        return startTime;
    }

    @Override
    public boolean shouldReset() {
        return display == null;
    }

    @Override
    public MComponent getDisplay() {
        return display;
    }

    public static final ConfigSerializer<TimerOverrideImpl> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.INT.entry("time", TimerOverrideImpl::getStartTime),
            MComponent.INLINE_SERIALIZER.entry("display", TimerOverrideImpl::getDisplay).optional(),
            TimerOverrideImpl::new
    );

}
