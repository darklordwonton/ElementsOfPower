package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.network.SetSpecialSlot;
import gigaherz.elementsofpower.slots.SlotContainer;
import gigaherz.elementsofpower.slots.SlotMagic;
import gigaherz.elementsofpower.slots.SlotSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEssentializer
        extends Container
{
    protected TileEssentializer tile;

    public ContainerEssentializer(TileEssentializer tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                addSlotToContainer(new SlotMagic(tileEntity.holder,
                        j + i * 2,
                        71 + j * 18, 8 + i * 18));
            }
        }

        addSlotToContainer(new SlotSource(tileEntity, 0, 26, 35));
        addSlotToContainer(new SlotContainer(tileEntity, 1, 134, 35));
        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(playerInventory,
                        j + i * 9 + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            Slot slot = this.inventorySlots.get(i);
            ItemStack newStack = slot.getStack();
            ItemStack current = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(current, newStack))
            {
                current = newStack == null ? null : newStack.copy();
                this.inventoryItemStacks.set(i, current);

                for (ICrafting crafter : this.crafters)
                {
                    if(slot instanceof SlotMagic && crafter instanceof EntityPlayerMP)
                        sendSpecialSlotContents((EntityPlayerMP)crafter, this, i, current);
                    else
                        crafter.sendSlotContents(this, i, current);
                }
            }
        }
    }

    private void sendSpecialSlotContents(EntityPlayerMP crafter, ContainerEssentializer containerEssentializer, int i, ItemStack current)
    {
        ElementsOfPower.channel.sendTo(new SetSpecialSlot(containerEssentializer.windowId, i, current), crafter);
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        if (slotIndex < 8)
        {
            return null;
        }

        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex >= 10)
        {
            boolean itemIsContainer = MagicDatabase.canItemContainMagic(stack);
            boolean itemContainsMagic = itemIsContainer && MagicDatabase.itemContainsMagic(stack);
            boolean itemHasEssence = MagicDatabase.itemHasEssence(stack);

            if (itemContainsMagic)
            {
                startIndex = 8;
                endIndex = 10;
            }
            else if (itemIsContainer)
            {
                startIndex = 9;
                endIndex = 10;
            }
            else if (itemHasEssence)
            {
                startIndex = 8;
                endIndex = 9;
            }
            else if (slotIndex < 37)
            {
                startIndex = 37;
                endIndex = 46;
            }
            else if (slotIndex >= 37 && slotIndex < 39)
            {
                startIndex = 10;
                endIndex = 37;
            }
            else
            {
                return null;
            }
        }
        else
        {
            startIndex = 10;
            endIndex = 46;
        }

        if (!this.mergeItemStack(stack, startIndex, endIndex, false))
        {
            return null;
        }

        if (stack.stackSize == 0)
        {
            slot.putStack(null);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.stackSize == stackCopy.stackSize)
        {
            return null;
        }

        slot.onPickupFromSlot(player, stack);
        return stackCopy;
    }
}
