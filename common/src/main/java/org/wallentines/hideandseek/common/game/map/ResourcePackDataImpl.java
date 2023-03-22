package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.map.ResourcePackData;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;

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

    public static final Serializer<ResourcePackDataImpl> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("url", ResourcePackDataImpl::getURL),
            Serializer.STRING.entry("hash", ResourcePackDataImpl::getHash).orElse(""),
            Serializer.BOOLEAN.entry("forced", ResourcePackDataImpl::isForced).orElse(true),
            MComponent.SERIALIZER.entry("prompt", ResourcePackDataImpl::getPrompt).orElse(null),
            ResourcePackDataImpl::new
    );

    public static final ResourcePackDataImpl DEFAULT = new ResourcePackDataImpl(null, "", false, null);

}
