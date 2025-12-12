package pro.fazeclan.river.stupid_express;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.fazeclan.river.stupid_express.role.amnesiac.RoleSelectionHandler;
import pro.fazeclan.river.stupid_express.role.arsonist.ArsonistItemGivingHandler;
import pro.fazeclan.river.stupid_express.role.arsonist.OilDousingHandler;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;
import pro.fazeclan.river.stupid_express.role.necromancer.RevivalSelectionHandler;

import java.util.HashMap;

public class StupidExpress implements ModInitializer {

    public static String MOD_ID = "stupid_express";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Getter
    private static final HashMap<String, Role> ROLES = new HashMap<>();

    public static ResourceLocation AMNESIAC_ID = id("amnesiac");

    public static Role AMNESIAC = registerRole(new Role(
            AMNESIAC_ID,
            0x9baae8,
            true,
            false,
            Role.MoodType.REAL,
            TMMRoles.CIVILIAN.getMaxSprintTime(),
            false
    ));

    public static ResourceLocation ARSONIST_ID = id("arsonist");

    public static Role ARSONIST = registerRole(new Role(
            ARSONIST_ID,
            0xfc9526,
            false,
            false,
            Role.MoodType.REAL,
            -1,
            true
    ));

    public static ResourceLocation AVARICIOUS_ID = id("avaricious");

    public static Role AVARICIOUS = registerRole(new Role(
            AVARICIOUS_ID,
            0x8f00ff,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));

    public static ResourceLocation NECROMANCER_ID = id("necromancer");

    public static Role NECROMANCER = registerRole(new Role(
            NECROMANCER_ID,
            0x9457ff,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));

    public static int LOVERS_COLOR = 0xf38aff;

    public static ResourceLocation LOVERS_ID = id("lovers");

    public static Role LOVERS = registerRole(new Role(
            LOVERS_ID,
            LOVERS_COLOR,
            false,
            false,
            Role.MoodType.REAL,
            -1,
            true
    ));

    @Override
    public void onInitialize() {

        /// AMNESIAC

        Harpymodloader.setRoleMaximum(AMNESIAC, 1);
        RoleSelectionHandler.init();

        /// ARSONIST

        Harpymodloader.setRoleMaximum(ARSONIST, 1);
        OilDousingHandler.init();
        ArsonistItemGivingHandler.init();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getPlayerCount() > 11) {
                Harpymodloader.setRoleMaximum(NECROMANCER, 1);
                Harpymodloader.setRoleMaximum(AVARICIOUS, 1);
            } else {
                Harpymodloader.setRoleMaximum(NECROMANCER, 0);
                Harpymodloader.setRoleMaximum(AVARICIOUS, 0);
            }
        });

        /// NECROMANCER

        RevivalSelectionHandler.init();

        /// AVARICIOUS

        AvariciousGoldHandler.onGameStart();

        Harpymodloader.setRoleMaximum(LOVERS, 0); // fake role for things

        // mod stuff
        ModItems.init();

        // temp fix (hopefully)
        sendAnnouncements();

    }

    public static Role registerRole(Role role) {
        TMMRoles.registerRole(role);
        ROLES.put(role.identifier().getPath(), role);
        return role;
    }

    public static ResourceLocation id(String key) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, key);
    }

    // for whatever reason harpymodloader doesn't send out announcements for roles other than
    // noelle's roles, so here's a workaround ig
    public void sendAnnouncements() {
        ModdedRoleAssigned.EVENT.register(((player, role) -> {
            if (!ROLES.containsValue(role)) {
                return;
            }
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            ServerPlayNetworking.send(
                    (ServerPlayer) player,
                    new AnnounceWelcomePayload(
                            RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(Harpymodloader.autogeneratedAnnouncements.get(role)),
                            gameWorldComponent.getAllKillerTeamPlayers().size(),
                            0
                    )
            );
        }));
    }

}
