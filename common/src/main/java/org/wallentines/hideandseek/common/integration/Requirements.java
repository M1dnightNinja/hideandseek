package org.wallentines.hideandseek.common.integration;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.RequirementType;

public class Requirements {

    public static final RequirementType<MPlayer> ROLE_REQUIREMENT = (pl, req, value) -> {

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(pl);
        if(session instanceof LobbySession sess) {
            if(!sess.isRunning()) return false;
            Role r = sess.getCurrentGame().getRole(pl);
            return r != null && value.equals(r.getId().toString());
        }
        return session instanceof ViewingSession || session instanceof EditingSession;
    };

    public static final RequirementType<MPlayer> CLASS_REQUIREMENT = (pl, req, value) -> {

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(pl);
        if(session instanceof LobbySession sess) {
            if(!sess.isRunning()) return false;
            PlayerClass cl = sess.getCurrentGame().getClass(pl);
            return cl != null && value.equals(cl.getId());
        }
        return session instanceof ViewingSession || session instanceof EditingSession;
    };

    public static final RequirementType<MPlayer> LOBBY_REQUIREMENT = (pl, req, value) -> {

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(pl);
        if(session instanceof LobbySession sess) {
            return sess.getLobby().getId().equals(value);
        }
        return false;
    };

    public static final RequirementType<MPlayer> MAP_REQUIREMENT = (pl, req, value) -> {

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(pl);
        if(session instanceof LobbySession sess) {
            if(!sess.isRunning()) return false;
            return sess.getCurrentGame().getMap().getId().equals(value);
        }
        return (session instanceof ViewingSession vs && vs.getMap().getId().equals(value)) ||
                (session instanceof EditingSession es && es.getMap().getId().equals(value));
    };

    public static final RequirementType<MPlayer> HIDER_REQUIREMENT = (pl, req, value) -> {

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(pl);
        if(session instanceof LobbySession sess) {
            if(!sess.isRunning()) return false;
            return sess.getLobby().getGameType().isHider(sess.getCurrentGame().getRole(pl));
        }
        return session instanceof ViewingSession || session instanceof EditingSession;
    };

    public static final RequirementType<MPlayer> SEEKER_REQUIREMENT = (pl, req, value) -> {

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(pl);
        if(session instanceof LobbySession sess) {
            if(!sess.isRunning()) return false;
            return sess.getLobby().getGameType().isSeeker(sess.getCurrentGame().getRole(pl));
        }
        return session instanceof ViewingSession || session instanceof EditingSession;
    };

    public static void register(Registry<RequirementType<MPlayer>> reg, String namespace) {

        reg.register(new Identifier(namespace, "role"), ROLE_REQUIREMENT);
        reg.register(new Identifier(namespace, "class"), CLASS_REQUIREMENT);
        reg.register(new Identifier(namespace, "lobby"), LOBBY_REQUIREMENT);
        reg.register(new Identifier(namespace, "map"), MAP_REQUIREMENT);
        reg.register(new Identifier(namespace, "hider"), HIDER_REQUIREMENT);
        reg.register(new Identifier(namespace, "seeker"), SEEKER_REQUIREMENT);

    }

}
