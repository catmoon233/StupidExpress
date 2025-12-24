package pro.fazeclan.river.stupid_express.mixin.role.initiate;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;

@Mixin(PlayerShopComponent.class)
public abstract class InitiateBuyMixin {

    @Shadow
    @Final
    private Player player;

    @Shadow
    public int balance;

    @Shadow
    public abstract void sync();

    @Inject(
            method = "tryBuy",
            at = @At("HEAD"),
            cancellable = true
    )
    private void initiateBuyItem(int index, CallbackInfo ci) {
        var gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (gameWorldComponent.isRole(player, SERoles.INITIATE)) {
            var entry = SERoles.INITIATE_SHOP.get(index);

            if (balance >= entry.price()) {
                this.balance -= entry.price();
                sync();
                entry.onBuy(player);

                if (player instanceof ServerPlayer) {
                    player.playNotifySound(WatheSounds.UI_SHOP_BUY, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
                }
            } else {
                player.displayClientMessage(Component.literal("Purchase Failed").withStyle(ChatFormatting.DARK_RED), true);

                if (player instanceof ServerPlayer) {
                    player.playNotifySound(WatheSounds.UI_SHOP_BUY_FAIL, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
                }
            }
            ci.cancel();
        }
    }

}
