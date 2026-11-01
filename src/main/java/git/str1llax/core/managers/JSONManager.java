package git.str1llax.core.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import git.str1llax.core.utils.DTOs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JSONManager {
    private final Gson gson;

    public JSONManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<DTOs.EffectDTO> loadEffects() {
        return loadList("resources/data/effects.json", new TypeToken<List<DTOs.EffectDTO>>(){}.getType());
    }

    public List<DTOs.ActiveSkillDTO> loadActiveSkills() {
        return loadList("resources/data/skills/active.json", new TypeToken<List<DTOs.ActiveSkillDTO>>(){}.getType());
    }

    public List<DTOs.PassiveSkillDTO> loadPassiveSkills() {
        return loadList("resources/data/skills/passive.json", new TypeToken<List<DTOs.PassiveSkillDTO>>(){}.getType());
    }

    public List<DTOs.EquipmentDTO> loadEquipment(String path) {
        return loadList(path, new TypeToken<List<DTOs.EquipmentDTO>>(){}.getType());
    }

    public List<DTOs.EntityDTO> loadHeroes() {
        return loadList("resources/data/heroes.json", new TypeToken<List<DTOs.EntityDTO>>(){}.getType());
    }

    public List<DTOs.EntityDTO> loadEnemies() {
        return loadList("resources/data/enemies.json", new TypeToken<List<DTOs.EntityDTO>>(){}.getType());
    }

    private <T> List<T> loadList(String filePath, Type typeToken) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("JSON file missing at path: " + filePath);
            return List.of();
        }
        try (FileReader reader = new FileReader(file)) {
            List<T> result = gson.fromJson(reader, typeToken);
            return result != null ? result : List.of();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
