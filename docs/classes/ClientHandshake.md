# ClientHandshake

`ClientHandshake` (`src/main/java/com/xsasakihaise/hellascontrol/client/ClientHandshake.java`) initiates the client-side handshake after login.

## Responsibilities
- Sends a `ModPing` packet when the client joins a remote server.
- Clears handshake state on logout.
