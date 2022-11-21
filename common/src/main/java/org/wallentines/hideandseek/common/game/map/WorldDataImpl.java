package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.map.WorldData;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorldDataImpl implements WorldData {

    private final Identifier dimensionType;
    private final boolean randomTime;
    private final boolean rain;
    private final boolean thunder;
    private final List<Vec3d> fireworkSpawners = new ArrayList<>();

    public WorldDataImpl(Identifier dimensionType, boolean randomTime, boolean rain, boolean thunder) {
        this.dimensionType = dimensionType;
        this.randomTime = randomTime;
        this.rain = rain;
        this.thunder = thunder;
    }

    @Override
    public Identifier getDimensionType() {
        return dimensionType;
    }

    @Override
    public boolean hasRandomTime() {
        return randomTime;
    }

    @Override
    public boolean hasRain() {
        return rain;
    }

    @Override
    public boolean hasThunder() {
        return thunder;
    }

    @Override
    public Collection<Vec3d> getFireworkSpawners() {
        return fireworkSpawners;
    }

    private static WorldDataImpl make(Identifier id, boolean randomTime, boolean rain, boolean thunder, Collection<Vec3d> fireworkSpawners) {

        WorldDataImpl out = new WorldDataImpl(id, randomTime, rain, thunder);
        out.fireworkSpawners.addAll(fireworkSpawners);

        return out;
    }

    public static final ConfigSerializer<WorldDataImpl> SERIALIZER = ConfigSerializer.create(
            Constants.ID_SERIALIZER.entry("dimension_type", WorldDataImpl::getDimensionType).orDefault(new Identifier("minecraft", "overworld")),
            PrimitiveSerializers.BOOLEAN.entry("random_time", WorldDataImpl::hasRandomTime).orDefault(false),
            PrimitiveSerializers.BOOLEAN.entry("rain", WorldDataImpl::hasRain).orDefault(false),
            PrimitiveSerializers.BOOLEAN.entry("thunder", WorldDataImpl::hasThunder).orDefault(false),
            Vec3d.SERIALIZER.listOf().entry("firework_spawners", WorldDataImpl::getFireworkSpawners).optional(),
            WorldDataImpl::make
    );

}
