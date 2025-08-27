# Souls Plugin

A Minecraft plugin where players collect souls to gain powerful abilities and survive death.

## Overview

Players start with 3 souls and lose 1 soul each time they die. When you reach 0 souls, you become a ghost (spectator mode) until someone revives you. The more souls you have, the more powerful abilities you unlock.

## How to Get Souls

- Kill other players to steal their soul
- Pick up dropped soul items when players die
- Receive souls from other players using `/givesoul`
- Admins can add souls with `/addsoul`

## Soul Powers

- **5 souls:** Speed I
- **8 souls:** Jump Boost I  
- **12 souls:** Water Breathing
- **15 souls:** Night Vision
- **20 souls:** Strength I
- **25 souls:** Regeneration I
- **30 souls:** Flight Ability

## Commands

### Player Commands
- `/souls` - Check your soul count
- `/givesoul <player>` - Give one of your souls to another player
- `/revive <player>` - Revive a ghost by giving them one of your souls
- `/soulsinfo` - Show plugin information and power requirements

### Admin Commands (OP only)
- `/addsoul <player> <amount>` - Add souls to a player
- `/removesoul <player> <amount>` - Remove souls from a player
- `/forcerevive <player>` - Revive a ghost without costing souls

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. The plugin will create a `playerdata.yml` file to save soul counts

## Requirements

- Bukkit/Spigot/Paper server
- Minecraft 1.21+