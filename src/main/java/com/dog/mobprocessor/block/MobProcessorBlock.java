package com.dog.mobprocessor.block;

import com.dog.mobprocessor.block.entity.MobProcessorBlockEntity;
import com.dog.mobprocessor.init.ModBlockEntities;
import com.dog.mobprocessor.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class MobProcessorBlock extends BaseEntityBlock {

    public MobProcessorBlock() {
        super(Properties.of(Material.METAL)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobProcessorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MOB_PROCESSOR.get(), MobProcessorBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MobProcessorBlockEntity mobProcessor) {
                NetworkHooks.openGui(serverPlayer, mobProcessor, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MobProcessorBlockEntity mobProcessor) {
            if (!level.isClientSide() && player.isCreative() && !mobProcessor.isEmpty()) {
                ItemStack itemStack = new ItemStack(ModBlocks.MOB_PROCESSOR.get());
                CompoundTag blockEntityTag = new CompoundTag();
                mobProcessor.saveToItemTag(blockEntityTag);
                if (!blockEntityTag.isEmpty()) {
                    itemStack.addTagElement("BlockEntityTag", blockEntityTag);
                }

                ItemEntity itemEntity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
