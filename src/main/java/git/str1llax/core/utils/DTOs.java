package git.str1llax.core.utils;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.equipment.Equipment;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;

import java.util.List;

public class DTOs {
    public record EffectDTO(
            Credentials Credentials,
            SkillEffect.EffectType EffectType,
            SkillEffect.ExecutionType ExecutionType,
            SkillEffect.TriggerType TriggerType,
            SkillEffect.TargetGroup TargetGroup,
            SkillEffect.ValueType ValueType,
            Double StatMultiplier,
            Double triggerChance,
            Integer durationTurns
    ) {}

    public record ActiveSkillDTO(
            Credentials Credentials,
            ActiveSkill.SkillType SkillType,
            ActiveSkill.SkillRange SkillRange,
            List<String> effectIds
    ) {}

    public record PassiveSkillDTO(
            Credentials Credentials,
            PassiveSkill.TriggerType TriggerType,
            Object Condition,
            List<String> effectIds
    ) {}

    public record EquipmentDTO(
            Credentials Credentials,
            Equipment.EquipmentType EquipmentType,
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
            Credentials Credentials,
            BaseStats BaseStats,
            DefaultLoadoutDTO defaultLoadout,
            String ultimateSkillId
    ) {}
}
