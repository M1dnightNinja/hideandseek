package org.wallentines.hideandseek.fabric.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.ViewingSession;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.game.BuiltinRoles;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.session.AbstractSession;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class FabricViewSession extends AbstractSession implements ViewingSession {

    private final Map map;
    private final MapInstance instance;

    private final Queue<MPlayer> waiting = new ArrayDeque<>();

    private final HashMap<MPlayer, Location> toTeleport = new HashMap<>();
    private boolean loaded = false;

    public FabricViewSession(Map map) {

        super(Constants.DEFAULT_NAMESPACE);

        this.map = map;
        this.instance = MapInstance.forViewer(this, map);

        instance.loadWorld(() -> {
            loaded = true;
            waiting.forEach(this::teleportPlayer);
            waiting.clear();
        }, this::shutdown);
    }

    @Override
    public void tick() {

        for(HashMap.Entry<MPlayer, Location> ent : toTeleport.entrySet()) {

            ent.getKey().teleport(ent.getValue());
        }
        toTeleport.clear();
    }

    @Override
    public void onDamaged(MPlayer player, MPlayer attacker, String sourceId, float amount) {

        if(attacker == null && map.getGameData().shouldResetOn(Constants.ID_SERIALIZER.deserialize(sourceId))) {
            toTeleport.put(player, instance.getSpawnLocation(BuiltinRoles.HIDER));
        }

    }

    @Override
    public Map getMap() {
        return map;
    }

    @Override
    protected boolean shouldAddPlayer(MPlayer player) {
        return true;
    }

    @Override
    protected void onAddPlayer(MPlayer player) {
        map.getGameData().getResourcePack().apply(player, status -> { });
        if(!loaded) {
            waiting.add(player);
            return;
        }
        teleportPlayer(player);
    }

    @Override
    protected void onRemovePlayer(MPlayer player) {
        if(map.getGameData().getResourcePack().isValid()) {
            HideAndSeekAPI.getInstance().getServerResourcePack().apply(player, status -> { });
        }
        instance.removeFromTeam(player);
    }

    @Override
    protected void onShutdown() {

        instance.unloadWorld();
    }

    private void teleportPlayer(MPlayer player) {

        player.teleport(instance.getSpawnLocation(BuiltinRoles.HIDER));
        instance.addToTeam(player, BuiltinRoles.HIDER);
    }
}
