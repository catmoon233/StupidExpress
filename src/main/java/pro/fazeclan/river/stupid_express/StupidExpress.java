package pro.fazeclan.river.stupid_express;

import dev.doctor4t.wathe.api.Role;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class StupidExpress implements ModInitializer {

    public static String MOD_ID = "stupid_express";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Getter
    private static final HashMap<String, Role> ROLES = new HashMap<>();

    @Override
    public void onInitialize() {

        SERoles.init();
        SEModifiers.init();

        // mod stuff
        SEItems.init();

        SECommands.registerCommands();

    }

    public static ResourceLocation id(String key) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, key);
    }

}
