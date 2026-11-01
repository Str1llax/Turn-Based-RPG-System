package git.str1llax.core.equipment;

import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;
import git.str1llax.core.utils.Credentials;

import java.util.List;

public record Equipment(
        Credentials credentials,
        EquipmentType equipmentType,
        String targetId,
        double mainStat,
        double offStat,
        String setId,
        List<ActiveSkill> activeSkills,
        List<PassiveSkill> passiveSkills
) {
    public enum EquipmentType {
        SWORD,
        SHIELD,
        ARMOR
    }
}
