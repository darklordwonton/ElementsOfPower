package gigaherz.elementsofpower.renders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class RenderingStuffs
{
    // A vertex format with normals that doesn't break the OBJ loader.
    // FIXME: Replace with DefaultvertexFormats.POSITION_TEX_COLOR_NORMAL when it works.
    public static final VertexFormat CUSTOM_FORMAT;

    static
    {
        CUSTOM_FORMAT = new VertexFormat();
        CUSTOM_FORMAT.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        CUSTOM_FORMAT.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR,    4));
        CUSTOM_FORMAT.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV,       2));
        CUSTOM_FORMAT.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE,  VertexFormatElement.EnumUsage.NORMAL,   3));
        CUSTOM_FORMAT.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE,  VertexFormatElement.EnumUsage.PADDING,  1));
    }

    public static void renderModel(IFlexibleBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
        for (BakedQuad bakedquad : model.getGeneralQuads())
            LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
        tessellator.draw();
    }

    public static IFlexibleBakedModel loadModel(String resourceName)
    {

        try
        {
            TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();
            IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation(resourceName));
            return mod.bake(mod.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT,
                    (location) -> textures.getAtlasSprite(location.toString()));
        }
        catch(IOException e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }
}