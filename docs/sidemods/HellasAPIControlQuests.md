# HellasAPIControlQuests

`HellasAPIControlQuests` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlQuests.java`) is the guard helper for the Hellas Quests sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `quests` entitlement via `CoreCheck.verifyEntitled("quests")`.

## When to call
Invoke `HellasAPIControlQuests.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
