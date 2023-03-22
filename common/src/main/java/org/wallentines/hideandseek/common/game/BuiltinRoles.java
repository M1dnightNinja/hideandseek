package org.wallentines.hideandseek.common.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.common.game.map.RoleDataImpl;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

public class BuiltinRoles {

    public static final Role HIDER       = makeRole("hider");
    public static final Role SEEKER      = makeRole("seeker");
    public static final Role MAIN_HIDER  = makeRole("main_hider");
    public static final Role MAIN_SEEKER = makeRole("main_seeker");

    public static final RoleDataImpl DEFAULT_HIDER_DATA = makeRoleData(9, "Hider", true);
    public static final RoleDataImpl DEFAULT_SEEKER_DATA = makeRoleData(12, "Seeker", false);

    private static Role makeRole(String id) {
        return new Role(new Identifier(HideAndSeekAPI.DEFAULT_NAMESPACE, id));
    }
    private static RoleDataImpl makeRoleData(int color, String name, boolean hideName) {

        Color clr = Color.fromRGBI(color);
        MComponent dName = new MTextComponent(name).withStyle(new MStyle().withColor(clr));

        return new RoleDataImpl().setName(dName).setColor(clr).setHideName(hideName);
    }

}
