package org.wallentines.hideandseek.common.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.timer.GameTimer;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.module.session.AbstractSession;

import java.util.HashMap;


public class LobbySessionImpl extends AbstractSession implements LobbySession {

    private final Lobby lobby;
    private final HashMap<MPlayer, CustomScoreboard> scoreboards = new HashMap<>();
    private final LangProvider provider = HideAndSeekAPI.getInstance().getLangProvider();

    private GameSession currentSession;
    private GameTimer startTimer;


    public LobbySessionImpl(Lobby lobby) {
        super(Constants.DEFAULT_NAMESPACE);
        this.lobby = lobby;
    }

    @Override
    public Lobby getLobby() {
        return lobby;
    }

    @Override
    public void startGame(MPlayer seeker, Map map) {

        if(startTimer != null) {
            startTimer.cancel();
            startTimer = null;
        }

        if(map == null) map = lobby.getRandomMap();

        broadcastMessage("lobby.map_selected", HideAndSeekAPI.getInstance().getLangProvider(), map, this, lobby);

        currentSession = HideAndSeekAPI.getInstance().getSessionManager().createGameSession(this, lobby.getGameType());
        if(currentSession == null) shutdown();

        currentSession.shutdownEvent().register(this, ev -> {
            getPlayers().forEach(player -> {
                player.teleport(lobby.getLocation());

                if(lobby.getScoreboardTemplate() != null) getPlayerScoreboard(player).addViewer(player);
            });
            currentSession = null;
            onPlayerCountChanged();
        });

        currentSession.loadMap(map, () -> {

            for(CustomScoreboard sb : scoreboards.values()) sb.clearViewers();
            currentSession.startGame(seeker);
        });
    }

    @Override
    public GameSession getCurrentGame() {

        return currentSession;
    }

    @Override
    public boolean isRunning() {
        return currentSession != null && currentSession.isRunning();
    }

    @Override
    protected boolean shouldAddPlayer(MPlayer player) {
        return !isRunning() && getPlayerCount() < lobby.getMaxPlayers();
    }

    @Override
    protected void onAddPlayer(MPlayer player) {

        getSavepointModule().resetPlayer(player);
        player.teleport(getLobby().getLocation());

        if(lobby.getScoreboardTemplate() != null) getPlayerScoreboard(player).addViewer(player);
        onPlayerCountChanged();

        broadcastMessage("lobby.join", provider, player, this, lobby);
    }

    @Override
    public void removePlayer(MPlayer player) {

        if(currentSession != null) currentSession.removePlayer(player);
        super.removePlayer(player);
    }

    @Override
    protected void onRemovePlayer(MPlayer player) {

        broadcastMessage("lobby.leave", provider, player, this, lobby);

        CustomScoreboard sb = scoreboards.get(player);
        if(sb != null) sb.clearViewers();

        onPlayerCountChanged();

        if(startTimer != null) startTimer.removeViewer(player);

    }

    @Override
    protected void onShutdown() {

        for(CustomScoreboard sb : scoreboards.values()) sb.clearViewers();
        scoreboards.clear();

        if(currentSession != null) currentSession.shutdown();
    }


    @Override
    public void tick() {

        if(currentSession != null) currentSession.tick();
    }

    @Override
    public void onDamaged(MPlayer player, MPlayer attacker, String sourceId, float amount) {

        if(currentSession != null) currentSession.onDamaged(player, attacker, sourceId, amount);
    }

    private CustomScoreboard getPlayerScoreboard(MPlayer mpl) {
        return scoreboards.computeIfAbsent(mpl, k -> MidnightCoreAPI.getInstance().createScoreboard(CustomScoreboard.generateRandomId(), lobby.getScoreboardTemplate().getTitle(mpl, HideAndSeekAPI.getInstance().getLangProvider(), this, lobby)));
    }

    private void onPlayerCountChanged() {

        if(isRunning()) return;

        int count = getPlayerCount();

        if(count >= lobby.getMinPlayers() && startTimer == null) {

            LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
            MComponent timer = prov.getMessage("lobby.start_timer", (String) null, this, lobby);
            startTimer = HideAndSeekAPI.getInstance().createTimer(timer, 180, i -> {
                if(i == 0) {
                    startGame(null, null);
                }
            });

            getPlayers().forEach(startTimer::addViewer);
            startTimer.run();

        } else if(count < lobby.getMinPlayers() && startTimer != null) {

            startTimer.cancel();
            startTimer = null;
        }

        if(lobby.getScoreboardTemplate() != null) {
            for (MPlayer mpl : getPlayers()) {
                CustomScoreboard sb = getPlayerScoreboard(mpl);
                lobby.getScoreboardTemplate().fill(sb, mpl, HideAndSeekAPI.getInstance().getLangProvider(), this, lobby);
                sb.update();
            }
        }
    }
}
