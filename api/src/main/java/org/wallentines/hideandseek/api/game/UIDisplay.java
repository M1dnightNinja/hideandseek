package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.TextColor;

import java.util.Collection;

public interface UIDisplay {

    MComponent getName();

    Collection<MComponent> getDescription();

    MItemStack getDisplayItem();

    TextColor getColor();

}
