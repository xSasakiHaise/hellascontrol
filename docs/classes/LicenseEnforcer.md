# LicenseEnforcer

`LicenseEnforcer` (`src/main/java/com/xsasakihaise/hellascontrol/enforcement/LicenseEnforcer.java`) performs license validation and exposes the result.

## Responsibilities
- Calls `LicenseManager.verifyServer()` and caches the license state.
- Provides `isServerLicensed()` for handshake and enforcement logic.
