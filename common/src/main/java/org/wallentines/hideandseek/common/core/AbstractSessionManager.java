package org.wallentines.hideandseek.common.core;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.hideandseek.api.core.SessionManager;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.game.LobbySessionImpl;
import org.wallentines.midnightcore.api.module.session.SessionModule;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.*;

public abstract class AbstractSessionManager implements SessionManager {

    private SessionModule module;
    private final HashMap<Lobby, UUID> idsByLobby = new HashMap<>();
    private final HashMap<Map, UUID> idsByViewingMap = new HashMap<>();
    private final HashMap<Map, UUID> idsByEditingMap = new HashMap<>();
    @Override
    public LobbySession getLobbySession(Lobby lobby) {

        UUID id = idsByLobby.computeIfAbsent(lobby, lby -> {
            for(Session sess : getModule().getSessions()) {
                if(sess instanceof LobbySession && ((LobbySession) sess).getLobby() == lby) {
                    sess.addShutdownCallback(() -> idsByLobby.remove(lby));
                    return sess.getId();
                }
            }
            return null;
        });

        return (LobbySession) getModule().getSession(id);
    }

    @Override
    public LobbySession createLobbySession(Lobby lobby) {

        if(lobby == null) throw new IllegalArgumentException("Cannot start a session in a null lobby!");
        if(idsByLobby.containsKey(lobby)) throw new IllegalStateException("There is already a session running for lobby " + lobby.getId());

        LobbySession sess = new LobbySessionImpl(lobby);
        getModule().registerSession(sess);

        return sess;
    }

    @Override
    public EditingSession getEditingSession(Map map) {

        UUID id = idsByEditingMap.computeIfAbsent(map, m -> {
            for(Session sess : getModule().getSessions()) {
                if(sess instanceof EditingSession && ((EditingSession) sess).getMap() == m) {
                    sess.addShutdownCallback(() -> idsByEditingMap.remove(m));
                    return sess.getId();
                }
            }
            return null;
        });
        return (EditingSession) getModule().getSession(id);
    }

    @Override
    public ViewingSession getViewingSession(Map map) {
        UUID id = idsByViewingMap.computeIfAbsent(map, m -> {
            for(Session sess : getModule().getSessions()) {
                if(sess instanceof ViewingSession && ((ViewingSession) sess).getMap() == m) {
                    sess.addShutdownCallback(() -> idsByViewingMap.remove(m));
                    return sess.getId();
                }
            }
            return null;
        });
        return (ViewingSession) getModule().getSession(id);
    }

    @Override
    public boolean damage(MPlayer player, MPlayer attacker, String sourceId, float amount) {
        Session s = getModule().getSession(player);
        if(s instanceof DamageListener) {
            ((DamageListener) s).onDamaged(player, attacker, sourceId, amount);
            return true;
        }
        return false;
    }

    public SessionModule getModule() {
        if(module == null) {
            module = MidnightCoreAPI.getInstance().getModuleManager().getModule(SessionModule.class);
        }
        return module;
    }
}
