package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.MagicAmounts;
import gigaherz.elementsofpower.MagicDatabase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemMagicContainer extends Item {
    private static final String[] subNames = {
            "lapisContainer", "emeraldContainer", "diamondContainer"
    };

    public ItemStack getStack(int count, int damageValue) {
        return new ItemStack(this, count, damageValue);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length) {
            sub = 0;
        }

        return getUnlocalizedName() + "." + subNames[sub];
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
        for (int meta = 0; meta < subNames.length; meta++) {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        return amounts != null && !amounts.isEmpty();
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {

        if (hasEffect(stack))
            return EnumRarity.RARE;
        return EnumRarity.UNCOMMON;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltipList, boolean showAdvancedInfo) {
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        if (amounts == null) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            if (amounts.amounts[i] == 0) {
                continue;
            }

            String magicName = MagicDatabase.getMagicName(i);
            String str = String.format("%s x%d", magicName, amounts.amounts[i]);
            tooltipList.add(str);
        }
    }
}