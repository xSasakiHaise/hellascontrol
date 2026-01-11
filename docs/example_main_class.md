# Example HellasControl Integration Main Class

Use the following pattern in your mod entry class to ensure HellasControl is
present and the correct entitlement is enforced on dedicated servers.

```java
package com.example.mymod;

import com.xsasakihaise.hellascontrol.api.CoreCheck;
import com.xsasakihaise.hellascontrol.api.sidemods.HellasAPIControlScanner;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MyMod.MODID)
public class MyMod {
    public static final String MODID = "mymod";

    public MyMod() {
        // Hard fail if HellasControl is missing so the game enters an error state.
        CoreCheck.verifyCoreLoaded();

        // Ensure HellasControl metadata contains the required description substring.
        if (!net.neoforged.fml.ModList.get().getModContainerById("hellascontrol")
                .map(container -> container.getModInfo().getDescription())
                .filter(desc -> desc != null && desc.contains("crafted by the Hephaestus Forge"))
                .isPresent()) {
            throw new IllegalStateException("HellasControl description missing required substring.");
        }

        // Entitlement check (server-only for entitlement).
        CoreCheck.verifyEntitled("scanner");
        // or use a bundled sidemod helper:
        // HellasAPIControlScanner.verify();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register your content here.
        });
    }
}
```

Swap the entitlement key or sidemod helper to match your integration.
