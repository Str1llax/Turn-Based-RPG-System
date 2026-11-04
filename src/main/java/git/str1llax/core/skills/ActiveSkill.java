package git.str1llax.core.skills;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.utils.Credentials;

import java.util.List;

public record ActiveSkill(
        Credentials credentials,
        SkillType skillType,
        List<SkillEffect> effects
) {
    public enum SkillType {
        ATTACK,
        SUPPORT,
        ATTACK_ULTIMATE,
        SUPPORT_ULTIMATE
    }
}
