package org.wallentines.hideandseek.api.game.map;

import org.wallentines.hideandseek.api.game.*;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface Map {

    String getId();

    MapMeta getMeta();

    GameData getGameData();

    UIDisplay getDisplay();

    WorldData getWorldData();

    PregameData getPregameData();

    ScoreboardTemplate getScoreboardTemplate();

    File getDataFolder();
    File getWorldFolder();

    PlayerClass getClass(String id);
    PlayerClass getOrGlobal(String id);

    boolean canView(MPlayer player);
    boolean canEdit(MPlayer player);

}
