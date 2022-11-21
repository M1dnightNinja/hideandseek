package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.Collection;
import java.util.UUID;

public interface MapMeta {

    UUID getAuthor();
    Collection<UUID> getEditors();

    boolean isAuthor(MPlayer player);

    boolean canEdit(MPlayer player);

}
