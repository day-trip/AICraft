package com.daytrip.aicraft.navigation;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlayerMoveController extends AIController {
    private Vec3 target = null;

    public PlayerMoveController(LocalPlayer player) {
        super(player);
    }

    public void init(Vec3 target) {
        this.target = target;
    }

    @Override
    void tick() {
        if (target == null) {
            return;
        }
        PlayerLookController lookController = Navigator.getInstance().getLookController();
        lookController.init(target);
        if (lookController.isActive()) {
            return;
        }
        if (player.distanceToSqr(target) < 2.500000277905201E-7) {
            player.zza = 0;
            return;
        }
        player.zza = 1;
        player.setSprinting(true);
        player.setJumping(false);

        double d = target.x - player.getX();
        double e = target.z - player.getZ();
        double o = target.y - player.getY();
        BlockPos blockPos = player.blockPosition();
        BlockState blockState = player.level().getBlockState(blockPos);
        VoxelShape voxelShape = blockState.getCollisionShape(player.level(), blockPos);
        if (o > (double)this.player.maxUpStep() && d * d + e * e < (double)Math.max(1.0F, this.player.getBbWidth()) || !voxelShape.isEmpty() && player.getY() < voxelShape.max(Direction.Axis.Y) + (double)blockPos.getY() && !blockState.is(BlockTags.DOORS) && !blockState.is(BlockTags.FENCES)) {
            player.setJumping(true);
            System.out.println("JUMP");
        }
    }

    @Override
    boolean isActive() {
        return target != null;
    }
}
