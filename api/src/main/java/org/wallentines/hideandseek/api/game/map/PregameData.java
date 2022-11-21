package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

public interface PregameData {

    Vec3d getSpawnCenter();

    double getSpawnRadius();

    void teleportPlayers(Session session, Identifier worldId);

}
