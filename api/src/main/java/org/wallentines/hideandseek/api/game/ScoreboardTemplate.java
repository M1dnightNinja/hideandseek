package org.wallentines.hideandseek.api.game;

import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.Collection;

public interface ScoreboardTemplate {

    String getTitle();

    Collection<String> getLines();

    MComponent getTitle(Object... ctx);

    void fill(CustomScoreboard board, Object... ctx);

}
