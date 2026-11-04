package git.str1llax.core.effects;

import git.str1llax.core.entities.Entity;

public class StatusEffect {
    private final SkillEffect effect;
    private final double value;
    private final int totalDuration;
    private int remainingTurns;
    private final Entity caster;

    public StatusEffect(Entity caster, SkillEffect effect, double value) {
        this.caster = caster;
        this.effect = effect;
        if (effect.durationTurns() == null || effect.durationTurns() == 0) {
            this.totalDuration = 1;
        } else {
            this.totalDuration = effect.durationTurns();
        }
        this.remainingTurns = this.totalDuration;
        this.value = value;
    }

    public void tick() {
        if (effect.executionType() != SkillEffect.ExecutionType.CONSTANT) --remainingTurns;
    }

    public boolean isExpired() {
        return remainingTurns <= 0;
    }

    public void setTurns(int newTurns) {
        this.remainingTurns = newTurns;
    }

    public int maxDuration() {
        return this.totalDuration;
    }

    public double value() {
        return value;
    }

    public SkillEffect getEffect() {
        return effect;
    }

    public int left() {
        return remainingTurns;
    }

    public Entity caster() {
        return caster;
    }
}
