# HellasAPIControlDeck

`HellasAPIControlDeck` (`src/main/java/com/xsasakihaise/hellascontrol/api/sidemods/HellasAPIControlDeck.java`) is the guard helper for the Hellas Deck sidemod.

## Responsibilities
- Confirms HellasControl is installed via `CoreCheck.verifyCoreLoaded()`.
- Requires the `deck` entitlement via `CoreCheck.verifyEntitled("deck")`.

## When to call
Invoke `HellasAPIControlDeck.verify()` during your mod constructor or common setup so unlicensed servers fail fast.
