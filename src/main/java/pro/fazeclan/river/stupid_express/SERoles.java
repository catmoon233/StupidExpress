package pro.fazeclan.river.stupid_express;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import lombok.Getter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.agmas.harpymodloader.Harpymodloader;
import pro.fazeclan.river.stupid_express.role.amnesiac.RoleSelectionHandler;
import pro.fazeclan.river.stupid_express.role.arsonist.ArsonistItemGivingHandler;
import pro.fazeclan.river.stupid_express.role.arsonist.OilDousingHandler;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;
import pro.fazeclan.river.stupid_express.role.necromancer.RevivalSelectionHandler;

import java.util.HashMap;

public class SERoles {

    @Getter
    private static final HashMap<String, Role> ROLES = new HashMap<>();

    public static Role AMNESIAC = registerRole(new Role(
            StupidExpress.id("amnesiac"),
            0x9baae8,
            true,
            false,
            Role.MoodType.REAL,
            TMMRoles.CIVILIAN.getMaxSprintTime(),
            false
    ));

    public static Role ARSONIST = registerRole(new Role(
            StupidExpress.id("arsonist"),
            0xfc9526,
            false,
            false,
            Role.MoodType.REAL,
            -1,
            true
    ));

    public static Role AVARICIOUS = registerRole(new Role(
            StupidExpress.id("avaricious"),
            0x8f00ff,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));

    public static Role NECROMANCER = registerRole(new Role(
            StupidExpress.id("necromancer"),
            0x9457ff,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));

    // not really a role, but it makes it easier to put in a win condition display
    public static Role LOVERS = registerRole(new Role(
            StupidExpress.id("lovers"),
            0xf38aff,
            false,
            false,
            Role.MoodType.REAL,
            -1,
            true
    ));

    public static void init() {
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

        /// LOVERS

        Harpymodloader.setRoleMaximum(LOVERS, 0); // fake role for things
    }

    public static Role registerRole(Role role) {
        TMMRoles.registerRole(role);
        ROLES.put(role.identifier().getPath(), role);
        return role;
    }

}
