package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.math.Vec3d;

public interface MapRegion {

    MComponent getName();

    boolean isWithin(Vec3d vec);

    boolean canEnter(Role role);

    MComponent getDenyMessage(MPlayer player, RoleData data);

}
