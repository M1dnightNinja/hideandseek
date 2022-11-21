package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

public interface WorldData {

    Identifier getDimensionType();

    boolean hasRandomTime();

    boolean hasRain();

    boolean hasThunder();

    Collection<Vec3d> getFireworkSpawners();

}
