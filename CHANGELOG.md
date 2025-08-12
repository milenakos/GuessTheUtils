# GuessTheUtils 0.9.6

## Shortcuts List Changes
- Changed `Drum Sticks` theme to `baget` shortcut
- Removed `Theatre` theme from `cine` shortcut
- Added `supermarkt` shortcut for `Supermarket` theme
- Added `cheminee` shortcut for `Chimney` and `Fireplace` themes
- Changed `Grappling Hook` theme to `Hak` shortcut
- Changed `Fruit Punch` theme to `ponch` shortcut
- Added `Wire` theme to `cavo` shortcut
- Changed `cela` shortcut to `cella`
- Added `Iron` theme to `va` shortcut
- Added `Cyborg` theme to `robot` shortcut
- Added `Meadow` theme to `prato` shortcut
- Changed `dom` shortcut to `ev`

## Bug Fixes
- Fixed a bug where certain lines from `/g member <name>` would be considered as player messages, resulting in the tracker becoming very confused (thanks @potato1075)
- Improved item hider behavior to hopefully prevent it from being active outside of GTB (thanks @potato1075)
- Fixed rare cases of tracker crashing after receiving incomplete data from the server at the start of the game

## Improvements
- Changed theme length display to individual word lengths
- Added a configurable offset for current builder's scoreboard line for improved visibility
- Added options for scoreboard background opacity and highlight strength
- Added an easy way to open replays folder by clicking on the replay saved message, or executing `/guesstheutils replay open`