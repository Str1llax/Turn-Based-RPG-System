package git.str1llax.core.skills;

import git.str1llax.core.utils.Credentials;

public record PassiveSkill(
        Credentials credentials,
        TriggerCondition triggerCondition,
        double triggerChance,
        String effectId
) {
    public enum TriggerCondition {
        ON_ATTACK,
        ON_DEFEND
    }
}
