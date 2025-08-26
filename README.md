# Magic Plugin - Warrior/Tank Class System

A comprehensive Minecraft plugin that adds warrior and tank class skills with a progression system.

## Features

### Class System
- **Basic Class**: Starting class with fundamental skills
- **Awoken Class**: Advanced class with powerful abilities
- **Legendary Skills**: Map-found ultra-powerful abilities

### Skill Categories

#### L - Learnable Skills (Both Classes)
- `taunt` - Mock enemies around you
- `flameshield` - Fire resistance for limited time
- `invuln` - Total immunity but prevents attacking
- `harmshield` - Reduces incoming damage by percentage
- `bladegrasps` - Parry the next physical attack
- `parrymagic` - Parry the next magical attack
- `headbutt` - Damage and stun target
- `toss` - Grab and throw target

#### B - Basic Class Progression Skills
- `forcepull` - Damage and pull target towards you
- `pulse` - Area damage to all nearby enemies
- `stealessence` - Steal beneficial effects from target
- `tremor` - Ground strike with knockback
- `undyingwill` - Cannot be killed for limited time
- `intervene` - Protect another player
- `earthwall` - Create temporary earth barrier
- `provoke` - Tactical advantage against target
- `boastfulbellow` - Damage and interrupt casting
- `barrier` - Protective barrier with retaliation

#### A - Awoken Class Progression Skills
- `reckoning` - Damage and pull all nearby enemies
- `voidgrasp` - Suppress target's abilities
- `dominance` - Fear enemies and increase damage taken
- `anchor` - Become immovable and pull enemies
- `shatter` - Break enemy defenses
- `retribution` - Reflect damage back to attackers
- `vanguard` - Inspire allies with protective aura
- `execute` - Instant kill low health enemies
- `ironwill` - Purge negatives and gain immunity
- `conqueror` - Ultimate transformation with massive buffs

#### AL - Legendary Map-Found Skills
- `worldshatter` - Devastating area destruction
- `voidlord` - Ascend to Void Lord form

## Admin Commands

- `/magic setclass <player> <basic|awoken>` - Set player's class
- `/magic setstats <player> <intellect|strength> <value>` - Set player stats
- `/magic info <player>` - Show player information
- `/magic reload` - Reload plugin configuration

## Permissions

- `magic.basic` - Use basic skills (default: true)
- `magic.awoken` - Use awoken class skills (default: false)
- `magic.legendary` - Use legendary skills (default: false)
- `magic.admin` - Use admin commands (default: op)
- `magic.*` - All permissions (default: op)

## Installation

1. Download the plugin JAR file from `build/libs/Magic-Plugin-1.0.jar`
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using `/magic` commands or edit `config.yml`

## Configuration

The plugin creates a `config.yml` file with various settings:
- Cooldown multipliers for skills
- Damage multipliers
- Class progression settings
- PvP settings
- World restrictions
- Visual effects settings

## Data Storage

Player data is automatically saved to `playerdata.yml` and includes:
- Class type (Basic/Awoken)
- Intellect and Strength stats
- Known skills
- Automatically saves every 5 minutes and on player quit

## Requirements

- Minecraft Server 1.21.5+
- Java 21+
- Paper/Spigot server

## Build Instructions

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Support

This plugin is designed for defensive gameplay mechanics and does not include any malicious features.