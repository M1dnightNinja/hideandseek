package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

public interface UIDisplay {

    MComponent getName();

    Collection<MComponent> getDescription();

    MItemStack getDisplayItem();

    TextColor getColor();

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

        public static final ConfigSerializer<CustomIconData> SERIALIZER = ConfigSerializer.create(
                PrimitiveSerializers.STRING.entry("image_file", ci -> ci.imageFile),
                PrimitiveSerializers.INT.entry("x", ci -> ci.x),
                PrimitiveSerializers.INT.entry("y", ci -> ci.y),
                PrimitiveSerializers.INT.entry("width", ci -> ci.width),
                PrimitiveSerializers.INT.entry("height", ci -> ci.height),
                CustomIconData::new
        );
    }

}
