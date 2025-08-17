# GuessTheUtils 0.9.6
## New Stuff
- Shortcuts reminder now uses the same list as https://gtb.zmh.me/themes, updating automatically and independently of the mod itself. (thanks zmhprogram)
- Added setting to filter CJK or non-ASCII shortcuts from reminder messages.
- Added `/translate <language> <theme>` command to get approved translations of a given theme. (thanks sophie.fox)
- Added `/qgtb` command to quickly queue/requeue GTB. (thanks sophie.fox)
- Added `/lrj` command to automatically leave and rejoin.
- Changed theme length display to show individual word lengths.
- Changed the location where the mod stores its configs and files to `<minecraft_root>/config/guesstheutils`. Your settings will be reset. Sorry!
- Score tracker is now more lenient when rejoining, only disabling if score mismatches are actually detected. (thanks sophie.fox)
- Added options for scoreboard background opacity and highlight strength.
- Added an easy way to open replays folder by clicking on the replay saved message, or executing `/guesstheutils replay open`.
## Bug Fixes
- Fixed a bug where `/g member <name>` would cause tracker to crash mid-game. (thanks potato1075)
- Improved item hider behavior to hopefully prevent it from being active outside of GTB. (thanks potato1075)
- Fixed a rare case of tracker crashing after receiving incomplete game setup data from the server.
