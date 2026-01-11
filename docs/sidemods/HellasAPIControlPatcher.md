# HellasAPIControlPatcher

`HellasAPIControlPatcher` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlPatcher.java`) is the guard helper for the Hellas Patcher sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `patcher` entitlement via `CoreCheck.verifyEntitled("patcher")`.

## When to call
Invoke `HellasAPIControlPatcher.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
