package org.wallentines.hideandseek.api.game.map;

import org.wallentines.hideandseek.api.game.timer.GameTimer;
import org.wallentines.hideandseek.api.game.timer.TimerOverride;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

public interface GameData {

    ResourcePackData getResourcePack();
    Collection<MapRegion> getRegions();

    boolean shouldTagOn(Identifier damageSource);
    boolean shouldResetOn(Identifier damageSource);

    int getHideTime();
    int getSeekTime();

    Collection<TimerOverride> getHideTimerOverrides();
    Collection<TimerOverride> getSeekTimerOverrides();

    String getDeathMessageKey(String damageSource);

    Collection<String> getStartCommands();
    RoleData getRoleData(Role role);

}
