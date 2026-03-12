package com.dog.mobprocessor.container;

import com.dog.mobprocessor.item.AugmentEggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class MobProcessorSlot extends SlotItemHandler {

    private final IntSupplier unlockedSlotCount;

    public MobProcessorSlot(IItemHandler handler, int index, int x, int y, IntSupplier unlockedSlotCount) {
        super(handler, index, x, y);
        this.unlockedSlotCount = unlockedSlotCount;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (getSlotIndex() >= unlockedSlotCount.getAsInt()) {
            return false;
        }
        return stack.getItem() instanceof AugmentEggItem && super.mayPlace(stack);
    }

    public boolean isLocked() {
        return getSlotIndex() >= unlockedSlotCount.getAsInt();
    }
}
