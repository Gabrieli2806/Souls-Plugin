package com.g2806.Souls;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Item;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.attribute.Attribute;
import org.bukkit.GameMode;

import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.io.File;
import java.io.IOException;

public class Souls extends JavaPlugin implements Listener {
    
    private Map<UUID, Integer> playerSouls = new HashMap<>();
    private Set<UUID> ghostPlayers = new HashSet<>();
    private FileConfiguration playerData;
    private File playerDataFile;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        loadPlayerData();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerEffects();
            }
        }.runTaskTimer(this, 0L, 20L);
        
        getLogger().info("Souls plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        savePlayerData();
        getLogger().info("Souls plugin disabled!");
    }
    
    private void loadPlayerData() {
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            getDataFolder().mkdirs();
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        
        for (String key : playerData.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int souls = playerData.getInt(key + ".souls", 3);
            boolean isGhost = playerData.getBoolean(key + ".ghost", false);
            
            playerSouls.put(uuid, souls);
            if (isGhost) {
                ghostPlayers.add(uuid);
            }
        }
    }
    
    private void savePlayerData() {
        for (Map.Entry<UUID, Integer> entry : playerSouls.entrySet()) {
            String key = entry.getKey().toString();
            playerData.set(key + ".souls", entry.getValue());
            playerData.set(key + ".ghost", ghostPlayers.contains(entry.getKey()));
        }
        
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (!playerSouls.containsKey(uuid)) {
            playerSouls.put(uuid, 3);
        }
        
        if (ghostPlayers.contains(uuid)) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.GRAY + "You are a ghost! Ask someone to revive you with /revive");
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        Player killer = player.getKiller();
        
        int currentSouls = playerSouls.getOrDefault(uuid, 3);
        currentSouls--;
        playerSouls.put(uuid, currentSouls);
        
        Location deathLoc = player.getLocation();
        dropSoul(deathLoc);
        
        if (killer instanceof Player) {
            UUID killerUuid = killer.getUniqueId();
            int killerSouls = playerSouls.getOrDefault(killerUuid, 3);
            killerSouls++;
            playerSouls.put(killerUuid, killerSouls);
            killer.sendMessage(ChatColor.GREEN + "You gained a soul! Total: " + killerSouls);
        }
        
        if (currentSouls <= 0) {
            ghostPlayers.add(uuid);
            player.sendMessage(ChatColor.RED + "You have lost all your souls! You are now a ghost.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You lost a soul! Souls remaining: " + currentSouls);
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (ghostPlayers.contains(uuid)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }.runTaskLater(this, 1L);
        }
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        
        if (itemStack.getType() == Material.SOUL_SAND && itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.DARK_PURPLE + "Soul")) {
                UUID uuid = player.getUniqueId();
                int souls = playerSouls.getOrDefault(uuid, 3);
                souls++;
                playerSouls.put(uuid, souls);
                
                player.sendMessage(ChatColor.GREEN + "You picked up a soul! Total: " + souls);
                event.setCancelled(true);
                item.remove();
                
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }
    
    private void dropSoul(Location location) {
        ItemStack soul = new ItemStack(Material.SOUL_SAND);
        ItemMeta meta = soul.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Soul");
        List<String> lore = Arrays.asList(ChatColor.GRAY + "A lost soul that can be picked up");
        meta.setLore(lore);
        soul.setItemMeta(meta);
        
        Item droppedSoul = location.getWorld().dropItem(location, soul);
        droppedSoul.setGlowing(true);
        
        location.getWorld().spawnParticle(Particle.SOUL, location, 10, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_GHAST_DEATH, 0.5f, 1.5f);
    }
    
    private void updatePlayerEffects() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (ghostPlayers.contains(player.getUniqueId())) {
                continue;
            }
            
            UUID uuid = player.getUniqueId();
            int souls = playerSouls.getOrDefault(uuid, 3);
            
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            player.removePotionEffect(PotionEffectType.WATER_BREATHING);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            
            if (souls >= 5) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 0, true, false));
            }
            if (souls >= 8) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 300, 0, true, false));
            }
            if (souls >= 12) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 300, 0, true, false));
            }
            if (souls >= 15) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false));
            }
            if (souls >= 20) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 0, true, false));
            }
            if (souls >= 25) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 0, true, false));
            }
            if (souls >= 30) {
                player.setAllowFlight(true);
            } else {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("souls")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            int souls = playerSouls.getOrDefault(uuid, 3);
            
            player.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GOLD + souls + ChatColor.AQUA + " souls.");
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("givesoul")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /givesoul <player>");
                return true;
            }
            
            Player giver = (Player) sender;
            Player receiver = getServer().getPlayer(args[0]);
            
            if (receiver == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            UUID giverUuid = giver.getUniqueId();
            UUID receiverUuid = receiver.getUniqueId();
            
            int giverSouls = playerSouls.getOrDefault(giverUuid, 3);
            if (giverSouls <= 1) {
                sender.sendMessage(ChatColor.RED + "You need at least 2 souls to give one away!");
                return true;
            }
            
            giverSouls--;
            int receiverSouls = playerSouls.getOrDefault(receiverUuid, 3);
            receiverSouls++;
            
            playerSouls.put(giverUuid, giverSouls);
            playerSouls.put(receiverUuid, receiverSouls);
            
            giver.sendMessage(ChatColor.GREEN + "You gave a soul to " + receiver.getName() + ". You now have " + giverSouls + " souls.");
            receiver.sendMessage(ChatColor.GREEN + giver.getName() + " gave you a soul! You now have " + receiverSouls + " souls.");
            
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("revive")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /revive <player>");
                return true;
            }
            
            Player reviver = (Player) sender;
            Player ghost = getServer().getPlayer(args[0]);
            
            if (ghost == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            UUID reviverUuid = reviver.getUniqueId();
            UUID ghostUuid = ghost.getUniqueId();
            
            if (!ghostPlayers.contains(ghostUuid)) {
                sender.sendMessage(ChatColor.RED + "That player is not a ghost!");
                return true;
            }
            
            int reviverSouls = playerSouls.getOrDefault(reviverUuid, 3);
            if (reviverSouls <= 1) {
                sender.sendMessage(ChatColor.RED + "You need at least 2 souls to revive someone!");
                return true;
            }
            
            reviverSouls--;
            playerSouls.put(reviverUuid, reviverSouls);
            playerSouls.put(ghostUuid, 1);
            ghostPlayers.remove(ghostUuid);
            
            ghost.setGameMode(GameMode.SURVIVAL);
            
            reviver.sendMessage(ChatColor.GREEN + "You revived " + ghost.getName() + "! You now have " + reviverSouls + " souls.");
            ghost.sendMessage(ChatColor.GREEN + reviver.getName() + " revived you! You have 1 soul.");
            
            ghost.playSound(ghost.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            ghost.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, ghost.getLocation(), 20, 1, 1, 1, 0.1);
            
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("soulsinfo")) {
            sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.DARK_PURPLE + "SOULS PLUGIN INFO" + ChatColor.GOLD + " ==========");
            sender.sendMessage(ChatColor.AQUA + "Welcome to the Souls Plugin!");
            sender.sendMessage(ChatColor.GRAY + "Survive, collect souls, and gain incredible powers!");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "How to get souls:");
            sender.sendMessage(ChatColor.WHITE + "• Start with 3 souls");
            sender.sendMessage(ChatColor.WHITE + "• Kill other players to steal their soul");
            sender.sendMessage(ChatColor.WHITE + "• Pick up dropped soul items");
            sender.sendMessage(ChatColor.WHITE + "• Receive souls from other players");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "Soul Powers (unlocked at soul thresholds):");
            sender.sendMessage(ChatColor.GREEN + "5 souls: " + ChatColor.WHITE + "Speed I");
            sender.sendMessage(ChatColor.GREEN + "8 souls: " + ChatColor.WHITE + "Jump Boost I");
            sender.sendMessage(ChatColor.GREEN + "12 souls: " + ChatColor.WHITE + "Water Breathing");
            sender.sendMessage(ChatColor.GREEN + "15 souls: " + ChatColor.WHITE + "Night Vision");
            sender.sendMessage(ChatColor.GREEN + "20 souls: " + ChatColor.WHITE + "Strength I");
            sender.sendMessage(ChatColor.GREEN + "25 souls: " + ChatColor.WHITE + "Regeneration I");
            sender.sendMessage(ChatColor.GREEN + "30 souls: " + ChatColor.WHITE + "Flight Ability");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.RED + "Death System:");
            sender.sendMessage(ChatColor.WHITE + "• Lose 1 soul on death");
            sender.sendMessage(ChatColor.WHITE + "• Drop a soul item when you die");
            sender.sendMessage(ChatColor.WHITE + "• Become a ghost (spectator) at 0 souls");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Commands:");
            sender.sendMessage(ChatColor.WHITE + "/souls - Check your soul count");
            sender.sendMessage(ChatColor.WHITE + "/givesoul <player> - Give a soul to someone");
            sender.sendMessage(ChatColor.WHITE + "/revive <player> - Revive a ghost player");
            sender.sendMessage(ChatColor.GOLD + "==========================================");
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("addsoul")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be OP to use this command!");
                return true;
            }
            
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /addsoul <player> <amount>");
                return true;
            }
            
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be a positive number!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount! Please enter a number.");
                return true;
            }
            
            UUID targetUuid = target.getUniqueId();
            int currentSouls = playerSouls.getOrDefault(targetUuid, 3);
            currentSouls += amount;
            playerSouls.put(targetUuid, currentSouls);
            
            if (ghostPlayers.contains(targetUuid)) {
                ghostPlayers.remove(targetUuid);
                target.setGameMode(GameMode.SURVIVAL);
                target.sendMessage(ChatColor.GREEN + "An admin added " + amount + " souls to you! You are no longer a ghost. Total souls: " + currentSouls);
                sender.sendMessage(ChatColor.GREEN + "Added " + amount + " souls to " + target.getName() + ". They are no longer a ghost and have " + currentSouls + " souls.");
            } else {
                target.sendMessage(ChatColor.GREEN + "An admin added " + amount + " souls to you! Total souls: " + currentSouls);
                sender.sendMessage(ChatColor.GREEN + "Added " + amount + " souls to " + target.getName() + ". They now have " + currentSouls + " souls.");
            }
            
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("removesoul")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be OP to use this command!");
                return true;
            }
            
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /removesoul <player> <amount>");
                return true;
            }
            
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be a positive number!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount! Please enter a number.");
                return true;
            }
            
            UUID targetUuid = target.getUniqueId();
            int currentSouls = playerSouls.getOrDefault(targetUuid, 3);
            
            if (currentSouls <= 0) {
                sender.sendMessage(ChatColor.RED + "That player already has 0 souls!");
                return true;
            }
            
            currentSouls -= amount;
            if (currentSouls < 0) currentSouls = 0;
            playerSouls.put(targetUuid, currentSouls);
            
            if (currentSouls <= 0) {
                ghostPlayers.add(targetUuid);
                target.setGameMode(GameMode.SPECTATOR);
                target.sendMessage(ChatColor.RED + "An admin removed " + amount + " souls from you! You are now a ghost.");
                sender.sendMessage(ChatColor.GREEN + "Removed " + amount + " souls from " + target.getName() + ". They are now a ghost.");
            } else {
                target.sendMessage(ChatColor.YELLOW + "An admin removed " + amount + " souls from you! Souls remaining: " + currentSouls);
                sender.sendMessage(ChatColor.GREEN + "Removed " + amount + " souls from " + target.getName() + ". They now have " + currentSouls + " souls.");
            }
            
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("forcerevive")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be OP to use this command!");
                return true;
            }
            
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /forcerevive <player>");
                return true;
            }
            
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            UUID targetUuid = target.getUniqueId();
            
            if (!ghostPlayers.contains(targetUuid)) {
                sender.sendMessage(ChatColor.RED + "That player is not a ghost!");
                return true;
            }
            
            ghostPlayers.remove(targetUuid);
            playerSouls.put(targetUuid, 1);
            target.setGameMode(GameMode.SURVIVAL);
            
            target.sendMessage(ChatColor.GREEN + "An admin force revived you! You have 1 soul.");
            sender.sendMessage(ChatColor.GREEN + "Force revived " + target.getName() + "! They now have 1 soul.");
            
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target.getLocation(), 20, 1, 1, 1, 0.1);
            
            return true;
        }
        
        return false;
    }
}