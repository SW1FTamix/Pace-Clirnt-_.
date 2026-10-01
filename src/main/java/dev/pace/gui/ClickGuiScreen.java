package dev.pace.gui;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.module.ModuleManager;
import dev.pace.module.impl.ClickGui;
import dev.pace.setting.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controls:
 *  - Left click a module: toggle it
 *  - Right click a module: open/close its settings
 *  - Key row: left click then press a key to bind; Backspace/Delete clears; Esc cancels
 *  - Hover a module and press Backspace: clears its keybind
 *  - Right Shift / Esc: close
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 120, HEAD = 16, ROW = 14;
    private static final Set<Module> OPEN = new HashSet<>();

    private record Hit(int x, int y, int w, int h, Module module, Runnable left, Runnable right) {}

    private final List<Hit> hits = new ArrayList<>();
    private NumSetting dragging;
    private int dragX;
    private Module listening;
    private int lastMx, lastMy;

    public ClickGuiScreen() { super(Component.literal("Pace Client")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        lastMx = mx; lastMy = my;
        g.fill(0, 0, width, height, 0x66000000);
        if (dragging != null) {
            double f = Math.max(0, Math.min(1, (mx - dragX) / (double) W));
            dragging.set(dragging.min + (dragging.max - dragging.min) * f);
        }
        hits.clear();
        int x = 12;
        for (Category c : Category.values()) {
            int y = 12;
            g.fill(x, y, x + W, y + HEAD, 0xFF14141F);
            g.drawString(font, c.label, x + 5, y + 4, ClickGui.light(), true);
            y += HEAD;
            for (Module m : ModuleManager.INSTANCE.in(c)) {
                boolean hover = mx >= x && mx < x + W && my >= y && my < y + ROW;
                int bg = m.isEnabled() ? ClickGui.accent() : hover ? 0xFF2E2E45 : 0xFF22222F;
                g.fill(x, y, x + W, y + ROW, bg);
                g.drawString(font, m.name, x + 5, y + 3, 0xFFFFFFFF, true);
                g.drawString(font, OPEN.contains(m) ? "-" : "+", x + W - 10, y + 3, 0xFFAAAAAA, true);
                final Module mm = m;
                hits.add(new Hit(x, y, W, ROW, m, mm::toggle, () -> { if (!OPEN.remove(mm)) OPEN.add(mm); }));
                y += ROW;
                if (OPEN.contains(m)) {
                    // keybind row
                    String keyText = listening == m ? "Key: press a key..." : "Key: " + keyName(m.getKey());
                    row(g, x, y, 0xFF1A1A26, keyText);
                    hits.add(new Hit(x, y, W, ROW, null, () -> listening = mm, () -> mm.setKey(-1)));
                    y += ROW;
                    for (Setting s : m.getSettings()) y = drawSetting(g, x, y, s);
                }
            }
            x += W + 8;
        }
    }

    private int drawSetting(GuiGraphics g, int x, int y, Setting s) {
        if (s instanceof BoolSetting b) {
            row(g, x, y, b.get() ? ClickGui.dark() : 0xFF1A1A26, s.name + ": " + (b.get() ? "ON" : "OFF"));
            hits.add(new Hit(x, y, W, ROW, null, b::toggle, null));
        } else if (s instanceof NumSetting n) {
            row(g, x, y, 0xFF1A1A26, "");
            int fill = (int) (W * (n.get() - n.min) / (n.max - n.min));
            g.fill(x, y, x + fill, y + ROW, ClickGui.dark());
            g.drawString(font, s.name + ": " + fmt(n.get()), x + 5, y + 3, 0xFFDDDDDD, true);
            final int fx = x;
            hits.add(new Hit(x, y, W, ROW, null, () -> { dragging = n; dragX = fx; }, null));
        } else if (s instanceof ModeSetting m) {
            row(g, x, y, 0xFF1A1A26, s.name + ": " + m.get());
            hits.add(new Hit(x, y, W, ROW, null, m::cycle, m::cycle));
        }
        return y + ROW;
    }

    private void row(GuiGraphics g, int x, int y, int color, String text) {
        g.fill(x, y, x + W, y + ROW, color);
        if (!text.isEmpty()) g.drawString(font, text, x + 5, y + 3, 0xFFDDDDDD, true);
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.format("%.1f", v);
    }

    private static String keyName(int key) {
        if (key <= 0) return "None";
        String n = GLFW.glfwGetKeyName(key, 0);
        if (n != null) return n.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            default -> (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) ? "F" + (key - GLFW.GLFW_KEY_F1 + 1) : "Key " + key;
        };
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        listening = null;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (event.x() >= h.x && event.x() < h.x + h.w && event.y() >= h.y && event.y() < h.y + h.h) {
                if (event.button() == 0 && h.left != null) h.left.run();
                else if (event.button() == 1 && h.right != null) h.right.run();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int k = event.key();
        if (listening != null) {
            if (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) listening.setKey(-1);
            else if (k != GLFW.GLFW_KEY_ESCAPE) listening.setKey(k);
            listening = null;
            return true;
        }
        if (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) {
            for (Hit h : hits) {
                if (h.module != null && lastMx >= h.x && lastMx < h.x + h.w && lastMy >= h.y && lastMy < h.y + h.h) {
                    h.module.setKey(-1);
                    return true;
                }
            }
        }
        if (k == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; }
        return super.keyPressed(event);
    }

    @Override public void removed() { ModuleManager.INSTANCE.saveConfig(); }
}
