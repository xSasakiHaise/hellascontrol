# Example HellasControl Integration Main Class

Use the following pattern in your mod entry class to ensure HellasControl is
present and the correct entitlement is enforced on dedicated servers.

```java
package com.example.mymod;

import com.xsasakihaise.hellascontrol.api.CoreCheck;
import com.xsasakihaise.hellascontrol.api.sidemods.HellasAPIControlScanner;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MyMod.MODID)
public class MyMod {
    public static final String MODID = "mymod";

    public MyMod() {
        // Hard fail if HellasControl is missing so the game enters an error state.
        CoreCheck.verifyCoreLoaded();

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
