# CoreCheck Class Guide

`CoreCheck` (`src/main/java/com/xsasakihaise/hellascontrol/api/CoreCheck.java`) is the
shared guardrail for HellasControl sidemods. It provides small, reusable checks
that sidemod entry points call during construction or setup to ensure:

- the core HellasControl mod is loaded, and
- the active license includes the entitlement a sidemod requires.

## Why it exists

Sidemods should fail fast when they are installed without a valid license. By
centralizing the checks in `CoreCheck`, every sidemod can share the same logic
and log messages, keeping behavior consistent across the ecosystem.

## Key methods

- `verifyCoreLoaded()`: Confirms the HellasControl core module is present before
  sidemods attempt to use shared state.
- `verifyEntitled(String entitlement)`: Ensures the current license grants the
  named entitlement before enabling the sidemod.
