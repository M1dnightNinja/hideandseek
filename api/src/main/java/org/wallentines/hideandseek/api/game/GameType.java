package org.wallentines.hideandseek.api.game;

import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

public interface GameType {

    Identifier getId();

    MComponent getName();

    int getMinPlayers();

    int getMaxPlayers();

    boolean canTag(GameSession session, MPlayer tagger, MPlayer tagged);

    void checkVictory(GameSession session);

    Role getTimeoutVictor();

    Role getRespawnPointRole(GameSession session);

    MComponent getHideTimerText(GameSession session, MPlayer mpl);

    MComponent getSeekTimerText(GameSession session, MPlayer mpl);

    MComponent getEndTimerText(GameSession session, MPlayer mpl);


    MComponent getStartTitleText(GameSession session, MPlayer player);

    MComponent getStartSubtitleText(GameSession session, MPlayer player);

    boolean isFrozen(GameSession session, MPlayer seeker);

    void setupPlayers(GameSession session, MPlayer seeker);

    void onTag(GameSession session, MPlayer player, MPlayer tagger);

    void onTag(GameSession session, MPlayer player, String sourceId);

    void onStartHiding(GameSession session);

    void onStartSeeking(GameSession session);

    void onEnd(GameSession session, Role winner);

    Collection<MPlayer> getSeekers(GameSession session);

    Collection<MPlayer> getHiders(GameSession session);

    Role getHiderRole();

    Role getSeekerRole();

    boolean isHider(Role role);

    boolean isSeeker(Role role);

}
