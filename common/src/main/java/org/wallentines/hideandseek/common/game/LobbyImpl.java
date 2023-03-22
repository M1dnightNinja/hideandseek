package org.wallentines.hideandseek.common.game;

import org.wallentines.hideandseek.api.game.GameType;
import org.wallentines.hideandseek.api.game.Lobby;
import org.wallentines.hideandseek.api.game.ScoreboardTemplate;
import org.wallentines.hideandseek.api.game.UIDisplay;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightcore.api.module.session.AbstractSession;
import org.wallentines.midnightlib.math.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class LobbyImpl implements Lobby {

    private final String id;
    private final Location location;
    private final GameType gameType;
    private final ScoreboardTemplateImpl scoreboardTemplate;

    private final UIDisplayImpl display;

    private int minPlayers;
    private int maxPlayers;

    private String permission;

    private final List<Map> maps = new ArrayList<>();

    public LobbyImpl(String id, Location location, GameType gameType, UIDisplayImpl display, ScoreboardTemplateImpl scoreboardTemplate) {
        this.id = id;
        this.location = location;
        this.gameType = gameType;

        this.display = display;
        this.scoreboardTemplate = scoreboardTemplate;

        this.minPlayers = gameType.getMinPlayers();
        this.maxPlayers = gameType.getMaxPlayers();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public UIDisplay getDisplay() {
        return display;
    }

    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = Math.max(minPlayers, gameType.getMinPlayers());
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = Math.min(maxPlayers, gameType.getMaxPlayers());
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean canAccess(MPlayer player) {

        return permission == null || player.hasPermission(permission) ;
    }

    @Override
    public boolean containsMap(Map m) {
        return maps.contains(m);
    }

    @Override
    public Map getRandomMap() {

        return maps.get(AbstractSession.RANDOM.nextInt(maps.size()));
    }

    @Override
    public Collection<Map> getMaps() {
        return maps;
    }

    @Override
    public ScoreboardTemplate getScoreboardTemplate() {
        return scoreboardTemplate;
    }

    private static LobbyImpl make(String id, Location location, GameType gameType, UIDisplayImpl display, int minPlayers, int maxPlayers, Collection<Map> maps, String permission, ScoreboardTemplateImpl template) {

        if(display == null) display = new UIDisplayImpl(new MTextComponent(id), new ArrayList<>(), Color.WHITE, null);
        LobbyImpl impl = new LobbyImpl(id, location, gameType, display, template);

        impl.setMinPlayers(minPlayers);
        impl.setMaxPlayers(maxPlayers);
        impl.maps.addAll(maps);
        impl.permission = permission;

        return impl;
    }

    public static final Serializer<LobbyImpl> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("id", LobbyImpl::getId),
            Location.SERIALIZER.entry("location", LobbyImpl::getLocation),
            ContentRegistryImpl.REGISTERED_GAME_TYPE.entry("game_type", lobby -> lobby.gameType),
            UIDisplayImpl.SERIALIZER.<LobbyImpl>entry("display", lobby -> lobby.display).optional(),
            Serializer.INT.entry("min_players", LobbyImpl::getMinPlayers).orElse(2),
            Serializer.INT.entry("max_players", LobbyImpl::getMaxPlayers).orElse(16),
            ContentRegistryImpl.REGISTERED_MAP.filteredListOf().entry("maps", l -> l.maps),
            Serializer.STRING.<LobbyImpl>entry("permission", lobby -> lobby.permission).optional(),
            ScoreboardTemplateImpl.SERIALIZER.<LobbyImpl>entry("scoreboard", lobby -> lobby.scoreboardTemplate).optional(),
            LobbyImpl::make
    );

    public static void registerPlaceholders(PlaceholderManager manager) {

        Constants.registerPlaceholder(manager, "lobby_game_type_name", PlaceholderSupplier.create(LobbyImpl.class, l -> l.getGameType().getName()));
        Constants.registerPlaceholder(manager, "lobby_name", PlaceholderSupplier.create(LobbyImpl.class, l -> l.getDisplay().getName()));

        Constants.registerInlinePlaceholder(manager, "lobby_id", PlaceholderSupplier.create(LobbyImpl.class, LobbyImpl::getId));
        Constants.registerInlinePlaceholder(manager, "lobby_color", PlaceholderSupplier.create(LobbyImpl.class, l -> l.getDisplay().getColor().toHex()));
        Constants.registerInlinePlaceholder(manager, "lobby_game_type_id", PlaceholderSupplier.create(LobbyImpl.class, l -> l.getGameType().getId().toString()));
        Constants.registerInlinePlaceholder(manager, "lobby_min_players", PlaceholderSupplier.create(LobbyImpl.class, l -> Objects.toString(l.getMinPlayers())));
        Constants.registerInlinePlaceholder(manager, "lobby_max_players", PlaceholderSupplier.create(LobbyImpl.class, l -> Objects.toString(l.getMaxPlayers())));
        Constants.registerInlinePlaceholder(manager, "lobby_permission", PlaceholderSupplier.create(LobbyImpl.class, l -> l.permission));
    }
}
