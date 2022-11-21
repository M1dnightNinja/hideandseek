package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.map.ResourcePackData;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;

import java.util.function.Consumer;

public class ResourcePackDataImpl implements ResourcePackData {

    private final String url;
    private final String hash;
    private final boolean force;
    private final MComponent prompt;

    public ResourcePackDataImpl(String url, String hash, boolean force, MComponent prompt) {
        this.url = url;
        this.hash = hash;
        this.force = force;
        this.prompt = prompt;
    }

    public String getURL() {
        return url;
    }

    public String getHash() {
        return hash;
    }

    public boolean isForced() {
        return force;
    }

    public MComponent getPrompt() {
        return prompt;
    }

    public boolean isValid() {
        return url != null;
    }

    public void apply(MPlayer player, Consumer<MPlayer.ResourcePackStatus> onResponse) {

        if(!isValid()) {
            onResponse.accept(MPlayer.ResourcePackStatus.LOADED);
            return;
        }

        player.applyResourcePack(url, hash, force, prompt, onResponse);
    }

    public static final ConfigSerializer<ResourcePackDataImpl> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.STRING.entry("url", ResourcePackDataImpl::getURL),
            PrimitiveSerializers.STRING.entry("hash", ResourcePackDataImpl::getHash).orDefault(""),
            PrimitiveSerializers.BOOLEAN.entry("forced", ResourcePackDataImpl::isForced).orDefault(true),
            MComponent.INLINE_SERIALIZER.entry("prompt", ResourcePackDataImpl::getPrompt).orDefault(null),
            ResourcePackDataImpl::new
    );

    public static final ResourcePackDataImpl DEFAULT = new ResourcePackDataImpl(null, "", false, null);

}
