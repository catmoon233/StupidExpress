package pro.fazeclan.river.stupid_express;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.ArrayList;
import java.util.List;

public class StupidExpress implements ModInitializer {

    public static String MOD_ID = "stupid_express";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final StupidExpressConfig CONFIG = ConfigApiJava.registerAndLoadConfig(StupidExpressConfig::new);
    public static List<Role> getEnableRoles() {
        ArrayList<Role> clone = new ArrayList<>(TMMRoles.ROLES.values());
        clone.removeIf(
                r -> HarpyModLoaderConfig.HANDLER.instance().disabled.contains(r.getIdentifier().toString()));
        return clone;
    }

    public static List<Role> getEnableKillerRoles() {
        ArrayList<Role> clone = new ArrayList<>(TMMRoles.ROLES.values());
        clone.removeIf(
                r -> !r.canUseKiller()
                        || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(r.getIdentifier().toString()));
        return clone;
    }

    @Override
    public void onInitialize() {

        SERoles.init();
        SEModifiers.init();

        // mod stuff
        SEItems.init();

    }

    public static ResourceLocation id(String key) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, key);
    }

}
