package net.bzbr.hotwaves.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.bzbr.hotwaves.events.HordeStartEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class HordeStartCommand {


    private static int executeCommand(CommandContext<ServerCommandSource> context) {

        var server = context.getSource().getServer();
        try {
            HordeStartEvent.EVENT.invoker().onHordeStartEvent("", server, 1);
        } catch (Exception e) {

            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("horde")
                .then(CommandManager.literal("start")
                        .executes(HordeStartCommand::executeCommand)));
    }
}
