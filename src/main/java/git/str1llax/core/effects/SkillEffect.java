package git.str1llax.core.effects;

import git.str1llax.core.utils.Credentials;

public record SkillEffect(
        Credentials credentials,
        EffectType effectType,
        ExecutionType executionType,
        TriggerType triggerType,
        TargetGroup targetGroup,
        ValueType valueType,
        Double statMultiplier,
        Double triggerChance,
        Integer durationTurns
) {
    public enum EffectType {
        DAMAGE,
        BONUS_DAMAGE,
        HEAL,
        FOCUS,
        THORN,
        PROTECTION,
        WEAKNESS,
        STRENGTH,
        SPEED,
        SLOWNESS,
        STUN,
        DISPEL_BUFFS,
        STAT_MODIFIER
    }

    public enum ExecutionType {
        INSTANT,
        DOT,
        CHARGE,
        CONSTANT
    }

    public enum TriggerType {
        ON_ATTACK,
        ON_DEFENSE,
        CONSTANT
    }

    public enum TargetGroup {
        PRIMARY_TARGET,
        ALL_ENEMIES,
        ALL_ALLIES,
        SELF,
        RANDOM_ENEMY
    }

    public enum ValueType {
        CASTER_ATTACK,
        DAMAGE_DEALT,
        TARGET_MAX_HP,
        FLAT
    }
}
