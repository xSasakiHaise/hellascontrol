# HellasControl Website Integration Guide

This guide documents how the HellasControl mod talks to the licensing
services that live on your website (currently the WordPress instance at
`web.hephaestus-forge.cc`). Use it when updating the site-side hooks or
building new automation around the licensing workflow so you do not break
existing servers or clients.

## High-Level Flow

1. The server boots and loads a cached licence record from
   `config/hellas/license.json`.
2. If the cache is missing or invalid, the server POSTs the licence data to
   the website verification endpoint and stores the response locally for later
   runs.
3. The first client that joins performs a ping/pong handshake to learn whether
   the server is licensed. Unlicensed servers cause the client to disconnect
   with a descriptive message provided by the licence cache.
4. Sidemods and admin commands read the cached entitlements and metadata to
   decide which features to expose.

The sections below spell out the expectations for each step and the data that
must be provided by the website integration.

## Server Licence Cache

- `LicenseManager.initialize` looks for `config/hellas/license.json` when the
  dedicated server starts, creating `config/hellas/` if needed and deserialising
  the JSON into the in-memory cache.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseManager.java†L16-L37】
- The JSON stores `licenseId`, `message`, `expires`, `entitlements[]`, and an
  optional `serverUrl`. Invalid or unreadable files become a negative cache with
  a human-readable error message that will be shown to players.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseCache.java†L11-L44】
- When `LicenseManager.verifyServer` is called, the cache is returned as-is if
  it is already valid. Otherwise, the server asks the website to refresh the
  data and replaces the cache with the new response when the call succeeds.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseManager.java†L39-L47】

### Machine Binding

To help the website tie licences to a specific machine, the server persists a
UUID inside `config/hellas/machine.id`. The helper will reuse the value on
subsequent runs and only falls back to an in-memory UUID if the file cannot be
written.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L62-L104】

## Website Verification Endpoint

The server contacts the WordPress REST endpoint at
`https://web.hephaestus-forge.cc/wp-json/hellas/v1/license/verify` using
`LicenseServerClient.verifyRemote`.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L15-L82】 The request is a JSON POST
containing:

- `licenseId`: trimmed value from the local cache (empty strings are skipped).
- `machineId`: the persisted UUID described above.
- `version`: optional metadata; currently hard-coded to `1.0.0` and can be used
  for compatibility logic on the website.

Your endpoint must return an object matching `LicenseResponse`:

```json
{
  "status": "valid",
  "licenseId": "HC-...",
  "message": "Custom disconnect message",
  "expires": "2024-12-31",
  "entitlements": ["forms", "garden"],
  "signature": "..." // optional, currently ignored
}
```

- `status` should be one of `valid`, `expired`, `revoked`, `not_found`, or
  `mismatch`. Only `valid` enables gameplay; everything else is treated as an
  invalid licence.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseResponse.java†L6-L17】【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseCache.java†L33-L36】
- `message` is displayed to users when the server is blocked and should explain
  the remediation steps (for example, “Please renew your HellasControl licence”).
- `entitlements` drive feature gating for sidemods and admin tooling.

If the endpoint returns a non-200 HTTP status, the mod logs the response body
for debugging and keeps the previous cache. Network or parse errors result in an
empty `Optional`, leaving the licence invalid until the next successful
verification.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L83-L98】

## Server Start-Up Hooks

`HellasControl.onServerStart` orchestrates the licence lifecycle during a
Forge dedicated-server boot.【F:src/main/java/com/xsasakihaise/hellascontrol/HellasControl.java†L55-L85】 The sequence is:

1. Resolve the server root and load the licence cache (as described above).
2. Call `LicenseEnforcer.enforceServerLicense`, which runs the remote check and
   records whether the server is considered licensed.【F:src/main/java/com/xsasakihaise/hellascontrol/enforcement/LicenseEnforcer.java†L10-L18】
3. Emit a warning to the console if the licence is still invalid so operators
   know why clients will refuse to connect.【F:src/main/java/com/xsasakihaise/hellascontrol/HellasControl.java†L79-L85】

The boolean flag maintained by `LicenseEnforcer` is the authoritative source for
later handshake and sidemod checks.

## Client Handshake and Enforcement

- `NetworkHandler.register` sets up a SimpleChannel with protocol version `1`
  and registers two packets: `ModPing` (client → server) and `ModPong`
  (server → client).【F:src/main/java/com/xsasakihaise/hellascontrol/network/NetworkHandler.java†L17-L34】
- When the server receives a ping, it responds with whether HellasControl is
  present and whether the licence is valid, along with the cached message.
  Clients store the result in `ClientModState` and immediately call
  `ClientEnforcer.checkAndDisconnectIfNeeded` to drop the connection if the
  server is unlicensed.【F:src/main/java/com/xsasakihaise/hellascontrol/network/NetworkHandler.java†L36-L57】【F:src/main/java/com/xsasakihaise/hellascontrol/ClientModState.java†L1-L16】【F:src/main/java/com/xsasakihaise/hellascontrol/client/ClientEnforcer.java†L10-L26】
- If the disconnect path runs, the message from the licence cache is displayed
  verbatim on the “Disconnected” screen. Make sure the website returns user-
  friendly copy for non-valid statuses.

## Sidemod and Admin Hooks

- Server-side integrations should call `HellasControl.requireEntitlement` or the
  convenience wrapper `CoreCheck.verifyEntitled` during startup to make sure the
  necessary entitlements are present. Missing entitlements throw an exception,
  preventing the sidemod from enabling incomplete features.【F:src/main/java/com/xsasakihaise/hellascontrol/HellasControl.java†L87-L109】【F:src/main/java/com/xsasakihaise/hellascontrol/api/CoreCheck.java†L13-L28】
- Admin commands `/hellas control version`, `/hellas control dependencies`, and
  `/hellas control features` read from `HellasControlInfoConfig`, which is
  populated at startup. Keep the bundled defaults up to date when adding new
  features so server operators can verify the integration from in game.【F:src/main/java/com/xsasakihaise/hellascontrol/HellasControl.java†L28-L53】【F:src/main/java/com/xsasakihaise/hellascontrol/commands/HellasControlVersionCommand.java†L1-L60】【F:src/main/java/com/xsasakihaise/hellascontrol/commands/HellasControlDependenciesCommand.java†L1-L36】【F:src/main/java/com/xsasakihaise/hellascontrol/commands/HellasControlFeaturesCommand.java†L1-L38】

## Operational Checklist for Website Changes

Whenever you alter the website integration, confirm the following:

1. The REST endpoint still accepts POST requests with the fields described
   above and responds within the configured timeouts (5s connect / 7s read).
2. The response schema has not removed required fields (`status`, `message`,
   `entitlements`) and continues to send actionable disconnect messages.
3. Licences that should be invalid return non-`valid` statuses so that clients
   disconnect immediately.
4. Any new entitlements you introduce are also documented in the release notes
   and defaults so sidemods can gate their features correctly.
5. Dedicated-server boots succeed with a valid licence, and a client connecting
   to an intentionally unlicensed server sees the updated messaging.
