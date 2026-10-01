package dev.pace.setting;

public class BoolSetting extends Setting {
    private boolean value;
    public BoolSetting(String name, boolean def) { super(name); this.value = def; }
    public boolean get() { return value; }
    public void set(boolean v) { value = v; }
    public void toggle() { value = !value; }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof Boolean b) value = b; }
}
