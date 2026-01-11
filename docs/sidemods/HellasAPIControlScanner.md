# HellasAPIControlScanner

`HellasAPIControlScanner` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlScanner.java`) is the guard helper for the Hellas Scanner sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `scanner` entitlement via `CoreCheck.verifyEntitled("scanner")`.

## When to call
Invoke `HellasAPIControlScanner.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
