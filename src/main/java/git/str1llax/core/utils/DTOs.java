package git.str1llax.core.utils;

import git.str1llax.core.equipment.Equipment;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;

import java.util.List;

public class DTOs {
    public record ActiveSkillDTO(
            Credentials credentials,
            ActiveSkill.SkillType skillType,
            List<String> effectIds
    ) {}

    public record PassiveSkillDTO(
            Credentials credentials,
            PassiveSkill.TriggerType triggerType,
            PassiveSkill.PassiveCondition condition,
            List<String> effectIds
    ) {}

    public record EquipmentDTO(
            Credentials credentials,
            Equipment.EquipmentType equipmentType,
            String targetId,
            double mainStat,
            double offStat,
            String setId,
            List<String> activeSkillIds,
            List<String> passiveSkillIds
    ) {}

    public record DefaultLoadoutDTO(
            String armorId,
            String swordId,
            String shieldId
    ) {}

    public record EntityDTO(
            Credentials credentials,
            BaseStats baseStats,
            DefaultLoadoutDTO defaultLoadout,
            String ultimateSkillId
    ) {}
}
