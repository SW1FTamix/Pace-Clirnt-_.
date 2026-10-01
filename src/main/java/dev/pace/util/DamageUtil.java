package dev.pace.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Approximate explosion damage (crystal power 6, anchor power 5). Ignores enchants/effects. */
public final class DamageUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static float explosion(Vec3 center, LivingEntity e, float power) {
        double radius = power * 2.0;
        double dist = Math.sqrt(e.distanceToSqr(center)) / radius;
        if (dist > 1.0) return 0f;
        double impact = (1.0 - dist) * exposure(center, e);
        float dmg = (float) ((impact * impact + impact) / 2.0 * 7.0 * radius + 1.0);

        Difficulty diff = mc.level.getDifficulty();
        if (diff == Difficulty.PEACEFUL) return 0f;
        if (diff == Difficulty.EASY) dmg = Math.min(dmg / 2f + 1f, dmg);
        else if (diff == Difficulty.HARD) dmg *= 1.5f;

        float armor = e.getArmorValue();
        float tough = (float) e.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float f = 2f + tough / 4f;
        float g = Mth.clamp(armor - dmg / f, armor * 0.2f, 20f);
        dmg *= 1f - g / 25f;
        return Math.max(dmg, 0f);
    }

    private static float exposure(Vec3 center, LivingEntity e) {
        AABB b = e.getBoundingBox();
        double dx = 1.0 / ((b.maxX - b.minX) * 2.0 + 1.0);
        double dy = 1.0 / ((b.maxY - b.minY) * 2.0 + 1.0);
        double dz = 1.0 / ((b.maxZ - b.minZ) * 2.0 + 1.0);
        double ox = (1.0 - Math.floor(1.0 / dx) * dx) / 2.0;
        double oz = (1.0 - Math.floor(1.0 / dz) * dz) / 2.0;
        int hit = 0, total = 0;
        for (double x = 0; x <= 1.0; x += dx)
            for (double y = 0; y <= 1.0; y += dy)
                for (double z = 0; z <= 1.0; z += dz) {
                    Vec3 p = new Vec3(Mth.lerp(x, b.minX, b.maxX) + ox, Mth.lerp(y, b.minY, b.maxY), Mth.lerp(z, b.minZ, b.maxZ) + oz);
                    var r = mc.level.clip(new ClipContext(p, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
                    if (r.getType() == HitResult.Type.MISS) hit++;
                    total++;
                }
        return total == 0 ? 0f : (float) hit / total;
    }

    /** True if taking this much damage is acceptable for the local player. */
    public static boolean safe(float selfDamage, double maxSelf, boolean antiSuicide) {
        if (selfDamage > maxSelf) return false;
        if (antiSuicide) {
            float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            return selfDamage < hp - 1f;
        }
        return true;
    }
}
