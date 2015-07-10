package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.CommonProxy;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.entities.EntityBallBase;
import gigaherz.elementsofpower.models.CustomMeshModel;
import gigaherz.elementsofpower.models.ModelRegistrationHelper;
import gigaherz.elementsofpower.client.render.RenderEntityProvidedStack;
import gigaherz.elementsofpower.client.render.RenderStack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerGuiOverlay() {
        MinecraftForge.EVENT_BUS.register(new GuiOverlayMagicContainer());
    }

    @Override
    public void registerCustomBakedModels() {
        ModelRegistrationHelper helper = ElementsOfPower.modelRegistrationHelper;

        registerCustomItemModel(helper, "wand_lapis");
        registerCustomItemModel(helper, "wand_emerald");
        registerCustomItemModel(helper, "wand_diamond");
        registerCustomItemModel(helper, "wand_creative");
        registerCustomItemModel(helper, "staff_lapis");
        registerCustomItemModel(helper, "staff_emerald");
        registerCustomItemModel(helper, "staff_diamond");
        registerCustomItemModel(helper, "staff_creative");
    }

    public void registerCustomItemModel(ModelRegistrationHelper helper, final String itemName) {

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory");
        IFlexibleBakedModel model = new CustomMeshModel(itemName);

        helper.registerCustomItemModel(loc, model, itemName);
    }

    public void registerCustomBlockModel(ModelRegistrationHelper helper, final String blockName, final String stateName) {

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, stateName);
        IFlexibleBakedModel model = new CustomMeshModel(blockName);

        helper.registerCustomBlockModel(loc, model, blockName);
    }

    @Override
    public void registerRenderers() {

        MinecraftForge.EVENT_BUS.register(new MagicTooltips());

        registerBlockTexture(ElementsOfPower.essentializer, "essentializer");
        registerBlockTexture(ElementsOfPower.dust, "dust");

        registerItemTexture(ElementsOfPower.magicOrb, 0, "orb_fire");
        registerItemTexture(ElementsOfPower.magicOrb, 1, "orb_water");
        registerItemTexture(ElementsOfPower.magicOrb, 2, "orb_air");
        registerItemTexture(ElementsOfPower.magicOrb, 3, "orb_earth");
        registerItemTexture(ElementsOfPower.magicOrb, 4, "orb_light");
        registerItemTexture(ElementsOfPower.magicOrb, 5, "orb_dark");
        registerItemTexture(ElementsOfPower.magicOrb, 6, "orb_life");
        registerItemTexture(ElementsOfPower.magicOrb, 7, "orb_death");
        registerItemTexture(ElementsOfPower.magicWand, 0, "wand_lapis");
        registerItemTexture(ElementsOfPower.magicWand, 1, "wand_emerald");
        registerItemTexture(ElementsOfPower.magicWand, 2, "wand_diamond");
        registerItemTexture(ElementsOfPower.magicWand, 3, "wand_creative");
        registerItemTexture(ElementsOfPower.magicWand, 4, "staff_lapis");
        registerItemTexture(ElementsOfPower.magicWand, 5, "staff_emerald");
        registerItemTexture(ElementsOfPower.magicWand, 6, "staff_diamond");
        registerItemTexture(ElementsOfPower.magicWand, 7, "staff_creative");
        registerItemTexture(ElementsOfPower.magicContainer, 0, "container_lapis");
        registerItemTexture(ElementsOfPower.magicContainer, 1, "container_emerald");
        registerItemTexture(ElementsOfPower.magicContainer, 2, "container_diamond");

        registerEntityRenderingHandler(EntityBallBase.class);
    }

    public void registerBlockTexture(final Block block, final String blockName) {
        registerBlockTexture(block, 0, blockName);
    }

    public void registerBlockTexture(final Block block, int meta, final String blockName) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, "inventory"));
    }

    public void registerItemTexture(final Item item, int meta, final String itemName) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory"));
        ModelBakery.addVariantName(item, ElementsOfPower.MODID + ":" + itemName);
    }

    public void registerEntityRenderingHandler(Class<? extends Entity> entityClass)
    {
        registerEntityRenderingHandler(entityClass,
                new RenderEntityProvidedStack(
                        Minecraft.getMinecraft().getRenderManager(),
                        Minecraft.getMinecraft().getRenderItem()));
    }

    public void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render render)
    {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
    }
}
