package pro.fazeclan.river.stupid_express.constants;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.Color;
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
import pro.fazeclan.river.stupid_express.modifier.refugee.cca.RefugeeComponent;
import pro.fazeclan.river.stupid_express.modifier.allergist.cca.AllergistComponent;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

public class SEModifiers {

    // Attribute modifier for tiny players
    private static AttributeModifier tinyModifier = new AttributeModifier(
            StupidExpress.id("tiny_modifier"), -0.15, AttributeModifier.Operation.ADD_VALUE);

    // Attribute modifier for tall players
    private static AttributeModifier tallModifier = new AttributeModifier(
            StupidExpress.id("tall_modifier"), 0.11, AttributeModifier.Operation.ADD_VALUE);

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

    public static Modifier TINY = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("tiny"),
            new Color(255, 166, 0).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier TALL = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("tall"),
            new Color(0, 255, 0).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier FEATHER = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("feather"),
            new Color(255, 236, 161).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier MAGNATE = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("magnate"),
            new Color(255, 255, 0).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier TASKMASTER = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("taskmaster"),
            new Color(255, 51, 153).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier ALLERGIST = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("allergist"),
            new Color(112, 255, 162).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier CURSED = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("cursed"),
            new Color(75, 0, 130).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier SECRETIVE = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("secretive"),
            new Color(50, 50, 50).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier KNIGHT = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("knight"),
            new Color(192, 192, 192).getRGB(),
            null,
            null,
            false,
            false));

    public static Modifier SPLIT_PERSONALITY = HMLModifiers.registerModifier(new Modifier(
            StupidExpress.id("split_personality"),
            new Color(138, 43, 226).getRGB(),
            null,
            null,
            false,
            true));

    public static void init() {
        initModifiersCount();
        assignModifierComponents();
        pro.fazeclan.river.stupid_express.modifier.magnate.MagnatePassiveIncomeHandler.init();
        pro.fazeclan.river.stupid_express.modifier.cursed.CursedHandler.init();
        pro.fazeclan.river.stupid_express.modifier.secretive.SecretiveHandler.init();
        pro.fazeclan.river.stupid_express.modifier.knight.KnightHandler.init();
        pro.fazeclan.river.stupid_express.modifier.split_personality.SplitPersonalityHandler.init();

        TMM.canCollide.add(p -> {
            var modifiers = WorldModifierComponent.KEY.get(p.level());
            if(modifiers.isModifier(p.getUUID(), FEATHER)){
                return true;
            }
            return false;
        });
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

            var level = lover.serverLevel();

            // choose second person
            ServerPlayer personTwo = null;
            var arrs = new ArrayList<>(level.players());
            Collections.shuffle(arrs);
            for (var can_person : arrs) {
                if (GameFunctions.isPlayerAliveAndSurvival(can_person)) {
                    if (!lover.equals(can_person)) {
                        personTwo = can_person;
                        break;
                    }
                }
            }
            if (personTwo == null) {
                personTwo = lover;
            }
            // assign both lovers
            var splitPersonalityComponent = SplitPersonalityComponent.KEY.get(lover);

            splitPersonalityComponent.setMainPersonality(player.getUUID());
            splitPersonalityComponent.setSecondPersonality(lover.getUUID());
            splitPersonalityComponent.sync();

            var splitPersonalityComponentTwo = SplitPersonalityComponent.KEY.get(personTwo);

            splitPersonalityComponentTwo.setMainPersonality(personTwo.getUUID());
            splitPersonalityComponentTwo.setSecondPersonality(lover.getUUID());
            splitPersonalityComponentTwo.sync();

            var worldModifierComponent = WorldModifierComponent.KEY.get(level);
            worldModifierComponent.addModifier(personTwo.getUUID(), SPLIT_PERSONALITY); // visually show lovers on the other player
        }));
        /// SPLIT_PERSONALITY
        ModifierAssigned.EVENT.register(((player, modifier) -> {
            if (!modifier.equals(SPLIT_PERSONALITY)) {
                return;
            }
            if (!(player instanceof ServerPlayer person)) {
                return;
            }

            var level = person.serverLevel();

            // 选择另一个平民作为第二人格
            ServerPlayer secondPersonality = null;
            var arrs = new ArrayList<>(level.players());
            Collections.shuffle(arrs);
            for (var candidate : arrs) {
                if (GameFunctions.isPlayerAliveAndSurvival(candidate)) {
                    if (!person.equals(candidate)) {
                        secondPersonality = candidate;
                        break;
                    }
                }
            }
            if (secondPersonality == null) {
                secondPersonality = person;
            }

            // 为两个人格都设置SplitPersonalityComponent
            var componentOne = SplitPersonalityComponent.KEY.get(person);
            componentOne.setMainPersonality(person.getUUID());
            componentOne.setSecondPersonality(secondPersonality.getUUID());
            componentOne.setCurrentActivePerson(person.getUUID()); // 主人格是第一个被激活的
            componentOne.sync();

            var componentTwo = SplitPersonalityComponent.KEY.get(secondPersonality);
            componentTwo.setMainPersonality(person.getUUID());
            componentTwo.setSecondPersonality(secondPersonality.getUUID());
            componentTwo.setCurrentActivePerson(person.getUUID()); // 主人格是第一个被激活的
            componentTwo.sync();

            var worldModifierComponent = WorldModifierComponent.KEY.get(level);
            worldModifierComponent.addModifier(secondPersonality.getUUID(), SPLIT_PERSONALITY); // 给第二人格添加修饰符
        }));

        /// TINY & TALL & FEATHER & ALLERGIST & CURSED & SECRETIVE & KNIGHT & SPLIT_PERSONALITY
        /// TINY & FEATHER & ALLERGIST & CURSED & SECRETIVE & KNIGHT &
        ModifierAssigned.EVENT.register(((player, modifier) -> {
            if (modifier.equals(TINY)) {
                var worldModifierComponent = WorldModifierComponent.KEY.get(player.level());
                // Cannot assign TALL if player has TINY
                if (worldModifierComponent.isModifier(player.getUUID(), TALL)) {
                    worldModifierComponent.removeModifier(player.getUUID(), TALL);
                    player.getAttribute(Attributes.SCALE).removeModifier(tallModifier);
                }
                player.getAttribute(Attributes.SCALE).removeModifier(tinyModifier);
                player.getAttribute(Attributes.SCALE).addPermanentModifier(tinyModifier);
            }
            if (modifier.equals(TALL)) {
                var worldModifierComponent = WorldModifierComponent.KEY.get(player.level());
                // Cannot assign TINY if player has TALL
                if (worldModifierComponent.isModifier(player.getUUID(), TINY)) {
                    worldModifierComponent.removeModifier(player.getUUID(), TINY);
                    player.getAttribute(Attributes.SCALE).removeModifier(tinyModifier);
                }
                player.getAttribute(Attributes.SCALE).removeModifier(tallModifier);
                player.getAttribute(Attributes.SCALE).addPermanentModifier(tallModifier);
            }
            if (modifier.equals(FEATHER)) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.SLOW_FALLING,
                        net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION,
                        0, true, false));
            }
            if (modifier.equals(ALLERGIST)) {
                var allergistComponent = AllergistComponent.KEY.get(player);
                allergistComponent.setAllergist(player.getUUID());
                allergistComponent.sync();
            }
            if (modifier.equals(CURSED)) {
                var cursedComponent = pro.fazeclan.river.stupid_express.modifier.cursed.cca.CursedComponent.KEY.get(player);
                cursedComponent.setCursed(player.getUUID());
                cursedComponent.sync();
            }
            if (modifier.equals(SECRETIVE)) {
                var secretiveComponent = pro.fazeclan.river.stupid_express.modifier.secretive.cca.SecretiveComponent.KEY.get(player);
                secretiveComponent.setSecretive(player.getUUID());
                secretiveComponent.sync();
            }
            if (modifier.equals(KNIGHT)) {
                var knightComponent = pro.fazeclan.river.stupid_express.modifier.knight.cca.KnightComponent.KEY.get(player);
                knightComponent.setKnight(player.getUUID());
                knightComponent.sync();
            }


        }));

        ResetPlayerEvent.EVENT.register(player -> {
            // Remove tiny modifier
            player.getAttribute(Attributes.SCALE).removeModifier(tinyModifier);
            // Remove tall modifier
            player.getAttribute(Attributes.SCALE).removeModifier(tallModifier);
            // Remove feather effect
            player.removeEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING);
            // Reset lovers component
            var component = LoversComponent.KEY.get(player);
            component.reset();
            component.sync();
            // Reset allergist component
            var allergistComponent = AllergistComponent.KEY.get(player);
            allergistComponent.reset();
            allergistComponent.sync();
            // Reset cursed component
            var cursedComponent = pro.fazeclan.river.stupid_express.modifier.cursed.cca.CursedComponent.KEY.get(player);
            cursedComponent.reset();
            cursedComponent.sync();
            // Reset secretive component
            var secretiveComponent = pro.fazeclan.river.stupid_express.modifier.secretive.cca.SecretiveComponent.KEY.get(player);
            secretiveComponent.reset();
            secretiveComponent.sync();
            // Reset knight component
            var knightComponent = pro.fazeclan.river.stupid_express.modifier.knight.cca.KnightComponent.KEY.get(player);
            knightComponent.reset();
            knightComponent.sync();
            // Reset split personality component
            var splitPersonalityComponent = pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent.KEY.get(player);
            // 清理库存数据
            pro.fazeclan.river.stupid_express.modifier.split_personality.SplitPersonalityHandler.cleanupInventoryData(player.getUUID());
            splitPersonalityComponent.reset();
            splitPersonalityComponent.sync();
            // Reset refugee component
            var refugeeC = RefugeeComponent.KEY.get(player.level());
            if (refugeeC != null) {
                refugeeC.reset();
            }
        });

    }

    public static void initModifiersCount() {
        /// LOVERS
        if (Math.random() < 0.1) {
            StupidExpress.LOGGER.info("Modifier [Lovers] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("lovers"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("lovers"), 0);
        }

        /// REFUGEE
        if (Math.random() < 0.1) {
            StupidExpress.LOGGER.info("Modifier [Refugee] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("refugee"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("refugee"), 0);
        }

        /// TINY
            StupidExpress.LOGGER.info("Modifier [Tiny] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("tiny"), 5);

        /// TALL
            StupidExpress.LOGGER.info("Modifier [Tall] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("tall"), 4);

        /// FEATHER
            StupidExpress.LOGGER.info("Modifier [Feather] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("feather"), 2);

        /// MAGNATE
        if (Math.random() < 0.5) {
            StupidExpress.LOGGER.info("Modifier [Magnate] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("magnate"), 2);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("magnate"), 0);
        }

        /// TASKMASTER
        if (Math.random() < 0.5) {
            StupidExpress.LOGGER.info("Modifier [Taskmaster] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("taskmaster"), 2);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("taskmaster"), 0);
        }

        /// ALLERGIST
        if (Math.random() < 0.2) {
            StupidExpress.LOGGER.info("Modifier [Allergist] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("allergist"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("allergist"), 0);
        }

        /// CURSED
        if (Math.random() < 0.3) {
            StupidExpress.LOGGER.info("Modifier [Cursed] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("cursed"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("cursed"), 0);
        }

        /// SECRETIVE
        if (Math.random() < 0.2) {
            StupidExpress.LOGGER.info("Modifier [Secretive] enabled in this round! (2)");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("secretive"), 2);
        } else {
            StupidExpress.LOGGER.info("Modifier [Secretive] enabled in this round! (1)");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("secretive"), 1);
        }

        /// KNIGHT
        if (Math.random() < 0.3) {
            StupidExpress.LOGGER.info("Modifier [Knight] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("knight"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("knight"), 0);
        }

        /// SPLIT_PERSONALITY
        if (Math.random() < 0.1) {
            StupidExpress.LOGGER.info("Modifier [Split Personality] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("split_personality"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("split_personality"), 0);
        }
    }

}
