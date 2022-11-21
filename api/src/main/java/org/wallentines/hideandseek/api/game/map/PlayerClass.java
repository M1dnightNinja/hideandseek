package org.wallentines.hideandseek.api.game.map;

import org.wallentines.hideandseek.api.game.UIDisplay;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

public interface PlayerClass {

    String getId();

    UIDisplay getDisplay();

    Collection<MItemStack> getItems();
    MItemStack getEquipmentItem(String slot);

    Collection<Identifier> getEffects();
    int getEffectLevel(Identifier id);

    boolean isTagImmune();

    PlayerClass getEquivalent(Map map, Role role);
    ConfigSection getExtraData();

    void apply(MPlayer player);

}
