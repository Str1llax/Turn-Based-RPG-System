package git.str1llax.core.utils;

import git.str1llax.core.managers.ResourceManager;
import git.str1llax.core.managers.StateManager;

public record GameContext(
        StateManager stateManager,
        ResourceManager resourceManager
) {
}
