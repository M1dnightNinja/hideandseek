package org.wallentines.hideandseek.fabric.game;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.*;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.integration.IntegrationManager;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.session.AbstractSession;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.level.*;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
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

    private ServerScoreboard scoreboard;

    private final HashMap<Role, Location> spawnLocations = new HashMap<>();
    private final HashMap<Role, PlayerTeam> teams = new HashMap<>();

    public MapInstance(Map map, Session session, String identifier, boolean createTeams, boolean save) {
        this.identifier = identifier;
        this.session = session;
        this.map = map;
        this.createTeams = createTeams;
        this.shouldSave = save;

        if(createTeams) {
            this.scoreboard = MidnightCore.getInstance().getServer().getScoreboard();
        }
    }

    public void loadWorld(Runnable onComplete, Runnable onFail) {
        loadWorld(onComplete, onFail, fl -> {});
    }

    public void loadWorld(Runnable onComplete, Runnable onFail, Consumer<Float> onProgress) {

        ResourceKey<LevelStem> key = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ConversionUtil.toResourceLocation(map.getWorldData().getDimensionType()));
        ResourceLocation loc = new ResourceLocation(Constants.DEFAULT_NAMESPACE, map.getId() + "_" + identifier);

        DynamicLevelStorage storage = LEVEL_STORAGE_CACHE.computeIfAbsent(map, k -> DynamicLevelStorage.create(map.getDataFolder().toPath(), map.getDataFolder().toPath().resolve("backups")));
        WorldConfig config = new WorldConfig(loc).noSave(!shouldSave).rootDimensionType(key).ignoreSessionLock(true).generator(EmptyGenerator.create(Biomes.FOREST));

        DynamicLevelContext ctx = storage.createWorldContext("world", config);

        Identifier worldId = ConversionUtil.toIdentifier(loc);

        if(IntegrationManager.isMidnightEssentialsPresent()) {
            org.wallentines.hideandseek.common.integration.MidnightEssentialsIntegration.loadBlockCommandsForWorld(map, worldId);
        }

        ctx.loadDimension(config.getRootDimensionId(), new DynamicLevelCallback() {
            @Override
            public void onLoaded(ServerLevel lvl) {

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

                if(IntegrationManager.playersWithMapPNG(session.getPlayers()) > 0) {
                    org.wallentines.hideandseek.common.integration.MapPNGIntegration.loadMapsForWorld(map, worldId);
                }

                onComplete.run();
            }

            @Override
            public void onProgress(float v) {
                onProgress.accept(v);
            }
        });
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

            Identifier id = ConversionUtil.toIdentifier(context.getConfig().getRootDimensionId().location());

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

        PlayerTeam team = teams.computeIfAbsent(role, k -> {

            RoleData data = map.getGameData().getRoleData(role);
            if(data == null) return null;

            PlayerTeam out = new PlayerTeam(scoreboard, generateRandomId());
            ChatFormatting fmt = ChatFormatting.getById(data.getColor().toRGBI());
            if(fmt != null) out.setColor(fmt);

            if(data.shouldHideName()) {
                out.setNameTagVisibility(Team.Visibility.NEVER);
            }

            Packet<?> pck = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(out, true);
            for(MPlayer mpl : session.getPlayers()) {
                ServerPlayer spl = FabricPlayer.getInternal(mpl);
                spl.connection.send(pck);
            }

            return out;
        });

        if(team == null) return;

        Packet<?> pck = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, player.getUsername(), ClientboundSetPlayerTeamPacket.Action.ADD);
        for(MPlayer mpl : session.getPlayers()) {
            ServerPlayer spl = FabricPlayer.getInternal(mpl);
            spl.connection.send(pck);
        }

    }

    public void removeFromTeam(MPlayer player) {

        if(!createTeams) return;

        String un = player.getUsername();
        for(PlayerTeam team : teams.values()) {
            if(!team.getPlayers().contains(un)) {
                for (MPlayer mpl : session.getPlayers()) {
                    ServerPlayer spl = FabricPlayer.getInternal(mpl);
                    spl.connection.send(ClientboundSetPlayerTeamPacket.createPlayerPacket(team, un, ClientboundSetPlayerTeamPacket.Action.REMOVE));
                }
            }
        }
    }

    public void clearTeams() {
        if(!createTeams) return;

        for(PlayerTeam t : teams.values()) {
            scoreboard.removePlayerTeam(t);
        }
    }

    public static MapInstance forLobby(LobbySession lobby, Map map) {

        String id = lobby.getLobby().getId();
        return new MapInstance(map, lobby, id, true, false);
    }

    public static MapInstance forEditor(Session session, Map map) {

        return new MapInstance(map, session, "editing", false, true);
    }

    public static MapInstance forViewer(Session session, Map map) {

        return new MapInstance(map, session, "viewing", false, false);
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
