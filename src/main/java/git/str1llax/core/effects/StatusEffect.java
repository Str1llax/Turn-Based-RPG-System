package git.str1llax.core.effects;

import git.str1llax.core.utils.Credentials;

public class StatusEffect {
    private final Credentials credentials;
    private final int durationTurns;
    private int activeDuration;
    // effect type DoT or Timer

    public StatusEffect(Credentials credentials, int durationTurns) {
        this.credentials = credentials;
        this.durationTurns = durationTurns;

        this.activeDuration = durationTurns;
    }

    public void tick() {
        --activeDuration;
    }

    public boolean isExpired() {
        return activeDuration <= 0;
    }
}
