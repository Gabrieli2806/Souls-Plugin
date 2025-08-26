# Magic Plugin - Complete Skills Guide

## Table of Contents
- [L - Learnable Skills (Both Classes)](#l---learnable-skills-both-classes)
- [B - Basic Class Progression Skills](#b---basic-class-progression-skills)
- [A - Awoken Class Progression Skills](#a---awoken-class-progression-skills)
- [AL - Legendary Map-Found Skills](#al---legendary-map-found-skills)

---

## L - Learnable Skills (Both Classes)
*Basic skills that both Basic and Awoken classes can learn*

### `/taunt`
- **Cooldown**: 8 seconds
- **Range**: 10 blocks
- **Effect**: Forces all nearby enemies (mobs) to target you
- **Purpose**: Tank skill for drawing aggro and protecting allies
- **Audio**: Strong attack sound
- **Usage**: Perfect for dungeons and group combat

### `/flameshield`
- **Cooldown**: 30 seconds
- **Duration**: 15 seconds
- **Effect**: Complete immunity to fire, fire tick, and lava damage
- **Visual**: 20 flame particles around player
- **Purpose**: Environmental protection and fire-based combat immunity
- **Note**: Essential for Nether exploration and fire-based enemies

### `/invuln`
- **Cooldown**: 2 minutes (120 seconds)
- **Duration**: 3 seconds
- **Effect**: 
  - Complete immunity to all damage
  - Cannot attack while active (prevents abuse)
- **Visual**: 30 Totem of Undying particles
- **Purpose**: Emergency survival skill for critical moments
- **Strategy**: Use when low on health or facing overwhelming damage

### `/harmshield`
- **Cooldown**: 45 seconds
- **Duration**: 20 seconds
- **Effect**: 50% damage reduction from all sources
- **Visual**: Enchanted hit particles around player
- **Purpose**: Sustained damage mitigation for extended fights
- **Note**: Stacks with armor and other protection

### `/bladegrasps`
- **Cooldown**: 25 seconds
- **Duration**: 5 seconds (window to parry)
- **Effect**: 
  - Next physical attack is completely blocked
  - Reflects 50% damage back to attacker
- **Audio**: Knockback attack sound
- **Purpose**: Skilled timing-based defense against melee attacks
- **Strategy**: Use right before enemy attack lands

### `/parrymagic`
- **Cooldown**: 30 seconds
- **Duration**: 5 seconds (window to parry)
- **Effect**: 
  - Next magical attack is completely blocked
  - Reflects 50% damage back to caster
- **Visual**: Purple witch particles
- **Purpose**: Counter magical attacks and spell-casting enemies
- **Note**: Works against any damage marked as magical

### `/headbutt [target]`
- **Cooldown**: 15 seconds
- **Range**: 3 blocks
- **Damage**: 4.0 damage
- **Effect**: 
  - Damages target
  - 3-second stun (Slowness 255 + Mining Fatigue 255)
- **Visual**: Crit particles at target location
- **Audio**: Strong attack sound
- **Purpose**: Close-range disable with moderate damage
- **Strategy**: Great for interrupting enemy abilities

### `/toss [target]`
- **Cooldown**: 20 seconds
- **Range**: 3 blocks
- **Effect**: 
  - Grabs target and throws them backward
  - Distance scales with Strength stat (2.0 + Strength × 0.2)
  - Adds upward momentum for dramatic effect
- **Audio**: Sweep attack sound
- **Purpose**: Crowd control and battlefield positioning
- **Note**: Higher Strength = further throws

---

## B - Basic Class Progression Skills
*10 skills learned through leveling as Basic class*

### `/forcepull [target]`
- **Cooldown**: 12 seconds
- **Range**: 8 blocks + (Intellect × 0.5) - scales with stats
- **Damage**: 3.0 damage
- **Effect**: 
  - Damages target
  - Pulls enemy toward you with 1.5x force
- **Visual**: 15 End Rod particles at target
- **Purpose**: Ranged damage + positioning control
- **Strategy**: Great for pulling enemies into melee range

### `/pulse`
- **Cooldown**: 10 seconds
- **Range**: 5 blocks radius
- **Damage**: 2.0 damage to all enemies
- **Effect**: Area-of-effect damage around player
- **Visual**: Single explosion particle
- **Audio**: Explosion sound (quiet, high pitch)
- **Purpose**: Multi-enemy damage for crowd control
- **Usage**: Perfect when surrounded by multiple enemies

### `/stealessence [target]`
- **Cooldown**: 25 seconds
- **Range**: 8 blocks
- **Effect**: 
  - Steals beneficial effects from target (Strength, Speed, Regen, Resistance)
  - Applies stolen effects to yourself with same duration/level
  - Shows count of stolen effects
- **Visual**: 20 witch particles at target
- **Purpose**: Buff stealing and enemy debuffing
- **Strategy**: Use against buffed enemies or boss mobs

### `/tremor`
- **Cooldown**: 18 seconds
- **Range**: 6 blocks radius
- **Damage**: 5.0 damage
- **Effect**: 
  - Ground-based AoE attack
  - 2.0x knockback force away from caster
  - Adds upward momentum
- **Visual**: 50 stone block particles in 3x3 area
- **Audio**: Explosion sound (loud, low pitch)
- **Purpose**: High-damage area control with knockback
- **Note**: Creates dramatic ground-strike effect

### `/undyingwill`
- **Cooldown**: 3 minutes (180 seconds)
- **Duration**: 5 seconds
- **Effect**: Cannot be reduced below 0.5 health (prevents death)
- **Visual**: 40 Totem of Undying particles
- **Purpose**: Ultimate survival skill for boss fights
- **Strategy**: Use when about to die to guarantee survival window

### `/intervene <player>`
- **Cooldown**: 30 seconds
- **Duration**: 10 seconds
- **Range**: 8 blocks maximum distance from target
- **Effect**: 
  - Marks target player for protection
  - You take 25% of damage they receive (if within range)
  - Reduces damage they take
- **Purpose**: Protect allies and tank damage for party members
- **Note**: Requires staying close to protected player

### `/earthwall`
- **Cooldown**: 35 seconds
- **Dimensions**: 5 blocks wide × 3 blocks high
- **Duration**: 30 seconds
- **Effect**: 
  - Creates cobblestone wall 2 blocks in front of player
  - Wall oriented perpendicular to your facing direction
  - Automatically removes after duration
- **Audio**: Stone placement sound
- **Purpose**: Tactical battlefield control and cover creation
- **Strategy**: Use for defense, blocking enemies, or creating chokepoints

### `/provoke [target]`
- **Cooldown**: 20 seconds
- **Range**: 10 blocks
- **Duration**: 8 seconds
- **Effect**: 
  - Forces mob to target you
  - Target deals 25% more damage to you
  - Target takes 15% more physical damage from all sources
  - Applies Glowing effect for visibility
- **Visual**: Angry villager particles above target
- **Purpose**: Tactical tanking with risk/reward mechanics
- **Strategy**: Use when team can capitalize on increased damage vulnerability

### `/boastfulbellow [target]`
- **Cooldown**: 15 seconds
- **Range**: 12 blocks primary target
- **AoE**: 4 blocks around target
- **Damage**: 3.0 damage to target and nearby enemies
- **Effect**: 
  - Interrupts casting/channeling abilities
  - Damages all enemies near target
- **Visual**: 20 note particles around target area
- **Audio**: Ender Dragon growl (loud, high pitch)
- **Purpose**: Interrupt enemy abilities while dealing AoE damage
- **Note**: Great against spellcasters and grouped enemies

### `/barrier`
- **Cooldown**: 60 seconds (1 minute)
- **Duration**: 15 seconds
- **Effect**: 
  - Retaliates against melee attackers within 3 blocks
  - Deals 75% of weapon damage back to attacker
  - Applies 3-second Weakness effect to attacker
- **Visual**: 30 enchanted hit particles around player
- **Purpose**: Punish melee attackers and discourage close combat
- **Strategy**: Use when expecting sustained melee combat

---

## A - Awoken Class Progression Skills
*10 advanced skills for Awoken class progression*

### `/reckoning`
- **Cooldown**: 45 seconds
- **Range**: 8 blocks radius
- **Damage**: 6.0 damage to all enemies
- **Effect**: 
  - Pulls all enemies toward you (1.2x force)
  - Applies Slowness (duration: 5 seconds, strength scales with Intellect)
  - Slow strength: 2 + (Intellect ÷ 10), max 5
- **Visual**: 60 Dragon Breath particles in 4×1×4 area
- **Audio**: Ender Dragon growl
- **Purpose**: Ultimate crowd control and area damage
- **Requirement**: Awoken class only

### `/voidgrasp [target]`
- **Cooldown**: 25 seconds
- **Range**: 15 blocks
- **Damage**: 8.0 damage
- **Duration**: 4 seconds
- **Effect**: 
  - High damage single-target attack
  - Applies Weakness II (prevents ability use)
  - Applies Mining Fatigue II (slows actions)
- **Visual**: 40 portal particles around target
- **Purpose**: Disable and damage priority targets
- **Strategy**: Use against dangerous enemies to neutralize their abilities

### `/dominance`
- **Cooldown**: 90 seconds (1.5 minutes)
- **Duration**: 12 seconds
- **Range**: 12 blocks radius
- **Effect**: 
  - All enemies gain Weakness I
  - Enemies are "feared" (knocked away from you)
  - You gain damage aura that affects all nearby enemies
- **Visual**: 50 Crimson Spore particles in 6×2×6 area
- **Purpose**: Area control and damage amplification
- **Note**: Creates zone of dominance around player

### `/anchor`
- **Cooldown**: 35 seconds
- **Duration**: 8 seconds
- **Range**: 6 blocks pull radius
- **Effect**: 
  - Player becomes immovable (Slowness 255)
  - Player gains Resistance II
  - Continuously pulls enemies toward you every 0.2 seconds
  - Enemies cannot use movement abilities while in range
- **Purpose**: Become an immovable tank that controls positioning
- **Strategy**: Use in chokepoints or when you need to hold ground

### `/shatter`
- **Cooldown**: 40 seconds
- **Range**: 8 blocks radius
- **Damage**: 4.0 base + up to 8.0 bonus (scales with enemy missing health)
- **Effect**: 
  - Damage increases based on enemy's missing health (up to 3x damage)
  - Applies 10-second Weakness I (armor shred effect)
  - Breaks enemy defensive abilities
- **Visual**: 100 glass break particles in 4×1×4 area
- **Audio**: Glass breaking sound (low pitch)
- **Purpose**: Execute low-health enemies and break defensive abilities
- **Note**: More effective against wounded enemies

### `/retribution`
- **Cooldown**: 60 seconds (1 minute)
- **Duration**: 10 seconds
- **Effect**: 
  - All damage taken is reflected back to attacker at 1.5x multiplier
  - Works against any damage source
  - Does not reduce incoming damage
- **Visual**: 40 enchanted hit particles around player
- **Purpose**: Turn enemy damage into a weapon
- **Strategy**: Use when facing high-damage enemies or multiple attackers

### `/vanguard`
- **Cooldown**: 45 seconds
- **Duration**: 15 seconds
- **Range**: 10 blocks radius
- **Effect**: 
  - All allied players in range gain Resistance I and Speed I
  - Buffs refresh every 5 seconds while in range
  - You become inspiration point for team
- **Purpose**: Team support and buff aura
- **Usage**: Essential for group content and party play
- **Note**: Does not affect the caster directly

### `/execute [target]`
- **Cooldown**: 30 seconds
- **Range**: 5 blocks
- **Damage**: 12.0 normal damage OR instant kill
- **Effect**: 
  - If target has ≤25% health: Instant kill (999 damage)
  - If target has >25% health: 12.0 damage
  - Shows different messages based on success
- **Visual**: 30 Elder Guardian particles for execute, normal crit for regular damage
- **Audio**: Critical hit sound (low pitch)
- **Purpose**: Finish off weakened enemies dramatically
- **Strategy**: Use as finisher when enemies are low on health

### `/ironwill`
- **Cooldown**: 2 minutes (120 seconds)
- **Duration**: 8 seconds
- **Effect**: 
  - Instantly removes all negative effects (Poison, Slowness, Weakness, etc.)
  - Grants immunity to new negative effects for duration
  - Purifies all debuffs on activation
- **Visual**: 25 Totem of Undying particles
- **Purpose**: Counter debuff-heavy enemies and maintain combat effectiveness
- **Strategy**: Save for when heavily debuffed or facing status effect enemies

### `/conqueror`
- **Cooldown**: 3 minutes (180 seconds)
- **Duration**: 20 seconds
- **Effect**: Massive stat boost transformation:
  - Strength III (+6 attack damage)
  - Resistance II (40% damage reduction)
  - Speed II (40% movement speed)
  - Regeneration II (fast health recovery)
- **Visual**: 60 Dragon Breath particles in 3×3×3 area
- **Audio**: Ender Dragon growl (loud, high pitch)
- **Purpose**: Ultimate temporary transformation for boss fights
- **Note**: Stacks with all other effects and equipment

---

## AL - Legendary Map-Found Skills
*Ultra-powerful skills found only on maps - Awoken class exclusive*

### `/worldshatter`
- **Cooldown**: 5 minutes (300 seconds)
- **Range**: 15 blocks radius
- **Damage**: 25.0 damage to all enemies
- **Effect**: 
  - **Massive Enemy Damage**: Highest damage skill in plugin
  - **Extreme Knockback**: 3.0x force + upward momentum to all enemies
  - **Terrain Destruction**: 
    - 15-block radius, 5 blocks high (y -2 to +2)
    - 30% chance each block is destroyed
    - Cannot destroy bedrock
    - Reshapes entire battlefield
- **Visual**: 100 explosion particles spread over 8×4×8 area
- **Audio**: Loud explosion (2x volume, low pitch)
- **Message**: "YOU SHATTER THE WORLD ITSELF!"
- **Purpose**: Ultimate area denial and boss clearing
- **Requirement**: Awoken class + Legendary permission
- **Warning**: Permanently alters terrain - use carefully!

### `/voidlord`
- **Cooldown**: 10 minutes (600 seconds)
- **Duration**: 30 seconds
- **Effect**: **Complete transformation into Void Lord form:**
  - **Strength V** (+10 attack damage)
  - **Resistance IV** (80% damage reduction)
  - **Speed III** (60% movement speed)  
  - **Regeneration IV** (very fast healing)
  - **Night Vision** (perfect darkness vision)
  - **Continuous Aura**: 2.0 damage per second to all enemies within 8 blocks
- **Visual**: 
  - 80 End Rod particles in 4×4×4 area on activation
  - 20 portal particles every second around player
- **Message**: "YOU ASCEND TO VOID LORD! Reality bends to your will!"
- **Purpose**: Ultimate boss form transformation
- **Requirement**: Awoken class + Legendary permission
- **Note**: Most powerful sustained effect in the entire plugin

---

## Stat Scaling System

### Intellect Effects:
- **Forcepull**: Range = 8 + (Intellect × 0.5)
- **Reckoning**: Slow strength = 2 + (Intellect ÷ 10), max 5

### Strength Effects:
- **Toss**: Distance = 2.0 + (Strength × 0.2)

### Class Requirements:
- **Basic Class**: Can use L and B category skills
- **Awoken Class**: Can use L, B, A, and AL category skills (with proper permissions)

---

## Permission Requirements:
- `magic.basic` - L and B skills (default: true)
- `magic.awoken` - A skills (default: false) 
- `magic.legendary` - AL skills (default: false)
- `magic.admin` - Admin commands (default: op)

## Usage Tips:
1. **Combo System**: Chain skills for maximum effectiveness (e.g., Forcepull → Headbutt → Execute)
2. **Cooldown Management**: Higher-tier skills have longer cooldowns - use strategically
3. **Class Progression**: Start with Basic class, master those skills, then advance to Awoken
4. **Team Play**: Use Intervene and Vanguard to support allies in group content
5. **Boss Fights**: Save ultimate skills (Undying Will, Conqueror, World Shatter) for major encounters