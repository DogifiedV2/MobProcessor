package com.dog.mobprocessor.container;

import com.dog.mobprocessor.item.UpgradeItem;
import com.dog.mobprocessor.item.UpgradeType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class UpgradeSlot extends SlotItemHandler {

    private final Supplier<IItemHandler> upgradeHandlerSupplier;

    public UpgradeSlot(IItemHandler upgradeHandler, int index, int x, int y) {
        super(upgradeHandler, index, x, y);
        this.upgradeHandlerSupplier = () -> upgradeHandler;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (!(stack.getItem() instanceof UpgradeItem incomingUpgrade)) {
            return false;
        }

        UpgradeType incomingType = incomingUpgrade.getUpgradeType();
        IItemHandler handler = upgradeHandlerSupplier.get();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (i == getSlotIndex()) continue;
            ItemStack existing = handler.getStackInSlot(i);
            if (!existing.isEmpty() && existing.getItem() instanceof UpgradeItem existingUpgrade) {
                if (existingUpgrade.getUpgradeType() == incomingType) {
                    return false;
                }
            }
        }

        return super.mayPlace(stack);
    }
}
