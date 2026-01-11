# HellasAPIHellasBattlebuddy

`HellasAPIHellasBattlebuddy` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIHellasBattlebuddy.java`) is the guard helper for the Hellas Battlebuddy sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `battlebuddy` entitlement via `CoreCheck.verifyEntitled("battlebuddy")`.

## When to call
Invoke `HellasAPIHellasBattlebuddy.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
