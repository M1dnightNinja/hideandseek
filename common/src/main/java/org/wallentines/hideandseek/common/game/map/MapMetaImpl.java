package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.map.MapMeta;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MapMetaImpl implements MapMeta {

    private final UUID author;
    private final List<UUID> editors = new ArrayList<>();

    public MapMetaImpl(UUID author) {
        this.author = author;
    }

    @Override
    public UUID getAuthor() {
        return author;
    }

    @Override
    public Collection<UUID> getEditors() {
        return editors;
    }

    @Override
    public boolean isAuthor(MPlayer player) {
        return player.getUUID().equals(author);
    }

    @Override
    public boolean canEdit(MPlayer player) {
        return player.getUUID().equals(author) || editors.contains(player.getUUID());
    }

    public static final ConfigSerializer<MapMetaImpl> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.UUID.entry("author", MapMetaImpl::getAuthor).orDefault(new UUID(0L, 0L)),
            PrimitiveSerializers.UUID.listOf().entry("editors", MapMetaImpl::getEditors).optional(),
            (author, editors) -> {
                MapMetaImpl out = new MapMetaImpl(author);
                out.editors.addAll(editors);
                return out;
            }
    );

}
