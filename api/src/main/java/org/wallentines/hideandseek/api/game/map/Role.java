package org.wallentines.hideandseek.api.game.map;

import org.wallentines.midnightlib.registry.Identifier;

public class Role {

    private final Identifier id;

    public Role(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public String toString() {
        return id.toString();
    }

}
