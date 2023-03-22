package org.wallentines.hideandseek.fabric.game;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.FireworkRocketItem;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.hideandseek.common.game.AbstractGameSession;
import org.wallentines.hideandseek.fabric.util.FireworkUtil;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.server.FabricServer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.List;

public class FabricGameSession extends AbstractGameSession {

    private MapInstance instance;

    public FabricGameSession(LobbySession lobbySession, GameType type) {
        super(lobbySession, type);

        Event.register(PlayerInteractEvent.class, this, ev -> {
            if(type.isFrozen(this, FabricPlayer.wrap(ev.getPlayer()))) {
                ev.setCancelled(true);
            }
        });
    }

    @Override
    protected void teleport(MPlayer player, Vec3d loc) {

        Location location = new Location(ConversionUtil.toIdentifier(instance.getLevel().dimension().location()), loc, player.getLocation().getYaw(), player.getLocation().getPitch());
        player.teleport(location);
    }

    @Override
    protected Location getSpawnLocation(Role role) {
        return instance.getSpawnLocation(role);
    }

    @Override
    protected void runTagActions(MPlayer player) {

        super.runTagActions(player);

        instance.removeFromTeam(player);
        instance.addToTeam(player, roles.get(player));
    }

    @Override
    protected void doMapLoad(Map map, Runnable finished) {

        if(instance != null) {
            getPlayers().forEach(player -> player.teleport(lobby.getLocation()));
            instance.unloadWorld();
        }

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();

        lobbySession.broadcastMessage("command.map.loading", prov, lobby, lobbySession, map);


        instance = MapInstance.forLobby(lobbySession, map);
        instance.loadWorld(finished, this::shutdown, progress -> {

            MComponent empty = prov.getMessage("lobby.progress_bar", prov.getDefaultLocale(), lobby, lobbySession, map);
            MComponent full = prov.getMessage("lobby.progress_bar_fill", prov.getDefaultLocale(), lobby, lobbySession, map);

            int emptyIndex = Math.max(0, (int) (progress * empty.getLength() - 1));
            empty = empty.subComponent(emptyIndex, empty.getLength() - 1);

            int fullIndex = Math.max(0, (int) (progress * full.getLength() - 1));
            full = full.subComponent(0, fullIndex);

            for(MPlayer mpl : lobbySession.getPlayers()) {
                MComponent message = new MTextComponent("");
                message.addChild(prov.getMessage("lobby.map_loading", mpl, lobby, lobbySession, map));
                message.addChild(full);
                message.addChild(empty);

                mpl.sendActionBar(message);
            }

        });
    }

    @Override
    protected void executeCommand(String command) {

        MServer srv = MidnightCoreAPI.getRunningServer();
        if(srv == null) {
            HideAndSeekAPI.getLogger().warn("Attempt to execute command /" + command + " before server started!");
            return;
        }

        MinecraftServer server = ((FabricServer) srv).getInternal();
        CommandSourceStack stack = server.createCommandSourceStack().withLevel(instance.getLevel());
        server.getCommands().performPrefixedCommand(stack, command);

    }

    @Override
    protected void spawnFireworks(Role role, Vec3d location, boolean explode) {

        if(role == null) return;
        RoleData data = currentMap.getGameData().getRoleData(role);

        List<Color> colors1 = List.of(data.getColor());
        List<Color> colors2 = List.of(Color.WHITE);

        FireworkRocketItem.Shape shape1 = FireworkRocketItem.Shape.LARGE_BALL;
        FireworkRocketItem.Shape shape2 = FireworkRocketItem.Shape.SMALL_BALL;

        Location loc = instance.makeLocation(location);

        if(explode) {

            FireworkUtil.spawnFireworkExplosion(colors1, colors2, shape1, loc);
            FireworkUtil.spawnFireworkExplosion(colors2, colors1, shape2, loc);

        } else {

            FireworkUtil.spawnFireworkEntity(colors1, colors1, AbstractGameSession.RANDOM.nextBoolean() ? shape1 : shape2, loc);
        }
    }

    @Override
    protected void onStart() {
        for(MPlayer mpl : getPlayers()) {
            instance.addToTeam(mpl, getRole(mpl));
        }
    }

    @Override
    protected Identifier getWorldId() {

        return ConversionUtil.toIdentifier(instance.getLevel().dimension().location());
    }

    @Override
    protected boolean isSafeLocation(MPlayer mpl) {

        ServerPlayer spl = FabricPlayer.getInternal(mpl);
        return spl.isOnGround() || spl.isInWater();
    }

    @Override
    protected void onRemovePlayer(MPlayer player) {

        super.onRemovePlayer(player);
        instance.removeFromTeam(player);
    }

    @Override
    protected void onShutdown() {

        super.onShutdown();
        instance.unloadWorld();

        Event.unregisterAll(this);
    }


}
