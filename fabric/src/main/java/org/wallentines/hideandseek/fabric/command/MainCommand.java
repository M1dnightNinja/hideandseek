package org.wallentines.hideandseek.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.core.SessionManager;
import org.wallentines.hideandseek.api.game.Lobby;
import org.wallentines.hideandseek.api.game.LobbySession;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.BuiltinRoles;
import org.wallentines.hideandseek.common.game.UIDisplayImpl;
import org.wallentines.hideandseek.common.game.map.*;
import org.wallentines.hideandseek.common.integration.IntegrationManager;
import org.wallentines.hideandseek.common.integration.Requirements;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.CustomPlaceholder;
import org.wallentines.midnightcore.api.text.CustomPlaceholderInline;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.CommandUtil;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommand {

    private static List<ResourceLocation> getRoleIds() {
        List<ResourceLocation> roleIds = new ArrayList<>();
        for(Role r : ContentRegistryImpl.INSTANCE.getRoles()) {
            roleIds.add(ConversionUtil.toResourceLocation(r.getId()));
        }

        return roleIds;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        ArgumentTypes.register();

        dispatcher.register(Commands.literal("has")
            .requires(Permissions.require("hideandseek.command", 2))
            .then(Commands.literal("join")
                .requires(Permissions.require("hideandseek.command.join", 2))
                .executes(context -> joinCommand(context, null))
                .then(Commands.argument("lobby", LobbyArgumentType.joinable())
                    .suggests(LobbyArgumentType::suggestJoinable)
                    .executes(context -> joinCommand(context, LobbyArgumentType.getLobby(context, "lobby")))
                )
            )
            .then(Commands.literal("leave")
                .requires(Permissions.require("hideandseek.command.leave", 2))
                .executes(MainCommand::leaveCommand)
            )
            .then(Commands.literal("start")
                .requires(Permissions.require("hideandseek.command.start", 2))
                .executes(context -> startCommand(context, null))
                .then(Commands.argument("map", MapArgumentType.inLobby())
                    .suggests(MapArgumentType::suggestLobby)
                    .executes(context -> startCommand(context, MapArgumentType.getMap(context, "map")))
                )
            )
            .then(Commands.literal("map")
                .requires(Permissions.require("hideandseek.command.map", 2))
                .then(Commands.literal("edit")
                    .executes(context -> viewOrEdit(context, null, true))
                    .then(Commands.argument("map", MapArgumentType.editable())
                        .suggests(MapArgumentType::suggestEditable)
                        .executes(context -> viewOrEdit(context, MapArgumentType.getMap(context, "map"), true))
                    )
                )
                .then(Commands.literal("view")
                    .executes(context -> viewOrEdit(context, null, false))
                    .then(Commands.argument("map", MapArgumentType.viewable())
                        .suggests(MapArgumentType::suggestViewable)
                        .executes(context -> viewOrEdit(context, MapArgumentType.getMap(context, "map"), false))
                    )
                )
                .then(Commands.literal("create")
                    .requires(Permissions.require("hideandseek.command.map.create", 3))
                    .then(Commands.argument("id", StringArgumentType.word())
                        .executes(context -> createCommand(context, context.getArgument("id", String.class)))
                    )
                )
                .then(Commands.literal("settings")
                    .then(Commands.argument("map", MapArgumentType.authored())
                        .suggests(MapArgumentType::suggestAuthored)
                        .then(Commands.literal("description")
                            .then(Commands.argument("description", StringArgumentType.greedyString())
                                .executes(context -> settingsDescriptionCommand(context, MapArgumentType.getMap(context, "map"), context.getArgument("description", String.class)))
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("reload")
                .requires(Permissions.require("hideandseek.command.reload", 4))
                .executes(MainCommand::reloadCommand)
            )
        );

        LiteralCommandNode<CommandSourceStack> executeNode = (LiteralCommandNode<CommandSourceStack>) dispatcher.getRoot().getChild("execute");
        dispatcher.register(executeNode.createBuilder().then(addConditionals(executeNode, Commands.literal("if"), true)).then(addConditionals(executeNode, Commands.literal("unless"), false)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(CommandNode<CommandSourceStack> baseNode, LiteralArgumentBuilder<CommandSourceStack> bld, boolean state) {

        return bld.then(Commands.literal("role")
            .then(Commands.argument("role", ResourceLocationArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(getRoleIds(), builder))
                .fork(baseNode, context -> state == Requirements.ROLE_REQUIREMENT.check(FabricPlayer.wrap(context.getSource().getPlayerOrException()), null, context.getArgument("role", ResourceLocation.class).toString()) ? Collections.singletonList(context.getSource()) : new ArrayList<>())
            )
        )
        .then(Commands.literal("class")
            .then(Commands.argument("class", StringArgumentType.word())
                .fork(baseNode, context -> state == Requirements.CLASS_REQUIREMENT.check(FabricPlayer.wrap(context.getSource().getPlayerOrException()), null, context.getArgument("class", String.class)) ? Collections.singletonList(context.getSource()) : new ArrayList<>())
            )
        )
        .then(Commands.literal("lobby")
            .then(Commands.argument("lobby", LobbyArgumentType.any())
                .suggests(LobbyArgumentType::suggestAny)
                .fork(baseNode, context ->
                        state == Requirements.LOBBY_REQUIREMENT.check(
                                FabricPlayer.wrap(context.getSource().getPlayerOrException()),
                                null,
                                LobbyArgumentType.getLobby(context, "lobby").getId()) ? Collections.singletonList(context.getSource()) : new ArrayList<>())
            )
        )
        .then(Commands.literal("map")
            .then(Commands.argument("map", MapArgumentType.any())
                .suggests(MapArgumentType::suggestAny)
                .fork(baseNode, context ->
                    state == Requirements.MAP_REQUIREMENT.check(
                        FabricPlayer.wrap(context.getSource().getPlayerOrException()),
                        null,
                        MapArgumentType.getMap(context, "map").getId()) ? Collections.singletonList(context.getSource()) : new ArrayList<>())
                )
        )
        .then(Commands.literal("seeker")
            .then(Commands.argument("state", BoolArgumentType.bool())
                .fork(baseNode, context -> state == (context.getArgument("state", Boolean.class) == Requirements.SEEKER_REQUIREMENT.check(FabricPlayer.wrap(context.getSource().getPlayerOrException()), null, null)) ? Collections.singletonList(context.getSource()) : new ArrayList<>())
            )
        );

    }

    private static int joinCommand(CommandContext<CommandSourceStack> context, Lobby lobby) throws CommandSyntaxException {

        MPlayer mp = FabricPlayer.wrap(context.getSource().getPlayerOrException());

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(mp);
        if (session != null) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.in_session", false);
            return 0;
        }

        if(lobby == null) {

            try {
                IntegrationManager.openLobbyMenu(lby -> {
                    try {
                        joinCommand(context, lby);
                    } catch (CommandSyntaxException e) {
                        // Ignore
                    }
                }, lby -> lby.canAccess(mp), true, mp);

            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
            return 2;
        }

        SessionManager manager = HideAndSeekAPI.getInstance().getSessionManager();

        LobbySession sess = manager.getLobbySession(lobby);
        if (sess == null) sess = manager.createLobbySession(lobby);
        if (sess == null) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.session_failed", false);
            return 0;
        }

        sess.addPlayer(mp);

        return 1;
    }

    private static int leaveCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer sp = context.getSource().getPlayerOrException();
        FabricPlayer fp = FabricPlayer.wrap(sp);

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(fp);
        if (session == null) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.no_session", false);
            return 0;
        }

        session.removePlayer(fp);

        return 1;
    }

    private static int startCommand(CommandContext<CommandSourceStack> context, Map m) throws CommandSyntaxException {

        ServerPlayer sp = context.getSource().getPlayerOrException();
        FabricPlayer fp = FabricPlayer.wrap(sp);

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(fp);
        if(!(session instanceof LobbySession lobbySession)) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.no_session", false);
            return 0;
        }

        lobbySession.startGame(null, m);
        return 1;

    }

    private static int viewOrEdit(CommandContext<CommandSourceStack> context, Map map, boolean edit) throws CommandSyntaxException {

        ServerPlayer sp = context.getSource().getPlayerOrException();
        FabricPlayer fp = FabricPlayer.wrap(sp);

        if (map == null) {

            IntegrationManager.openMapMenu(m -> {
                try {
                    viewOrEdit(context, m, edit);
                } catch (CommandSyntaxException e) {
                    // Ignore
                }
            }, m -> edit ? m.canEdit(fp) : m.canView(fp), fp);

            return 2;
        }

        Session session = HideAndSeekAPI.getInstance().getSessionManager().getModule().getSession(fp);
        if (session != null) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.in_session");
            return 0;
        }

        Session sess;
        if (edit) {
            sess = HideAndSeekAPI.getInstance().getSessionManager().getEditingSession(map);
            if (sess == null) sess = HideAndSeekAPI.getInstance().getSessionManager().createEditingSession(map);
        } else {
            sess = HideAndSeekAPI.getInstance().getSessionManager().getViewingSession(map);
            if (sess == null) sess = HideAndSeekAPI.getInstance().getSessionManager().createViewingSession(map);
        }

        if(sess == null) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.session_failed", false);
            return 0;
        }

        CommandUtil.sendCommandSuccess(context, HideAndSeekAPI.getInstance().getLangProvider(), false, "command.map.loading");
        sess.addPlayer(fp);

        return 1;
    }

    private static int createCommand(CommandContext<CommandSourceStack> context, String id) throws CommandSyntaxException{

        ServerPlayer sp = context.getSource().getPlayerOrException();
        FabricPlayer fp = FabricPlayer.wrap(sp);

        if (!Constants.VALID_ID.matcher(id).matches()) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.invalid_id");
            return 0;
        }

        if (HideAndSeekAPI.getInstance().getContentRegistry().getMap(id) != null) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.map_exists");
            return 0;
        }

        File f = new File(HideAndSeekAPI.getInstance().getMapFolder(), id);
        if (!f.isDirectory() && !f.mkdirs()) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.map_creation_failed");
            HideAndSeekAPI.getLogger().error("Unable to create map " + id + "! Folder " + f.getAbsolutePath() + " could not be created!");
            return 0;
        }

        MapMetaImpl meta = new MapMetaImpl(fp.getUUID());
        UIDisplayImpl display = UIDisplayImpl.createDefault(id);
        WorldDataImpl world = new WorldDataImpl(new Identifier("minecraft", "overworld"), false, false, false);
        GameDataImpl game = new GameDataImpl(ResourcePackDataImpl.DEFAULT, new GameDataImpl.TimerData(30), new GameDataImpl.TimerData(240));

        Vec3d spawn = new Vec3d(0.5d, 64.0d, 0.5d);

        game.setRoleData(BuiltinRoles.HIDER, new RoleDataImpl().inheritFrom(ContentRegistryImpl.INSTANCE.getDefaultData(BuiltinRoles.HIDER)).setSpawnLocation(spawn));
        game.setRoleData(BuiltinRoles.SEEKER, new RoleDataImpl().inheritFrom(ContentRegistryImpl.INSTANCE.getDefaultData(BuiltinRoles.SEEKER)).setSpawnLocation(spawn));
        game.setRoleData(BuiltinRoles.MAIN_HIDER, new RoleDataImpl().inheritFrom(ContentRegistryImpl.INSTANCE.getDefaultData(BuiltinRoles.MAIN_HIDER)).setSpawnLocation(spawn));
        game.setRoleData(BuiltinRoles.MAIN_SEEKER, new RoleDataImpl().inheritFrom(ContentRegistryImpl.INSTANCE.getDefaultData(BuiltinRoles.MAIN_SEEKER)).setSpawnLocation(spawn));

        MapImpl m = new MapImpl(id, f, meta, display, world, game, null, null);

        FileConfig mapConf = FileConfig.findOrCreate("map", f);
        mapConf.setRoot(MapImpl.createSerializer(f).serialize(m));
        mapConf.save();

        File worldFolder = m.getWorldFolder();
        if (!worldFolder.isDirectory() && !worldFolder.mkdirs()) {
            CommandUtil.sendCommandFailure(context, HideAndSeekAPI.getInstance().getLangProvider(), "command.error.map_creation_failed");
            HideAndSeekAPI.getLogger().error("Unable to create map " + id + "! World folder " + worldFolder.getAbsolutePath() + " could not be created!");
            return 0;
        }

        HideAndSeekAPI.getInstance().getContentRegistry().registerMap(m);

        CommandUtil.sendCommandSuccess(context, HideAndSeekAPI.getInstance().getLangProvider(), false, "command.map.created", CustomPlaceholderInline.create("id", id));

        return 1;
    }

    private static int settingsDescriptionCommand(CommandContext<CommandSourceStack> context, Map map, String description) {

        if(map.getDisplay() instanceof UIDisplayImpl udi) {

            udi.setDescription(description);
            CommandUtil.sendCommandSuccess(context, HideAndSeekAPI.getInstance().getLangProvider(), false, "command.map.settings.description", CustomPlaceholder.create("description", udi.getDescriptionFlattened()));

            return 1;
        }

        CommandUtil.sendCommandSuccess(context, HideAndSeekAPI.getInstance().getLangProvider(), false, "command.error.not_editable", CustomPlaceholderInline.create("id", map.getId()));

        return 0;
    }

    private static int reloadCommand(CommandContext<CommandSourceStack> context) {

        long time = System.currentTimeMillis();
        HideAndSeekAPI.getInstance().reload();
        time = System.currentTimeMillis() - time;

        CommandUtil.sendCommandSuccess(context, HideAndSeekAPI.getInstance().getLangProvider(), false, "command.reload", CustomPlaceholderInline.create("time", time+""));

        return 1;
    }

}
