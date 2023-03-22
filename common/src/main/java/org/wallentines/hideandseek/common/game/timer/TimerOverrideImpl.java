package org.wallentines.hideandseek.common.game.timer;

import org.wallentines.hideandseek.api.game.timer.TimerOverride;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.text.MComponent;

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

    public static final Serializer<TimerOverride> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, TimerOverride value) {
            if(!(value instanceof TimerOverrideImpl)) return SerializeResult.failure(value + " is not a TimerOverrideImpl!");
            return INTERNAL_SERIALIZER.serialize(context, (TimerOverrideImpl) value);
        }

        @Override
        public <O> SerializeResult<TimerOverride> deserialize(SerializeContext<O> context, O value) {
            return INTERNAL_SERIALIZER.deserialize(context, value).flatMap(mri -> mri);
        }
    };

    private static final Serializer<TimerOverrideImpl> INTERNAL_SERIALIZER = ObjectSerializer.create(
            Serializer.INT.entry("time", TimerOverrideImpl::getStartTime),
            MComponent.SERIALIZER.entry("display", TimerOverrideImpl::getDisplay).optional(),
            TimerOverrideImpl::new
    );

}
