# HellasControlInfoConfig

`HellasControlInfoConfig` (`src/main/java/com/xsasakihaise/hellascontrol/HellasControlInfoConfig.java`) loads and stores the descriptive metadata shown to admins or tools.

## Responsibilities
- Loads `hellas_patcher_info.json` from disk when present.
- Falls back to the bundled `config/hellaspatcher.json` resource.
- Exposes version, dependency list, and feature list strings.
