# HellasAPIControlElo

`HellasAPIControlElo` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlElo.java`) is the guard helper for the Hellas Elo sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `elo` entitlement via `CoreCheck.verifyEntitled("elo")`.

## When to call
Invoke `HellasAPIControlElo.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
