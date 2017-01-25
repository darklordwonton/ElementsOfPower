package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.blocks.BlockDust;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class DustEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x000000;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 10;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        float damage = 5 * cast.getDamageForce();

        entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(cast.player, cast.player), damage);
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

        damageEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb));
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.WATER_SPLASH,
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

        Block block = currentState.getBlock();

        if (block == Blocks.AIR)
        {
            cast.world.setBlockState(blockPos, ElementsOfPower.dust.getDefaultState().withProperty(BlockDust.DENSITY, 16));
        }
    }
}
