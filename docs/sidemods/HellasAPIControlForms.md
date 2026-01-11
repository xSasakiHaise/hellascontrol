# HellasAPIControlForms

`HellasAPIControlForms` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlForms.java`) is the guard helper for the Hellas Forms sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `forms` entitlement via `CoreCheck.verifyEntitled("forms")`.

## When to call
Invoke `HellasAPIControlForms.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
