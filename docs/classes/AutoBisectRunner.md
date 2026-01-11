# AutoBisectRunner

`AutoBisectRunner` (`src/main/java/com/xsasakihaise/hellascontrol/bisect/AutoBisectRunner.java`) runs an automated bisect across mod lists to isolate failing mods.

## Responsibilities
- Loads bisect configuration and short-circuits when disabled.
- Spins up repeated server runs with different mod subsets.
- Parses logs to detect success/failure markers and summarizes results.
