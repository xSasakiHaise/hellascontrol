# ClientHandshakeTicker

`ClientHandshakeTicker` (`src/main/java/com/xsasakihaise/hellascontrol/client/ClientHandshakeTicker.java`) handles client tick-based handshake timeouts.

## Responsibilities
- Tracks elapsed time after a handshake ping.
- Marks timeouts and disconnects when a server never responds.
