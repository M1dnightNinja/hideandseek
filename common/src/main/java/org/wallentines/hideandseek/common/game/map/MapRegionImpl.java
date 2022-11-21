package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.map.MapRegion;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.CustomPlaceholder;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.math.Region;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapRegionImpl implements MapRegion {

    private final MComponent name;
    private final Region region;
    private final Set<Role> denied = new HashSet<>();

    public MapRegionImpl(MComponent name, Region region) {
        this.name = name;
        this.region = region;
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
        return denied.contains(role);
    }

    @Override
    public MComponent getDenyMessage(MPlayer player, RoleData data) {

        return HideAndSeekAPI.getInstance().getLangProvider().getMessage("region.deny", player, player, data, CustomPlaceholder.create("region_name", name));
    }

    public static final ConfigSerializer<MapRegionImpl> SERIALIZER = new ConfigSerializer<>() {
        @Override
        public MapRegionImpl deserialize(ConfigSection section) {

            MComponent name = section.get("name", MComponent.class);
            Region reg = section.get("bounds", Region.class);

            MapRegionImpl out = new MapRegionImpl(name, reg);
            if(section.has("denied", List.class)) {
                section.getListFiltered("denied", Constants.ID_SERIALIZER).forEach(id -> {
                    Role r = ContentRegistryImpl.INSTANCE.getRole(id);
                    if(r == null) return;
                    out.denied.add(r);
                });
            }

            return out;
        }

        @Override
        public ConfigSection serialize(MapRegionImpl object) {
            return new ConfigSection().with("name", object.name).with("bounds", object.region).with("denied", new ArrayList<>(object.denied));
        }
    };
}
