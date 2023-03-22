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
import org.wallentines.hideandseek.api.game.Lobby;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class LobbyArgumentType implements ArgumentType<LobbyArgumentType.ParsedLobby> {

    private static final SimpleCommandExceptionType INVALID_MAP = new SimpleCommandExceptionType(Component.literal("That is not a valid lobby!"));

    private final BiFunction<Lobby, CommandSourceStack, Boolean> filter;

    private LobbyArgumentType(BiFunction<Lobby, CommandSourceStack, Boolean> filter) {

        this.filter = filter;
    }

    public static LobbyArgumentType any() {
        return new LobbyArgumentType((m, ctx) -> true);
    }

    public static LobbyArgumentType joinable() {

        return filtered((m, ctx) -> {
            ServerPlayer spl = ctx.getPlayer();
            return spl == null || m.canAccess(FabricPlayer.wrap(spl));
        });
    }

    public static LobbyArgumentType filtered(BiFunction<Lobby, CommandSourceStack, Boolean> filter) {

        return new LobbyArgumentType(filter);
    }


    public static Lobby getLobby(CommandContext<CommandSourceStack> ctx, String arg) {

        ParsedLobby m = ctx.getArgument(arg, ParsedLobby.class);
        return m.filter(ctx);

    }

    public static CompletableFuture<Suggestions> suggestAny(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, (m, c) -> true);
    }

    public static CompletableFuture<Suggestions> suggestJoinable(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        return suggest(ctx, builder, (m, c) -> {
            ServerPlayer spl = c.getPlayer();
            return spl == null || m.canAccess(FabricPlayer.wrap(spl));
        });
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, BiFunction<Lobby, CommandSourceStack, Boolean> filter) {

        List<String> maps = new ArrayList<>();
        for(Lobby l : ContentRegistryImpl.INSTANCE.getLobbies()) {
            if(filter.apply(l, ctx.getSource())) maps.add(l.getId());
        }

        return SharedSuggestionProvider.suggest(maps, builder);
    }

    @Override
    public ParsedLobby parse(StringReader reader) throws CommandSyntaxException {

        String arg = ArgumentTypes.getSingleArg(reader);

        Lobby l = ContentRegistryImpl.REGISTERED_LOBBY.readString(arg);
        if(l == null) throw INVALID_MAP.createWithContext(reader);

        return new ParsedLobby(l, filter);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        if(!(context.getSource() instanceof CommandSourceStack)) return null;

        List<String> maps = new ArrayList<>();
        for(Lobby m : ContentRegistryImpl.INSTANCE.getLobbies()) {
            if(filter.apply(m, (CommandSourceStack) context.getSource())) maps.add(m.getId());
        }

        return SharedSuggestionProvider.suggest(maps, builder);
    }

    public static class ParsedLobby {

        private final Lobby parsed;
        private final BiFunction<Lobby, CommandSourceStack, Boolean> filter;

        public ParsedLobby(Lobby parsed, BiFunction<Lobby, CommandSourceStack, Boolean> filter) {
            this.parsed = parsed;
            this.filter = filter;
        }

        public Lobby filter(CommandContext<CommandSourceStack> ctx) {

            return filter.apply(parsed, ctx.getSource()) ? parsed : null;
        }
    }
}
