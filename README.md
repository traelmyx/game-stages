# Game Stages (Unofficial 1.21.1 Port)

Unofficial NeoForge 1.21.1 port of [Game Stages](https://github.com/Darkhax-Minecraft/Game-Stages) by Darkhax.

Game Stages is a framework for custom progression. Players unlock named stages, and other mods, scripts and advancements can lock content behind them. The original stopped at 1.20.1, and Darkhax [said a 1.21.1 version isn't planned](https://github.com/Darkhax-Minecraft/Game-Stages/issues/89), so here it is. This port isn't affiliated with Darkhax. Please report problems here, not on the original repo.

Based on the original `1.20.1` branch at [5444586](https://github.com/Darkhax-Minecraft/Game-Stages/commit/5444586cb26dbeb6c4a455558089070e710cfe2e).

## Features

- `/gamestage add|remove|check|info|clear|all|reload`
- `gamestages:has_stage` advancement trigger
- CraftTweaker support (`player.addGameStage`, `mods.gamestages.StageHelper`, ...)
- Same mod id, API, config files and save format as the original, so addons and scripts keep working
- No Bookshelf dependency

## Changes from 1.20.1

- `has_stage` advancements now check their own stage. Before, they completed for any stage.
- Namespaced stage names (`example:stage`) work in commands.
- The stage sync no longer errors for clients that can't receive it.

## Building

```
./gradlew build
```

This also runs the game tests.

## License

LGPL-2.1, same as the original.
