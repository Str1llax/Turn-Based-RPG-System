package git.str1llax.core.equipment;

import git.str1llax.core.managers.ResourceManager;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;

import java.util.ArrayList;
import java.util.List;

public class Loadout {
    private Equipment sword;
    private Equipment shield;
    private Equipment armor;

    public Loadout() {}

    public Loadout equip(Equipment item) {
        if (item == null) {
            return this;
        }
        switch (item.equipmentType()) {
            case Equipment.EquipmentType.SWORD -> sword = item;
            case Equipment.EquipmentType.SHIELD -> shield = item;
            case Equipment.EquipmentType.ARMOR -> armor = item;
        }

        return this;
    }

    public List<ActiveSkill> getActiveSkills() {
        List<ActiveSkill> activeSkills = new ArrayList<>();
        if (sword != null && sword.activeSkills() != null) {
            activeSkills.addAll(sword.activeSkills());
        }
        if (shield != null && shield.activeSkills() != null) {
            activeSkills.addAll(shield.activeSkills());
        }
        if (armor != null && armor.activeSkills() != null) {
            activeSkills.addAll(armor.activeSkills());
        }
        return activeSkills;
    }

    public List<PassiveSkill> getPassiveSkills() {
        List<PassiveSkill> passiveSkills = new ArrayList<>();
        if (sword != null && sword.passiveSkills() != null) {
            passiveSkills.addAll(sword.passiveSkills());
        }
        if (shield != null && shield.passiveSkills() != null) {
            passiveSkills.addAll(shield.passiveSkills());
        }
        if (armor != null && armor.passiveSkills() != null) {
            passiveSkills.addAll(armor.passiveSkills());
        }
        if (sword != null && shield != null && sword.setId() != null && shield.setId() != null && sword.setId().equals(shield.setId())) {
            passiveSkills.add(ResourceManager.getPassiveSkill(sword.setId()));
        }
        return passiveSkills;
    }

    public Equipment getSword() {
        return sword;
    }

    public Equipment getShield() {
        return shield;
    }

    public Equipment getArmor() {
        return armor;
    }
}
