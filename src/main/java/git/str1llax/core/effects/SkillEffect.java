package git.str1llax.core.effects;

import git.str1llax.core.utils.Credentials;

public record SkillEffect(
        Credentials credentials,
        EffectType effectType,
        ExecutionType executionType,
        TargetGroup targetGroup,
        ScaleType scaleType,
        Double statMultiplier,
        Double triggerChance,
        Integer durationTurns
) {
    public enum EffectType {
        DAMAGE,
        BONUS_DAMAGE,
        HEAL,
        HEALTH_BOOST,
        FOCUS,
        THORN,
        PROTECTION,
        WEAKNESS,
        STRENGTH,
        SPEED,
        SLOWNESS,
        STUN,
        DISPEL_BUFFS,
        DISPEL_DEBUFFS,
        IMMUNE,
        REVIVE,
        MODIFY_CRIT_RATE,
        MODIFY_CRIT_DAMAGE,
    }

    public enum ExecutionType {
        INSTANT,
        DOT,
        CHARGE,
        CONSTANT
    }

    public enum TargetGroup {
        SELF,
        TARGET,
        SELECTED_ALLY,
        ALL_ENEMIES,
        ALL_ALLIES,
        MOST_INJURED_ALLY,
        MOST_INJURED_ENEMY,
        LEAST_INJURED_AlLY,
        LEAST_INJURED_ENEMY,
        ALL_LIVING_ALLIES,
        ALL_LIVING_ENEMIES,
        ALL_DEAD_ALLIES,
        ALL_DEAD_ENEMIES,
        RANDOM_ENEMY,
        RANDOM_ALLY
    }

    public enum ScaleType {
        FLAT,
        DAMAGE_DEALT,
        HEAL_RECEIVED,
        CASTER_MAX_HP,
        CASTER_CURRENT_HP,
        CASTER_ATTACK,
        CASTER_DEFENSE,
        CASTER_AGILITY,
        TARGET_MAX_HP,
        TARGET_CURRENT_HP,
        TARGET_ATTACK,
        TARGET_DEFENSE,
        TARGET_AGILITY
    }
}
