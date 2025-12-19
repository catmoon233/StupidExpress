package pro.fazeclan.river.stupid_express.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import pro.fazeclan.river.stupid_express.cca.SEConfig;

public class ConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stupid_express:config")
                        .then(Commands.literal("necromancer_has_shop")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ConfigCommand::necromancerHasShopExecute)
                                )
                        )
        );
    }

    private static int necromancerHasShopExecute(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var config = SEConfig.KEY.get(source.getLevel());
        var value = ctx.getArgument("value", Boolean.class);

        config.setNecromancerHasShop(value);
        config.sync();

        source.sendSystemMessage(Component.translatable("commands.stupid_express.set_config_value", "necromancer_has_shop", value));
        return 1;
    }

}
