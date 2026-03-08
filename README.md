# TC-Ridecounters

Main command: `/ridecounter`

[Hangar Page](https://hangar.papermc.io/MrTibo1/TC-Ridecounters)

*Depends on*:
- TrainCarts
- BKCommonLib

## Features
1. Display top ridecounters on map displays  
2. Dynamic and flexible custom backgrounds for map displays ([.mcmeta format](https://minecraft.wiki/w/Resource_pack#GUI) for tiling, stretching or nine-slice)
3. TrainCarts signaction to increase the ridecount of every player in a train

```
[+train]
rc
<ride id>
```

## Commands
| Command | Permission | Description |
|---|---|---|
| `/rc display <rideId> [backgroundImage]`| `ridecounters.display` | Create a ridecounter display map |
| `/rc create <rideId> <rideName>`| `ridecounters.create` | Create a new ride |
| `/rc delete <rideId>` | `ridecounters.delete` | Delete an existing ride |
| `/rc total <player> <rideId>`| `ridecounters.total` | View a player's total ridecount |
| `/rc set <player> <rideId> <value>` | `ridecounters.set` | Manually set a player's ridecount |
| `/rc increment <players> <rideId> [--silent]`| `ridecounters.increment` | Increment ridecounts for players |
| `/rc top <rideId> <limit>`| `ridecounters.top` | View the top ridecounts for a ride |
| `/rc list [page]`| `ridecounters.list` | See a list of registered rides |
| `/rc rename <rideId> <name>`| `ridecounters.rename` | Change the main name of a ride, which will display in chat |
| `/rc alternative <rideId> <name>`| `ridecounters.setalternativename` | Set the alternative name, which will display on ridecount displays |

## Gallery
<img width="600" alt="image" src="https://github.com/user-attachments/assets/aba98bf4-d78f-4532-b084-a4cb2e449eeb" />

## Contributors:
- [@Thojs](https://github.com/thojs)

## bStats
TC-Ridecounters is tracked on [bStats](https://bstats.org/plugin/bukkit/TC-Ridecounters/29184)  
You can disable this in the bStats config  
