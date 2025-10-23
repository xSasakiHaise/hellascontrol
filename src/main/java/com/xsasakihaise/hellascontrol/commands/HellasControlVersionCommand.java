package com.xsasakihaise.hellascontrol.commands;

import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.HellasControlInfoConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class HellasControlVersionCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("hellas")
                        .then(Commands.literal("control")
                                .then(Commands.literal("version")
                                        .executes(ctx -> {
                                            HellasControlInfoConfig cfg = HellasControl.infoConfig;
                                            if (cfg == null || !cfg.isValid() || cfg.getVersion() == null) {
                                                ctx.getSource().sendSuccess(new StringTextComponent("Fehler: HellasControl-Info nicht geladen (fehlende oder ungültige JSON)."), false);
                                                return 0;
                                            }
                                            return sendFormatted(ctx.getSource(), cfg.getVersion());
                                        })
                                )
                        )
        );
    }

    private static int sendFormatted(CommandSource source, String text) {
        source.sendSuccess(new StringTextComponent(
                "-----------------------------------\n" + text + "\n-----------------------------------"
        ), false);
        return 1;
    }
}