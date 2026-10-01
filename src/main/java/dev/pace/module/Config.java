package dev.pace.module;

import com.google.gson.*;
import dev.pace.PaceClient;
import dev.pace.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("pace-client.json"); }

    public static void save(List<Module> modules) {
        try {
            JsonObject root = new JsonObject();
            for (Module m : modules) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.isEnabled());
                o.addProperty("key", m.getKey());
                JsonObject s = new JsonObject();
                for (Setting st : m.getSettings()) {
                    Object v = st.save();
                    if (v instanceof Boolean b) s.addProperty(st.name, b);
                    else if (v instanceof Number n) s.addProperty(st.name, n);
                    else s.addProperty(st.name, String.valueOf(v));
                }
                o.add("settings", s);
                root.add(m.name, o);
            }
            JsonArray fr = new JsonArray();
            for (String f : dev.pace.util.Friends.all()) fr.add(f);
            root.add("_friends", fr);
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) {
            PaceClient.LOG.error("Failed to save config", e);
        }
    }

    public static void load(List<Module> modules) {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            for (Module m : modules) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("key")) m.setKey(o.get("key").getAsInt());
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting st : m.getSettings()) {
                        if (!s.has(st.name)) continue;
                        JsonPrimitive prim = s.get(st.name).getAsJsonPrimitive();
                        Object v = prim.isBoolean() ? (Object) prim.getAsBoolean()
                                 : prim.isNumber() ? (Object) prim.getAsDouble()
                                 : prim.getAsString();
                        st.load(v);
                    }
                }
                if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
            }
            if (root.has("_friends")) {
                dev.pace.util.Friends.clear();
                for (JsonElement e : root.getAsJsonArray("_friends")) dev.pace.util.Friends.add(e.getAsString());
            }
        } catch (Exception e) {
            PaceClient.LOG.error("Failed to load config", e);
        }
    }
}
