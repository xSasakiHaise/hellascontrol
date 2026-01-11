# HellasAPIHellasLibrary

`HellasAPIHellasLibrary` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIHellasLibrary.java`) is the guard helper for the Hellas Library sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `library` entitlement via `CoreCheck.verifyEntitled("library")`.

## When to call
Invoke `HellasAPIHellasLibrary.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
