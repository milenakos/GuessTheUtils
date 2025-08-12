# GuessTheUtils 0.9.6

## Shortcut Reminder Rewrite
The shortcut reminder module has been completely reworked, and now automatically pulls translation data from an external source (GTB Platform).
This means that the shortcut list will not be included with GuessTheUtils anymore, instead updating independently of the mod.
Due to a large amount of multiwords using non-latin characters, a new option has been added to hide those from the reminder messages.
Big thanks to [zmh-program](https://github.com/zmh-program) for making his database easily accessible.

## Config Location Change
From now on, all GuessTheUtils related files will be located in `<minecraft_root>/config/guesstheutils`.
This is a breaking change, so your previous config will be reset. Sorry!
To manually transfer your old config, rename `<minecraft_root>/config/guesstheutils.json` to `config.json`, and put it into the `<minecraft_root>/config/guesstheutils` directory.

## Bug Fixes
- Fixed a bug where certain lines from `/g member <name>` would be considered as player messages, resulting in the tracker becoming very confused (thanks @potato1075)
- Improved item hider behavior to hopefully prevent it from being active outside of GTB (thanks @potato1075)
- Fixed rare cases of tracker crashing after receiving incomplete data from the server at the start of the game

## Improvements
- Changed theme length display to individual word lengths
- Added a configurable offset for current builder's scoreboard line for improved visibility
- Added options for scoreboard background opacity and highlight strength
- Added an easy way to open replays folder by clicking on the replay saved message, or executing `/guesstheutils replay open`