# HellasControl

HellasControl is the central licensing and compliance mod for the Hellas suite of sidemods.
It boots alongside every server to load license metadata, verify entitlements with the Hellas
licensing backend, and exposes a client/server handshake so that players only connect to
properly licensed environments.

## Feature Overview
- **Server license bootstrap** – loads `config/hellas/license.json`, verifies it against the
  Hellas licensing API, and caches entitlement metadata for other mods.
- **Live enforcement** – the `LicenseEnforcer` runs on server start and its state is consumed by
  Forge event listeners as well as the network handshake to block unlicensed servers.
- **Client handshake & disconnect flow** – clients automatically send a `ModPing`, receive
  `ModPong` with licensing state, and disconnect with a descriptive message when the server is
  missing HellasControl or a valid license.
- **Sidemod entitlement API** – lightweight helpers such as `HellasAPIControlGarden.verify()` make
  it easy for Hellas sidemods to assert both core presence and entitlement keys during startup.
- **Info config distribution** – `HellasControlInfoConfig` loads a descriptive JSON payload that can
  be surfaced by launchers or admin tools to describe the Hellas environment (versions,
  dependencies, feature highlights).

## Technical Overview
- **Entry point (`HellasControl`)** – registers Forge lifecycle hooks, loads
  `HellasControlInfoConfig`, starts the network channel, and orchestrates server license
  initialization in `onServerStart`.
- **License stack (`license` package)** – `LicenseManager` reads/writes the cache, `LicenseServerClient`
  talks to the WordPress REST endpoint, `LicenseCache` holds immutable results, and `LicenseResponse`
  mirrors the JSON schema.
- **Enforcement (`enforcement` package)** – `LicenseEnforcer` runs verification and shares the
  boolean state, while `LicenseEvents` hooks the Forge server-start event to log outcomes.
- **Networking (`network` + `client`)** – `NetworkHandler` registers the `ModPing`/`ModPong`
  packets, `ClientHandshake` emits the ping after login, and `ClientEnforcer` disconnects the
  player if the pong reports an invalid state. `ClientModState` holds the results for GUI checks.
- **API surface (`api` package)** – `CoreCheck` and the `HellasAPI*` helpers expose a stable way for
  sidemods to ensure HellasControl is available and that their entitlement (garden, wilds, elo,
  etc.) is present.

## Extending HellasControl
- **Adding a new sidemod entitlement** – create a new helper in
  `com.xsasakihaise.hellascontrol.api.sidemods` mirroring the existing classes
  (e.g., `HellasAPIControlAudio`). Have your sidemod call `verify()` during setup so it fails fast
  when the entitlement is missing.
- **Checking license state in code** – call `HellasControl.hasEntitlement("yourKey")` for optional
  features or `HellasControl.requireEntitlement("yourKey")` if the mod must not run without it.
- **Customizing the info config** – edit or generate `config/hellas/patcher/hellas_patcher_info.json`
  on the server, or use `HellasControl.infoConfig` to expose additional metadata to GUIs.

## Dependencies & Environment
- Targets **Minecraft 1.16.5** with **Forge 36.2.42** (see `build.gradle`).
- Designed to run alongside the broader Hellas suite (HellasWilds, HellasGarden, etc.) but those
  modules must declare their entitlements individually.
- No explicit Pixelmon dependency in this module, though most sidemods that call these APIs are
  Pixelmon-focused.

## Migration Notes
- The licensing stack depends on Forge's `FMLServerStartingEvent` and SimpleChannel networking
  APIs from 1.16.5; migrating to newer Minecraft/Forge versions means updating lifecycle hooks and
  packet registration semantics.
- `LicenseServerClient` assumes a WordPress REST payload structure; if the backend changes, ensure
  `LicenseResponse` mirrors the new schema and adjust signature validation if added.
- Client enforcement uses `ClientPlayNetHandler.onDisconnect`—Minecraft networking refactors in
  later versions may require new disconnect mechanics.
