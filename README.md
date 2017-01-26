# BadgerProtection

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

<h2>Summary</h2>
This plugin is a continuation of the once awesome mcpvp-based spawn protection of [jProtection] (http://dev.bukkit.org/bukkit-plugins/mcspawnprot/). I have upgraded the plugin however also by making the selection of the spawn and such by cuboid and not a radius. Have you ever wanted a mcpvp-based spawn protection and everyone wants to charge you for it!? Look no more! BadgerProtection is brought to you by ~ TheWolfBadger

<h2>Get rid of the advertisement</h2>
Yes, this plugin is free, but the plugin does have an advertisement that only shows to players who join for the first time. [https:www.mc-market.org/resources/272] (https:www.mc-market.org/resources/272) is the same plugin, however it gets rid of the BadgerProtection author credit message (The plugin costs $2.50). So instead of your players seeing the following when they first join, BadgerProtection Advertisement Message they will no longer see this since the version does not include it.

![alt text] (http://embed.gyazo.com/5e2f697f5e03cad75095d1f214286ef4.png "BadgerProtection Advertisement Message")

<h2>Help</h2>
[Video Guide] (https://www.youtube.com/watch?v=OxIbBV8J9go)

<h2>Permissions</h2>
BadgerProtection.Admin - This gives the users permission to use the command "/prot".

<h2>Commands</h2>
./prot wand - Go into BadgerProtection selection mode.

./prot setspawn - Set the spawn for the command /spawn.

./prot cancel - Exit the BadgerProtection selection mode.

./prot complete - Complete the BadgerProtection selection mode if you set the positions.

./spawn - Teleport to the BadgerProtection set spawn.

<h2>Config</h2>
```YAML
TeleportDelay: 10 # (Seconds)
CheckRadius: 10 # This is the radius around the player it checks. If there is a player within this radius then they have to wait the TeleportDelay until they can teleport safely.
Messages:
  GainProtection: '&7You have gained BadgerProtection!'
  LostProtection: '&7You have lost BadgerProtection!'
  PlayerHasProtection: '&7The player &d{PLAYER} &7currently has BadgerProtection!'
  PlayersNearby: '&7There are players nearby! You must wait &e{DELAY}&7 seconds to teleport!'
  PreventPlace: '&cYou can not break a block protected by &eBadgerProtection&c!'
  PreventBreak: '&cYou can not place in a Cuboid protected by &eBadgerProtection&c!'
  TeleportCancelled: '&cWarping cancelled!'
```  
