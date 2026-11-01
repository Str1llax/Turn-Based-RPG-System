package git.str1llax.core.effects;

import git.str1llax.core.utils.Credentials;

public class StatusEffect {
    private final Credentials credentials;
    private final SkillEffect.EffectType effectType;
    private final SkillEffect.ExecutionType executionType;
    private final double value;
    private final int totalDuration;
    private int remainingTurns;

    public StatusEffect(Credentials credentials, SkillEffect.EffectType effectType,
                        SkillEffect.ExecutionType executionType, double value, int durationTurns) {
        this.credentials = credentials;
        this.effectType = effectType;
        this.executionType = executionType;
        this.value = value;
        this.totalDuration = durationTurns;
        this.remainingTurns = durationTurns;
    }

    public static StatusEffect fromBlueprint(SkillEffect blueprint, double calculatedValue) {
        int duration = blueprint.durationTurns() != null ? blueprint.durationTurns() : 1;
        return new StatusEffect(
                blueprint.credentials(),
                blueprint.effectType(),
                blueprint.executionType(),
                calculatedValue,
                duration
        );
    }

    public void tick() {
        --remainingTurns;
    }

    public boolean isExpired() {
        return remainingTurns <= 0;
    }
}
