package net.bzbr.hotwaves.common.registrators;

import net.bzbr.hotwaves.common.commands.HordeEndCommand;
import net.bzbr.hotwaves.common.commands.HordeStartCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandsRegistrator {

    public static void Register(){
        CommandRegistrationCallback.EVENT.register(HordeStartCommand::register);
        CommandRegistrationCallback.EVENT.register(HordeEndCommand::register);
    }
}
