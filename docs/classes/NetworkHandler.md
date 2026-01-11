# NetworkHandler

`NetworkHandler` (`src/main/java/com/xsasakihaise/hellascontrol/network/NetworkHandler.java`) owns the handshake network channel and packet handlers.

## Responsibilities
- Registers `ModPing` and `ModPong` packets on a `SimpleChannel`.
- Responds to pings with license status and updates client state on pongs.
