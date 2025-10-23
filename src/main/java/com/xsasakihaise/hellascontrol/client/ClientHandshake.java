package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.network.ModPing;
import com.xsasakihaise.hellascontrol.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientHandshake {
    private ClientHandshake() {}

    /** Fired after the client has logged into a server (remote or integrated). */
    @SubscribeEvent
    public static void onClientLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent e) {
        // Ping the server once to ask: “Do you have HellasControl, and is it licensed?”
        NetworkHandler.CHANNEL.sendToServer(new ModPing());
    }
}
