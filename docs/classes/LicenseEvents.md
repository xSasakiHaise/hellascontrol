# LicenseEvents

`LicenseEvents` (`src/main/java/com/xsasakihaise/hellascontrol/enforcement/LicenseEvents.java`) is a NeoForge event subscriber that logs server startup and defers enforcement to `HellasControl`.

## Responsibilities
- Receives server-start lifecycle events on the Forge/NeoForge bus.
- Confirms enforcement is handled by the main entry point.
