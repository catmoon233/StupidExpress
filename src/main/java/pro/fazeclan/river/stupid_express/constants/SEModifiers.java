package pro.fazeclan.river.stupid_express.constants;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;

import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

public class SEModifiers {

    public static Modifier LOVERS = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("lovers"),
            0xf38aff,
            null,
            null,
            false,
            false));

    public static Modifier REFUGEE = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("refugee"),
            0x55ff55,
            null,
            null,
            false,
            false));

    public static void init() {
        setCount();
        assignModifierComponents();
    }

    public static void assignModifierComponents() {

        /// LOVERS
        ModifierAssigned.EVENT.register(((player, modifier) -> {
            if (!modifier.equals(LOVERS)) {
                return;
            }
            if (!(player instanceof ServerPlayer lover)) {
                return;
            }

            var level = (ServerLevel) lover.level();
            var gameWorldComponent = GameWorldComponent.KEY.get(level);

            // choose second lover
            ServerPlayer loverTwo = null;
            var arrs = new ArrayList<>(level.players());
            Collections.shuffle(arrs);
            for (var can_i_love : arrs) {
                if (GameFunctions.isPlayerAliveAndSurvival(can_i_love)) {
                    // Role role = gameWorldComponent.getRole(can_i_love);
                    // if (role != null) {
                    // if (role.isInnocent()) {
                    // if
                    // (!role.getIdentifier().getPath().equals(TMMRoles.VIGILANTE.getIdentifier().getPath()))
                    // {
                    if (!lover.equals(can_i_love)) {
                        loverTwo = can_i_love;
                        break;
                    }
                    // }
                    // }
                    // }
                }

            }
            if (loverTwo == null) {
                loverTwo = lover;
            }
            // assign both lovers
            var loverComponentOne = LoversComponent.KEY.get(lover);

            loverComponentOne.setLover(loverTwo.getUUID());
            loverComponentOne.sync();

            var loverComponentTwo = LoversComponent.KEY.get(loverTwo);

            loverComponentTwo.setLover(lover.getUUID());
            loverComponentTwo.sync();

            var worldModifierComponent = WorldModifierComponent.KEY.get(level);
            worldModifierComponent.addModifier(loverTwo.getUUID(), LOVERS); // visually show lovers on the other player
        }));

        ResetPlayerEvent.EVENT.register(player -> {
            var component = LoversComponent.KEY.get(player);
            component.reset();
            component.sync();
        });

    }

    public static void initModifiersCount() {
        /// LOVERS
        if (Math.random() < 0.1) {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("lovers"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("lovers"), 0);
        }

        /// REFUGEE
        if (Math.random() < 0.1) {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("refugee"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("refugee"), 0);
        }
    }

}
