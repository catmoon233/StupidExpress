package pro.fazeclan.river.stupid_express;

import dev.doctor4t.ratatouille.util.registrar.ItemRegistrar;
import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import pro.fazeclan.river.stupid_express.role.arsonist.item.LighterItem;

public class SEItems {

    private static ItemRegistrar registrar = new ItemRegistrar(StupidExpress.MOD_ID);

    private static final ResourceKey<CreativeModeTab> EQUIPMENT_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, TMM.id("equipment"));

    public static final Item JERRY_CAN = registrar.create("jerry_can", new Item(new Item.Properties().stacksTo(1)), EQUIPMENT_GROUP);
    public static final Item LIGHTER = registrar.create("lighter", new LighterItem(new Item.Properties().stacksTo(1)), EQUIPMENT_GROUP);

    public static void init() {
        registrar.registerEntries();
    }

}
