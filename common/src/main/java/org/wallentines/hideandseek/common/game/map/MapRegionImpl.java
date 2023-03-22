package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.map.MapRegion;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.CustomPlaceholder;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.math.Region;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.*;

public class MapRegionImpl implements MapRegion {

    private final MComponent name;
    private final Region region;
    private final Set<Role> denied = new HashSet<>();

    public MapRegionImpl(MComponent name, Region region) {
        this.name = name;
        this.region = region;
    }

    public MapRegionImpl(MComponent name, Region region, Collection<Role> denied) {
        this.name = name;
        this.region = region;
        this.denied.addAll(denied);
    }

    @Override
    public MComponent getName() {
        return name;
    }

    @Override
    public boolean isWithin(Vec3d vec) {
        return region.isWithin(vec);
    }

    @Override
    public boolean canEnter(Role role) {
        return !denied.contains(role);
    }

    @Override
    public boolean isDenied(Role role) {
        return denied.contains(role);
    }

    @Override
    public MComponent getDenyMessage(MPlayer player, RoleData data) {

        return HideAndSeekAPI.getInstance().getLangProvider().getMessage("region.deny", player, player, data, CustomPlaceholder.create("region_name", name));
    }

    public static final Serializer<MapRegion> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, MapRegion value) {
            if(!(value instanceof MapRegionImpl)) return SerializeResult.failure(value + " is not a MapRegionImpl!");
            return INTERNAL_SERIALIZER.serialize(context, (MapRegionImpl) value);
        }

        @Override
        public <O> SerializeResult<MapRegion> deserialize(SerializeContext<O> context, O value) {
            return INTERNAL_SERIALIZER.deserialize(context, value).flatMap(mri -> mri);
        }
    };

    private static final Serializer<MapRegionImpl> INTERNAL_SERIALIZER = ObjectSerializer.create(
            MComponent.SERIALIZER.entry("name", MapRegionImpl::getName),
            Region.SERIALIZER.entry("bounds", mri -> mri.region),
            ContentRegistryImpl.REGISTERED_ROLE.listOf().entry("denied", mri -> mri.denied),
            MapRegionImpl::new
    );
}
