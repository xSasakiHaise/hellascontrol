# Mod Integration Tasks for HellasControl

This playbook is written so another OpenAI Codex instance (or any automation) can wire a mod into HellasControl `v2.0.0` on Minecraft `1.16.5` without guessing. Every step below maps to a concrete edit or code block.

## Quick Reference to Boot-Time Licence Helpers
| Helper | Located in | When to call | What it enforces |
| --- | --- | --- | --- |
| `CoreCheck.verifyCoreLoaded()` | `com.xsasakihaise.hellascontrol.api.CoreCheck` | First thing in your mod's common setup | Stops startup if HellasControl is missing anywhere (client or server). |
| `CoreCheck.verifyEntitled("<key>")` | `com.xsasakihaise.hellascontrol.api.CoreCheck` | Immediately after the core check, server side only | Validates the named entitlement when running on a dedicated server. |
| `HellasAPIHellasBattlebuddy.verify()` | `com.xsasakihaise.hellascontrol.api.sidemods` | Use instead of the two calls above when targeting the Battlebuddy entitlement | Bundles the core + entitlement (`"battlebuddy"`). |
| `HellasAPIHellasLibrary.verify()` | same package | Use for the Library entitlement | Bundles the core + entitlement (`"library"`). |
| `HellasAPIHellasTextures.verify()` | same package | Use for the Textures entitlement | Bundles the core + entitlement (`"textures"`). |
| `HellasAPIHellasWilds.verify()` | same package | Use for the Wilds entitlement | Bundles the core + entitlement (`"wilds"`). |

If you integrate a different sidemod, swap in its helper or call the two `CoreCheck` methods directly.

## Automation-Friendly Task List
1. **Declare HellasControl as a required dependency.**
   - Open your mod's `META-INF/mods.toml`.
   - Ensure a dependency block exists:
     ```toml
     [[dependencies.yourmodid]]
     modId="hellascontrol"
     mandatory=true
     versionRange="[2.0.0,)"
     ordering="NONE"
     side="BOTH"
     ```
2. **Add HellasControl to the Gradle classpath.**
   - In your mod project `build.gradle`, add the HellasControl JAR (local file or Maven coordinate) to `dependencies { compileOnly ... }` so the API classes above compile.
   - To pull the library straight from the public GitHub repo via JitPack, add the repository and dependency:
     ```groovy
     repositories {
         maven { url 'https://jitpack.io' }
     }

     dependencies {
         compileOnly fg.deobf('com.github.xSasakiHaise:hellascontrol:2.0.0')
     }
     ```
   - Swap the version string to match the release tag you target, or replace `compileOnly` with `implementation` if you shade the API into your mod.
3. **Import the licence helper you need.**
   - In the mod entry class (usually under `src/main/java/<your package>/<YourMod>.java`), add the relevant import, e.g.:
     ```java
     import com.xsasakihaise.hellascontrol.api.CoreCheck;
     // or
     import com.xsasakihaise.hellascontrol.api.sidemods.HellasAPIHellasBattlebuddy;
     ```
4. **Run the boot-time checks before any content registration.**
   - Inside your constructor or `FMLCommonSetupEvent` handler, insert the guard calls:
     ```java
     // Step 1: refuse to boot without the core present
     CoreCheck.verifyCoreLoaded();

     // Step 2: enforce the entitlement on dedicated servers
     CoreCheck.verifyEntitled("your_entitlement_key");
     // or, when using a bundled sidemod helper:
     HellasAPIHellasBattlebuddy.verify();
     ```
   - Do **not** wrap these in a catch that swallows the exception—the mod must abort if the licence fails.
   - Keep the calls in your primary mod class (the one annotated with `@Mod`). That ensures the entitlement checks run before
     any registries or side-setup hooks can introduce unlicensed content.
5. **Optionally short-circuit when HellasControl is absent on clients.**
   - Add the Forge helper import if you do not have it already:
     ```java
     import net.minecraftforge.fml.ModList;
     ```
   - Guard your registration code so licensed content never loads without HellasControl:
     ```java
     if (!ModList.get().isLoaded("hellascontrol")) {
         return; // skip everything because the whole mod is licensed
     }
     ```

### Integration Self-Check for Sidemod Main Classes
- Confirm the verification block above executes from your mod's entry class constructor or common-setup handler—not from a
  later event. The goal is to fail fast and avoid partially registering content before licence validation.
- If your mod splits client and server initialisation, keep the entitlement call on the server-only path and leave the core
  presence check in the shared path so single-player still validates.
- Re-run your build to confirm no accidental refactors removed the imports or calls; the mod should refuse to boot when the
  entitlement is missing on a dedicated server.

## Verification Steps
- Launch a dedicated server with a valid `config/hellas/license.json`; confirm the server reaches the “Done” state.
- Repeat with a missing/invalid licence; the server should halt during your guard call with a clear error message.
- Start a client without HellasControl installed; verify the guard in step 5 keeps the licensed content disabled while allowing the game to continue.
