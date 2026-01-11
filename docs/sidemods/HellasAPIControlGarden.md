# HellasAPIControlGarden

`HellasAPIControlGarden` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlGarden.java`) is the guard helper for the Hellas Garden sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `garden` entitlement via `CoreCheck.verifyEntitled("garden")`.

## When to call
Invoke `HellasAPIControlGarden.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
