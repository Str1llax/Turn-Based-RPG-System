package git.str1llax.core;

import git.str1llax.core.managers.ResourceManager;
import git.str1llax.core.managers.StateManager;
import git.str1llax.core.states.MainMenuState;
import git.str1llax.core.utils.GameContext;

public class Engine {
    private final GameContext gameContext;

    public Engine() {
        this.gameContext = new GameContext(new StateManager(), new ResourceManager());
    }

    public void loadResources() {
        gameContext.resourceManager().initialize();
    }

    public void startGame() {
        gameContext.stateManager().push(new MainMenuState(gameContext));
    }

    public void update() {
        if (isRunning()) {
            gameContext.stateManager().peek().update();
        }
    }

    public void render() {
        if (isRunning()) {
            gameContext.stateManager().peek().render();
        }
    }

    public boolean isRunning() {
        return !gameContext.stateManager().isEmpty();
    }
}
