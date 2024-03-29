package skycat.ramamc.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import skycat.ramamc.RamaMc;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @ModifyVariable(method = "addExhaustion", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float changeExhaustion(float exhaustion) {
        if (RamaMc.isDay()) {
            exhaustion *= RamaMc.CONFIG.HUNGER_MULTIPLIER_DAY;
        }
        return exhaustion;
    }
}
