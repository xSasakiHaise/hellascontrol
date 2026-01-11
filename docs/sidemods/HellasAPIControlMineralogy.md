# HellasAPIControlMineralogy

`HellasAPIControlMineralogy` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlMineralogy.java`) is the guard helper for the Hellas Mineralogy sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `mineralogy` entitlement via `CoreCheck.verifyEntitled("mineralogy")`.

## When to call
Invoke `HellasAPIControlMineralogy.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
