package com.xsasakihaise.hellascontrol.commands;

import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.HellasControlInfoConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class HellasControlDependenciesCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("hellas")
                        .then(Commands.literal("control")
                                .then(Commands.literal("dependencies")
                                        .executes(ctx -> {
                                            HellasControlInfoConfig cfg = HellasControl.infoConfig;
                                            if (cfg == null || !cfg.isValid()) {
                                                ctx.getSource().sendSuccess(new StringTextComponent("Fehler: HellasControl-Info nicht geladen (fehlende oder ungültige JSON)."), false);
                                                return 0;
                                            }
                                            StringBuilder sb = new StringBuilder();
                                            for (String d : cfg.getDependencies()) {
                                                sb.append(d).append("\n");
                                            }
                                            String out = sb.toString().trim();
                                            if (out.isEmpty()) out = "(keine Abhängigkeiten)";
                                            return sendFormatted(ctx.getSource(), out);
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