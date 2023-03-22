package org.wallentines.hideandseek.common.game.timer;

import org.wallentines.hideandseek.api.game.timer.GameTimer;
import org.wallentines.hideandseek.api.game.timer.TimerOverride;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightlib.math.Color;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractTimer implements GameTimer {

    private static final Set<AbstractTimer> RUNNING_TIMERS = new HashSet<>();
    private static final MStyle TIMER_STYLE = new MStyle().withColor(Color.WHITE).withBold(true);

    private final Function<MPlayer, MComponent> messageFunc;
    private final int time;
    private final Timer timer;

    protected final Consumer<Integer> onTick;
    protected int currentTime;

    private final List<TimerOverride> overrides = new ArrayList<>();

    private final Set<MPlayer> viewers = new HashSet<>();

    public AbstractTimer(Function<MPlayer, MComponent> messageFunc, int time, Consumer<Integer> onTick) {
        this.messageFunc = messageFunc;
        this.time = time;
        this.onTick = onTick;
        this.currentTime = time;

        this.timer = new Timer();
    }

    public void run() {
        if(RUNNING_TIMERS.contains(this)) return;

        RUNNING_TIMERS.add(this);
        timer.schedule(createTask(), 0L, 1000L);
    }

    public void cancel() {
        if(!RUNNING_TIMERS.contains(this)) return;

        RUNNING_TIMERS.remove(this);
        timer.cancel();
    }

    public void addViewer(MPlayer player) {
        viewers.add(player);
    }

    public void removeViewer(MPlayer player) {
        viewers.remove(player);
    }

    public int getTimeLeft() {
        return currentTime;
    }

    public void setTimeLeft(int i) {
        currentTime = i;
    }

    public void reset() {
        currentTime = time;
    }

    private void show(MComponent time) {

        viewers.forEach(v -> {
            MComponent send = messageFunc.apply(v).copy().withChild(time);
            v.sendActionBar(send);
        });
    }

    private TimerTask createTask() {

        currentTime += 1;

        return new TimerTask() {

            private final PriorityQueue<TimerOverride> queue = new PriorityQueue<>(overrides);
            private MComponent override = null;

            @Override
            public void run() {
                currentTime--;

                MComponent timer;
                if(!queue.isEmpty() && queue.peek().getStartTime() == currentTime) {
                    override = queue.remove().getDisplay();
                }

                timer = override == null ? getTimeText(currentTime) : override;

                show(timer);
                executeTick();

                if(currentTime == 0) {
                    AbstractTimer.this.cancel();
                }
            }
        };
    }

    @Override
    public void addOverride(TimerOverride override) {

        overrides.add(override);
    }

    private MComponent getTimeText(int tick) {

        return new MTextComponent(" " + formatTime(tick * 1000L)).withStyle(TIMER_STYLE);
    }

    protected abstract void executeTick();

    public static void cancelAll() {
        RUNNING_TIMERS.forEach(rt -> rt.timer.cancel());
        RUNNING_TIMERS.clear();
    }

    public static String formatTime(long milliseconds) {
        final long days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        final long hours = TimeUnit.MILLISECONDS.toHours(milliseconds - TimeUnit.DAYS.toMillis(days));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours));
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));

        if(days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        if(hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FunctionalInterface
    public interface TimerCreator {

        GameTimer create(Function<MPlayer, MComponent> text, int time, Consumer<Integer> onTick);

    }

}
