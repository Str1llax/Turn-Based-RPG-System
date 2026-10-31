package git.str1llax.core.equipment;

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
        switch (item.type()) {
            case Equipment.EquipmentType.SWORD -> sword = item;
            case Equipment.EquipmentType.SHIELD -> shield = item;
            case Equipment.EquipmentType.ARMOR -> armor = item;
        }

        return this;
    }

    public List<ActiveSkill> getActiveSkills() {
        return (armor != null && armor.activeSkills() != null) ? armor.activeSkills() : List.of();
    }

    public List<PassiveSkill> getPassiveSkills() {
        List<PassiveSkill> passiveSkills = new ArrayList<>();
        if (sword != null && sword.passiveSkills() != null) {
            passiveSkills.addAll(sword.passiveSkills());
        }
        if (shield != null && shield.passiveSkills() != null) {
            passiveSkills.addAll(shield.passiveSkills());
        }
        if (sword != null && shield != null && sword.setId() != null && shield.setId() != null && sword.setId().equals(shield.setId())) {
            passiveSkills.add(); //TODO add equipment set effect
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
