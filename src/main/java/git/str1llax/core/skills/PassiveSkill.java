package git.str1llax.core.skills;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.utils.Credentials;

import java.util.List;

public record PassiveSkill(
        Credentials credentials,
        TriggerType triggerType,
        Object condition,
        List<SkillEffect> effects
) {
    public enum TriggerType {
        ON_ATTACK,
        ON_DEFENSE,
        CONSTANT
    }
}
