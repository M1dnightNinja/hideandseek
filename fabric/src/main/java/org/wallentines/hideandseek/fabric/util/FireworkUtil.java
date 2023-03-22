package org.wallentines.hideandseek.fabric.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightlib.math.Color;

import java.util.List;

public class FireworkUtil {

    public static void spawnFireworkExplosion(List<Color> color, List<Color> fadeColor, FireworkRocketItem.Shape type, Location location) {

        FireworkRocketEntity ent = spawnFireworkEntity(color,fadeColor,type,location);
        if(ent == null) return;

        MinecraftServer server = ent.getServer();
        if(server == null) return;

        ent.level.broadcastEntityEvent(ent, (byte)17);
        ent.discard();
    }

    public static FireworkRocketEntity spawnFireworkEntity(List<Color> color, List<Color> fadeColor, FireworkRocketItem.Shape type, Location location) {
        ServerLevel world = LocationUtil.getLevel(location);
        if(world == null) return null;

        CompoundTag fireworkTag = new CompoundTag();
        CompoundTag fireworks = new CompoundTag();

        ListTag explosions = new ListTag();

        CompoundTag firework1 = new CompoundTag();
        firework1.put("Colors", generateColorArray(color));
        firework1.put("FadeColors", generateColorArray(fadeColor));
        firework1.put("Type", IntTag.valueOf(type.getId()));

        explosions.add(firework1);

        fireworks.put("Explosions", explosions);
        fireworkTag.put("Fireworks", fireworks);
        fireworkTag.put("LifeTime", IntTag.valueOf(30));

        ItemStack fireworkItem = new ItemStack(Items.FIREWORK_ROCKET, 1);
        fireworkItem.setTag(fireworkTag);


        FireworkRocketEntity fwk = new FireworkRocketEntity(world, location.getX(), location.getY() + 1.5, location.getZ(), fireworkItem);
        world.addFreshEntity(fwk);

        return fwk;
    }

    private static IntArrayTag generateColorArray(List<Color> colorList) {
        int[] colors = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); i++) {
            colors[i] = colorList.get(i).toDecimal();
        }

        return new IntArrayTag(colors);
    }


}
