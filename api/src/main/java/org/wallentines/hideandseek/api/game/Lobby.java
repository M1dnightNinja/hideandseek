package org.wallentines.hideandseek.api.game;

import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.TextColor;

import java.util.Collection;

public interface Lobby {

    String getId();

    UIDisplay getDisplay();

    int getMinPlayers();

    int getMaxPlayers();

    GameType getGameType();

    Location getLocation();

    boolean canAccess(MPlayer player);

    boolean containsMap(Map m);

    Map getRandomMap();

    Collection<Map> getMaps();

    ScoreboardTemplate getScoreboardTemplate();

}
