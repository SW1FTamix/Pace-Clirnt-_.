package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.util.Friends;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/** Bind a key to this module: pressing it while looking at a player adds/removes them as a friend. */
public class FriendKey extends Module {
    public FriendKey() { super("FriendKey", "Keybind: look at a player to add/remove a friend", Category.CLIENT); }

    @Override public void toggle() {
        if (mc.screen != null || mc.player == null) return;
        if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof Player p) {
            String name = p.getName().getString();
            boolean now = Friends.toggle(name);
            mc.player.displayClientMessage(Component.literal(name + (now ? " added to friends" : " removed from friends")), true);
        }
    }
    @Override public void setEnabled(boolean v) { /* action module, never stays enabled */ }
}
