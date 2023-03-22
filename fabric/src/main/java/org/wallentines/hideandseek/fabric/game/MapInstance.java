package org.wallentines.hideandseek.fabric.game;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.scores.Team;
import org.wallentines.dll.DynamicLevelCallback;
import org.wallentines.dll.DynamicLevelContext;
import org.wallentines.dll.DynamicLevelStorage;
import org.wallentines.dll.WorldConfig;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.hideandseek.common.integration.IntegrationManager;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.module.session.AbstractSession;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.server.FabricServer;
import org.wallentines.midnightcore.fabric.text.TeamBuilder;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.function.Consumer;

public class MapInstance {

    private static final HashMap<Map, DynamicLevelStorage> LEVEL_STORAGE_CACHE = new HashMap<>();

    private final Map map;
    private final Session session;

    private final String identifier;

    private final boolean shouldSave;
    private final boolean createTeams;


    private DynamicLevelContext context;
    private ServerLevel level;

    private final boolean init;

    private final HashMap<Role, Location> spawnLocations = new HashMap<>();
    private final HashMap<Role, TeamBuilder> teams = new HashMap<>();

    public MapInstance(Map map, Session session, String identifier, boolean createTeams, boolean save, boolean init) {
        this.identifier = identifier;
        this.session = session;
        this.map = map;
        this.createTeams = createTeams;
        this.shouldSave = save;
        this.init = init;
    }

    public void loadWorld(Runnable onComplete, Runnable onFail) {
        loadWorld(onComplete, onFail, fl -> {});
    }

    public void loadWorld(Runnable onComplete, Runnable onFail, Consumer<Float> onProgress) {

        ResourceKey<DimensionType> key = ResourceKey.create(Registries.DIMENSION_TYPE, ConversionUtil.toResourceLocation(map.getWorldData().getDimensionType()));
        ResourceLocation loc = new ResourceLocation(HideAndSeekAPI.DEFAULT_NAMESPACE, map.getId() + "_" + identifier);

        DynamicLevelStorage storage = LEVEL_STORAGE_CACHE.computeIfAbsent(map, k -> DynamicLevelStorage.create(map.getDataFolder().toPath(), map.getDataFolder().toPath().resolve("backups")));

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) {
            HideAndSeekAPI.getLogger().warn("Attempt to load a HideAndSeek world before server startup!");
            onFail.run();
            return;
        }

        RegistryAccess.Frozen access = ((FabricServer) server).getInternal().registryAccess();

        WorldConfig.Builder builder = WorldConfig.builder()
                .autoSave(shouldSave)
                .ignoreSessionLock(true)
                .levelName("world")
                .pregenRadius(5)
                .addDimension(
                    loc,
                    WorldConfig.dimension(access, LevelStem.OVERWORLD)
                        .type(key)
                        .emptyGenerator(Biomes.FOREST)
                );

        if(init) {
            builder.gameRule(GameRules.RULE_COMMANDBLOCKOUTPUT, false);
            builder.gameRule(GameRules.RULE_DOMOBSPAWNING, false);
            builder.gameRule(GameRules.RULE_MOBGRIEFING, false);
            builder.gameRule(GameRules.RULE_DOINSOMNIA, false);
            builder.gameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false);
            builder.gameRule(GameRules.RULE_SPECTATORSGENERATECHUNKS, false);
            builder.gameRule(GameRules.RULE_DOFIRETICK, false);
        }

        DynamicLevelContext ctx = storage.createWorldContext(builder.build());

        Identifier worldId = ConversionUtil.toIdentifier(loc);

        if(IntegrationManager.isMidnightEssentialsPresent()) {
            org.wallentines.hideandseek.common.integration.MidnightEssentialsIntegration.loadBlockCommandsForWorld(map, worldId);
        }

        ctx.loadAllDimensions(DynamicLevelCallback.of(
                lvl -> {
                    if (lvl == null) {
                        onFail.run();
                        return;
                    }
                    lvl.noSave = !shouldSave;

                    context = ctx;
                    level = lvl;

                    lvl.setWeatherParameters(0, Integer.MAX_VALUE, map.getWorldData().hasRain(), map.getWorldData().hasThunder());
                    if (map.getWorldData().hasRandomTime()) {
                        lvl.setDayTime(AbstractSession.RANDOM.nextLong(24000L));
                    }

                    if(session != null && IntegrationManager.playersWithMapPNG(session.getPlayers()) > 0) {
                        org.wallentines.hideandseek.common.integration.MapPNGIntegration.loadMapsForWorld(map, worldId);
                    }

                    onComplete.run();
                },
                onFail,
                onProgress
        ));
    }

    public void unloadWorld() {

        DynamicLevelStorage storage = LEVEL_STORAGE_CACHE.get(map);
        if(storage == null || context == null) {
            HideAndSeekAPI.getLogger().warn("Attempt to unload a map world which is not loaded!");
            return;
        }

        context.unload(shouldSave);
        clearTeams();

        if(IntegrationManager.isMidnightEssentialsPresent()) {

            Identifier id = ConversionUtil.toIdentifier(context.getConfig().getDimensionKey(LevelStem.OVERWORLD).location());

            if(shouldSave) {
                org.wallentines.hideandseek.common.integration.MidnightEssentialsIntegration.saveBlockCommandsForWorld(map, id);
            }
            org.wallentines.hideandseek.common.integration.MidnightEssentialsIntegration.unloadBlockCommandsForWorld(id);
        }
    }

    public Location getSpawnLocation(Role role) {

        return spawnLocations.computeIfAbsent(role, r -> {

            RoleData data = map.getGameData().getRoleData(role);
            Vec3d tp;

            if(data == null || (tp = data.getSpawnLocation()) == null) {
                throw new IllegalArgumentException("There is no spawn location in map " + map.getId() + " for role " + role.getId() + "!");
            }
            float rotation = data.getSpawnRotation();

            return new Location(ConversionUtil.toIdentifier(level.dimension().location()), tp, rotation, 0.0f);
        });
    }

    public Location makeLocation(Vec3d coords) {

        return new Location(ConversionUtil.toIdentifier(level.dimension().location()), coords, 0.0f, 0.0f);
    }


    public void addToTeam(MPlayer player, Role role) {
        if(!createTeams) return;

        TeamBuilder team = teams.computeIfAbsent(role, k -> {

            RoleData data = map.getGameData().getRoleData(role);
            if(data == null) return null;

            TeamBuilder out = new TeamBuilder(generateRandomId()).color(ChatFormatting.getById(data.getColor().toRGBI()));
            if(data.shouldHideName()) {
                out.nameTagVisibility(Team.Visibility.NEVER);
            }

            Packet<?> pck = out.addPacket();
            for(MPlayer mpl : session.getPlayers()) {
                ServerPlayer spl = FabricPlayer.getInternal(mpl);
                spl.connection.send(pck);
            }

            return out;
        });

        if(team == null) return;

        team.addMember(player.getUsername());
        Packet<?> pck = team.addMembersPacket(ImmutableList.of(player.getUsername()));
        for(MPlayer mpl : session.getPlayers()) {
            ServerPlayer spl = FabricPlayer.getInternal(mpl);
            spl.connection.send(pck);
        }

    }

    public void removeFromTeam(MPlayer player) {

        if(!createTeams) return;

        String un = player.getUsername();
        for(TeamBuilder team : teams.values()) {
            if(team.getMembers().contains(un)) {
                team.removeMember(un);
                for (MPlayer mpl : session.getPlayers()) {
                    ServerPlayer spl = FabricPlayer.getInternal(mpl);
                    spl.connection.send(team.removeMembersPacket(ImmutableList.of(un)));
                }
            }
        }
    }

    public void clearTeams() {
        if(!createTeams) return;

        for(TeamBuilder t : teams.values()) {
            Packet<?> pck = t.removePacket();
            for(MPlayer mpl : session.getPlayers()) {
                FabricPlayer.getInternal(mpl).connection.send(pck);
            }
        }
    }

    public static MapInstance forLobby(LobbySession lobby, Map map) {

        String id = lobby.getLobby().getId();
        return new MapInstance(map, lobby, id, true, false, false);
    }

    public static MapInstance forEditor(Session session, Map map, boolean initialize) {

        return new MapInstance(map, session, "editing", false, true, initialize);

    }

    public static MapInstance forViewer(Session session, Map map) {

        return new MapInstance(map, session, "viewing", false, false, false);
    }

    public ServerLevel getLevel() {
        return level;
    }

    private static final String VALUES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static String generateRandomId() {

        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < 16 ; i++) {

            int index = AbstractSession.RANDOM.nextInt(VALUES.length());
            builder.append(VALUES.charAt(index));
        }

        return builder.toString();
    }
}
