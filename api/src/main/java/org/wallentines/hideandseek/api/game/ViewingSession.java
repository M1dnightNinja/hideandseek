package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.hideandseek.api.game.map.Map;

public interface ViewingSession extends Session, DamageListener  {

    Map getMap();

}
