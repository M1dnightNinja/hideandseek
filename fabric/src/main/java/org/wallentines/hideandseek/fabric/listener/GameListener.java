package org.wallentines.hideandseek.fabric.listener;

import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.core.SessionManager;
import org.wallentines.hideandseek.api.game.EditingSession;
import org.wallentines.hideandseek.api.game.LobbySession;
import org.wallentines.hideandseek.api.game.ViewingSession;
import org.wallentines.hideandseek.common.game.timer.AbstractTimer;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.PermissionCache;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.fabric.event.entity.EntityDamageEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerFoodLevelChangeEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.event.Event;

import java.util.HashSet;

public class GameListener {

    private static final SessionManager SESSION_MANAGER = HideAndSeekAPI.getInstance().getSessionManager();
    private static final ResourceLocation EARLY_PHASE = new ResourceLocation(HideAndSeekAPI.DEFAULT_NAMESPACE, "early");

    private static final HashSet<ServerPlayer> ADMINS = new HashSet<>();

    public static void register() {

        Event.register(ServerStopEvent.class, GameListener.class, ev -> AbstractTimer.cancelAll());
        Event.register(PlayerJoinEvent.class, GameListener.class, GameListener::onJoin);
        Event.register(EntityDamageEvent.class, GameListener.class, GameListener::onDamage);
        Event.register(PlayerFoodLevelChangeEvent.class, GameListener.class, GameListener::onHunger);

        PermissionCheckEvent.EVENT.addPhaseOrdering(EARLY_PHASE, net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE);
        PermissionCheckEvent.EVENT.register(EARLY_PHASE, (ssp, perm) -> {

            if(!(ssp instanceof ServerPlayer sp) || ADMINS.contains(sp)) return TriState.DEFAULT;
            FabricPlayer fp = FabricPlayer.wrap(sp);

            Session session = SESSION_MANAGER.getModule().getSession(fp);
            PermissionCache cache;

            if(session instanceof LobbySession ls) {
                if(!ls.isRunning()) return TriState.DEFAULT;
                cache = ContentRegistryImpl.INSTANCE.getPermissions("lobby");
            } else if(session instanceof EditingSession) {
                cache = ContentRegistryImpl.INSTANCE.getPermissions("editing");
            } else if(session instanceof ViewingSession) {
                cache = ContentRegistryImpl.INSTANCE.getPermissions("viewing");
            } else {
                return TriState.DEFAULT;
            }

            Boolean b = cache.hasPermission(perm);
            if(b == null) return cache.shouldPassthrough() ? TriState.DEFAULT : TriState.FALSE;

            return TriState.of(b);
        });

    }

    private static void onJoin(PlayerJoinEvent event) {
        if (Permissions.check(event.getPlayer(), "hideandseek.command_passthrough")) {
            ADMINS.add(event.getPlayer());
        }
    }

    private static void onDamage(EntityDamageEvent event) {

        if(!(event.getEntity() instanceof ServerPlayer sp)) return;

        FabricPlayer fp = FabricPlayer.wrap(sp);
        FabricPlayer afp = null;

        Entity attacker = event.getSource().getEntity();
        if(attacker instanceof ServerPlayer ap) {
            afp = FabricPlayer.wrap(ap);
        }

        if(SESSION_MANAGER.damage(fp, afp, event.getSource().getMsgId(), event.getAmount())) {
            event.setCancelled(true);
        }
    }

    private static void onHunger(PlayerFoodLevelChangeEvent event) {

        FabricPlayer fp = FabricPlayer.wrap(event.getPlayer());

        Session session = SESSION_MANAGER.getModule().getSession(fp);
        if(session != null && session.getNamespace().equals(HideAndSeekAPI.DEFAULT_NAMESPACE)) {
            event.setCancelled(true);
        }
    }

}
