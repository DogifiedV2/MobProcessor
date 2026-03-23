package com.dog.mobprocessor.loot;

import com.dog.mobprocessor.mixin.LivingEntityDropInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LootTableRoller {

    public static List<ItemStack> rollEntityLoot(ServerLevel serverLevel, BlockPos pos,
                                                 EntityType<?> entityType, int lootingLevel,
                                                 boolean simulatePlayerKill) {
        Entity temporaryEntity = entityType.create(serverLevel);
        if (temporaryEntity == null) {
            return Collections.emptyList();
        }

        try {
            temporaryEntity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            initializeSpawnState(temporaryEntity, serverLevel, pos);

            if (temporaryEntity instanceof LivingEntity livingEntity) {
                return rollVanillaLivingDrops(serverLevel, livingEntity, lootingLevel, simulatePlayerKill);
            }

            return Collections.emptyList();
        } finally {
            temporaryEntity.discard();
        }
    }

    private static void initializeSpawnState(Entity entity, ServerLevel serverLevel, BlockPos pos) {
        if (entity instanceof Mob mob) {
            DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(pos);
            mob.finalizeSpawn(serverLevel, difficulty, MobSpawnType.SPAWN_EGG, null, null);
        }
    }

    private static List<ItemStack> rollVanillaLivingDrops(ServerLevel serverLevel, LivingEntity livingEntity,
                                                          int lootingLevel, boolean simulatePlayerKill) {
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);
        ItemStack previousHeldItem = fakePlayer.getMainHandItem().copy();
        DamageSource damageSource = DamageSource.GENERIC;
        boolean attackedRecently = false;
        Collection<ItemEntity> previousCapture = livingEntity.captureDrops(new ArrayList<>());

        try {
            if (lootingLevel > 0) {
                ItemStack lootingSword = new ItemStack(Items.DIAMOND_SWORD);
                lootingSword.enchant(Enchantments.MOB_LOOTING, lootingLevel);
                fakePlayer.getInventory().items.set(fakePlayer.getInventory().selected, lootingSword);
            } else {
                fakePlayer.getInventory().items.set(fakePlayer.getInventory().selected, ItemStack.EMPTY);
            }

            if (simulatePlayerKill) {
                livingEntity.setLastHurtByPlayer(fakePlayer);
                damageSource = DamageSource.playerAttack(fakePlayer);
                attackedRecently = true;
            }

            int effectiveLooting = attackedRecently
                    ? ForgeHooks.getLootingLevel(livingEntity, damageSource.getEntity(), damageSource)
                    : 0;

            LivingEntityDropInvoker dropInvoker = (LivingEntityDropInvoker) livingEntity;
            dropInvoker.mobprocessor$invokeDropFromLootTable(damageSource, attackedRecently);
            dropInvoker.mobprocessor$invokeDropCustomDeathLoot(damageSource, effectiveLooting, attackedRecently);

            Collection<ItemEntity> capturedDrops = livingEntity.captureDrops(previousCapture);
            return toItemStacks(capturedDrops);
        } finally {
            if (livingEntity.captureDrops() != previousCapture) {
                livingEntity.captureDrops(previousCapture);
            }
            fakePlayer.getInventory().items.set(fakePlayer.getInventory().selected, previousHeldItem);
        }
    }

    private static List<ItemStack> toItemStacks(Collection<ItemEntity> capturedDrops) {
        if (capturedDrops == null || capturedDrops.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> loot = new ArrayList<>(capturedDrops.size());
        for (ItemEntity itemEntity : capturedDrops) {
            loot.add(itemEntity.getItem().copy());
        }
        return loot;
    }
}
