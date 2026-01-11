# HellasAPIHellasWilds

`HellasAPIHellasWilds` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIHellasWilds.java`) is the guard helper for the Hellas Wilds sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `wilds` entitlement via `CoreCheck.verifyEntitled("wilds")`.

## When to call
Invoke `HellasAPIHellasWilds.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
