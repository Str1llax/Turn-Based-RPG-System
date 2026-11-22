package git.str1llax.core.skills;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.utils.Credentials;

import java.util.List;

public record PassiveSkill(
        Credentials credentials,
        TriggerType triggerType,
        PassiveCondition condition,
        List<SkillEffect> effects
) {
    public record PassiveCondition(
            ConditionType conditionType,
            Double threshold
    ) {
        public enum ConditionType {
            HP_BELOW_PERCENT,
            HP_ABOVE_PERCENT,
            DAMAGE_MORE_THAN,
            DAMAGE_LESS_THAN,
            RAGE_CHILI_BELOW,
            RAGE_CHILI_ABOVE,
        }
    }

    public enum TriggerType {
        ON_ATTACK,  // Before calculating damage
        ON_DEFENSE, // Before taking damage
        ON_HIT,     // After taking damage
        ON_HEAL,    // Before healing
        ON_KILL,    // If caster killed target after attack
        ON_DEATH,   // If target died from casters attack
        CONSTANT    // Every turn
    }
}
