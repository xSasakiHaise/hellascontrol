# HellasControl Integration Tasks

Use this checklist when preparing a HellasControl release (currently `v2.0.0` for Minecraft `1.16.5`) to ensure third-party mods keep working against the published hooks.

## Keep the Public Hooks Stable
- Preserve the behaviour of `com.xsasakihaise.hellascontrol.HellasControl#hasEntitlement` and `#requireEntitlement` so sidemods can gate their features on the same entitlements as the base mod.
- Announce and document any changes to the ping/pong handshake implemented in `com.xsasakihaise.hellascontrol.network.NetworkHandler` (protocol string, payload fields, or disconnect rules).
- Ensure the client helpers in `com.xsasakihaise.hellascontrol.ClientModState` continue to expose the server licence status that integrations rely on.
- Keep the admin commands registered in `HellasControl#onRegisterCommands` (`/hellascontrolversion`, `/hellascontroldependencies`, `/hellascontrolfeatures`) available so server operators can confirm integration readiness.

## Ship Reference Materials with the Release
- Bundle or publish an up-to-date example `config/hellas/license.json` that demonstrates the JSON structure consumed by `LicenseManager` and the message shown to unlicensed clients.
- Update the `HellasControlInfoConfig` defaults when new informational fields are introduced so integrators see consistent display data.
- Provide changelog notes describing new or removed licence entitlements so dependent mods can adjust their checks.

## Validate Dedicated-Server Behaviour
- Spin up a dedicated Forge 1.16.5 server with the release candidate and confirm `LicenseManager.initialize` reads the licence file from the `config/hellas` directory.
- Connect a development client to verify the handshake reported by `NetworkHandler` and that `ClientEnforcer` only disconnects players when the server is actually unlicensed.
- Exercise any sample sidemod that calls `HellasControl.requireEntitlement` to confirm integration points remain intact.

## Communicate Integration Updates
- Document any handshake protocol changes and the minimum compatible client build in the release notes.
- Highlight new commands, config fields, or entitlements that integrations should adopt.
- Remind integrators to retest against the release candidate and report compatibility findings for inclusion in the registry.
