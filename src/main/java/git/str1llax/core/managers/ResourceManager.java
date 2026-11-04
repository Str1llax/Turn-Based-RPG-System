package git.str1llax.core.managers;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.entities.Entity;
import git.str1llax.core.equipment.Equipment;
import git.str1llax.core.equipment.Loadout;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;
import git.str1llax.core.utils.DTOs;

import java.util.*;

public class ResourceManager {
    private final JSONManager jsonManager;

    private static final Map<String, SkillEffect> effectsRegistry = new HashMap<>();
    private static final Map<String, ActiveSkill> activeSkillsRegistry = new HashMap<>();
    private static final Map<String, PassiveSkill> passiveSkillsRegistry = new HashMap<>();
    private static final Map<String, Equipment> equipmentRegistry = new HashMap<>();
    private static final Map<String, Entity> heroTemplates = new HashMap<>();
    private static final Map<String, Entity> enemyTemplates = new HashMap<>();

    public ResourceManager() {
        this.jsonManager = new JSONManager();
    }

    public static void registerHero(Entity newHero) {
        heroTemplates.put(newHero.credentials().id(), newHero);
    }

    public void initialize() {
        loadEffects();
        loadSkills();
        loadEquipment();
        loadEntities();
    }

    private void loadEffects() {
        for (SkillEffect effect : jsonManager.loadEffects()) {
            effectsRegistry.put(effect.credentials().id(), effect);
        }
    }

    private void loadSkills() {
        for (DTOs.ActiveSkillDTO dto : jsonManager.loadActiveSkills()) {
            List<SkillEffect> effects = new ArrayList<>();
            if (dto.effectIds() != null) {
                for (String id : dto.effectIds()) {
                    SkillEffect e = effectsRegistry.get(id);
                    if (e != null) effects.add(e);
                }
            }
            ActiveSkill skill = new ActiveSkill(
                    dto.credentials(), dto.skillType(), effects
            );
            activeSkillsRegistry.put(dto.credentials().id(), skill);
        }

        for (DTOs.PassiveSkillDTO dto : jsonManager.loadPassiveSkills()) {
            List<SkillEffect> effects = new ArrayList<>();
            if (dto.effectIds() != null) {
                for (String id : dto.effectIds()) {
                    SkillEffect e = effectsRegistry.get(id);
                    if (e != null) effects.add(e);
                }
            }
            PassiveSkill skill = new PassiveSkill(
                    dto.credentials(), dto.triggerType(), dto.condition(), effects
            );
            passiveSkillsRegistry.put(dto.credentials().id(), skill);
        }
    }

    private void loadEquipment() {
        List<DTOs.EquipmentDTO> allEquipment = new ArrayList<>();
        allEquipment.addAll(jsonManager.loadEquipment("data/equipment/armor.json"));
        allEquipment.addAll(jsonManager.loadEquipment("data/equipment/swords.json"));
        allEquipment.addAll(jsonManager.loadEquipment("data/equipment/shields.json"));

        for (DTOs.EquipmentDTO dto : allEquipment) {
            List<ActiveSkill> activeSkills = new ArrayList<>();
            if (dto.activeSkillIds() != null) {
                for (String id : dto.activeSkillIds()) {
                    ActiveSkill s = activeSkillsRegistry.get(id);
                    if (s != null) activeSkills.add(s);
                }
            }

            List<PassiveSkill> passiveSkills = new ArrayList<>();
            if (dto.passiveSkillIds() != null) {
                for (String id : dto.passiveSkillIds()) {
                    PassiveSkill p = passiveSkillsRegistry.get(id);
                    if (p != null) passiveSkills.add(p);
                }
            }

            Equipment equipment = new Equipment(
                    dto.credentials(), dto.equipmentType(), dto.targetId(),
                    dto.mainStat(), dto.offStat(), dto.setId(),
                    activeSkills, passiveSkills
            );
            equipmentRegistry.put(dto.credentials().id(), equipment);
        }
    }

    private void loadEntities() {
        for (DTOs.EntityDTO dto : jsonManager.loadHeroes()) {
            Entity hero = buildEntityFromDTO(dto);
            heroTemplates.put(dto.credentials().id(), hero);
        }

        for (DTOs.EntityDTO dto : jsonManager.loadEnemies()) {
            Entity enemy = buildEntityFromDTO(dto);
            enemyTemplates.put(dto.credentials().id(), enemy);
        }
    }

    private Entity buildEntityFromDTO(DTOs.EntityDTO dto) {
        Loadout loadout = new Loadout();
        if (dto.defaultLoadout() != null) {
            if (dto.defaultLoadout().armorId() != null) {
                loadout.equip(equipmentRegistry.get(dto.defaultLoadout().armorId()));
            }
            if (dto.defaultLoadout().swordId() != null) {
                loadout.equip(equipmentRegistry.get(dto.defaultLoadout().swordId()));
            }
            if (dto.defaultLoadout().shieldId() != null) {
                loadout.equip(equipmentRegistry.get(dto.defaultLoadout().shieldId()));
            }
        }

        ActiveSkill ultimate = null;
        if (dto.ultimateSkillId() != null) {
            ultimate = activeSkillsRegistry.get(dto.ultimateSkillId());
        }

        return new Entity(dto.credentials(), dto.baseStats(), loadout, ultimate);
    }

    public Entity createHeroInstance(String id) {
        Entity template = heroTemplates.get(id);
        return template != null ? new Entity(template) : null;
    }

    public Entity createEnemyInstance(String id) {
        Entity template = enemyTemplates.get(id);
        return template != null ? new Entity(template) : null;
    }

    public static SkillEffect getEffect(String id) {
        return effectsRegistry.get(id);
    }
    public static ActiveSkill getActiveSkill(String id) {
        return activeSkillsRegistry.get(id);
    }
    public static PassiveSkill getPassiveSkill(String id) {
        return passiveSkillsRegistry.get(id);
    }
    public static Equipment getEquipment(String id) {
        return equipmentRegistry.get(id);
    }
    public static Map<String, Entity> getHeroTemplates() {
        return Collections.unmodifiableMap(heroTemplates);
    }
    public static Map<String, Entity> getEnemyTemplates() {
        return Collections.unmodifiableMap(enemyTemplates);
    }
}
