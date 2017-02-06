package gigaherz.elementsofpower.spells.blocks;

import java.util.Random;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCrumblingStone extends BlockFalling {
    public static final PropertyInteger STRENGTH = PropertyInteger.create("strength", 1, 11);
    private static final AxisAlignedBB DUMMY_AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    public BlockCrumblingStone(String name)
    {
        this(name, Material.ROCK);
    }

    public BlockCrumblingStone(String name, Material mat)
    {
        super(mat);
        setHardness(1.5F);
        setSoundType(SoundType.STONE);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(STRENGTH, 11));
        setRegistryName(name);
        setUnlocalizedName(ElementsOfPower.MODID + "." + name);
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);

        rescheduleUpdate(worldIn, pos, worldIn.rand);
    }

    private void rescheduleUpdate(World worldIn, BlockPos pos, Random rand)
    {
        worldIn.scheduleUpdate(pos, this, 4 + rand.nextInt(12));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    	super.updateTick(worldIn, pos, state, rand);
    	
    	int density = state.getValue(STRENGTH) - 1;
        if (density <= 0)
            worldIn.setBlockToAir(pos);
        else
            worldIn.setBlockState(pos, state.withProperty(STRENGTH, density));
        
        rescheduleUpdate(worldIn, pos, rand);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(STRENGTH, 11 - meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 11 - state.getValue(STRENGTH);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, STRENGTH);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }
    
    public ItemBlock createItemBlock()
    {
        return (ItemBlock) new ItemBlock(this).setRegistryName(getRegistryName());
    }
    
    @Override
    protected void onStartFalling(EntityFallingBlock fallingEntity)
    {
        fallingEntity.setHurtEntities(true);
    }
}
