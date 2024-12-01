package net.bzbr.hotwaves.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.bzbr.hotwaves.events.HordeEndEvent;
import net.bzbr.hotwaves.events.HordeStartEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class HordeEndCommand {

    private static int executeCommand(CommandContext<ServerCommandSource> context) {

        try {
            HordeEndEvent.EVENT.invoker().onHordeEndEvent();
        } catch (Exception e) {

            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("horde")
                .then(CommandManager.literal("end")
                        .executes(HordeEndCommand::executeCommand)));
    }
}
