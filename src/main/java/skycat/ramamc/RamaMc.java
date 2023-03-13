package skycat.ramamc;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

@Environment(EnvType.SERVER)
public class RamaMc implements DedicatedServerModInitializer, ServerWorldEvents.Load, ServerEntityCombatEvents.AfterKilledOtherEntity, EntitySleepEvents.AllowResettingTime, EntitySleepEvents.StopSleeping, ServerPlayConnectionEvents.Join {
    public static ServerWorld world = null;
    public static MinecraftServer server = null;
    public static final Logger LOGGER = LoggerFactory.getLogger("rama-mc");
    public static final BigMealManager BIG_MEAL_MANAGER = new BigMealManager();
    public static final Random RANDOM = new Random();
    public static boolean allowSleep = true;
    public static final HashMap<UUID, Float> removeAbsorptionMap = new HashMap<>();

    @Override
    public void afterKilledOtherEntity(ServerWorld world, Entity entity, LivingEntity killedEntity) {
        if (entity.isPlayer() && isDay()) {
            if (killedEntity instanceof Monster) {
                ((PlayerEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0));
            }
        }
    }

    @Override
    public boolean allowResettingTime(PlayerEntity player) {
        return allowSleep;
    }

    @Override
    public void onInitializeServer() {
        ServerWorldEvents.LOAD.register(this);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this);
        EntitySleepEvents.STOP_SLEEPING.register(this);
        EntitySleepEvents.ALLOW_RESETTING_TIME.register(this);
        ServerPlayConnectionEvents.JOIN.register(this);
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        LOGGER.info("login check");
        ServerPlayerEntity player = handler.getPlayer();
        UUID uuid = player.getUuid();
        if (removeAbsorptionMap.containsKey(uuid)) { // If they logged off with absorption from a big meal that has expired, remove it
            player.setAbsorptionAmount(player.getAbsorptionAmount() - Math.min(removeAbsorptionMap.get(uuid), player.getAbsorptionAmount()));
            LOGGER.info("removed absorption");
            player.sendMessage(Text.of("Your big meal bonus ran out while you were away."));
            removeAbsorptionMap.remove(uuid);
        }
    }

    @Override
    public void onStopSleeping(LivingEntity entity, BlockPos sleepingPos) {
        if (entity.isPlayer() && allowSleep) {
                allowSleep = false;
                ((RunnableTimerAccess) world).rama_mc_setRunnableTimer(()-> RamaMc.allowSleep = true, 24000L * 3); // Allow sleeping in three days
        }
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        RamaMc.world = world;
        RamaMc.server = server;
    }

    public static boolean isDay() {
        return server.getOverworld().isDay();
    }
}
