package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.NumSetting;
import dev.pace.util.Compat;
import dev.pace.util.InvUtil;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

/** Re-equips armor into empty armor slots (e.g. after a piece breaks). */
public class AutoArmor extends Module {
    private static final EquipmentSlot[] SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    private final NumSetting delay = add(new NumSetting("Delay", 2, 0, 20, 1));
    private int timer;

    public AutoArmor() { super("AutoArmor", "Equips armor from your inventory into empty slots", Category.COMBAT); }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (mc.screen != null && !(mc.screen instanceof InventoryScreen)) return;
        var inv = mc.player.getInventory();
        for (EquipmentSlot es : SLOTS) {
            if (!mc.player.getItemBySlot(es).isEmpty()) continue;
            for (int i = 0; i < 36; i++) {
                var st = inv.getItem(i);
                if (st.isEmpty() || st.is(Items.ELYTRA) || st.is(Items.CARVED_PUMPKIN)) continue;
                var eq = st.get(DataComponents.EQUIPPABLE);
                if (eq != null && eq.slot() == es) {
                    Compat.click(mc.player.inventoryMenu.containerId, InvUtil.menuSlot(i), 0, ClickType.QUICK_MOVE);
                    timer = delay.getInt();
                    return;
                }
            }
        }
    }
}
