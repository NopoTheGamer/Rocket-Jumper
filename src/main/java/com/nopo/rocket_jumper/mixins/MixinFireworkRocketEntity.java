package com.nopo.rocket_jumper.mixins;

import com.nopo.rocket_jumper.main.RocketJumper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(FireworkRocketEntity.class)
public abstract class MixinFireworkRocketEntity extends ProjectileEntity {

    public MixinFireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, World world) {
        super(entityType, world);
    }

    boolean shouldDo = true;

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (!shouldDo) return;
        if (this.world.getServer() == null) return;
        PlayerEntity player = (PlayerEntity) this.getOwner();
        if (player == null) return; // I don't see how this is possible but intellij was yelling at me and :(
        if (player.isFallFlying() || player.isSneaking()) {
            shouldDo = false;
            return; //just breaks elytra lmao
        }
        /*
        * If we are in single player
        * Your position gets set instead of teleporting because it's much smoother
         */
        if (RocketJumper.getInstance().isClient) {
            try {
                Class<?> clazz = Class.forName("net.minecraft.client.MinecraftClient");
                Field mcClientInstance = clazz.getDeclaredField("instance");
                Object mcClient = mcClientInstance.get(null);
                Field playerField = clazz.getDeclaredField("player");
                Object playerObj = playerField.get(mcClient);
                Class<?> playerClass = playerObj.getClass();
                Method teleportMethod = playerClass.getMethod("setPosition", double.class, double.class, double.class);
                Method velocityMethod = playerClass.getMethod("setVelocity", double.class, double.class, double.class);
                teleportMethod.invoke(playerObj, this.getX(), this.getY(), this.getZ());
                velocityMethod.invoke(playerObj, 0, 0, 0);
                return; // Client World

            } catch (IllegalAccessException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | InvocationTargetException | RuntimeException e) {
                e.printStackTrace();
            }
        }
        player.teleport(this.getX(), this.getY(), this.getZ()); // Server World
        player.setVelocity(0, 0, 0);
    }
}
