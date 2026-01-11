# HellasAPIControlHelper

`HellasAPIControlHelper` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlHelper.java`) is the guard helper for the Hellas Helper sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `helper` entitlement via `CoreCheck.verifyEntitled("helper")`.

## When to call
Invoke `HellasAPIControlHelper.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
