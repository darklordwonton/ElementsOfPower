package gigaherz.elementsofpower.spells.blocks;

import gigaherz.elementsofpower.common.BlockRegistered;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockFrost extends BlockRegistered
{
    public static final PropertyInteger STRENGTH = PropertyInteger.create("strength", 1, 16);

    public BlockFrost(String name)
    {
        super(name, Material.ICE);
        setHardness(1.0F);
        setSoundType(SoundType.GLASS);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(STRENGTH, 11));
        setTickRandomly(true);
        setLightOpacity(0);
        this.slipperiness = 0.98f;
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }
    
    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, STRENGTH);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(STRENGTH, 16 - meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 16 - state.getValue(STRENGTH);
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }
    
    private void rescheduleUpdate(World worldIn, BlockPos pos, Random rand)
    {
        worldIn.scheduleUpdate(pos, this, 1);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();

        return block == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
    
    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        int density = state.getValue(STRENGTH) - 1;

        if (density <= 0)
        {
            worldIn.setBlockToAir(pos);
        }
        else
        {
            worldIn.setBlockState(pos, state.withProperty(STRENGTH, density));
        }

        worldIn.scheduleUpdate(pos, this, 1);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

}
