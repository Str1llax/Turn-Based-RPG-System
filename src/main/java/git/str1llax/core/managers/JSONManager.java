package git.str1llax.core.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.utils.DTOs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class JSONManager {
    private final Gson gson;

    public JSONManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<SkillEffect> loadEffects() {
        return loadList("data/effects.json", new TypeToken<List<SkillEffect>>(){}.getType());
    }

    public List<DTOs.ActiveSkillDTO> loadActiveSkills() {
        return loadList("data/skills/active.json", new TypeToken<List<DTOs.ActiveSkillDTO>>(){}.getType());
    }

    public List<DTOs.PassiveSkillDTO> loadPassiveSkills() {
        return loadList("data/skills/passive.json", new TypeToken<List<DTOs.PassiveSkillDTO>>(){}.getType());
    }

    public List<DTOs.EquipmentDTO> loadEquipment(String path) {
        return loadList(path, new TypeToken<List<DTOs.EquipmentDTO>>(){}.getType());
    }

    public List<DTOs.EntityDTO> loadHeroes() {
        return loadList("data/heroes.json", new TypeToken<List<DTOs.EntityDTO>>(){}.getType());
    }

    public List<DTOs.EntityDTO> loadEnemies() {
        return loadList("data/enemies.json", new TypeToken<List<DTOs.EntityDTO>>(){}.getType());
    }

    private <T> List<T> loadList(String filePath, Type typeToken) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        if (stream == null) {
            System.err.println("Failed to load JSON file: " + filePath);
        }
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(stream), StandardCharsets.UTF_8)) {
            List<T> result = gson.fromJson(reader, typeToken);
            return result != null ? result : List.of();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
