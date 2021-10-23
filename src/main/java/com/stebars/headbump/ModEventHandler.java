package com.stebars.headbump;

import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;


@EventBusSubscriber
public class ModEventHandler {

	@SubscribeEvent
	public static void headBump(final PlayerTickEvent event) {
		PlayerEntity player = event.player;
		if (player.noPhysics) return;

		Vector3d v = player.getDeltaMovement();
		if (v.y <= 0) return;

		Vector3d adjusted = collide(v, player);
		if (adjusted.y == v.y) return;

		double diff = v.y - adjusted.y; // This is magnitude of hit. It's ~0.05 in a 3-block gap, 0.42 in a 2-block gap
		if (diff < 0.03) return;

		float damage = diff < .2 ? .1F : .2F; // so either 5% or 10% of a heart
		player.hurt(DamageSource.FALLING_BLOCK, damage);
		//player.playSound(SoundEvents.PLAYER_SMALL_FALL, 1.0F, 1.0F);
	}

	// Copied from Entity.collide, because it's private
	private static Vector3d collide(Vector3d p_213306_1_, Entity entity) {
		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
		ISelectionContext iselectioncontext = ISelectionContext.of(entity);
		VoxelShape voxelshape = entity.level.getWorldBorder().getCollisionShape();
		Stream<VoxelShape> stream = VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(axisalignedbb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
		Stream<VoxelShape> stream1 = entity.level.getEntityCollisions(entity, axisalignedbb.expandTowards(p_213306_1_), (p_233561_0_) -> {
			return true;
		});
		ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
		Vector3d vector3d = p_213306_1_.lengthSqr() == 0.0D ? p_213306_1_ : Entity.collideBoundingBoxHeuristically(entity, p_213306_1_, axisalignedbb, entity.level, iselectioncontext, reuseablestream);
		boolean flag = p_213306_1_.x != vector3d.x;
		boolean flag1 = p_213306_1_.y != vector3d.y;
		boolean flag2 = p_213306_1_.z != vector3d.z;
		boolean flag3 = entity.isOnGround() || flag1 && p_213306_1_.y < 0.0D;
		if (entity.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
			Vector3d vector3d1 = Entity.collideBoundingBoxHeuristically(entity, new Vector3d(p_213306_1_.x, (double)entity.maxUpStep, p_213306_1_.z), axisalignedbb, entity.level, iselectioncontext, reuseablestream);
			Vector3d vector3d2 = Entity.collideBoundingBoxHeuristically(entity, new Vector3d(0.0D, (double)entity.maxUpStep, 0.0D), axisalignedbb.expandTowards(p_213306_1_.x, 0.0D, p_213306_1_.z), entity.level, iselectioncontext, reuseablestream);
			if (vector3d2.y < (double)entity.maxUpStep) {
				Vector3d vector3d3 = Entity.collideBoundingBoxHeuristically(entity, new Vector3d(p_213306_1_.x, 0.0D, p_213306_1_.z), axisalignedbb.move(vector3d2), entity.level, iselectioncontext, reuseablestream).add(vector3d2);
				if (Entity.getHorizontalDistanceSqr(vector3d3) > Entity.getHorizontalDistanceSqr(vector3d1)) {
					vector3d1 = vector3d3;
				}
			}

			if (Entity.getHorizontalDistanceSqr(vector3d1) > Entity.getHorizontalDistanceSqr(vector3d)) {
				return vector3d1.add(Entity.collideBoundingBoxHeuristically(entity, new Vector3d(0.0D, -vector3d1.y + p_213306_1_.y, 0.0D), axisalignedbb.move(vector3d1), entity.level, iselectioncontext, reuseablestream));
			}
		}

		return vector3d;
	}

}
