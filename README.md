# Pace Client (Fabric, Minecraft 1.21.11)

## Getting the .jar (no local setup)
1. Create a new GitHub repo and upload everything in this folder (keep the `.github` folder).
2. Open the repo's **Actions** tab. The `build` workflow runs on every push (or press "Run workflow").
3. When it finishes, download the `pace-client-jar` artifact. Use `pace-client-0.1.0.jar` (NOT the `-sources` one).
4. Put it in `.minecraft/mods` with Fabric Loader 0.18.1+ and Fabric API for 1.21.11.

## Building locally instead
Install JDK 21 and Gradle 9.3 (or generate a wrapper with `gradle wrapper`), then run `gradle build`.
The jar lands in `build/libs/`.

## Using it
- RIGHT SHIFT opens the ClickGUI.
- Left click = toggle. Right click = settings. Key row: click, press a key; Backspace clears.
- Hover a module + Backspace also clears its bind.
- Action modules (PearlThrow, FriendKey): bind a key; pressing it performs the action.
- Config saves to `config/pace-client.json`.

## Modules
Combat: AutoCrystal, CrystalMacro, AnchorMacro, AutoAnchor, AutoTotem, HoverTotem, Surround, AutoArmor, PearlThrow
Client: ClickGUI (themes), HUD (watermark, module list, totem count, target HUD), ESP, FriendKey (friends are skipped by targeting)

## If the build fails
Version-sensitive calls are isolated in `util/Compat.java`; GUI input signatures are in `gui/ClickGuiScreen.java`.
Paste the compiler errors back to Claude and they can be fixed quickly.
