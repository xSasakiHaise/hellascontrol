# ClientModState

`ClientModState` (`src/main/java/com/xsasakihaise/hellascontrol/ClientModState.java`) tracks the latest server handshake results on the client.

## Responsibilities
- Stores whether the remote server runs HellasControl and is licensed.
- Tracks handshake timing and timeout state.
- Provides message text used by client disconnect flows.
