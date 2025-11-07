# Integrated Mods Compatible with HellasControl

This registry tracks third-party mods that officially integrate with HellasControl `v2.0.0` on Minecraft `1.16.5`. Only record mods that have been verified against the current release.

## Current Integrations

| Mod Name | Maintainer | Minimum Mod Version | Tested HellasControl Version | Integration Notes |
| --- | --- | --- | --- | --- |
| Control Audio | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlAudio`) | v2.0.0 | Calls `CoreCheck.verifyEntitled("audio")`; requires the Audio entitlement. |
| Control Deck | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlDeck`) | v2.0.0 | Validates the Deck entitlement before sidemod features are enabled. |
| Control Elo | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlElo`) | v2.0.0 | Gated by the Elo entitlement for ranked systems. |
| Control Forms | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlForms`) | v2.0.0 | Requires the Forms entitlement during startup checks. |
| Control Garden | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlGarden`) | v2.0.0 | Depends on the Garden entitlement before enabling content. |
| Control Helper | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlHelper`) | v2.0.0 | Ensures Helper entitlement access for automation helpers. |
| Control Mineralogy | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlMineralogy`) | v2.0.0 | Locks features behind the Mineralogy entitlement. |
| Control Patcher | HellasControl Team | Bundled (`api/sidemods/HellasAPIControlPatcher`) | v2.0.0 | Checks the Patcher entitlement during verification. |
| Hellas Battlebuddy | HellasControl Team | Bundled (`api/sidemods/HellasAPIHellasBattlebuddy`) | v2.0.0 | Calls `CoreCheck.verifyEntitled("battlebuddy")`; requires the Battlebuddy entitlement. |
| Hellas Library | HellasControl Team | Bundled (`api/sidemods/HellasAPIHellasLibrary`) | v2.0.0 | Calls `CoreCheck.verifyEntitled("library")`; requires the Library entitlement. |
| Hellas Textures | HellasControl Team | Bundled (`api/sidemods/HellasAPIHellasTextures`) | v2.0.0 | Calls `CoreCheck.verifyEntitled("textures")`; requires the Textures entitlement. |
| Hellas Wilds | HellasControl Team | Bundled (`api/sidemods/HellasAPIHellasWilds`) | v2.0.0 | Calls `CoreCheck.verifyEntitled("wilds")`; requires the Wilds entitlement. |

## How to Update This List
- Confirm the mod passes the dedicated-server and client-handshake checks described in the integration guides before adding it here.
- Record the exact mod version and the HellasControl build used during testing.
- Use the Integration Notes column to capture relevant entitlements, commands, or configuration steps.
- Remove or archive rows when a mod drops support for the current HellasControl release line.
