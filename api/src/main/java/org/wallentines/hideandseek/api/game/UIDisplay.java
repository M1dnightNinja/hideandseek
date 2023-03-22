package org.wallentines.hideandseek.api.game;

import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.math.Color;

import java.util.Collection;

public interface UIDisplay {

    MComponent getName();

    Collection<MComponent> getDescription();

    MItemStack getDisplayItem();

    Color getColor();

    CustomIconData getCustomIcon();

    class CustomIconData {

        public String imageFile;
        public int x;
        public int y;
        public int width;
        public int height;
        public CustomIconData(String imageFile, int x, int y, int width, int height) {
            this.imageFile = imageFile;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public static final Serializer<CustomIconData> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("image_file", ci -> ci.imageFile),
                Serializer.INT.entry("x", ci -> ci.x),
                Serializer.INT.entry("y", ci -> ci.y),
                Serializer.INT.entry("width", ci -> ci.width),
                Serializer.INT.entry("height", ci -> ci.height),
                CustomIconData::new
        );
    }

}
