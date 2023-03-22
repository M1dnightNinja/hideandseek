package org.wallentines.hideandseek.fabric.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.LobbySession;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class MapArgumentType implements ArgumentType<MapArgumentType.ParsedMap> {

    private static final SimpleCommandExceptionType INVALID_MAP = new SimpleCommandExceptionType(Component.literal("That is not a valid map!"));

    private final BiFunction<Map, CommandSourceStack, Boolean> filter;

    private MapArgumentType(BiFunction<Map, CommandSourceStack, Boolean> filter) {

        this.filter = filter;
    }

    public static MapArgumentType any() {
        return new MapArgumentType((m,ctx) -> true);
    }

    public static MapArgumentType inLobby() {
        return new MapArgumentType(MapArgumentType::isInLobby);
    }

    public static MapArgumentType editable() {

        return filtered((m, ctx) -> {
            ServerPlayer spl = ctx.getPlayer();
            return spl == null || m.canEdit(FabricPlayer.wrap(spl));
        });
    }

    public static MapArgumentType viewable() {

        return filtered((m, ctx) -> {
            ServerPlayer spl = ctx.getPlayer();
            return spl == null || m.canView(FabricPlayer.wrap(spl));
        });
    }

    public static MapArgumentType authored() {

        return filtered((m, c) -> {
            ServerPlayer spl = c.getPlayer();
            return spl == null || m.getMeta().isAuthor(FabricPlayer.wrap(spl));
        });
    }

    public static MapArgumentType filtered(BiFunction<Map, CommandSourceStack, Boolean> filter) {

        return new MapArgumentType(filter);
    }


    public static Map getMap(CommandContext<CommandSourceStack> ctx, String arg) {

        ParsedMap m = ctx.getArgument(arg, ParsedMap.class);
        return m.filter(ctx);

    }

    public static CompletableFuture<Suggestions> suggestAny(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, (m, c) -> true);
    }

    public static CompletableFuture<Suggestions> suggestLobby(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, MapArgumentType::isInLobby);
    }

    public static CompletableFuture<Suggestions> suggestEditable(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, (m, c) -> {
            ServerPlayer spl = c.getPlayer();
            return spl == null || m.canEdit(FabricPlayer.wrap(spl));
        });
    }

    public static CompletableFuture<Suggestions> suggestViewable(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, (m, c) -> {
            ServerPlayer spl = c.getPlayer();
            return spl == null || m.canView(FabricPlayer.wrap(spl));
        });
    }

    public static CompletableFuture<Suggestions> suggestAuthored(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, (m, c) -> {
            ServerPlayer spl = c.getPlayer();
            return spl == null || m.getMeta().isAuthor(FabricPlayer.wrap(spl));
        });
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, BiFunction<Map, CommandSourceStack, Boolean> filter) {

        List<String> maps = new ArrayList<>();
        for(Map m : ContentRegistryImpl.INSTANCE.getMaps()) {
            if(filter.apply(m, ctx.getSource())) maps.add(m.getId());
        }

        return SharedSuggestionProvider.suggest(maps, builder);
    }

    @Override
    public MapArgumentType.ParsedMap parse(StringReader reader) throws CommandSyntaxException {

        String arg = ArgumentTypes.getSingleArg(reader);

        Map m = ContentRegistryImpl.REGISTERED_MAP.readString(arg);
        if(m == null) throw INVALID_MAP.createWithContext(reader);

        return new ParsedMap(m, filter);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        if(!(context.getSource() instanceof CommandSourceStack)) return null;

        List<String> maps = new ArrayList<>();
        for(Map m : ContentRegistryImpl.INSTANCE.getMaps()) {
            if(filter.apply(m, (CommandSourceStack) context.getSource())) maps.add(m.getId());
        }

        return SharedSuggestionProvider.suggest(maps, builder);
    }

    private static boolean isInLobby(Map m, CommandSourceStack c) {

        ServerPlayer spl = c.getPlayer();
        if(spl == null) return false;
        Session sess = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(FabricPlayer.wrap(spl));
        return sess instanceof LobbySession lSess && lSess.getLobby().getMaps().contains(m);
    }

    public static class ParsedMap {

        private final Map parsed;
        private final BiFunction<Map, CommandSourceStack, Boolean> filter;

        public ParsedMap(Map parsed, BiFunction<Map, CommandSourceStack, Boolean> filter) {
            this.parsed = parsed;
            this.filter = filter;
        }

        public Map filter(CommandContext<CommandSourceStack> ctx) {

            return filter.apply(parsed, ctx.getSource()) ? parsed : null;
        }
    }
}
