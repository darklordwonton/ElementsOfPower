package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ModelRegistrationHelper {

    protected List<ResourceLocation> itemTextures;
    protected List<ResourceLocation> blockTextures;

    protected Map<ResourceLocation, IFlexibleBakedModel> itemsToInject;
    protected Map<ResourceLocation, IFlexibleBakedModel> blocksToInject;

    public ModelRegistrationHelper() {
        itemTextures = new ArrayList<ResourceLocation>();
        blockTextures = new ArrayList<ResourceLocation>();
        itemsToInject = new Hashtable<ResourceLocation, IFlexibleBakedModel>();
        blocksToInject = new Hashtable<ResourceLocation, IFlexibleBakedModel>();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void registerCustomItemModel(ResourceLocation resourceLocation, IFlexibleBakedModel bakedModel, final String itemName) {
        itemsToInject.put(resourceLocation, bakedModel);
    }

    public void registerCustomBlockModel(ResourceLocation resourceLocation, IFlexibleBakedModel bakedModel, final String blockName) {
        blocksToInject.put(resourceLocation, bakedModel);
    }

    public void registerItemSprite(ResourceLocation resourceLocation) {
        if (!itemTextures.contains(resourceLocation))
            itemTextures.add(resourceLocation);
    }

    public void registerBlockSprite(ResourceLocation resourceLocation) {
        if (!blockTextures.contains(resourceLocation))
            blockTextures.add(resourceLocation);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent event) {
        if (event.map == Minecraft.getMinecraft().getTextureMapBlocks()) {
            registerSprites(event.map, blockTextures);
        } else {
            registerSprites(event.map, itemTextures);
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        registerCustomBakedModels(event.modelManager, event.modelRegistry);
    }

    protected void registerCustomBakedModels(
            ModelManager modelManager,
            IRegistry modelRegistry) {

        for (Map.Entry<ResourceLocation, IFlexibleBakedModel> entry : itemsToInject.entrySet()) {

            ResourceLocation loc = entry.getKey();
            IFlexibleBakedModel model = entry.getValue();

            if (model instanceof IInitializeBakedModel) {

                IInitializeBakedModel initializeModel = (IInitializeBakedModel) model;

                TRSRTransformation thirdPerson = TRSRTransformation.identity();
                TRSRTransformation firstPerson = TRSRTransformation.identity();
                TRSRTransformation head = TRSRTransformation.identity();
                TRSRTransformation gui = TRSRTransformation.identity();

                ResourceLocation icon = new ResourceLocation(loc.getResourceDomain(), "item/" + loc.getResourcePath());
                ModelBlock modelblock = loadModelResource(icon);
                if (modelblock != null) {
                    thirdPerson = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getThirdPersonTransform());
                    firstPerson = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getFirstPersonTransform());
                    head = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getHeadTransform());
                    gui = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getInGuiTransform());
                }

                initializeModel.initialize(thirdPerson, firstPerson, head, gui, icon, modelManager);
            }

            modelRegistry.putObject(loc, model);
        }

        for (Map.Entry<ResourceLocation, IFlexibleBakedModel> entry : blocksToInject.entrySet()) {

            ResourceLocation loc = entry.getKey();
            IFlexibleBakedModel model = entry.getValue();

            if (model instanceof IInitializeBakedModel) {
                IInitializeBakedModel initializeModel = (IInitializeBakedModel) model;

                TRSRTransformation thirdPerson = TRSRTransformation.identity();
                TRSRTransformation firstPerson = TRSRTransformation.identity();
                TRSRTransformation head = TRSRTransformation.identity();
                TRSRTransformation gui = TRSRTransformation.identity();

                ResourceLocation icon = new ResourceLocation(loc.getResourceDomain(), "block/" + loc.getResourcePath());
                ModelBlock modelblock = loadModelResource(icon);
                if (modelblock != null) {
                    thirdPerson = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getThirdPersonTransform());
                    firstPerson = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getFirstPersonTransform());
                    head = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getHeadTransform());
                    gui = new net.minecraftforge.client.model.TRSRTransformation(modelblock.getInGuiTransform());
                }

                initializeModel.initialize(thirdPerson, firstPerson, head, gui, icon, modelManager);
            }

            modelRegistry.putObject(loc, model);
        }
    }

    protected void registerSprites(TextureMap map, List<ResourceLocation> texturesToRegister) {
        for (ResourceLocation loc : texturesToRegister) {
            map.registerSprite(loc);
        }
    }

    protected ResourceLocation getModelLocation(ResourceLocation loc) {
        return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
    }

    protected ModelBlock loadModelResource(Map<ResourceLocation, ModelBlock> map, final ResourceLocation loc) {
        Reader reader = null;
        ModelBlock modelblock = map.get(loc);

        if (modelblock != null)
            return modelblock;

        if (loc.getResourcePath().startsWith("builtin/"))
            return null;

        try {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getModelLocation(loc));
            if (iresource != null) {
                reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

                modelblock = ModelBlock.deserialize(reader);
                modelblock.name = loc.toString();
                map.put(loc, modelblock);

                ResourceLocation parentLoc = modelblock.getParentLocation();
                if (parentLoc != null) {

                    ModelBlock parentModel = loadModelResource(map, parentLoc);
                    if (parentModel != null) {
                        modelblock.getParentFromMap(map);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelblock;
    }

    protected ModelBlock loadModelResource(final ResourceLocation loc) {
        return loadModelResource(new Hashtable<ResourceLocation, ModelBlock>(), loc);
    }

}