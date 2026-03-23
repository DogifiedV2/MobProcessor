package com.dog.mobprocessor.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityDropInvoker {

    @Invoker("dropFromLootTable")
    void mobprocessor$invokeDropFromLootTable(DamageSource damageSource, boolean attackedRecently);

    @Invoker("dropCustomDeathLoot")
    void mobprocessor$invokeDropCustomDeathLoot(DamageSource damageSource, int lootingLevel, boolean attackedRecently);
}
