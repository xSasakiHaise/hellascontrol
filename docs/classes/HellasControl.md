# HellasControl

`HellasControl` (`src/main/java/com/xsasakihaise/hellascontrol/HellasControl.java`) is the core mod entry point.

## Responsibilities
- Registers lifecycle listeners for common setup and server startup.
- Initializes licensing (license file creation, cache initialization, enforcement).
- Registers the client/server handshake network channel.
- Emits diagnostics about loaded mods and world load events.
