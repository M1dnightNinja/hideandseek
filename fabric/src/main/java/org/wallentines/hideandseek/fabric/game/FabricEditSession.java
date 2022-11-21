package org.wallentines.hideandseek.fabric.game;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.EditingSession;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.game.BuiltinRoles;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.session.AbstractSession;

import java.util.ArrayDeque;
import java.util.Queue;

public class FabricEditSession extends AbstractSession implements EditingSession {

    private final Map map;
    private final MapInstance instance;

    private final Queue<MPlayer> waiting = new ArrayDeque<>();
    private boolean loaded = false;

    public FabricEditSession(Map map) {

        super(Constants.DEFAULT_NAMESPACE);

        this.map = map;
        this.instance = MapInstance.forEditor(this, map);

        instance.loadWorld(() -> {
            loaded = true;
            waiting.forEach(this::teleportPlayer);
            waiting.clear();
        }, this::shutdown);
    }

    @Override
    public void tick() { }

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

        player.setGameMode(MPlayer.GameMode.CREATIVE);
    }
}
