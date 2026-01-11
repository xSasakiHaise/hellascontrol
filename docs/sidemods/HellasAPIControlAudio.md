# HellasAPIControlAudio

`HellasAPIControlAudio` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlAudio.java`) is the guard helper for the Hellas Audio sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `audio` entitlement via `CoreCheck.verifyEntitled("audio")`.

## When to call
Invoke `HellasAPIControlAudio.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
