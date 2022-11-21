package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.map.PregameData;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

public class PregameDataImpl implements PregameData {

    private final Vec3d center;
    private final double radius;

    public PregameDataImpl(Vec3d center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public Vec3d getSpawnCenter() {
        return center;
    }

    @Override
    public double getSpawnRadius() {
        return radius;
    }

    @Override
    public void teleportPlayers(Session session, Identifier worldId) {

        int n = session.getPlayerCount();

        double degrees = 360.0d / (double) n;

        int index = 0;
        for(MPlayer mpl : session.getPlayers()) {

            double d = index * degrees;
            double A = d % 90.0d;
            double B = d / 90.0d;

            double r = getSpawnRadius();
            double x = Math.sin(Math.toRadians(A)) / r;
            double z = Math.sin(Math.toRadians(B)) / r;

            float yaw = (float) d - 270.0f;
            if(yaw < -180.0f) yaw += 360.0f;

            Location l = new Location(worldId, getSpawnCenter().add(new Vec3d(x, 0, z)), yaw, 0.0f);
            mpl.teleport(l);
        }
    }

    public static final ConfigSerializer<PregameDataImpl> SERIALIZER = ConfigSerializer.create(
        Vec3d.SERIALIZER.entry("spawn_center", PregameDataImpl::getSpawnCenter),
        PrimitiveSerializers.DOUBLE.entry("spawn_radius", PregameDataImpl::getSpawnRadius),
        PregameDataImpl::new
    );

}
