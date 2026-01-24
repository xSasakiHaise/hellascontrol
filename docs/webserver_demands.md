# HellasControl Webserver Demands

This document describes **everything the webserver must provide** to let
HellasControl verify licenses, gate entitlements, and drive client messaging.
It is written so a developer can implement the WordPress plugin (or any
compatible service) using this document alone.

## 1) Required REST Endpoint

### Endpoint URL

HellasControl calls the WordPress REST endpoint hard-coded in the mod:

```
https://web.hephaestus-forge.cc/wp-json/hellas/v2/license/verify
```

The client uses `LicenseServerClient.verifyRemote()` to POST JSON to this URL
every time it needs to validate a license (initial startup and hourly refresh
while the server remains online).【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L18-L94】【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseManager.java†L39-L160】

### HTTP Requirements

| Requirement | Value |
| --- | --- |
| Method | `POST` |
| Content-Type | `application/json` |
| Connect timeout | 5 seconds |
| Read timeout | 7 seconds |
| Success response | HTTP 200 only (non-200 is treated as failure) |

If the endpoint returns a non-200 status, HellasControl logs the error body (if
available) and **keeps the previous local cache** unchanged.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L55-L98】

### Request Body (JSON)

The request body includes the following fields:

```json
{
  "licenseId": "HC-....",
  "machineId": "uuid",
  "version": "1.0.0"
}
```

Field details:

| Field | Required | Meaning |
| --- | --- | --- |
| `licenseId` | Yes | Trimmed license identifier read from `config/hellascontrol/license.txt`. Empty string skips remote verification. |
| `machineId` | Yes | Stable UUID generated on first run and persisted to `config/hellascontrol/machine.id`. |
| `version` | Optional | Currently hard-coded to `"1.0.0"`; can be used for server-side compatibility logic. |

Behavioral notes:

- If `licenseId` is missing or empty, the mod **does not call** the webserver
  and returns `Optional.empty()` instead.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L35-L44】
- `machineId` is created on first use and reused for later verification so the
  service can bind a license to a specific host.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L62-L104】

## 2) Required Response Schema

The webserver must respond with JSON that maps to `LicenseResponse`. A minimal,
valid response looks like this:

```json
{
  "status": "valid",
  "licenseId": "HC-...",
  "message": "Custom disconnect message",
  "expires": "2024-12-31",
  "entitlements": ["forms", "garden"],
  "currentToken": "token-string",
  "nextToken": "next-token-string",
  "currentValidFrom": "2024-11-01T00:00:00Z",
  "currentValidTo": "2024-12-01T00:00:00Z",
  "nextValidFrom": "2024-12-01T00:00:00Z",
  "nextValidTo": "2025-01-01T00:00:00Z"
}
```

### Required Fields

| Field | Required | Notes |
| --- | --- | --- |
| `status` | **Yes** | Determines validity. Only `"valid"` enables the server. |
| `message` | **Yes** | Human-readable message shown to players if blocked. |
| `entitlements` | **Yes** | List of entitlement keys used to unlock Hellas sidemods. |

The mod treats **anything not equal to `"valid"`** (case-insensitive) as an
invalid license. Valid statuses are: `valid`, `expired`, `revoked`,
`not_found`, `mismatch` (the last four block the server).【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseResponse.java†L6-L30】【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseCache.java†L58-L83】

### Optional Fields

These are accepted and propagated if present:

| Field | Usage |
| --- | --- |
| `licenseId` | Stored in cache for visibility/logging. |
| `expires` | Stored in cache and used for server fingerprinting. |
| `currentToken` / `nextToken` | Used for token rotation logic and client display. |
| `currentValidFrom` / `currentValidTo` | ISO-8601 timestamps used to validate `currentToken`. |
| `nextValidFrom` / `nextValidTo` | ISO-8601 timestamps used to validate `nextToken`. |

Token timestamps are parsed with `Instant.parse(...)`, so supply UTC timestamps
in ISO-8601 format (example: `"2024-11-01T00:00:00Z"`). Invalid formats are
silently ignored and treated as missing timestamps.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseCache.java†L73-L169】

### Error Handling Expectations

If the response is missing or malformed (e.g., null JSON or missing `status`),
the mod treats the verification as failed and keeps the previous cache.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L83-L98】

## 3) License Cache & Local Files

The webserver does not manage these files, but the plugin must understand how
they are used:

- `config/hellascontrol/license.txt` holds the license ID. This is the **only**
  local input sent to the webserver during verification.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseManager.java†L28-L125】
- `config/hellascontrol/machine.id` stores a stable UUID used as `machineId`.
- Legacy servers may still store `config/hellas/license.json`; if present it is
  read as a cache fallback on first run.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseManager.java†L58-L95】

The webserver should **not** expect the mod to send any additional data beyond
`licenseId`, `machineId`, and `version`.

## 4) License Enforcement Hooks

The webserver must supply data that supports these enforcement hooks:

### Server Startup Gate

At server start, HellasControl verifies the license; if invalid, the server is
shut down and clients never connect.【F:src/main/java/com/xsasakihaise/hellascontrol/HellasControl.java†L92-L139】【F:src/main/java/com/xsasakihaise/hellascontrol/enforcement/LicenseEnforcer.java†L22-L42】

### Entitlement Gate

Entitlements returned by the webserver are compared to installed Hellas
side-mods. If an installed Hellas mod does not have a matching entitlement, the
server shuts down with an entitlement mismatch message.【F:src/main/java/com/xsasakihaise/hellascontrol/enforcement/LicenseEnforcer.java†L44-L83】

**Mapping rule:** each mod with ID `hellas<name>` (excluding `hellascontrol`)
requires entitlement `<name>` (case-insensitive). For example, `hellasforms`
requires `forms`.

### Client Handshake

When a client connects, it sends a `ModPing` and receives `ModPong` with the
server’s license status and message. If `status != valid`, the client is
disconnected and shown the `message` from the response. This makes the
webserver’s `message` field user-facing and critical for support workflows.【F:src/main/java/com/xsasakihaise/hellascontrol/network/NetworkHandler.java†L36-L88】【F:src/main/java/com/xsasakihaise/hellascontrol/network/ModPong.java†L17-L110】

## 5) Field-By-Field Contract Summary

Implement the plugin so each request/response obeys the following contract:

### Request (server → webserver)

| Field | Required | Example | Notes |
| --- | --- | --- | --- |
| `licenseId` | Yes | `"HC-1234"` | Empty/absent skips verification. |
| `machineId` | Yes | `"550e8400-e29b-41d4-a716-446655440000"` | Stable UUID per server. |
| `version` | Optional | `"1.0.0"` | Reserved for compatibility checks. |

### Response (webserver → server)

| Field | Required | Example | Notes |
| --- | --- | --- | --- |
| `status` | **Yes** | `"valid"` | Only `valid` is accepted. |
| `message` | **Yes** | `"Please renew your license"` | Shown to server admins and players. |
| `entitlements` | **Yes** | `["forms", "garden"]` | Used for sidemod gating. |
| `licenseId` | Optional | `"HC-1234"` | Returned for logging/display. |
| `expires` | Optional | `"2024-12-31"` | Used in cache fingerprinting. |
| `currentToken` | Optional | `"token"` | Current token for rotation logic. |
| `nextToken` | Optional | `"next-token"` | Next token for rotation logic. |
| `currentValidFrom` | Optional | `"2024-11-01T00:00:00Z"` | ISO-8601. |
| `currentValidTo` | Optional | `"2024-12-01T00:00:00Z"` | ISO-8601. |
| `nextValidFrom` | Optional | `"2024-12-01T00:00:00Z"` | ISO-8601. |
| `nextValidTo` | Optional | `"2025-01-01T00:00:00Z"` | ISO-8601. |

## 6) Implementation Checklist for WP Plugin

1. Register a `POST` route at `/wp-json/hellas/v2/license/verify`.
2. Read JSON body and validate `licenseId` + `machineId`.
3. Verify license status, entitlements, and (optional) token rotation metadata.
4. Return HTTP 200 with JSON body matching the schema above.
5. Return a **non-`valid`** `status` and a support-friendly `message` whenever
   the server should be blocked.
6. Keep responses fast (< 7 seconds) to stay within client timeouts.

## 7) Operational Notes

- The mod retries verification hourly while the server is up; your endpoint
  should be stable under repeated calls.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseManager.java†L75-L160】
- If the webserver is unreachable or the response is invalid, HellasControl
  keeps the previous cache, so delayed changes may not immediately propagate
  until the next successful verification.【F:src/main/java/com/xsasakihaise/hellascontrol/license/LicenseServerClient.java†L55-L98】
