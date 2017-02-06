package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.blocks.BlockDust;
import gigaherz.elementsofpower.spells.shapes.ConeShape;
import gigaherz.elementsofpower.spells.shapes.LashShape;
import gigaherz.elementsofpower.spells.shapes.SingleShape;
import gigaherz.elementsofpower.spells.shapes.BeamShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class EarthEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x663300;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * cast.getDamageForce();
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 10;
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
        	entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(cast.player, cast.player), damage);        entity.addVelocity(0, 0.2 * cast.getDamageForce(), 0);
        if (entity instanceof EntityPlayerMP)
        {
            ElementsOfPower.channel.sendTo(new AddVelocityPlayer(0, 0.2 * cast.getDamageForce(), 0), (EntityPlayerMP) entity);
        }
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - Math.sqrt(cast.getDamageForce()),
                hitVec.yCoord - cast.getDamageForce(),
                hitVec.zCoord - Math.sqrt(cast.getDamageForce()),
                hitVec.xCoord + Math.sqrt(cast.getDamageForce()),
                hitVec.yCoord + cast.getDamageForce(),
                hitVec.zCoord + Math.sqrt(cast.getDamageForce()));

        damageEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb));

        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {
    }

    @Override
	public void damageEntities(Spellcast cast, Vec3d hitVec, List<? extends Entity> living) {
        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, cast.getDamageForce() - ll);

            e.attackEntityFrom(DamageSource.causeIndirectMagicDamage(cast.player, cast.player), (float)(2 + 2 * lv));
            e.addVelocity(0, 0.2 * cast.getDamageForce(), 0);
            if (e instanceof EntityPlayerMP)
            {
                ElementsOfPower.channel.sendTo(new AddVelocityPlayer(0, 0.2 * cast.getDamageForce(), 0), (EntityPlayerMP) e);
            }
        }
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
        Random rand = new Random();
        int limit = Math.max(1,cast.getDamageForce() - (int)r + rand.nextInt(Math.max(1, (int)(cast.getDamageForce() - 3 * r))));
        
        if (blockPos.getY() == cast.end.yCoord)
        	for (int i = 1 - limit; i < limit; i++) {
        		if (cast.world.getBlockState(blockPos.add(0,i,0)) == Blocks.AIR.getDefaultState())
        				cast.world.setBlockState(blockPos.add(0, i, 0), ElementsOfPower.crumble.getDefaultState());
        	}
    }
}
