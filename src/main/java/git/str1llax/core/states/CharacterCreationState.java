package git.str1llax.core.states;

import git.str1llax.core.entities.Entity;
import git.str1llax.core.equipment.Loadout;
import git.str1llax.core.managers.InputManager;
import git.str1llax.core.managers.ResourceManager;
import git.str1llax.core.utils.BaseStats;
import git.str1llax.core.utils.Credentials;
import git.str1llax.core.utils.GameContext;

public class CharacterCreationState implements GameState {
    private final GameContext gameContext;

    public CharacterCreationState(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public void load() {
        System.out.println("\n=== CHARACTER CREATION MENU ===");
        System.out.println("Menu:");
        System.out.println("1. Create Character");
        System.out.println("2. Exit");
    }

    @Override
    public void update() {
        switch (InputManager.getValidInput(1, 2)) {
            case 1 -> createCharacter();
            case 2 -> gameContext.stateManager().pop();
        }
    }

    private void createCharacter() {
        System.out.print("Enter Character Name: ");
        String name = InputManager.SCANNER.nextLine();
        String id = "custom_" + name.toLowerCase().replace(" ", "_");

        System.out.println("Enter Base Stats.");
        System.out.print("Max HP (e.g 20-40): ");
        double hp = InputManager.getValidInput(0d, 100d);

        System.out.print("Attack Damage (e.g 10-20): ");
        double ad = InputManager.getValidInput(0d, 100d);

        System.out.print("Defense (e.g 15-30): ");
        double df = InputManager.getValidInput(0d, 100d);

        System.out.print("Agility (e.g 5-15): ");
        double ag = InputManager.getValidInput(0d, 100d);

        Credentials credentials = new Credentials(id, name, "Custom created hero");
        BaseStats baseStats = new BaseStats(hp, ad, df, ag);

        Loadout loadout = new Loadout()
                .equip(ResourceManager.getEquipment("sword"))
                .equip(ResourceManager.getEquipment("shield"))
                .equip(ResourceManager.getEquipment("knight"));

        Entity newHero = new Entity(credentials, baseStats, loadout, ResourceManager.getActiveSkill("heroic_strike"));

        ResourceManager.registerHero(newHero);

        System.out.printf("Character %s created successfully!\nUse menu input to continue.\n", name);
    }

    @Override
    public void render() {

    }

    @Override
    public void unload() {

    }
}
