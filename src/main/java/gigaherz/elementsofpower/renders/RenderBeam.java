package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.entities.EntityBeamBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class RenderBeam extends Render<EntityBeamBase>
{
    private final RenderItem renderItem;
    private final ItemStack stack;

    public RenderBeam(RenderManager renderManager, ItemStack stack, RenderItem renderItem)
    {
        super(renderManager);
        this.renderItem = renderItem;
        this.stack = stack;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doe
     */
    public void doRender(EntityBeamBase entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
        float scale = 0.15f;

        Vec3 ep = entity.getEndPoint();
        if (ep == null)
            ep = new Vec3(x, y, z);

        double dx = ep.xCoord - entity.posX;
        double dy = ep.yCoord - entity.posY;
        double dz = ep.zCoord - entity.posZ;
        double d10x = dx / 10;
        double d10y = dy / 10;
        double d10z = dz / 10;

        GlStateManager.pushMatrix();
        for (int i = 0; i <= 10; i++)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (x + d10x * i), (float) (y + d10y * i), (float) (z + d10z * i));
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            this.bindTexture(TextureMap.locationBlocksTexture);
            this.renderItem.func_181564_a(stack, TransformType.NONE);
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, p_76986_8_, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityBeamBase entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}