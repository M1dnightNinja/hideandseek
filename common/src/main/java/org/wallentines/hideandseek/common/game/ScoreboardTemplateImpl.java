package org.wallentines.hideandseek.common.game;

import org.wallentines.hideandseek.api.game.ScoreboardTemplate;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScoreboardTemplateImpl implements ScoreboardTemplate {

    private final String title;
    private final List<String> lines;

    public ScoreboardTemplateImpl(String title, Collection<String> lines) {
        this.title = title;
        this.lines = new ArrayList<>(lines);
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLines() {
        return lines;
    }

    public MComponent getTitle(Object... ctx) {

        return PlaceholderManager.INSTANCE.parseText(title, ctx);
    }

    public void fill(CustomScoreboard board, Object... ctx) {

        int lineCount = lines.size();

        for(int i = lineCount ; i > 0 ; i--) {

            board.setLine(i, PlaceholderManager.INSTANCE.parseText(lines.get(lineCount - i),  ctx));
        }
    }

    public static final ConfigSerializer<ScoreboardTemplateImpl> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.STRING.entry("title", ScoreboardTemplateImpl::getTitle),
            PrimitiveSerializers.STRING.listOf().entry("lines", ScoreboardTemplateImpl::getLines),
            ScoreboardTemplateImpl::new
    );

    public static final ScoreboardTemplateImpl LOBBY_DEFAULT = new ScoreboardTemplateImpl(
            "%hideandseek_lobby_color%&l%lang<text.game_name>%",
            Arrays.asList(
                    "",
                    "%lang<text.lobby>%: %hideandseek_lobby_name%",
                    "%lang<text.game_type>%: %hideandseek_lobby_game_type_name%",
                    "",
                    "%lang<text.players>%: %hideandseek_lobby_color%%hideandseek_session_player_count% &7/ %hideandseek_lobby_color%%hideandseek_lobby_max_players%"
            ));

    public static final ScoreboardTemplateImpl MAP_DEFAULT = new ScoreboardTemplateImpl(
            "%hideandseek_map_color%&l%lang<text.game_name>%",
            Arrays.asList(
                    "",
                    "%lang<text.map>%: %hideandseek_map_name%",
                    "%lang<text.phase>%: %hideandseek_game_phase_name%",
                    "",
                    "%lang<text.role>%: %hideandseek_role_name%",
                    "%lang<text.class>%: %hideandseek_class_name%",
                    "",
                    "%lang<text.hiders>%: %hideandseek_map_role_color<hider>%%hideandseek_game_hider_count%",
                    "%lang<text.seekers>%: %hideandseek_map_role_color<seeker>%%hideandseek_game_seeker_count%"
            ));

}
