package org.wallentines.hideandseek.common.gametype;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.common.game.BuiltinRoles;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.HashMap;

public class ClassicGameType implements GameType {

    private static final Identifier ID = new Identifier(HideAndSeekAPI.DEFAULT_NAMESPACE, "classic");

    private static final HashMap<GameSession, MComponent> HIDE_TIMERS = new HashMap<>();
    private static final HashMap<GameSession, HashMap<Role, MComponent>> SEEK_TIMERS = new HashMap<>();
    private static final HashMap<GameSession, MComponent> END_TIMERS = new HashMap<>();

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public MComponent getName() {
        return new MTextComponent("Classic").withStyle(new MStyle().withColor(Color.fromRGBI(11)));
    }

    @Override
    public int getMinPlayers() { return 2; }

    @Override
    public int getMaxPlayers() { return 100; }


    @Override
    public void setupPlayers(GameSession session, MPlayer seeker) {

        if(seeker == null) seeker = session.getRandomPlayer();
        session.setRole(seeker, BuiltinRoles.MAIN_SEEKER);

        for(MPlayer player : session.getPlayers()) {
            if(player.equals(seeker)) continue;
            session.setRole(player, BuiltinRoles.HIDER);
        }
    }

    @Override
    public boolean isFrozen(GameSession session, MPlayer seeker) {
        return session.getState() == GameSession.GameState.HIDING && session.getRole(seeker) != BuiltinRoles.HIDER;
    }

    @Override
    public void onTag(GameSession session, MPlayer player, MPlayer tagger) {

        session.setRole(player, BuiltinRoles.SEEKER);

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        MComponent comp = prov.getMessage("game.tag", (String) null, player, session.getMap().getGameData().getRoleData(BuiltinRoles.HIDER),
                CustomPlaceholder.create("tagger_name", tagger.getName()),
                CustomPlaceholder.create("remains", getRemainsText(session)));

        session.broadcastMessage(comp);

    }

    @Override
    public void onTag(GameSession session, MPlayer player, String sourceId) {

        session.setRole(player, BuiltinRoles.SEEKER);

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        MComponent deathMessage;
        String key = session.getMap().getGameData().getDeathMessageKey(sourceId);
        if(key == null) {
            deathMessage = new MTranslateComponent("death.attack." + sourceId, player.getName());
        } else {
            deathMessage = prov.getMessage(key, player, player);
        }
        MComponent comp = prov.getMessage("game.tag.environment", prov.getDefaultLocale(), player, session.getMap().getGameData().getRoleData(BuiltinRoles.HIDER),
                CustomPlaceholder.create("death_message", deathMessage),
                CustomPlaceholder.create("remains", getRemainsText(session)));

        session.broadcastMessage(comp);
    }


    private MComponent getRemainsText(GameSession session) {

        int count = session.getPlayers(pl -> session.getRole(pl) == BuiltinRoles.HIDER).size();
        String key = count == 1 ? "game.remains.singular" : "game.remains";

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        return prov.getMessage(key, prov.getDefaultLocale(), session.getMap().getGameData().getRoleData(BuiltinRoles.HIDER), CustomPlaceholderInline.create("hider_count", count+""));
    }

    @Override
    public void onStartHiding(GameSession session) { }

    @Override
    public void onStartSeeking(GameSession session) { }

    @Override
    public void onEnd(GameSession session, Role winner) {

        HIDE_TIMERS.remove(session);
        SEEK_TIMERS.remove(session);
        END_TIMERS.remove(session);

    }

    @Override
    public Collection<MPlayer> getSeekers(GameSession session) {
        return session.getPlayers(pl -> isSeeker(session.getRole(pl)));
    }

    @Override
    public Collection<MPlayer> getHiders(GameSession session) {
        return session.getPlayers(pl -> isHider(session.getRole(pl)));
    }

    @Override
    public Role getHiderRole() {
        return BuiltinRoles.HIDER;
    }

    @Override
    public Role getSeekerRole() {
        return BuiltinRoles.SEEKER;
    }

    @Override
    public boolean isHider(Role role) {
        return role == BuiltinRoles.HIDER;
    }

    @Override
    public boolean isSeeker(Role role) {
        return role == BuiltinRoles.MAIN_SEEKER || role == BuiltinRoles.SEEKER;
    }

    @Override
    public void checkVictory(GameSession session) {

        int hiders = session.getPlayers(pl -> session.getRole(pl) == BuiltinRoles.HIDER).size();
        int seekers = session.getPlayers(pl -> session.getRole(pl) == BuiltinRoles.SEEKER || session.getRole(pl) == BuiltinRoles.MAIN_SEEKER).size();

        if(seekers == 0) {
            session.endGame(GameSession.EndType.winner(BuiltinRoles.HIDER));
        } else if(hiders == 0) {
            session.endGame(GameSession.EndType.winner(BuiltinRoles.SEEKER));
        }
    }

    @Override
    public Role getTimeoutVictor() {
        return BuiltinRoles.HIDER;
    }

    @Override
    public Role getRespawnPointRole(GameSession session) {

        return session.getState() == GameSession.GameState.HIDING ? BuiltinRoles.SEEKER : BuiltinRoles.HIDER;
    }

    @Override
    public MComponent getHideTimerText(GameSession session, MPlayer player) {

        return HIDE_TIMERS.computeIfAbsent(session, k -> {
            LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
            return prov.getMessage("game.hide_timer", prov.getDefaultLocale(), session.getMap().getGameData().getRoleData(BuiltinRoles.MAIN_SEEKER));
        });
    }

    @Override
    public MComponent getSeekTimerText(GameSession session, MPlayer player) {

        HashMap<Role, MComponent> roleMap = SEEK_TIMERS.computeIfAbsent(session, k -> new HashMap<>());

        return roleMap.computeIfAbsent(session.getRole(player), k -> {

            LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
            return prov.getMessage("game.seek_timer", prov.getDefaultLocale(), session.getMap().getGameData().getRoleData(k));
        });

    }

    @Override
    public MComponent getEndTimerText(GameSession session, MPlayer player) {

        return END_TIMERS.computeIfAbsent(session, k -> {
            LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
            return prov.getMessage("game.end_timer", prov.getDefaultLocale(), session.getMap(), session.getLobbySession().getLobby());
        });
    }

    @Override
    public MComponent getStartTitleText(GameSession session, MPlayer player) {

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        return prov.getMessage("game.start_title", player, player, session.getMap().getGameData().getRoleData(session.getRole(player)));
    }

    @Override
    public MComponent getStartSubtitleText(GameSession session, MPlayer player) {

        Role r = session.getRole(player);
        Role display = r == BuiltinRoles.HIDER ? BuiltinRoles.MAIN_SEEKER : BuiltinRoles.HIDER;

        String key = r == BuiltinRoles.HIDER ? "hider" : "seeker";

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        return prov.getMessage("game.start_subtitle." + key, player, player, session.getMap().getGameData().getRoleData(display));
    }

    @Override
    public boolean canTag(GameSession session, MPlayer tagger, MPlayer tagged) {

        Role taggedRole = session.getRole(tagged);
        if(taggedRole != BuiltinRoles.HIDER) return false;
        if(tagger == null) return true;

        Role taggerRole = session.getRole(tagger);

        return (taggerRole == BuiltinRoles.MAIN_SEEKER || taggerRole == BuiltinRoles.SEEKER) && (session.getClass(tagged) == null || !session.getClass(tagged).isTagImmune());
    }

}
