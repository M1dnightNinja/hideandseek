package org.wallentines.hideandseek.common.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.timer.GameTimer;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.game.timer.AbstractTimer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.module.vanish.VanishModule;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.api.module.session.AbstractSession;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public abstract class AbstractGameSession extends AbstractSession implements GameSession {

    private static final Identifier TICK_SOUND_ID = new Identifier("minecraft", "block.note_block.pling");
    private static final Identifier START_SOUND_ID = new Identifier("minecraft", "entity.ender_dragon.growl");
    private static final Identifier END_SOUND_ID = new Identifier("minecraft", "ui.toast.challenge_complete");
    protected final HashMap<MPlayer, Role> roles = new HashMap<>();
    protected final HashMap<MPlayer, PlayerClass> classes = new HashMap<>();
    protected final HashMap<MPlayer, CustomScoreboard> scoreboards = new HashMap<>();
    protected final HashMap<MPlayer, Vec3d> locations = new HashMap<>();
    protected final HashSet<MPlayer> toTeleport = new HashSet<>();
    protected final LangProvider provider;
    protected Map currentMap;
    protected GameType type;
    protected LobbySession lobbySession;
    protected Lobby lobby;
    protected GameTimer currentTimer;
    protected GameState state;

    private final HashSet<MPlayer> loading = new HashSet<>();
    private Runnable onLoad;

    protected AbstractGameSession(LobbySession lobbySession, GameType type) {

        super(HideAndSeekAPI.DEFAULT_NAMESPACE, EnumSet.of(Savepoint.SaveFlag.GAME_MODE, Savepoint.SaveFlag.LOCATION, Savepoint.SaveFlag.DATA_TAG));

        this.type = type;
        this.lobbySession = lobbySession;
        this.lobby = lobbySession.getLobby();
        this.provider = HideAndSeekAPI.getInstance().getLangProvider();
        this.state = GameState.NOT_STARTED;

        for(MPlayer mpl : lobbySession.getPlayers()) {
            addPlayer(mpl);
        }
    }

    @Override
    public Role getRole(MPlayer player) {
        return roles.get(player);
    }

    @Override
    public PlayerClass getClass(MPlayer player) {
        return classes.get(player);
    }

    @Override
    public Map getMap() {
        return currentMap;
    }

    @Override
    public void loadMap(Map map, Runnable finished) {

        if(map == null) {

            LOGGER.warn("Attempt to load a null map!");
            shutdown();

            return;
        }

        currentMap = map;
        doMapLoad(map, finished);
    }

    @Override
    public void startGame(MPlayer player) {

        try {

            onLoad = this::startHiding;
            setupPlayers(player);

        } catch (Throwable th) {
            th.printStackTrace();
            shutdown();
        }
    }

    private void startLoading(MPlayer player) {

        loading.add(player);
    }

    private void finishLoading(MPlayer player) {

        loading.remove(player);
        if(loading.isEmpty() && onLoad != null) {
            onLoad.run();
            onLoad = null;
        }
    }

    protected void setupPlayers(MPlayer seeker) {

        try {
            type.setupPlayers(this, seeker);

            SavepointModule spm = getSavepointModule();

            // Platform implementation-specific codee
            onStart();

            getPlayers().forEach(this::startLoading);

            for (MPlayer mpl : getPlayers()) {

                // Teleport Players
                mpl.teleport(getSpawnLocation(roles.get(mpl)));

                // Put everyone in Adventure mode
                mpl.setGameMode(MPlayer.GameMode.ADVENTURE);

                // Choose and apply a class for the player
                PlayerClass clazz = classes.computeIfAbsent(mpl, k -> currentMap.getOrGlobal(currentMap.getGameData().getRoleData(roles.get(mpl)).getRandomClass()));

                spm.resetPlayer(mpl);
                if (clazz != null) {
                    clazz.apply(mpl);
                }

                // Send the map resource pack, if applicable
                currentMap.getGameData().getResourcePack().apply(mpl, status -> finishLoading(mpl));
            }
        } catch (Throwable th) {
            LOGGER.warn("An error occurred while preparing a HideAndSeek Game!");
            th.printStackTrace();

            shutdown();
        }
    }

    protected void startHiding() {

        try {
            state = GameState.HIDING;
            currentTimer = HideAndSeekAPI.getInstance().createTimer(pl -> type.getHideTimerText(this, pl), currentMap.getGameData().getHideTime(), time -> {
                if (time <= 5 && time > 0) {
                    getPlayers().forEach(pl -> pl.playSound(TICK_SOUND_ID, "hostile", 1.0f, 1.0f));
                }
                if (time == 0) {
                    getPlayers().forEach(pl -> pl.playSound(START_SOUND_ID, "hostile", 1.0f, 1.0f));
                    startSeeking();
                }
            });
            currentMap.getGameData().getHideTimerOverrides().forEach(currentTimer::addOverride);

            VanishModule mod = MidnightCoreAPI.getModule(VanishModule.class);
            Collection<MPlayer> seekers = getPlayers(pl -> !currentMap.getGameData().getRoleData(getRole(pl)).shouldHideName());

            for (MPlayer pl : getPlayers()) {
                currentTimer.addViewer(pl);

                MComponent title = type.getStartTitleText(this, pl);
                MComponent subtitle = type.getStartSubtitleText(this, pl);

                pl.sendTitle(title, 20, 80, 20);
                pl.sendSubtitle(subtitle, 20, 80, 20);

                if (mod != null) {
                    RoleData r = currentMap.getGameData().getRoleData(roles.get(pl));
                    if (r.shouldHideName()) {
                        for (MPlayer sk : seekers) {
                            mod.vanishPlayerFor(pl, sk);
                        }
                    }
                }

                updateScoreboard(pl);
            }
            currentTimer.run();

            type.onStartHiding(this);

            for (String s : currentMap.getGameData().getStartCommands()) {
                executeCommand(s);
            }

        } catch (Throwable th) {
            LOGGER.warn("An error occurred while starting a HideAndSeek Game!");
            th.printStackTrace();
            shutdown();
        }
    }

    protected void startSeeking() {

        state = GameState.SEEKING;

        for(MPlayer pl : getPlayers()) updateScoreboard(pl);

        currentTimer.cancel();
        currentTimer = HideAndSeekAPI.getInstance().createTimer(pl -> type.getSeekTimerText(this, pl), currentMap.getGameData().getSeekTime(), time -> {
            if(time <= 5 && time > 0) {
                getPlayers().forEach(pl -> pl.playSound(TICK_SOUND_ID, "hostile", 1.0f, 1.0f));
            }
            if(time == 0) {
                endGame(GameSession.EndType.winner(type.getTimeoutVictor()));
            }
        });
        currentMap.getGameData().getSeekTimerOverrides().forEach(currentTimer::addOverride);

        VanishModule mod = MidnightCoreAPI.getModule(VanishModule.class);
        Collection<MPlayer> seekers = getPlayers(pl -> !currentMap.getGameData().getRoleData(getRole(pl)).shouldHideName());
        for(MPlayer pl : getPlayers()) {
            currentTimer.addViewer(pl);

            if(mod != null) {
                RoleData r = currentMap.getGameData().getRoleData(roles.get(pl));
                if (r.shouldHideName()) {
                    for (MPlayer sk : seekers) {
                        mod.revealPlayerFor(pl, sk);
                    }
                }
            }
        }
        currentTimer.run();

        type.onStartSeeking(this);
    }

    @Override
    public void endGame(GameSession.EndType type) {

        if(state == GameState.ENDED) return;
        state = GameState.ENDED;

        for(MPlayer pl : getPlayers()) updateScoreboard(pl);

        if(getPlayerCount() == 0) {
            shutdown();
            return;
        }

        MComponent titleText;
        if(type.getVictor() == null) {
            titleText = provider.getMessage("game.end_title.draw", (String) null, this, currentMap);
        } else {
            titleText = provider.getMessage("game.end_title", (String) null, this, currentMap, currentMap.getGameData().getRoleData(type.getVictor()));
        }

        if(currentTimer != null) currentTimer.cancel();
        currentTimer = HideAndSeekAPI.getInstance().createTimer(pl -> this.type.getEndTimerText(this, pl), 15, time -> {
            if(time % 4 == 0) {
                for(Vec3d vec : currentMap.getWorldData().getFireworkSpawners()) {
                    spawnFireworks(type.getVictor(), vec, false);
                }
            }
            if (time == 0) {
                shutdown();
            }
        });
        getPlayers().forEach(pl -> {
            currentTimer.addViewer(pl);
            pl.playSound(END_SOUND_ID, "hostile", 1.0f, 1.0f);
            pl.sendTitle(titleText, 20, 80, 20);
        });
        currentTimer.run();

        this.type.onEnd(this, type.getVictor());

    }

    @Override
    public LobbySession getLobbySession() {
        return lobbySession;
    }

    @Override
    public boolean isRunning() {
        return state != GameState.NOT_STARTED;
    }

    @Override
    protected boolean shouldAddPlayer(MPlayer player) {
        return !isRunning() && !contains(player);
    }

    @Override
    public void setRole(MPlayer player, Role role) {
        roles.put(player, role);
    }

    @Override
    public void setClass(MPlayer player, PlayerClass clazz) {
        classes.put(player, clazz);
        if(state != GameState.NOT_STARTED) clazz.apply(player);
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public void tick() {

        if(state == GameState.NOT_STARTED) return;

        for(MPlayer player : toTeleport) {

            teleport(player, locations.get(player));
        }
        toTeleport.clear();

        for(MPlayer player : getPlayers()) {

            Vec3d current = player.getLocation().getCoordinates();
            Vec3d prev = locations.getOrDefault(player, current);

            if(type.isFrozen(this, player) && !current.equals(prev)) {
                toTeleport.add(player);
                locations.put(player, prev);
                continue;
            }

            if(isSafeLocation(player)) {
                locations.put(player, current);
            }

            for(MapRegion reg : currentMap.getGameData().getRegions()) {
                Role r = roles.get(player);
                if(reg.isWithin(current) && reg.isDenied(r)) {

                    toTeleport.add(player);
                    locations.put(player, prev);
                    player.sendMessage(reg.getDenyMessage(player, currentMap.getGameData().getRoleData(r)));
                    break;
                }
            }
        }
    }

    @Override
    public void onDamaged(MPlayer player, MPlayer attacker, String sourceId, float amount) {

        if(currentMap == null || loading.contains(player)) return;

        if(attacker == null) {
            if(currentMap.getGameData().shouldResetOn(Constants.ID_SERIALIZER.readString(sourceId))) {
                locations.put(player, getSpawnLocation(type.getRespawnPointRole(this)).getCoordinates());
                toTeleport.add(player);
            }
            if(currentMap.getGameData().shouldTagOn(Constants.ID_SERIALIZER.readString(sourceId)) && type.canTag(this, null, player)) {
                tagPlayer(player, sourceId);
            }
        } else if (type.canTag(this, attacker, player)) {
            tagPlayer(player, attacker);
        }

        type.checkVictory(this);
    }

    protected void tagPlayer(MPlayer player, MPlayer tagger) {

        if(state == GameState.ENDED) return;

        type.onTag(this, player, tagger);
        runTagActions(player);
    }

    protected void tagPlayer(MPlayer player, String sourceId) {

        if(state == GameState.ENDED) return;

        type.onTag(this, player, sourceId);
        runTagActions(player);
    }

    protected void runTagActions(MPlayer player) {

        spawnFireworks(roles.get(player), locations.get(player), true);

        PlayerClass newClass = null;
        PlayerClass clazz = getClass(player);
        Role r = getRole(player);

        if(clazz != null) {
            newClass = clazz.getEquivalent(currentMap, r);
        }

        getSavepointModule().resetPlayer(player);
        if(newClass == null) newClass = currentMap.getOrGlobal(currentMap.getGameData().getRoleData(r).getRandomClass());
        if(newClass != null) {
            classes.put(player, newClass);
            newClass.apply(player);
        }

        for(MPlayer pl : getPlayers()) updateScoreboard(pl);

        if(state == GameState.HIDING) {
            VanishModule mod = MidnightCoreAPI.getModule(VanishModule.class);

            if(mod != null) {
                Collection<MPlayer> hiders = getPlayers(pl -> currentMap.getGameData().getRoleData(getRole(pl)).shouldHideName());
                for (MPlayer h : hiders) {
                    mod.vanishPlayerFor(h, player);
                }
            }
        }
    }

    @Override
    protected void onAddPlayer(MPlayer player) { }

    @Override
    protected void onRemovePlayer(MPlayer player) {

        if(state == GameState.HIDING) {
            RoleData rd = currentMap.getGameData().getRoleData(roles.get(player));

            VanishModule mod = MidnightCoreAPI.getModule(VanishModule.class);
            if(mod != null) {
                if(!rd.shouldHideName()) {
                    Collection<MPlayer> hiders = getPlayers(pl -> currentMap.getGameData().getRoleData(getRole(pl)).shouldHideName());
                    for (MPlayer h : hiders) {
                        mod.revealPlayerFor(h, player);
                    }
                }
            }
        }

        finishLoading(player);
        if(currentMap.getGameData().getResourcePack().isValid()) {
            HideAndSeekAPI.getInstance().getServerResourcePack().apply(player, status -> { });
        }

        roles.remove(player);
        classes.remove(player);

        if(scoreboards.containsKey(player)) {
            scoreboards.get(player).clearViewers();
            scoreboards.remove(player);
        }

        if(currentTimer != null) {
            currentTimer.removeViewer(player);
        }

        if(getPlayerCount() == 0) {
            endGame(GameSession.EndType.draw());
            return;
        }

        type.checkVictory(this);
        for(MPlayer pl : getPlayers()) updateScoreboard(pl);
    }

    @Override
    protected void onShutdown() {
        if(currentTimer != null) currentTimer.cancel();
    }

    protected void updateScoreboard(MPlayer player) {

        if(currentMap == null || currentMap.getScoreboardTemplate() == null) return;

        RoleData r = currentMap.getGameData().getRoleData(getRole(player));
        PlayerClass c = getClass(player);

        Object[] context = { provider, player, this, r, c, currentMap, lobbySession, lobby };

        CustomScoreboard sb = scoreboards.computeIfAbsent(player, k -> {

            MidnightCoreAPI api = MidnightCoreAPI.getInstance();
            if(api == null) {
                throw new IllegalStateException("MidnightCoreAPI is null!"); // Should never happen.
            }
            CustomScoreboard board = api.createScoreboard(CustomScoreboard.generateRandomId(), currentMap.getScoreboardTemplate().getTitle(context));
            board.addViewer(player);

            return board;

        });

        currentMap.getScoreboardTemplate().fill(sb, context);
        sb.update();
    }

    protected abstract void teleport(MPlayer player, Vec3d loc);
    protected abstract Location getSpawnLocation(Role role);
    protected abstract void doMapLoad(Map map, Runnable finished);
    protected abstract void executeCommand(String command);
    protected abstract void spawnFireworks(Role role, Vec3d location, boolean explode);
    protected abstract void onStart();
    protected abstract Identifier getWorldId();
    protected abstract boolean isSafeLocation(MPlayer mpl);

    public static void registerPlaceholders(PlaceholderManager manager) {

        Constants.registerPlaceholder(manager, "game_phase_name", ctx -> {

            MPlayer mpl = ctx.getArgument(MPlayer.class);
            AbstractGameSession sess = ctx.getArgument(AbstractGameSession.class);
            if(sess == null) return null;

            return HideAndSeekAPI.getInstance().getLangProvider().getMessage("phase." + sess.state.getLangId(), mpl, ctx.getArgs());
        });

        Constants.registerInlinePlaceholder(manager,"game_hider_count", PlaceholderSupplier.create(AbstractGameSession.class, sess -> sess.type.getHiders(sess).size() + ""));
        Constants.registerInlinePlaceholder(manager,"game_seeker_count", PlaceholderSupplier.create(AbstractGameSession.class, sess -> sess.type.getSeekers(sess).size() + ""));
        Constants.registerInlinePlaceholder(manager,"game_current_seconds", PlaceholderSupplier.create(AbstractGameSession.class, sess -> sess.currentTimer.getTimeLeft() + ""));
        Constants.registerInlinePlaceholder(manager,"game_current_time", PlaceholderSupplier.create(AbstractGameSession.class, sess -> AbstractTimer.formatTime(sess.currentTimer.getTimeLeft() * 1000L) + ""));

    }

}
