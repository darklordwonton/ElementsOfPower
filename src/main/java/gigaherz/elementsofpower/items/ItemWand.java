package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemWand extends ItemGemContainer
{
    public ItemWand(String name)
    {
        super(name);
        setUnlocalizedName(ElementsOfPower.MODID + ".wand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ElementsOfPower.proxy.beginTracking(playerIn, hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
    }
}