package org.wallentines.hideandseek.common.game;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PermissionCache {

    private final Set<String> permissions = new HashSet<>();
    private final boolean passthrough;

    public PermissionCache(boolean passthrough) {
        this.passthrough = passthrough;
    }

    public PermissionCache(boolean passthrough, String... permissions) {
        this.passthrough = passthrough;
        this.permissions.addAll(Arrays.asList(permissions));
    }

    public Boolean hasPermission(String permission) {

        if(permissions.contains(permission)) {
            return true;
        }
        if(permissions.contains("-" + permission)) {
            return false;
        }

        return null;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean shouldPassthrough() {
        return passthrough;
    }

    public static final ConfigSerializer<PermissionCache> SERIALIZER = new ConfigSerializer<>() {
        @Override
        public PermissionCache deserialize(ConfigSection section) {

            PermissionCache out = new PermissionCache(section.getBoolean("passthrough"));
            out.permissions.addAll(section.getStringList("permissions"));

            return out;
        }

        @Override
        public ConfigSection serialize(PermissionCache o) {
            return new ConfigSection().with("passthrough", o.passthrough).with("permissions", new ArrayList<>(o.permissions));
        }
    };
}
