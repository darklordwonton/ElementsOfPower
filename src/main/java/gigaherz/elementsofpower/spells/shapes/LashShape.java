package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LashShape extends SpellShape {

	@Override
	public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast) {
		return cast;
	}

	@Override
	public boolean isInstant() {
		return true;
	}
	
	@Override
	public void spellTick (Spellcast cast) {
		EntityPlayer player = cast.getCastingPlayer();
		SpellEffect effect = cast.getEffect();
		Vec3d lookVec = new Vec3d(player.getLookVec().xCoord,0,player.getLookVec().zCoord).normalize();
		int length = cast.getDamageForce() * 3;
		int radius = (int) (Math.sqrt(cast.getDamageForce()) + 0.5) - 1;
		Vec3d pos = new Vec3d(player.getPosition().add(lookVec.xCoord * length, lookVec.yCoord * length, lookVec.zCoord * length));
		cast.end = pos;
		
		if (!effect.processEntitiesAroundBefore(cast, pos))
			return;
        
		for (int x = -radius; x <= radius; x++)
		for (int y = -radius; y <= radius; y++)
		for (int z = 0; z <= length; z++) {
			Vec3d offset = new Vec3d(x,y,z + 1.5).rotateYaw(-(float)Math.toRadians(player.getPitchYaw().y));
			BlockPos bp = new BlockPos(player.getPositionVector().add(offset));
			AxisAlignedBB aabb = new AxisAlignedBB(bp);
			for (Entity e : cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb))
				effect.processDirectHit(cast, e, player.getPositionVector());

			IBlockState currentState = cast.world.getBlockState(bp);
			
			effect.processBlockWithinRadius(cast, bp, currentState, Math.max((float) Math.sqrt(bp.distanceSq(new BlockPos(pos))) - cast.getDamageForce(), 0f),null);
		}	
	}

}
