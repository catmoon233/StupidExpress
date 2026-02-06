package pro.fazeclan.river.stupid_express;

import dev.doctor4t.ratatouille.util.registrar.SoundEventRegistrar;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.event.OnPlayerDeath;
import dev.doctor4t.trainmurdermystery.game.GameReplayManager;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.GameInitializeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import pro.fazeclan.river.stupid_express.modifier.refugee.cca.RefugeeComponent;
import pro.fazeclan.river.stupid_express.network.SplitPersonalityPackets;
import pro.fazeclan.river.stupid_express.role.initiate.InitiateUtils;

import java.util.ArrayList;
import java.util.List;

public class StupidExpress implements ModInitializer {

    public static String MOD_ID = "stupid_express";
    public static final SoundEventRegistrar SoundRegistrar = new SoundEventRegistrar(MOD_ID);
    public static final SoundEvent SOUND_REGUGEE = SoundRegistrar.create("refugee.music");

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

        // mod stuff
        SEItems.init();
        SEModifiers.init();
        InitiateUtils.InitiateChange();

        // 初始化网络包处理
        SplitPersonalityPackets.registerPackets();
        pro.fazeclan.river.stupid_express.network.SplitPersonalitySwitchPacket.register();

        GameInitializeEvent.EVENT.register((ServerLevel, gameWorldComponent, serverPlayers) -> {
            var refugeeC = RefugeeComponent.KEY.get(ServerLevel);
            if (refugeeC != null) {
                refugeeC.reset();
            }
            SEModifiers.initModifiersCount();
        });
        OnPlayerDeath.EVENT.register((victim, deathReason) -> {
            var gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
            if (gameWorldComponent != null) {
                Role role = gameWorldComponent.getRole(victim);
                if (role != null) {
                    if (role.identifier().getPath().equals(TMMRoles.LOOSE_END.identifier().getPath())) {
                        var refugeeComponent = RefugeeComponent.KEY.get(victim.level());
                        refugeeComponent.onLooseEndDeath(victim);
                    }
                }
            }
        });

        GameReplayManager.cantSeeEvent.add(
                (player -> {
                    WorldModifierComponent modifierComponent = WorldModifierComponent.KEY.get(player.level());
                    return modifierComponent.isModifier(player, SEModifiers.SPLIT_PERSONALITY);
                })
        );
    }

    public static ResourceLocation id(String key) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, key);
    }

}
