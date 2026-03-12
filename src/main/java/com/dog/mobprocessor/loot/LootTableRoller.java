package com.dog.mobprocessor.loot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

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

            ResourceLocation lootTableLocation = entityType.getDefaultLootTable();
            LootTable lootTable = serverLevel.getServer().getLootTables().get(lootTableLocation);

            if (simulatePlayerKill) {
                return rollWithPlayerKill(serverLevel, pos, temporaryEntity, lootTable, lootingLevel);
            } else {
                return rollWithoutPlayerKill(serverLevel, pos, temporaryEntity, lootTable);
            }
        } finally {
            temporaryEntity.discard();
        }
    }

    private static List<ItemStack> rollWithPlayerKill(ServerLevel serverLevel, BlockPos pos,
                                                       Entity temporaryEntity, LootTable lootTable,
                                                       int lootingLevel) {
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);

        try {
            if (lootingLevel > 0) {
                ItemStack lootingSword = new ItemStack(Items.DIAMOND_SWORD);
                lootingSword.enchant(Enchantments.MOB_LOOTING, lootingLevel);
                fakePlayer.getInventory().items.set(fakePlayer.getInventory().selected, lootingSword);
            } else {
                fakePlayer.getInventory().items.set(fakePlayer.getInventory().selected, ItemStack.EMPTY);
            }

            DamageSource damageSource = DamageSource.playerAttack(fakePlayer);

            LootContext.Builder builder = new LootContext.Builder(serverLevel)
                    .withRandom(serverLevel.random)
                    .withParameter(LootContextParams.THIS_ENTITY, temporaryEntity)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, fakePlayer)
                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer)
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)
                    .withLuck(fakePlayer.getLuck());

            return lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY));
        } finally {
            fakePlayer.getInventory().items.set(fakePlayer.getInventory().selected, ItemStack.EMPTY);
        }
    }

    private static List<ItemStack> rollWithoutPlayerKill(ServerLevel serverLevel, BlockPos pos,
                                                          Entity temporaryEntity, LootTable lootTable) {
        DamageSource damageSource = DamageSource.GENERIC;

        LootContext.Builder builder = new LootContext.Builder(serverLevel)
                .withRandom(serverLevel.random)
                .withParameter(LootContextParams.THIS_ENTITY, temporaryEntity)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, null)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, null);

        return lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY));
    }
}
