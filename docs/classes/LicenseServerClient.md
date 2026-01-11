# LicenseServerClient

`LicenseServerClient` (`src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java`) calls the remote license verification endpoint.

## Responsibilities
- Posts license and machine ID to the API.
- Persists a stable machine ID under `config/hellascontrol/machine.id`.
