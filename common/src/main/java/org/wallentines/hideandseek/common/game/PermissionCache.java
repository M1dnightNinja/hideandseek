package org.wallentines.hideandseek.common.game;

import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.*;

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

    public PermissionCache(boolean passthrough, Collection<String> permissions) {
        this.passthrough = passthrough;
        this.permissions.addAll(permissions);
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


    public static final Serializer<PermissionCache> SERIALIZER = ObjectSerializer.create(
            Serializer.BOOLEAN.entry("passthrough", pc -> pc.passthrough),
            Serializer.STRING.listOf().entry("permissions", PermissionCache::getPermissions),
            PermissionCache::new
    );

}
