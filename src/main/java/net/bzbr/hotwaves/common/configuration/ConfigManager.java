package net.bzbr.hotwaves.common.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.bzbr.hotwaves.common.enums.MobStrength;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final File CONFIG_FILE = new File("config/hotwaves.json");
    private static int spawnRadius = 100;
    private static int waveIntervalDays = 7;
    private static int lowStrengthMobMaxCount = 20;
    private static int lowStrengthMobMinCount = 6;
    private static int midStrengthMobMaxCount = 10;
    private static int midStrengthMobMinCount = 5;
    private static int hardStrengthMobMaxCount = 5;
    private static int hardStrengthMobMinCount = 1;
    private static final List<MobConfig> waveMobs = new ArrayList<>();

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {

            var defaultConfObject = getJsonObject();

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new Gson();
                gson.toJson(defaultConfObject, writer);  // Запись объекта в JSON
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            spawnRadius = json.get("spawnRadius").getAsInt();
            waveIntervalDays = json.get("waveIntervalDays").getAsInt();
            lowStrengthMobMaxCount = json.get("lowStrengthMobMaxCount").getAsInt();
            lowStrengthMobMinCount = json.get("lowStrengthMobMinCount").getAsInt();
            midStrengthMobMaxCount = json.get("midStrengthMobMaxCount").getAsInt();
            midStrengthMobMinCount = json.get("midStrengthMobMinCount").getAsInt();
            hardStrengthMobMaxCount = json.get("hardStrengthMobMaxCount").getAsInt();
            hardStrengthMobMinCount = json.get("hardStrengthMobMinCount").getAsInt();

            waveMobs.clear();
            JsonArray mobsArray = json.getAsJsonArray("waveMobs");
            for (int i = 0; i < mobsArray.size(); i++) {
                JsonObject mobObj = mobsArray.get(i).getAsJsonObject();
                String mobId = mobObj.get("mobId").getAsString();
                int count = mobObj.get("count").getAsInt();
                int minDay = mobObj.get("minDay").getAsInt();
                int maxDay = mobObj.get("maxDay").getAsInt();
                MobStrength strength = MobStrength.valueOf(mobObj.get("strength").getAsString());
                waveMobs.add(new MobConfig(mobId, count, minDay, maxDay, strength));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static @NotNull JsonObject getJsonObject() {
        var waveMobsJson = new JsonArray();

        JsonObject mob = new JsonObject();
        mob.addProperty("mobId", "minecraft:zombie");
        mob.addProperty("count", 10);
        mob.addProperty("minDay", 1);
        mob.addProperty("maxDay", -1);
        mob.addProperty("strength", MobStrength.LOW.toString());

        waveMobsJson.add(mob);

        var defaultConfObject = new JsonObject();
        defaultConfObject.addProperty("spawnRadius", spawnRadius);
        defaultConfObject.addProperty("waveIntervalDays", waveIntervalDays);
        defaultConfObject.addProperty("lowStrengthMobMaxCount", lowStrengthMobMaxCount);
        defaultConfObject.addProperty("lowStrengthMobMinCount", lowStrengthMobMinCount);
        defaultConfObject.addProperty("midStrengthMobMaxCount", midStrengthMobMaxCount);
        defaultConfObject.addProperty("midStrengthMobMinCount", midStrengthMobMinCount);
        defaultConfObject.addProperty("hardStrengthMobMaxCount", hardStrengthMobMaxCount);
        defaultConfObject.addProperty("hardStrengthMobMinCount", hardStrengthMobMinCount);
        defaultConfObject.add("waveMobs", waveMobsJson);
        return defaultConfObject;
    }

    public static int getSpawnRadius() {
        return spawnRadius;
    }

    public static int getWaveIntervalDays() {
        return waveIntervalDays;
    }

    public static List<MobConfig> getWaveMobs() {
        return waveMobs;
    }

    public static int getAllMobsCount() {

        return waveMobs.stream().mapToInt(x->x.count).sum();
    }

    public static int getLowStrengthMobMaxCount() {
        return lowStrengthMobMaxCount;
    }

    public static int getLowStrengthMobMinCount() {
        return lowStrengthMobMinCount;
    }

    public static int getMidStrengthMobMaxCount() {
        return midStrengthMobMaxCount;
    }

    public static int getMidStrengthMobMinCount() {
        return midStrengthMobMinCount;
    }

    public static int getHardStrengthMobMaxCount() {
        return hardStrengthMobMaxCount;
    }

    public static int getHardStrengthMobMinCount() {
        return hardStrengthMobMinCount;
    }

    public record MobConfig(String mobId, int count, int minDay, int maxDay, MobStrength strength) {
    }
}
