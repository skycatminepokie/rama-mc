package skycat.ramamc.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skycat.ramamc.BigMealManager;
import skycat.ramamc.RamaMc;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    public void canConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!RamaMc.isDay()); // Stop from eating if it is day, allow if it is night
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    public float modifyAttackAmount(float amount) {
        if (RamaMc.isDay()) {
            return amount * 1.1F;
        }
        return amount;
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        // Check for nearby big meals
        PlayerEntity player = (PlayerEntity) (Object) this;
        BigMealManager.BigMeal meal = RamaMc.BIG_MEAL_MANAGER.getMealInRange(player);
        if (meal == null) { // If there isn't one
            meal = RamaMc.BIG_MEAL_MANAGER.startMeal(player.getBlockPos()); // Start one
        }
        // Join the meal
        meal.addParticipant(player);
    }
}
