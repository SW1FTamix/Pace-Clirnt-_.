package dev.pace.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PlaceUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    /** Right-clicks the given face of the given block with the given hand. */
    public static boolean interact(BlockPos pos, Direction face, InteractionHand hand) {
        Vec3 hit = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
        var result = mc.gameMode.useItemOn(mc.player, hand, new BlockHitResult(hit, face, pos, false));
        if (result.consumesAction()) {
            mc.player.swing(hand);
            return true;
        }
        return false;
    }
}
