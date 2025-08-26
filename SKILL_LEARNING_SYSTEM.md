# How Players Learn Skills - Magic Plugin

## 🎓 Skill Learning System

### Starting Skills
Every new player begins with:
- **Taunt** - Basic aggro control
- **Flameshield** - Fire protection

### Learning Methods

#### 1. **Experience & Leveling**
- **Gain XP** by using skills and defeating enemies
- **Level up** to unlock higher-tier skills
- **Level Formula**: √(XP ÷ 100) + 1
- **Stat Bonus**: +2 Intellect and +2 Strength every 5 levels

#### 2. **Skill Shop System**
Use `/magic learn` to see available skills:
- **Green** skills = can afford
- **Red** skills = need more XP
- **Cost Formula**: Required Level × 100 XP

#### 3. **Admin Commands** (for server management)
- `/magic givexp <player> <amount>` - Grant experience
- `/magic setclass <player> <awoken>` - Promote to Awoken class

### Skill Categories & Requirements

#### **L - Learnable Skills** (Levels 1-10)
| Skill | Level | Cost | Description |
|-------|-------|------|-------------|
| taunt | 1 | FREE | Starting skill |
| flameshield | 1 | FREE | Starting skill |
| invuln | 3 | 300 XP | Emergency immunity |
| harmshield | 3 | 300 XP | Damage reduction |
| bladegrasps | 5 | 500 XP | Physical parry |
| parrymagic | 5 | 500 XP | Magical parry |
| headbutt | 7 | 700 XP | Stun attack |
| toss | 7 | 700 XP | Throw enemy |

#### **B - Basic Class Skills** (Levels 10-25)
| Skill | Level | Cost | Description |
|-------|-------|------|-------------|
| forcepull | 10 | 1000 XP | Damage + pull |
| pulse | 10 | 1000 XP | AoE damage |
| stealessence | 12 | 1200 XP | Steal buffs |
| tremor | 12 | 1200 XP | Ground slam |
| undyingwill | 15 | 1500 XP | Death prevention |
| intervene | 15 | 1500 XP | Protect allies |
| earthwall | 18 | 1800 XP | Create barrier |
| provoke | 18 | 1800 XP | Tactical taunt |
| boastfulbellow | 22 | 2200 XP | AoE interrupt |
| barrier | 22 | 2200 XP | Damage reflection |

#### **A - Awoken Class Skills** (Levels 25-50)
*Requires class promotion to Awoken*
| Skill | Level | Cost | Description |
|-------|-------|------|-------------|
| reckoning | 25 | 2500 XP | Mass pull + damage |
| voidgrasp | 25 | 2500 XP | Disable target |
| dominance | 30 | 3000 XP | Fear aura |
| anchor | 30 | 3000 XP | Immovable tank |
| shatter | 35 | 3500 XP | Defense breaking |
| retribution | 35 | 3500 XP | Damage reflection |
| vanguard | 40 | 4000 XP | Team buffs |
| execute | 40 | 4000 XP | Low-health finisher |
| ironwill | 45 | 4500 XP | Debuff immunity |
| conqueror | 45 | 4500 XP | Ultimate transformation |

#### **AL - Legendary Skills** (Map-Found Only)
- **worldshatter** - Found in special map locations
- **voidlord** - Found in special map locations
- **Cannot be learned normally** - must be discovered!

## 📚 Player Commands

### **Learning Commands**
- `/magic learn` - View available skills to learn
- `/magic skills` - List your known skills  
- `/magic buy <skillname>` - Purchase a skill with XP
- `/magic info` - View your level, XP, and stats

### **Usage Commands**
- `/<skillname>` - Use any skill you know
- Skills have cooldowns and requirements

## 🎯 Earning Experience

### XP Sources:
1. **Using Skills** - Gain XP each time you successfully use abilities
2. **Defeating Enemies** - Kill mobs and players for XP rewards
3. **Admin Grants** - Server operators can give bonus XP
4. **Quests/Events** - Server-specific XP events

### XP Requirements:
- **Level 1**: 0 XP
- **Level 5**: 2,500 XP  
- **Level 10**: 10,000 XP
- **Level 25**: 62,500 XP
- **Level 50**: 250,000 XP

## 🏛️ Class Progression

### **Basic Class** (Starting)
- Access to **L** and **B** skill categories
- Learn through combat and practice
- Max level ~25 for full Basic skill access

### **Awoken Class** (Advanced)
- **Promoted by server admins** or through special quests
- Access to **A** category skills (levels 25-50)
- Much more powerful abilities
- Can still use all Basic and Learnable skills

### **Legendary Access**
- Requires **Awoken class** + **magic.legendary permission**
- **Map-found skills only** - cannot be learned normally
- Server admin decides how players obtain these

## 🛠️ Server Setup

### For Server Admins:
1. **Set Permissions**:
   - `magic.basic: true` (default for all players)
   - `magic.awoken: false` (promote worthy players)
   - `magic.legendary: false` (special events only)

2. **Class Promotion**:
   - `/magic setclass <player> awoken` - Promote deserving players
   - `/magic givexp <player> <amount>` - Reward quests/events

3. **Legendary Skills**:
   - Place in special dungeon/quest rewards
   - Give `magic.legendary` permission for map completion
   - Create special events around these ultimate abilities

### Progression Tips:
- **Early Game**: Master L-category skills (levels 1-10)
- **Mid Game**: Learn B-category skills, work toward Awoken promotion
- **Late Game**: Awoken class progression (A-category skills)
- **End Game**: Hunt for legendary skills on special maps

This creates a natural progression system where players start with basic skills and gradually unlock more powerful abilities through gameplay!