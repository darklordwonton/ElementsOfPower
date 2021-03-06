package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public abstract class SpellEffect
{
    public abstract int getColor(Spellcast cast);

    public abstract int getDuration(Spellcast cast);

    public abstract int getInterval(Spellcast cast);

    public int getForceModifier(Spellcast cast)
    {
        return 0;
    }

    public abstract void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec);

    public abstract boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec);

    public abstract void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec);

    public abstract void spawnBallParticles(Spellcast cast, RayTraceResult mop);

    public abstract void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float distance, @Nullable RayTraceResult mop);

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

            if (e != cast.player)
            	e.attackEntityFrom(DamageSource.causeIndirectMagicDamage(cast.player, cast.player), (float)(4 + 4 * lv));
        }
	}

    protected static void causePotionEffect(Spellcast cast, EntityLivingBase e, Potion potion, int amplifier, double distance, double durationBase)
    {
        if (potion.isInstant())
        {
            potion.affectEntity(cast.projectile, cast.player, e, amplifier, distance);
        }
        else
        {
            int j = (int) (distance * durationBase + 0.5D);

            if (j > 20)
            {
                e.addPotionEffect(new PotionEffect(potion, j, amplifier));
            }
        }
    }
}
