package dev.pace.module;

public enum Category {
    COMBAT("Combat"), CLIENT("Client");
    public final String label;
    Category(String label) { this.label = label; }
}
