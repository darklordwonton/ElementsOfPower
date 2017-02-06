package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.blocks.BlockFrost;
import gigaherz.elementsofpower.spells.blocks.BlockMist;
import gigaherz.elementsofpower.spells.shapes.BeamShape;
import gigaherz.elementsofpower.spells.shapes.ConeShape;
import gigaherz.elementsofpower.spells.shapes.LashShape;
import gigaherz.elementsofpower.spells.shapes.SingleShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nullable;

public class FrostEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x0080FF;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * cast.getDamageForce();
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 8;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        float damage = 4 + 4 * cast.getDamageForce();
		if (cast.getShape() instanceof ConeShape || cast.getShape() instanceof BeamShape || cast.getShape() instanceof LashShape)
			damage = damage / 2;
        if (cast.getShape() instanceof SingleShape)
        	damage *= 2;
        
        if (entity != cast.player)
        	entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(cast.player, cast.player), damage);
	    freezeEntity(entity,(int)(cast.getDamageForce() / 2f + 0.5));
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - cast.getDamageForce(),
                hitVec.yCoord - cast.getDamageForce(),
                hitVec.zCoord - cast.getDamageForce(),
                hitVec.xCoord + cast.getDamageForce(),
                hitVec.yCoord + cast.getDamageForce(),
                hitVec.zCoord + cast.getDamageForce());

        freezeEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb));
        damageEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb));
    }
    
	public void freezeEntities(Spellcast cast, Vec3d hitVec, List<? extends Entity> living) {
        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            if (e != cast.player)
            	freezeEntity(e,(int)(cast.getDamageForce() / 4f + 0.5));
        }
	}
	
	public void freezeEntity (Entity entity, int strength) {
		for (int x = -1; x <= 1; x++)
			for (int z = -1; z <= 1; z++)
				for (int y = 0; y <= (int) entity.height + 1; y++)
					if (entity.getEntityWorld().isAirBlock(entity.getPosition().add(x,y,z)))
						entity.getEntityWorld().setBlockState(entity.getPosition().add(x,y,z), ElementsOfPower.frost.getDefaultState().withProperty(BlockFrost.STRENGTH, Math.max(Math.min(strength,16),1)));
	}

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.SNOWBALL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        World world = cast.world;
        Block block = currentState.getBlock();

        int layers = (int) Math.min(cast.getDamageForce() - r, 7);

        if (block == Blocks.FIRE)
        {
            world.setBlockToAir(blockPos);
        }
        else if (layers > 0)
        {
            if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
            {
                if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.COBBLESTONE.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState());
                }
                return;
            }
            else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
            {
                if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.PACKED_ICE.getDefaultState());
                }
                return;
            }
            else if (!Blocks.SNOW_LAYER.canPlaceBlockOnSide(world, blockPos, EnumFacing.UP))
            {
                return;
            }

            IBlockState below = world.getBlockState(blockPos.down());
            if (below.getBlock() == Blocks.SNOW_LAYER)
            {
                if (below.getValue(BlockSnow.LAYERS) < 8)
                {
                    blockPos = blockPos.down();
                }
            }

            while (layers > 0)
            {
                currentState = world.getBlockState(blockPos);
                block = currentState.getBlock();

                if (block == Blocks.SNOW_LAYER)
                {
                    int l = currentState.getValue(BlockSnow.LAYERS);
                    if (l == 8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    world.setBlockState(blockPos, currentState.withProperty(BlockSnow.LAYERS, l));
                    layers -= add;
                }
                else if (block == Blocks.AIR)
                {
                    int add = Math.min(8, layers);
                    world.setBlockState(blockPos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, add));
                    layers -= add;
                }
                else
                {
                    break;
                }
            }
        }
    }
}
