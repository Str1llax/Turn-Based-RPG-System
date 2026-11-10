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
            ON_DEATH,
            RAGE_CHILI_BELOW,
            RAGE_CHILI_ABOVE,
        }
    }

    public enum TriggerType {
        ON_ATTACK,
        ON_DEFENSE,
        ON_HEAL,
        CONSTANT
    }
}
