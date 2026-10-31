# BadgerProtection

Spawn protection in the style of [jProtection](https://dev.bukkit.org/projects/mcspawnprot), the MCPVP spawn plugin. The spawn area is a cuboid you select in-game. Protection is a flag, not a safe zone you can walk back into.

Built for CraftBukkit / Spigot 1.10.

## How protection works

- A player who spawns, respawns, or uses `/spawn` inside the region is protected.
- Walking out of the region removes protection.
- Walking back in does not restore it. That player can still be hit, including inside the region, until they `/spawn`.
- A protected player takes no damage. Hitting them tells you they are protected.
- Hitting someone who is not protected removes your own protection, and that hit lands.
- Blocks inside the region cannot be broken, placed, or filled with buckets unless the player has the admin permission. Explosions do not destroy those blocks.

## `/spawn`

If another player is within `CheckRadius`, `/spawn` waits `TeleportDelay` seconds. The player has to stand still. Looking around does not cancel the warp. Walking does. When the warp finishes, they get protection back.

## Setup

1. Build the plugin and put the jar in `plugins`. `plugin.yml` and `config.yml` need to be at the root of the jar.
2. Restart or `/reload` is not required if you drop it in before startup.
3. In game, as an op:

```
/prot wand
```

Left-click one corner and right-click the other, then:

```
/prot complete
/prot setspawn
```

Stand where `/spawn` should place players before `setspawn`. That spot should be inside the region.

## Commands

| Command | What it does |
| --- | --- |
| `/prot wand` | Gives the selection wand and starts region selection |
| `/prot complete` | Saves the two corners as the spawn region |
| `/prot cancel` | Leaves selection mode and removes the wand |
| `/prot setspawn` | Sets the `/spawn` destination to where you are standing |
| `/prot info` | Shows the region, spawn, and whether you are protected |
| `/prot reload` | Reloads `config.yml` |
| `/spawn` | Warps to spawn and restores protection |

`/prot` can also be run as `/bp` or `/badgerprot`.

## Permissions

| Permission | Default | What it allows |
| --- | --- | --- |
| `badgerprotection.admin` | op | `/prot`, and building inside the region |
| `BadgerProtection.Admin` | op | Same admin access, kept for older permission setups |
| `badgerprotection.spawn` | everyone | `/spawn` |

## Config

`config.yml` is created in the plugin folder on first run. `TeleportDelay` is the wait in seconds. `CheckRadius` is how close another player has to be to force that wait. Messages use `&` color codes. `{PLAYER}` and `{DELAY}` are filled in where those messages use them.
