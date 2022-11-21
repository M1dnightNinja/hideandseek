package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.Collection;

public interface RoleData {

    TextColor getColor();

    MComponent getName();

    MComponent getProperName();

    MComponent getPluralName();

    Vec3d getSpawnLocation();

    float getSpawnRotation();

    boolean shouldHideName();

    Collection<String> getClassNames();

    String getRandomClass();

}
