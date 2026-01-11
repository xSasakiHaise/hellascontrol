# HellasAPIHellasTextures

`HellasAPIHellasTextures` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIHellasTextures.java`) is the guard helper for the Hellas Textures sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `textures` entitlement via `CoreCheck.verifyEntitled("textures")`.

## When to call
Invoke `HellasAPIHellasTextures.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
