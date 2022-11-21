package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.player.MPlayer;

public interface DamageListener {

    void onDamaged(MPlayer player, MPlayer attacker, String sourceId, float amount);

}
