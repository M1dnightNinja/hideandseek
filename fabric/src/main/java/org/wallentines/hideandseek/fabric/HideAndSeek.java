package org.wallentines.hideandseek.fabric;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.api.game.map.ResourcePackData;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.HideAndSeekImpl;
import org.wallentines.hideandseek.common.game.map.ResourcePackDataImpl;
import org.wallentines.hideandseek.fabric.command.MainCommand;
import org.wallentines.hideandseek.fabric.core.FabricSessionManager;
import org.wallentines.hideandseek.fabric.game.FabricTimer;
import org.wallentines.hideandseek.fabric.listener.GameListener;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStartEvent;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HideAndSeek implements ModInitializer {

    HideAndSeekImpl api;
    ResourcePackData pack = new ResourcePackDataImpl(Constants.EMPTY_RESOURCE_PACK, Constants.EMPTY_RESOURCE_PACK_HASH, false, null);

    @Override
    public void onInitialize() {


        // Determine the data folder
        Path dataFolder = Paths.get("config/HideAndSeek");

        // Create the API
        api = new HideAndSeekImpl(dataFolder, new FabricSessionManager(), FabricTimer::new, this::getServerPack, this::applyClass);

        Event.register(CommandLoadEvent.class, this, this::onCommandLoad);
        Event.register(ServerStartEvent.class, this, ev -> {

            if(ev.getServer().getServerResourcePack().isPresent()) {

                MinecraftServer.ServerResourcePackInfo info = ev.getServer().getServerResourcePack().get();
                pack = new ResourcePackDataImpl(info.url(), info.hash(), info.isRequired(), ConversionUtil.toMComponent(info.prompt()));
            }
        });

        ConfigSection langDefaults = JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/hideandseek/lang/en_us.json"));
        api.loadContents(langDefaults);

        GameListener.register();
    }

    private ResourcePackData getServerPack() {
        return pack;
    }


    private void onCommandLoad(CommandLoadEvent event) {

        MainCommand.register(event.getDispatcher());

    }

    private void applyClass(MPlayer player, PlayerClass clazz) {

        ServerPlayer spl = FabricPlayer.getInternal(player);
        for(MItemStack is : clazz.getItems()) {
            player.giveItem(is.copy());
        }

        for(EquipmentSlot slot : EquipmentSlot.values()) {
            MItemStack is = clazz.getEquipmentItem(slot.getName());
            if(is != null) spl.setItemSlot(slot, ((FabricItem) is.copy()).getInternal());
        }

        for(Identifier effect : clazz.getEffects()) {

            int lvl = clazz.getEffectLevel(effect);
            MobEffect eff = Registry.MOB_EFFECT.get(ConversionUtil.toResourceLocation(effect));
            if(eff == null) continue;

            spl.addEffect(new MobEffectInstance(eff, Integer.MAX_VALUE, lvl, true, false, true));
        }

    }

}