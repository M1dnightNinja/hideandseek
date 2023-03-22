package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.map.*;
import org.wallentines.hideandseek.api.game.timer.TimerOverride;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.timer.TimerOverrideImpl;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;
import java.util.Map;

public class GameDataImpl implements GameData {

    final ResourcePackDataImpl resourcePack;
    final List<MapRegion> regions = new ArrayList<>();
    final Set<Identifier> tagSources = new HashSet<>();
    final Set<Identifier> resetSources = new HashSet<>();
    final TimerData hideTimer;
    final TimerData seekTimer;
    final HashMap<String, String> customDeathMessageKeys = new HashMap<>();
    final HashMap<Role, RoleDataImpl> roleData = new HashMap<>();
    final List<String> startCommands = new ArrayList<>();

    public GameDataImpl(ResourcePackDataImpl resourcePack, TimerData hideTimer, TimerData seekTimer) {
        this.resourcePack = resourcePack;
        this.hideTimer = hideTimer;
        this.seekTimer = seekTimer;
    }

    @Override
    public ResourcePackData getResourcePack() {
        return resourcePack;
    }

    @Override
    public Collection<MapRegion> getRegions() {
        return regions;
    }

    @Override
    public boolean shouldTagOn(Identifier damageSource) {
        return tagSources.contains(damageSource);
    }

    @Override
    public boolean shouldResetOn(Identifier damageSource) {
        return resetSources.contains(damageSource);
    }

    @Override
    public int getHideTime() {
        return hideTimer.getTime();
    }

    @Override
    public int getSeekTime() {
        return seekTimer.getTime();
    }

    @Override
    public Collection<TimerOverride> getHideTimerOverrides() {
        return hideTimer.getOverrides();
    }

    @Override
    public Collection<TimerOverride> getSeekTimerOverrides() {
        return seekTimer.getOverrides();
    }

    @Override
    public String getDeathMessageKey(String damageSource) {
        return customDeathMessageKeys.get(damageSource);
    }

    @Override
    public Collection<String> getStartCommands() {
        return startCommands;
    }

    @Override
    public RoleData getRoleData(Role role) {

        RoleData data = roleData.get(role);
        if(data == null) {
            HideAndSeekAPI.getLogger().warn("Data for role " + role.getId() + " was requested, but was not found!");
        }
        return data;
    }

    public void setRoleData(Role role, RoleDataImpl data) {

        roleData.put(role, data.inheritFrom(ContentRegistryImpl.INSTANCE.getDefaultData(role)));
    }


    private static GameDataImpl make(ResourcePackDataImpl packData, Collection<MapRegion> regions,
                                     Collection<Identifier> tagSources, Collection<Identifier> resetSources,
                                     TimerData hideTimer, TimerData seekTimer, Map<String, String> deathMessages,
                                     Map<Role, RoleDataImpl> roles, Collection<String> startCommands) {

        GameDataImpl out = new GameDataImpl(packData, hideTimer, seekTimer);
        roles.forEach(out::setRoleData);

        if(regions != null) out.regions.addAll(regions);
        if(tagSources != null) out.tagSources.addAll(tagSources);
        if(resetSources != null) out.resetSources.addAll(resetSources);
        if(deathMessages != null) out.customDeathMessageKeys.putAll(deathMessages);
        if(startCommands != null) out.startCommands.addAll(startCommands);
        return out;
    }

    public static final Serializer<GameDataImpl> SERIALIZER = ObjectSerializer.create(
            ResourcePackDataImpl.SERIALIZER.<GameDataImpl>entry("resource_pack", gd -> gd.resourcePack).orElse(ResourcePackDataImpl.DEFAULT),
            MapRegionImpl.SERIALIZER.listOf().entry("regions", GameDataImpl::getRegions).optional(),
            Identifier.serializer(HideAndSeekAPI.DEFAULT_NAMESPACE).listOf().<GameDataImpl>entry("tag_sources", gd -> gd.tagSources).optional(),
            Identifier.serializer(HideAndSeekAPI.DEFAULT_NAMESPACE).listOf().<GameDataImpl>entry("reset_sources", gd -> gd.resetSources).optional(),
            TimerData.SERIALIZER.entry("hide_timer", gd -> gd.hideTimer),
            TimerData.SERIALIZER.entry("seek_timer", gd -> gd.seekTimer),
            Serializer.STRING.mapOf().<GameDataImpl>entry("death_messages", gd -> gd.customDeathMessageKeys).optional(),
            RoleDataImpl.SERIALIZER.mapOf(ContentRegistryImpl.REGISTERED_ROLE).entry("roles", gd -> gd.roleData),
            Serializer.STRING.listOf().entry("start_commands", GameDataImpl::getStartCommands).optional(),
            GameDataImpl::make
    );


    public static class TimerData {

        private final int time;
        private final List<TimerOverride> overrides = new ArrayList<>();

        public TimerData(int time) {
            this.time = time;
        }

        public int getTime() {
            return time;
        }

        public List<TimerOverride> getOverrides() {
            return overrides;
        }

        private static final Serializer<TimerData> SERIALIZER = ObjectSerializer.create(
            Serializer.INT.entry("time", TimerData::getTime),
            TimerOverrideImpl.SERIALIZER.listOf().entry("overrides", TimerData::getOverrides).optional(),
            (time, overrides) -> {
                TimerData out = new TimerData(time);
                if(overrides != null) out.overrides.addAll(overrides);
                return out;
            }
        );

    }

}
