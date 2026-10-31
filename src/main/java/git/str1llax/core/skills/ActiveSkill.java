package git.str1llax.core.skills;

import git.str1llax.core.utils.Credentials;

public record ActiveSkill(
        Credentials credentials,
        SkillType type,
        SkillRange range,
        double triggerChance,
        String effectId
) {
    public enum SkillType {
        ATTACK,
        SUPPORT,
        ULTIMATE
    }

    public enum SkillRange {
        SINGLE,
        AOE
    }
}
