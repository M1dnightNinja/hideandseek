package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.function.Consumer;

public interface ResourcePackData {

    String getURL();

    String getHash();

    boolean isForced();

    MComponent getPrompt();

    boolean isValid();

    void apply(MPlayer player, Consumer<MPlayer.ResourcePackStatus> onResponse);

}
