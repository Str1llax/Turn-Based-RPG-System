package git.str1llax.core.states;

import git.str1llax.core.entities.Entity;
import git.str1llax.core.managers.InputManager;
import git.str1llax.core.managers.ResourceManager;
import git.str1llax.core.utils.BattleContext;
import git.str1llax.core.utils.GameContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattlePreparationState implements GameState {
    private final GameContext gameContext;

    private final List<Entity> selectedHeroes;

    private final int MAX_TEAM_SIZE = 3;

    public BattlePreparationState(GameContext gameContext) {
        this.gameContext = gameContext;
        this.selectedHeroes = new ArrayList<>();
    }

    @Override
    public void load() {
        System.out.println("\n=== BATTLE PREPARATION ===");
    }

    @Override
    public void update() {
        System.out.printf("\nTeam (%d/%d):\n", selectedHeroes.size(), MAX_TEAM_SIZE);
        System.out.println("Menu:");
        System.out.println("1. Add Hero to team");
        System.out.println("2. Replace Hero");
        System.out.println("3. Create a new Hero");
        System.out.println("4. Start Battle");
        System.out.println("5. Exit");

        switch (InputManager.getValidInput(1, 5)) {
            case 1 -> selectHero();
            case 2 -> replaceHero();
            case 3 -> gameContext.stateManager().push(new CharacterCreationState(gameContext));
            case 4 -> startBattle();
            case 5 -> gameContext.stateManager().pop();
        }
    }

    private void selectHero() {
        if (selectedHeroes.size() < MAX_TEAM_SIZE) {
            System.out.println("Select a Hero");
            System.out.println("0. Cancel");
            int i = 1;
            for (Entity hero : ResourceManager.getHeroTemplates().values()) {
                System.out.printf("%d. %s\n", i, hero.credentials().name());
                ++i;
            }
            int selectedHero = InputManager.getValidInput(0, ResourceManager.getHeroTemplates().size());
            if (selectedHero == 0) return;
            Entity hero = ResourceManager.getHeroTemplates().values().stream().toList().get(selectedHero-1);
            for (Entity selected : selectedHeroes) {
                if (hero.credentials().id().equals(selected.credentials().id())) {
                    System.out.println("This hero already in the team. Select another one");
                    selectHero();
                    return;
                }
            }
            selectedHeroes.add(new Entity(hero));
        } else {
            replaceHero();
        }
    }

    private void replaceHero() {
        if (selectedHeroes.isEmpty()) {
            System.out.println("Nobody to replace");
            return;
        }
        System.out.println("Select a hero to replace:");
        System.out.println("0. Cancel");
        for (int i = 1; i <= selectedHeroes.size(); ++i) {
            System.out.printf("%d. %s\n", i, selectedHeroes.get(i - 1).credentials().name());
        }
        int replaceId = InputManager.getValidInput(0, selectedHeroes.size());
        if (replaceId == 0) return;
        System.out.println("Select a new Hero");
        int i = 1;
        for (Entity hero : ResourceManager.getHeroTemplates().values()) {
            System.out.printf("%d. %s\n", i, hero.credentials().name());
            ++i;
        }
        int selectedHero = InputManager.getValidInput(1, ResourceManager.getHeroTemplates().size());
        Entity hero = ResourceManager.getHeroTemplates().values().stream().toList().get(selectedHero-1);
        selectedHeroes.set(replaceId-1, new Entity(hero));
    }

    private void startBattle() {
        if (selectedHeroes.isEmpty()) {
            System.out.println("Please select at least one Hero.");
            return;
        }
        selectedHeroes.forEach(Entity::reset);

        System.out.println("\nSelect turn order");
        System.out.println("Menu:");
        System.out.println("1. Static");
        System.out.println("2. Dynamic");
        int choice =  InputManager.getValidInput(1, 2);
        BattleState.TurnOrderMode order = (choice == 1) ? BattleState.TurnOrderMode.FIXED_QUEUE : BattleState.TurnOrderMode.DYNAMIC_CSLL;

        gameContext.stateManager().push(new BattleState(gameContext, new BattleContext(selectedHeroes, generateEnemies()), order));
    }

    private List<Entity> generateEnemies() {
        List<Entity> enemies = new ArrayList<>();
        Random random = new Random();
        int enemyCount = random.nextInt(1, 5);
        for (int i = 0; i < enemyCount; ++i) {
            enemies.add(new Entity(ResourceManager.getEnemyTemplates().values().stream().toList().get(random.nextInt(ResourceManager.getEnemyTemplates().size()))));
        }
        return enemies;
    }

    @Override
    public void render() {

    }

    @Override
    public void unload() {

    }
}
