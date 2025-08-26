package com.g2806.Magic;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.attribute.Attribute;

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

/**
 * Magic Plugin - Warrior/Tank Class System
 *
 * Skill Categories:
 * L - Learnable by both Basic and Awoken classes (most basic skills)
 * B - Basic class learns through leveling (10 skills, basic to somewhat advanced)
 * A - Awoken class learns through leveling (10 skills, somewhat advanced to advanced)
 * AL - Awoken class only, found on map (very powerful skills)
 */
public class Magic extends JavaPlugin implements Listener {

    private Map<UUID, PlayerData> playerData = new HashMap<>();
    private Map<UUID, Map<String, Long>> skillCooldowns = new HashMap<>();
    private Map<UUID, Set<String>> activeEffects = new HashMap<>();
    private Map<UUID, Map<String, Object>> interventionTargets = new HashMap<>();
    private File playerDataFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        // Create player data file
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        loadPlayerData();
        
        // Auto-save player data every 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                savePlayerData();
            }
        }.runTaskTimer(this, 6000L, 6000L); // 5 minutes
        
        getLogger().info("Magic Plugin - Warrior/Tank Class System Enabled!");
    }

    @Override
    public void onDisable() {
        savePlayerData();
        getLogger().info("Magic Plugin Disabled!");
    }

    // Player data management
    private static class PlayerData {
        boolean isAwokenClass;
        int intellect;
        int strength;
        int level;
        int experience;
        Set<String> knownSkills;

        public PlayerData() {
            this.isAwokenClass = false;
            this.intellect = 10;
            this.strength = 10;
            this.level = 1;
            this.experience = 0;
            this.knownSkills = new HashSet<>();
            // Start with basic learnable skills
            this.knownSkills.add("taunt");
            this.knownSkills.add("flameshield");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!playerData.containsKey(playerId)) {
            playerData.put(playerId, new PlayerData());
            skillCooldowns.put(playerId, new HashMap<>());
            activeEffects.put(playerId, new HashSet<>());
            interventionTargets.put(playerId, new HashMap<>());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data when they quit
        UUID playerId = event.getPlayer().getUniqueId();
        if (playerData.containsKey(playerId)) {
            savePlayerData();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();
        Set<String> effects = activeEffects.get(playerId);
        if (effects == null) return;

        // Handle invulnerability
        if (effects.contains("invulnerable")) {
            event.setCancelled(true);
            return;
        }

        // Handle undying will (prevent death)
        if (effects.contains("undying_will") && player.getHealth() - event.getFinalDamage() <= 0) {
            event.setDamage(player.getHealth() - 0.5); // Leave at 0.5 health
            return;
        }

        // Handle flameshield (fire resistance)
        if (effects.contains("flameshield") && 
            (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
             event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
             event.getCause() == EntityDamageEvent.DamageCause.LAVA)) {
            event.setCancelled(true);
            return;
        }

        // Handle harm shield (damage reduction)
        if (effects.contains("harmshield_50")) {
            event.setDamage(event.getDamage() * 0.5);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle attacker effects
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Set<String> attackerEffects = activeEffects.get(attacker.getUniqueId());
            if (attackerEffects != null && attackerEffects.contains("cannot_attack")) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "You cannot attack while invulnerable!");
                return;
            }
        }

        // Handle victim effects
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            UUID victimId = victim.getUniqueId();
            Set<String> effects = activeEffects.get(victimId);
            if (effects == null) return;

            // Handle parrying
            if (effects.contains("parry_physical") && 
                event.getCause() != EntityDamageEvent.DamageCause.MAGIC) {
                event.setCancelled(true);
                effects.remove("parry_physical");
                victim.sendMessage(ChatColor.GREEN + "You parried the physical attack!");
                if (event.getDamager() instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity) event.getDamager();
                    attacker.damage(event.getDamage() * 0.5, victim);
                }
                return;
            }

            if (effects.contains("parry_magical") && 
                event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                event.setCancelled(true);
                effects.remove("parry_magical");
                victim.sendMessage(ChatColor.GREEN + "You parried the magical attack!");
                if (event.getDamager() instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity) event.getDamager();
                    attacker.damage(event.getDamage() * 0.5, victim);
                }
                return;
            }

            // Handle retribution (reflect damage)
            if (effects.contains("retribution") && event.getDamager() instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) event.getDamager();
                attacker.damage(event.getDamage() * 1.5, victim);
                victim.sendMessage(ChatColor.GOLD + "You reflect damage back to your attacker!");
            }

            // Handle barrier retaliation
            if (effects.contains("barrier_retaliation") && 
                event.getDamager() instanceof LivingEntity &&
                victim.getLocation().distance(event.getDamager().getLocation()) <= 3) {
                LivingEntity attacker = (LivingEntity) event.getDamager();
                attacker.damage(event.getDamage() * 0.75, victim);
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                victim.sendMessage(ChatColor.BLUE + "Your barrier retaliates!");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use magic skills!");
            return true;
        }

        Player player = (Player) sender;
        String skillName = command.getName().toLowerCase();

        // Execute the appropriate skill
        switch (skillName) {
            // L - Basic skills learnable by both classes
            case "taunt": return executeTaunt(player);
            case "flameshield": return executeFlameshield(player);
            case "invuln": return executeInvuln(player);
            case "harmshield": return executeHarmShield(player);
            case "bladegrasps": return executeBladegrasps(player);
            case "parrymagic": return executeParryMagic(player);
            case "headbutt": return executeHeadbutt(player, args);
            case "toss": return executeToss(player, args);

            // B - Basic class leveling skills (10 skills)
            case "forcepull": return executeForcepull(player, args);
            case "pulse": return executePulse(player);
            case "stealessence": return executeStealEssence(player, args);
            case "tremor": return executeTremor(player);
            case "undyingwill": return executeUndyingWill(player);
            case "intervene": return executeIntervene(player, args);
            case "earthwall": return executeEarthwall(player);
            case "provoke": return executeProvoke(player, args);
            case "boastfulbellow": return executeBoastfulBellow(player, args);
            case "barrier": return executeBarrier(player);

            // A - Awoken class leveling skills (10 skills)
            case "reckoning": return executeReckoning(player);
            case "voidgrasp": return executeVoidGrasp(player, args);
            case "dominance": return executeDominance(player);
            case "anchor": return executeAnchor(player);
            case "shatter": return executeShatter(player);
            case "retribution": return executeRetribution(player);
            case "vanguard": return executeVanguard(player);
            case "execute": return executeExecute(player, args);
            case "ironwill": return executeIronWill(player);
            case "conqueror": return executeConqueror(player);

            // AL - Awoken class map-found skills (very powerful)
            case "worldshatter": return executeWorldShatter(player);
            case "voidlord": return executeVoidLord(player);

            // Admin commands
            case "magic": return executeMagicAdmin(player, args);

            default:
                player.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
                return false;
        }
    }

    // Utility methods
    private boolean isSkillOnCooldown(Player player, String skill, long cooldownMs) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> cooldowns = skillCooldowns.get(playerId);

        // Apply cooldown multiplier from config
        double multiplier = getConfig().getDouble("cooldown-multipliers." + skill, 1.0);
        long adjustedCooldown = (long)(cooldownMs * multiplier);

        if (cooldowns.containsKey(skill)) {
            long timeLeft = cooldowns.get(skill) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.YELLOW + skill + " is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return true;
            }
        }

        cooldowns.put(skill, System.currentTimeMillis() + adjustedCooldown);
        return false;
    }

    private List<LivingEntity> getNearbyEnemies(Player player, double radius) {
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                enemies.add((LivingEntity) entity);
            }
        }
        return enemies;
    }

    private Player getTargetPlayer(Player caster, String[] args) {
        if (args.length == 0) return null;
        return getServer().getPlayer(args[0]);
    }

    private LivingEntity getTargetEntity(Player player, double maxDistance) {
        Entity target = player.getTargetEntity((int) maxDistance, false);
        if (target instanceof LivingEntity) {
            return (LivingEntity) target;
        }
        return null;
    }

    // L - Basic skills learnable by both classes

    private boolean executeTaunt(Player player) {
        if (isSkillOnCooldown(player, "taunt", 8000)) return true;

        List<LivingEntity> enemies = getNearbyEnemies(player, 10);
        for (LivingEntity enemy : enemies) {
            if (enemy instanceof Mob) {
                ((Mob) enemy).setTarget(player);
            }
        }

        player.sendMessage(ChatColor.YELLOW + "You taunt nearby enemies!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.5f);
        return true;
    }

    private boolean executeFlameshield(Player player) {
        if (isSkillOnCooldown(player, "flameshield", 30000)) return true;

        final int duration = 15; // seconds
        activeEffects.get(player.getUniqueId()).add("flameshield");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("flameshield");
                player.sendMessage(ChatColor.RED + "Flameshield has worn off.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.GOLD + "You are now resistant to fire for " + duration + " seconds!");
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 20, 1, 1, 1, 0.1);
        return true;
    }

    private boolean executeInvuln(Player player) {
        if (isSkillOnCooldown(player, "invuln", 120000)) return true; // 2 minute cooldown

        final int duration = 3; // seconds
        Set<String> effects = activeEffects.get(player.getUniqueId());
        effects.add("invulnerable");
        effects.add("cannot_attack");

        new BukkitRunnable() {
            @Override
            public void run() {
                Set<String> playerEffects = activeEffects.get(player.getUniqueId());
                playerEffects.remove("invulnerable");
                playerEffects.remove("cannot_attack");
                player.sendMessage(ChatColor.YELLOW + "Invulnerability has worn off.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "You are invulnerable for " + duration + " seconds but cannot attack!");
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 30, 1, 2, 1, 0.2);
        return true;
    }

    private boolean executeHarmShield(Player player) {
        if (isSkillOnCooldown(player, "harmshield", 45000)) return true;

        final int duration = 20; // seconds
        final int damageReduction = 50; // 50% damage reduction
        activeEffects.get(player.getUniqueId()).add("harmshield_50");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("harmshield_50");
                player.sendMessage(ChatColor.BLUE + "Harm Shield has worn off.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.AQUA + "Harm Shield active! Damage reduced by " + damageReduction + "% for " + duration + " seconds.");
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, player.getLocation(), 25, 1, 1, 1, 0.1);
        return true;
    }

    private boolean executeBladegrasps(Player player) {
        if (isSkillOnCooldown(player, "bladegrasps", 25000)) return true;

        final int duration = 5; // seconds
        activeEffects.get(player.getUniqueId()).add("parry_physical");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("parry_physical");
                player.sendMessage(ChatColor.GRAY + "Bladegrasps has expired.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.WHITE + "Ready to parry the next physical attack within " + duration + " seconds!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.5f);
        return true;
    }

    private boolean executeParryMagic(Player player) {
        if (isSkillOnCooldown(player, "parrymagic", 30000)) return true;

        final int duration = 5; // seconds
        activeEffects.get(player.getUniqueId()).add("parry_magical");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("parry_magical");
                player.sendMessage(ChatColor.DARK_PURPLE + "Parry Magic has expired.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "Ready to parry the next magical attack within " + duration + " seconds!");
        player.getWorld().spawnParticle(Particle.WITCH, player.getLocation(), 15, 1, 1, 1, 0.1);
        return true;
    }

    private boolean executeHeadbutt(Player player, String[] args) {
        if (isSkillOnCooldown(player, "headbutt", 15000)) return true;

        LivingEntity target = getTargetEntity(player, 3.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        final double damage = 4.0;
        final int stunDuration = 3; // seconds

        target.damage(damage, player);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunDuration * 20, 255, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, stunDuration * 20, 255, false, false));

        player.sendMessage(ChatColor.DARK_RED + "You headbutt your target for " + damage + " damage and stun them!");
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
        return true;
    }

    private boolean executeToss(Player player, String[] args) {
        if (isSkillOnCooldown(player, "toss", 20000)) return true;

        LivingEntity target = getTargetEntity(player, 3.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        PlayerData data = playerData.get(player.getUniqueId());
        double throwDistance = 2.0 + (data.strength * 0.2); // Strength affects distance

        Vector direction = player.getLocation().getDirection().multiply(-1); // Opposite direction
        direction.setY(0.5); // Add upward momentum
        direction.normalize().multiply(throwDistance);

        target.setVelocity(direction);

        player.sendMessage(ChatColor.DARK_GREEN + "You grab and toss your target!");
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
        return true;
    }

    // B - Basic class leveling skills (10 skills)

    private boolean executeForcepull(Player player, String[] args) {
        if (isSkillOnCooldown(player, "forcepull", 12000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        double range = 8.0 + (data.intellect * 0.5); // Intellect affects range

        LivingEntity target = getTargetEntity(player, range);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        final double damage = 3.0;
        target.damage(damage, player);

        // Pull target towards player
        Vector direction = player.getLocation().subtract(target.getLocation()).toVector();
        direction.setY(0.2);
        direction.normalize().multiply(1.5);
        target.setVelocity(direction);

        player.sendMessage(ChatColor.DARK_BLUE + "You deal " + damage + " damage and pull your target towards you!");
        player.getWorld().spawnParticle(Particle.END_ROD, target.getLocation(), 15, 0.5, 1, 0.5, 0.1);
        return true;
    }

    private boolean executePulse(Player player) {
        if (isSkillOnCooldown(player, "pulse", 10000)) return true;

        final double damage = 2.0;
        final int radius = 5;

        List<LivingEntity> enemies = getNearbyEnemies(player, radius);
        for (LivingEntity enemy : enemies) {
            enemy.damage(damage, player);
        }

        player.sendMessage(ChatColor.RED + "You deal " + damage + " damage to all enemies within " + radius + " blocks!");
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1, 0, 0, 0, 0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);
        return true;
    }

    private boolean executeStealEssence(Player player, String[] args) {
        if (isSkillOnCooldown(player, "stealessence", 25000)) return true;

        LivingEntity target = getTargetEntity(player, 8.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        // Remove beneficial effects from target and apply to player
        Collection<PotionEffect> effects = target.getActivePotionEffects();
        int stolen = 0;

        for (PotionEffect effect : effects) {
            PotionEffectType type = effect.getType();
            // Check if it's a beneficial effect
            if (type.equals(PotionEffectType.STRENGTH) || type.equals(PotionEffectType.SPEED) ||
                    type.equals(PotionEffectType.REGENERATION) || type.equals(PotionEffectType.RESISTANCE)) {
                target.removePotionEffect(type);
                player.addPotionEffect(new PotionEffect(type, effect.getDuration(), effect.getAmplifier()));
                stolen++;
            }
        }

        if (stolen > 0) {
            player.sendMessage(ChatColor.DARK_PURPLE + "You steal " + stolen + " beneficial effects from your target!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Target has no beneficial effects to steal.");
        }

        player.getWorld().spawnParticle(Particle.WITCH, target.getLocation(), 20, 1, 1, 1, 0.2);
        return true;
    }

    private boolean executeTremor(Player player) {
        if (isSkillOnCooldown(player, "tremor", 18000)) return true;

        final double damage = 5.0;
        final int radius = 6;
        final double knockbackForce = 2.0;

        List<LivingEntity> targets = getNearbyEnemies(player, radius);
        for (LivingEntity target : targets) {
            target.damage(damage, player);

            // Knockback
            Vector direction = target.getLocation().subtract(player.getLocation()).toVector();
            direction.setY(0.5);
            direction.normalize().multiply(knockbackForce);
            target.setVelocity(direction);
        }

        player.sendMessage(ChatColor.DARK_GRAY + "You strike the ground with tremendous force!");
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 50, 3, 0.1, 3, 0.5, Material.STONE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        return true;
    }

    private boolean executeUndyingWill(Player player) {
        if (isSkillOnCooldown(player, "undyingwill", 180000)) return true; // 3 minute cooldown

        final int duration = 5; // seconds
        activeEffects.get(player.getUniqueId()).add("undying_will");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("undying_will");
                player.sendMessage(ChatColor.GOLD + "Undying Will has worn off.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.YELLOW + "You cannot be killed for the next " + duration + " seconds!");
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 40, 1, 2, 1, 0.3);
        return true;
    }

    private boolean executeIntervene(Player player, String[] args) {
        if (isSkillOnCooldown(player, "intervene", 30000)) return true;

        Player target = getTargetPlayer(player, args);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Please specify a valid player target!");
            return true;
        }

        final int duration = 10; // seconds
        final int maxDistance = 8; // blocks
        final double damageReduction = 25; // 25% damage taken by caster

        Map<String, Object> interventionData = interventionTargets.get(player.getUniqueId());
        interventionData.put("target", target.getUniqueId());
        interventionData.put("maxDistance", maxDistance);
        interventionData.put("damageReduction", damageReduction);
        interventionData.put("endTime", System.currentTimeMillis() + (duration * 1000));

        player.sendMessage(ChatColor.GREEN + "You mark " + target.getName() + " for Intervention!");
        target.sendMessage(ChatColor.GREEN + player.getName() + " is protecting you with Intervention!");

        new BukkitRunnable() {
            @Override
            public void run() {
                interventionData.clear();
                player.sendMessage(ChatColor.YELLOW + "Intervention has expired.");
            }
        }.runTaskLater(this, duration * 20L);

        return true;
    }

    private boolean executeEarthwall(Player player) {
        if (isSkillOnCooldown(player, "earthwall", 35000)) return true;

        final int wallLength = 5;
        Location startLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
        Vector perpendicular = player.getLocation().getDirection().getCrossProduct(new Vector(0, 1, 0)).normalize();

        List<Block> wallBlocks = new ArrayList<>();

        for (int i = -wallLength/2; i <= wallLength/2; i++) {
            Location blockLoc = startLoc.clone().add(perpendicular.clone().multiply(i));
            for (int y = 0; y < 3; y++) {
                Block block = blockLoc.clone().add(0, y, 0).getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.COBBLESTONE);
                    wallBlocks.add(block);
                }
            }
        }

        // Remove wall after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : wallBlocks) {
                    if (block.getType() == Material.COBBLESTONE) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(this, 600L); // 30 seconds

        player.sendMessage(ChatColor.DARK_GRAY + "You create a wall of earth!");
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        return true;
    }

    private boolean executeProvoke(Player player, String[] args) {
        if (isSkillOnCooldown(player, "provoke", 20000)) return true;

        LivingEntity target = getTargetEntity(player, 10.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        final int duration = 8; // seconds
        final int damageBonus = 25; // 25% more damage against player
        final int vulnerabilityIncrease = 15; // 15% more physical damage taken

        // Apply effects (simplified - in full implementation you'd track these)
        if (target instanceof Mob) {
            ((Mob) target).setTarget(player);
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 0));

        player.sendMessage(ChatColor.DARK_RED + "You provoke your target! They deal more damage to you but take more physical damage!");
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, target.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
        return true;
    }

    private boolean executeBoastfulBellow(Player player, String[] args) {
        if (isSkillOnCooldown(player, "boastfulbellow", 15000)) return true;

        LivingEntity target = getTargetEntity(player, 12.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        final double damage = 3.0;
        final int radius = 4;

        // Damage primary target
        target.damage(damage, player);

        // Damage enemies around target and interrupt casting
        List<LivingEntity> nearbyEnemies = new ArrayList<>();
        for (Entity entity : target.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity enemy = (LivingEntity) entity;
                enemy.damage(damage, player);
                nearbyEnemies.add(enemy);
            }
        }

        player.sendMessage(ChatColor.GOLD + "You unleash a boastful bellow, dealing " + damage + " damage and interrupting casting!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.NOTE, target.getLocation(), 20, 2, 2, 2, 0.1);
        return true;
    }

    private boolean executeBarrier(Player player) {
        if (isSkillOnCooldown(player, "barrier", 60000)) return true;

        final int duration = 15; // seconds
        final int disarmDuration = 3; // seconds for retaliation disarm
        final double retaliationDamage = 0.75; // 75% of weapon damage

        activeEffects.get(player.getUniqueId()).add("barrier_retaliation");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("barrier_retaliation");
                player.sendMessage(ChatColor.BLUE + "Protective Barrier has worn off.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.AQUA + "Protective Barrier active! You will retaliate against melee attacks for " + duration + " seconds!");
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, player.getLocation(), 30, 2, 2, 2, 0.2);
        return true;
    }

    // A - Awoken class leveling skills (10 skills)

    private boolean executeReckoning(Player player) {
        if (isSkillOnCooldown(player, "reckoning", 45000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Reckoning!");
            return true;
        }

        final int radius = 8;
        final double damage = 6.0;
        final int slowDuration = 5; // seconds
        final int slowStrength = Math.min(2 + (data.intellect / 10), 5); // Intellect affects slow strength

        List<LivingEntity> enemies = getNearbyEnemies(player, radius);
        for (LivingEntity enemy : enemies) {
            enemy.damage(damage, player);

            // Pull towards player
            Vector direction = player.getLocation().subtract(enemy.getLocation()).toVector();
            direction.setY(0.3);
            direction.normalize().multiply(1.2);
            enemy.setVelocity(direction);

            // Apply slow effect
            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDuration * 20, slowStrength));
        }

        player.sendMessage(ChatColor.DARK_PURPLE + "You reckon all enemies within " + radius + " blocks, dealing " + damage + " damage!");
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 60, 4, 1, 4, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.0f);
        return true;
    }

    // Additional A - Awoken class leveling skills (remaining 9 skills)

    private boolean executeVoidGrasp(Player player, String[] args) {
        if (isSkillOnCooldown(player, "voidgrasp", 25000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Void Grasp!");
            return true;
        }

        LivingEntity target = getTargetEntity(player, 15.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        final double damage = 8.0;
        final int suppressDuration = 4; // seconds - prevents ability use

        target.damage(damage, player);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, suppressDuration * 20, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, suppressDuration * 20, 2));

        player.sendMessage(ChatColor.DARK_PURPLE + "You grasp your target with void energy, suppressing their abilities!");
        player.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 40, 1, 2, 1, 0.5);
        return true;
    }

    private boolean executeDominance(Player player) {
        if (isSkillOnCooldown(player, "dominance", 90000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Dominance!");
            return true;
        }

        final int duration = 12; // seconds
        final int radius = 12;

        activeEffects.get(player.getUniqueId()).add("dominance_aura");

        // All enemies in range take increased damage and are feared
        List<LivingEntity> enemies = getNearbyEnemies(player, radius);
        for (LivingEntity enemy : enemies) {
            enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration * 20, 1));
            // Fear effect - make them run away
            Vector awayDirection = enemy.getLocation().subtract(player.getLocation()).toVector().normalize();
            enemy.setVelocity(awayDirection.multiply(0.8));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("dominance_aura");
                player.sendMessage(ChatColor.DARK_RED + "Dominance aura has faded.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.DARK_RED + "You assert your dominance! Enemies flee and take increased damage!");
        player.getWorld().spawnParticle(Particle.CRIMSON_SPORE, player.getLocation(), 50, 6, 2, 6, 0.2);
        return true;
    }

    private boolean executeAnchor(Player player) {
        if (isSkillOnCooldown(player, "anchor", 35000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Anchor!");
            return true;
        }

        final int duration = 8; // seconds
        final int radius = 6;

        activeEffects.get(player.getUniqueId()).add("anchored");

        // Player becomes immovable and immune to knockback/displacement
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 2));

        // Enemies within range are pulled and cannot use movement abilities
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= duration * 20) {
                    activeEffects.get(player.getUniqueId()).remove("anchored");
                    player.sendMessage(ChatColor.GRAY + "Anchor effect has ended.");
                    this.cancel();
                    return;
                }

                List<LivingEntity> enemies = getNearbyEnemies(player, radius);
                for (LivingEntity enemy : enemies) {
                    Vector pullDirection = player.getLocation().subtract(enemy.getLocation()).toVector();
                    pullDirection.setY(0);
                    pullDirection.normalize().multiply(0.3);
                    enemy.setVelocity(pullDirection);
                }

                ticks += 4;
            }
        }.runTaskTimer(this, 0L, 4L);

        player.sendMessage(ChatColor.DARK_GRAY + "You anchor yourself and pull enemies towards you!");
        return true;
    }

    private boolean executeShatter(Player player) {
        if (isSkillOnCooldown(player, "shatter", 40000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Shatter!");
            return true;
        }

        final double baseDamage = 4.0;
        final int radius = 8;

        List<LivingEntity> enemies = getNearbyEnemies(player, radius);
        for (LivingEntity enemy : enemies) {
            // Damage increases based on enemy's missing health
            double maxHealth = enemy.getAttribute(Attribute.MAX_HEALTH).getValue();
            double currentHealth = enemy.getHealth();
            double healthPercent = currentHealth / maxHealth;
            double damage = baseDamage + (baseDamage * (1.0 - healthPercent) * 2); // Up to 3x damage on low health enemies

            enemy.damage(damage, player);

            // Armor shred effect
            enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1)); // 10 seconds
        }

        player.sendMessage(ChatColor.YELLOW + "You shatter the defenses of all nearby enemies!");
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 100, 4, 1, 4, 0.5, Material.GLASS.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        return true;
    }

    private boolean executeRetribution(Player player) {
        if (isSkillOnCooldown(player, "retribution", 60000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Retribution!");
            return true;
        }

        final int duration = 10; // seconds
        final double damageMultiplier = 1.5;

        activeEffects.get(player.getUniqueId()).add("retribution");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("retribution");
                player.sendMessage(ChatColor.GOLD + "Retribution has ended.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.GOLD + "Retribution activated! Damage taken is reflected back for " + duration + " seconds!");
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, player.getLocation(), 40, 2, 2, 2, 0.3);
        return true;
    }

    private boolean executeVanguard(Player player) {
        if (isSkillOnCooldown(player, "vanguard", 45000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Vanguard!");
            return true;
        }

        final int duration = 15; // seconds
        final int radius = 10;

        activeEffects.get(player.getUniqueId()).add("vanguard_aura");

        // Allies gain damage resistance and movement speed
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof Player) {
                        Player ally = (Player) entity;
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, false, false));
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 100L);

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("vanguard_aura");
                player.sendMessage(ChatColor.BLUE + "Vanguard aura has faded.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.BLUE + "You inspire allies with your vanguard presence!");
        return true;
    }

    private boolean executeExecute(Player player, String[] args) {
        if (isSkillOnCooldown(player, "execute", 30000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Execute!");
            return true;
        }

        LivingEntity target = getTargetEntity(player, 5.0);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No valid target found!");
            return true;
        }

        double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = target.getHealth();
        double healthPercent = currentHealth / maxHealth;

        if (healthPercent <= 0.25) { // 25% health or below
            target.damage(999.0, player); // Instant kill
            player.sendMessage(ChatColor.DARK_RED + "EXECUTED! Your target was slain instantly!");
            player.getWorld().spawnParticle(Particle.ELDER_GUARDIAN, target.getLocation(), 30, 1, 1, 1, 0.3);
        } else {
            double damage = 12.0;
            target.damage(damage, player);
            player.sendMessage(ChatColor.RED + "Execute deals " + damage + " damage (target health too high for instant kill).");
        }

        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.5f);
        return true;
    }

    private boolean executeIronWill(Player player) {
        if (isSkillOnCooldown(player, "ironwill", 120000)) return true;

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Iron Will!");
            return true;
        }

        final int duration = 8; // seconds

        // Remove all negative effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (type.equals(PotionEffectType.POISON) || type.equals(PotionEffectType.SLOWNESS) ||
                    type.equals(PotionEffectType.WEAKNESS) || type.equals(PotionEffectType.MINING_FATIGUE) ||
                    type.equals(PotionEffectType.NAUSEA) || type.equals(PotionEffectType.BLINDNESS)) {
                player.removePotionEffect(type);
            }
        }

        // Grant immunity to negative effects
        activeEffects.get(player.getUniqueId()).add("iron_will");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("iron_will");
                player.sendMessage(ChatColor.GRAY + "Iron Will has faded.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.WHITE + "Your iron will purges all negative effects and grants immunity!");
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 25, 1, 2, 1, 0.2);
        return true;
    }

    private boolean executeConqueror(Player player) {
        if (isSkillOnCooldown(player, "conqueror", 180000)) return true; // 3 minute cooldown

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Conqueror!");
            return true;
        }

        final int duration = 20; // seconds

        // Massive stat boost
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, 2)); // Strength III
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 1)); // Resistance II
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 1)); // Speed II
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 1)); // Regen II

        activeEffects.get(player.getUniqueId()).add("conqueror");

        new BukkitRunnable() {
            @Override
            public void run() {
                activeEffects.get(player.getUniqueId()).remove("conqueror");
                player.sendMessage(ChatColor.GOLD + "Conqueror has ended.");
            }
        }.runTaskLater(this, duration * 20L);

        player.sendMessage(ChatColor.GOLD + "You become a CONQUEROR! All abilities greatly enhanced!");
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 60, 3, 3, 3, 0.4);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        return true;
    }

    // AL - Awoken class map-found skills (very powerful) - Examples

    private boolean executeWorldShatter(Player player) {
        if (isSkillOnCooldown(player, "worldshatter", 300000)) return true; // 5 minute cooldown

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use World Shatter!");
            return true;
        }

        final double damage = 25.0;
        final int radius = 15;

        // Massive area devastation
        List<LivingEntity> enemies = getNearbyEnemies(player, radius);
        for (LivingEntity enemy : enemies) {
            enemy.damage(damage, player);

            // Massive knockback
            Vector direction = enemy.getLocation().subtract(player.getLocation()).toVector();
            direction.setY(1.0);
            direction.normalize().multiply(3.0);
            enemy.setVelocity(direction);
        }

        // Destroy terrain in radius
        Location center = player.getLocation();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -2; y <= 2; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    if (blockLoc.distance(center) <= radius) {
                        Block block = blockLoc.getBlock();
                        if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                            if (Math.random() < 0.3) { // 30% chance to destroy each block
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }

        player.sendMessage(ChatColor.DARK_RED + "YOU SHATTER THE WORLD ITSELF!");
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 100, 8, 4, 8, 1.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
        return true;
    }

    private boolean executeVoidLord(Player player) {
        if (isSkillOnCooldown(player, "voidlord", 600000)) return true; // 10 minute cooldown

        PlayerData data = playerData.get(player.getUniqueId());
        if (!data.isAwokenClass) {
            player.sendMessage(ChatColor.RED + "Only Awoken classes can use Void Lord!");
            return true;
        }

        final int duration = 30; // seconds

        // Transform into Void Lord form
        activeEffects.get(player.getUniqueId()).add("void_lord");

        // Massive stat boost and special abilities
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, 4)); // Strength V
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 3)); // Resistance IV
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 2)); // Speed III
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 3)); // Regen IV
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration * 20, 0));

        // Continuous void energy aura
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= duration * 20) {
                    activeEffects.get(player.getUniqueId()).remove("void_lord");
                    player.sendMessage(ChatColor.DARK_PURPLE + "You return from the Void Lord form.");
                    this.cancel();
                    return;
                }

                // Damage nearby enemies continuously
                List<LivingEntity> enemies = getNearbyEnemies(player, 8);
                for (LivingEntity enemy : enemies) {
                    enemy.damage(2.0, player);
                }

                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 20, 2, 2, 2, 0.5);
                ticks += 20;
            }
        }.runTaskTimer(this, 0L, 20L);

        player.sendMessage(ChatColor.DARK_PURPLE + "YOU ASCEND TO VOID LORD! Reality bends to your will!");
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 80, 4, 4, 4, 0.8);
        return true;
    }

    private boolean executeMagicAdmin(Player player, String[] args) {
        if (!player.hasPermission("magic.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use magic admin commands!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== Magic Plugin Commands ===");
            player.sendMessage(ChatColor.YELLOW + "/magic setclass <player> <basic|awoken> - Set player's class");
            player.sendMessage(ChatColor.YELLOW + "/magic setstats <player> <intellect|strength> <value> - Set player stats");
            player.sendMessage(ChatColor.YELLOW + "/magic info <player> - Show player info");
            player.sendMessage(ChatColor.YELLOW + "/magic reload - Reload plugin data");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "setclass":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /magic setclass <player> <basic|awoken>");
                    return true;
                }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                String className = args[2].toLowerCase();
                PlayerData data = playerData.get(target.getUniqueId());
                if (className.equals("awoken")) {
                    data.isAwokenClass = true;
                    player.sendMessage(ChatColor.GREEN + target.getName() + " is now an Awoken class!");
                } else if (className.equals("basic")) {
                    data.isAwokenClass = false;
                    player.sendMessage(ChatColor.GREEN + target.getName() + " is now a Basic class!");
                } else {
                    player.sendMessage(ChatColor.RED + "Class must be 'basic' or 'awoken'!");
                }
                return true;

            case "setstats":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /magic setstats <player> <intellect|strength> <value>");
                    return true;
                }
                target = getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                String stat = args[2].toLowerCase();
                int value;
                try {
                    value = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Value must be a number!");
                    return true;
                }
                data = playerData.get(target.getUniqueId());
                if (stat.equals("intellect")) {
                    data.intellect = value;
                    player.sendMessage(ChatColor.GREEN + target.getName() + "'s intellect set to " + value);
                } else if (stat.equals("strength")) {
                    data.strength = value;
                    player.sendMessage(ChatColor.GREEN + target.getName() + "'s strength set to " + value);
                } else {
                    player.sendMessage(ChatColor.RED + "Stat must be 'intellect' or 'strength'!");
                }
                return true;

            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /magic info <player>");
                    return true;
                }
                target = getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                data = playerData.get(target.getUniqueId());
                player.sendMessage(ChatColor.GOLD + "=== " + target.getName() + " Info ===");
                player.sendMessage(ChatColor.YELLOW + "Class: " + (data.isAwokenClass ? "Awoken" : "Basic"));
                player.sendMessage(ChatColor.YELLOW + "Intellect: " + data.intellect);
                player.sendMessage(ChatColor.YELLOW + "Strength: " + data.strength);
                player.sendMessage(ChatColor.YELLOW + "Known Skills: " + data.knownSkills.size());
                return true;

            case "reload":
                savePlayerData();
                reloadConfig();
                loadPlayerData();
                player.sendMessage(ChatColor.GREEN + "Magic plugin configuration and data reloaded!");
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand: " + subcommand);
                return true;
        }
    }

    private void savePlayerData() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            String uuid = entry.getKey().toString();
            PlayerData data = entry.getValue();
            
            config.set(uuid + ".isAwokenClass", data.isAwokenClass);
            config.set(uuid + ".intellect", data.intellect);
            config.set(uuid + ".strength", data.strength);
            config.set(uuid + ".knownSkills", new ArrayList<>(data.knownSkills));
        }
        
        try {
            config.save(playerDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save player data: " + e.getMessage());
        }
    }
    
    private void loadPlayerData() {
        if (!playerDataFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);
        
        for (String uuidString : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                PlayerData data = new PlayerData();
                
                data.isAwokenClass = config.getBoolean(uuidString + ".isAwokenClass", false);
                data.intellect = config.getInt(uuidString + ".intellect", 10);
                data.strength = config.getInt(uuidString + ".strength", 10);
                
                List<String> skills = config.getStringList(uuidString + ".knownSkills");
                data.knownSkills = new HashSet<>(skills);
                
                playerData.put(uuid, data);
                skillCooldowns.put(uuid, new HashMap<>());
                activeEffects.put(uuid, new HashSet<>());
                interventionTargets.put(uuid, new HashMap<>());
                
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID in player data: " + uuidString);
            }
        }
    }

    // Command registration in plugin.yml would include all skills:
    /*
    name: Magic
    version: 1.0
    main: com.g2806.Magic.Magic
    api-version: 1.21

    commands:
      # L - Basic skills (learnable by both Basic and Awoken classes)
      taunt:
        description: "Mocks enemies around you"
      flameshield:
        description: "Become resistant to fire"
      invuln:
        description: "Grants total immunity but prevents attacking"
      harmshield:
        description: "Reduces incoming damage"
      bladegrasps:
        description: "Parry the next physical attack"
      parrymagic:
        description: "Parry the next magical attack"
      headbutt:
        description: "Headbutt target for damage and stun"
      toss:
        description: "Grab and throw your target"

      # B - Basic class leveling skills (10 skills)
      forcepull:
        description: "Deal damage and force target towards you"
      pulse:
        description: "Deal damage to all enemies within range"
      stealessence:
        description: "Steal beneficial effects from target"
      tremor:
        description: "Strike ground with powerful tremor"
      undyingwill:
        description: "Cannot be killed for limited time"
      intervene:
        description: "Mark target for intervention protection"
      earthwall:
        description: "Create a wall of earth"
      provoke:
        description: "Provoke target for tactical advantage"
      boastfulbellow:
        description: "Bellow that damages and interrupts"
      barrier:
        description: "Protective barrier with retaliation"

      # A - Awoken class leveling skills (10 skills)
      reckoning:
        description: "Reckon all enemies with damage and pull"
      voidgrasp:
        description: "Grasp target with void energy"
      dominance:
        description: "Assert dominance, fear enemies"
      anchor:
        description: "Become immovable and pull enemies"
      shatter:
        description: "Shatter defenses of all nearby enemies"
      retribution:
        description: "Reflect damage back to attackers"
      vanguard:
        description: "Inspire allies with protective aura"
      execute:
        description: "Instant kill low health enemies"
      ironwill:
        description: "Purge negatives and gain immunity"
      conqueror:
        description: "Become an unstoppable conqueror"

      # AL - Awoken class map-found skills (very powerful)
      worldshatter:
        description: "Shatter the world itself"
      voidlord:
        description: "Ascend to Void Lord form"
    */
}