package com.popupmc.possibleloseinv;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PossibleLoseInv extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("PossibleLoseInv is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("PossibleLoseInv is disabled");
    }

    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Stop if not a normal player in survival mode
        if(player.isOp() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;

        // Get permissions
        boolean keepInv = player.hasPermission("essentials.keepinv");
        boolean keepExp = player.hasPermission("essentials.keepxp");

        // Stop if player does not have either keepinv and keepexp
        if(!keepInv && !keepExp)
            return;

        // Roll for keeping either separately
        if(keepExp)
            rollKeepExp(player);
        if(keepInv)
            rollKeepInv(player);
    }

    public void rollKeepInv(Player player) {
        // 75% chance of keep inv
        if(random.nextInt(100 + 1) <= 50)
            return;

        // Drop everything
        for (ItemStack itemStack : player.getInventory()) {
            if(itemStack == null)
                continue;

            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }

        // Clear
        player.getInventory().clear();
    }

    public void rollKeepExp(Player player) {

        // 75% chance of keep xp
        if(random.nextInt(100 + 1) <= 50)
            return;

        // Get XP & Level
        float xp = player.getExp();
        int level = player.getLevel();

        // Follow vanilla guidelines for normal xp drop
        // https://minecraft.gamepedia.com/Player

        // Calculate level start xp
        // https://minecraft.gamepedia.com/Experience
        float levelStartXP;

        if(level <= 16)
            levelStartXP = getXpToLvl16(level);
        else if(level <= 31)
            levelStartXP = getXpToLvl31(level);
        else
            levelStartXP = getXpAboveLvl31(level);

        // Calculate difference and ensure stays 0 or above
        float xpProgressInLevel = clamp(xp - levelStartXP, 0f, Float.MAX_VALUE);

        // Calculate xp drop and ensure it stays between 0 & 100
        float dropXP = clamp(xpProgressInLevel * 7, 0f, 100f);

        // Also ensure it doesn't go above players own xp
        dropXP = clamp(dropXP, 0f, xp);

        // Apply XP difference
        player.setExp(xp - dropXP);

        // Drop the XP
        ExperienceOrb xpOrb = player.getLocation().getWorld().spawn(
                player.getLocation(), ExperienceOrb.class
        );

        // Make sure any fractions are rounded up instead of down
        xpOrb.setExperience((int)Math.ceil(dropXP));
    }

    public float getXpToLvl16(int level) {
        return (float)(Math.sqrt(level) + 6 * level);
    }

    public float getXpToLvl31(int level) {
        return (float)(2.5 * Math.sqrt(level) - 40.5 * level + 360);
    }

    public float getXpAboveLvl31(int level) {
        return (float)(4.5 * Math.sqrt(level) - 162.5 * level + 2220);
    }

    public float clamp(float val, float min, float max) {
        if(val < min)
            val = min;
        else if(val > max)
            val = max;

        return val;
    }

    public static final Random random = new Random();
    public static PossibleLoseInv plugin;
}
