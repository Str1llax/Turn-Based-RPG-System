package git.str1llax.core.states;

import git.str1llax.core.managers.InputManager;
import git.str1llax.core.utils.GameContext;

public class MainMenuState implements GameState {
    private final GameContext gameContext;

    public MainMenuState(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public void load() {
        System.out.println("\n=== Welcome to the Angry Birds: EPIC! CLI Remake ===");
        System.out.println("Enter:");
        System.out.println("1. Start game.");
        System.out.println("2. Exit game.");
    }

    @Override
    public void update() {
        switch (InputManager.getValidInput(1, 2)) {
            case 1 -> gameContext.stateManager().push(new BattlePreparationState(gameContext));
            case 2 -> gameContext.stateManager().pop();
        }
    }

    @Override
    public void render() {

    }

    @Override
    public void unload() {
        System.out.println("Goodbye...");
    }
}
