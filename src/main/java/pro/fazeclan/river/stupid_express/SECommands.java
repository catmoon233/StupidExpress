package pro.fazeclan.river.stupid_express;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import pro.fazeclan.river.stupid_express.command.ConfigCommand;

public class SECommands {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            ConfigCommand.register(dispatcher);
        }));
    }

}
