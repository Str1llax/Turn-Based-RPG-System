package git.str1llax.core.states;

import git.str1llax.core.effects.StatusEffect;
import git.str1llax.core.entities.Entity;
import git.str1llax.core.equipment.Equipment;
import git.str1llax.core.managers.InputManager;
import git.str1llax.core.managers.ResourceManager;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;
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
        int i = 1;
        for (Entity hero : selectedHeroes) {
            System.out.printf("%d. %s\n", i++, hero.credentials().name());
        }
        System.out.println("Menu:");
        System.out.println("0. Exit");
        System.out.println("1. Add Hero to team");
        System.out.println("2. Replace Hero");
        System.out.println("3. Create a new Hero");
        System.out.println("4. Select equipment");
        System.out.println("5. Start Battle");

        switch (InputManager.getValidInput(0, 5)) {
            case 0 -> gameContext.stateManager().pop();
            case 1 -> selectHero();
            case 2 -> replaceHero();
            case 3 -> gameContext.stateManager().push(new CharacterCreationState(gameContext));
            case 4 -> selectGear();
            case 5 -> startBattle();
        }
    }

    private void selectHero() {
        if (selectedHeroes.size() < MAX_TEAM_SIZE) {
            System.out.println("Select a Hero");
            System.out.println("0. Cancel");
            int i = 1;
            for (Entity hero : ResourceManager.getHeroTemplates().values()) {
                System.out.printf("%d. ", i++);
                printHero(hero);
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
            System.out.printf("%d. ", i++);
            printHero(hero);
        }
        int selectedHero = InputManager.getValidInput(1, ResourceManager.getHeroTemplates().size());
        Entity hero = ResourceManager.getHeroTemplates().values().stream().toList().get(selectedHero-1);
        selectedHeroes.set(replaceId-1, new Entity(hero));
    }

    private void selectGear() {
        if (selectedHeroes.isEmpty()) {
            System.out.println("Nobody to select");
            return;
        }
        System.out.println("Select a hero to change equipment:");
        System.out.println("0. Cancel");
        int i = 1;
        for (Entity hero : selectedHeroes) {
            System.out.printf("%d. ", i++);
            printHero(hero);
        }
        int replaceId = InputManager.getValidInput(0, selectedHeroes.size());
        if (replaceId == 0) return;
        Entity hero = selectedHeroes.get(replaceId - 1);
        System.out.println("Choose slot ot replace:\n1. Class\n2. Weapon\n3. Off-Hand Item\n");
        int slot = InputManager.getValidInput(0, 3);
        switch (slot) {
            case 1 -> chooseClass(hero);
            case 2 -> chooseWeapon(hero);
            case 3 -> chooseOffhand(hero);
        }
    }

    private void chooseClass(Entity hero) {
        int i = 1;
        List<Equipment> classes = ResourceManager.getHeadEquip(hero.credentials().id());
        System.out.println("Choose class:");
        for (Equipment armor : classes) {
            System.out.printf("%d. %s - %s\n",
                    i++, armor.credentials().name(),
                    armor.credentials().description());
            if (!armor.activeSkills().isEmpty()) {
                System.out.println("\tActive Skills:");
                for (ActiveSkill skill : armor.activeSkills()) {
                    System.out.printf("\t\t- %s\n\t\t%s\n", skill.credentials().name(), skill.credentials().description());
                }
            }
            if (!armor.passiveSkills().isEmpty()) {
                int j = 1;
                System.out.println("\tPassive Skills:");
                for (PassiveSkill passiveSkill : armor.passiveSkills()) {
                    System.out.printf("\t\t%d. %s\n\t\t%s\n", j++, passiveSkill.credentials().name(), passiveSkill.credentials().description());
                }
            }
        }
        int selectedId = InputManager.getValidInput(1, classes.size());
        hero.equip(classes.get(selectedId - 1));
        System.out.printf("Successfully equipped %s on %s!\n", classes.get(selectedId - 1).credentials().name(), hero.credentials().name());
    }

    private void chooseWeapon(Entity hero) {
        int i = 1;
        List<Equipment> weapons = ResourceManager.getWeaponEquip(hero.credentials().id());
        System.out.println("Choose a weapon:");
        for (Equipment weapon : weapons) {
            System.out.printf("%d. %s - %s\n",
                    i++, weapon.credentials().name(),
                    weapon.credentials().description());
            if (!weapon.activeSkills().isEmpty()) {
                int j = 1;
                System.out.println("\tActive Skills:");
                for (ActiveSkill skill : weapon.activeSkills()) {
                    System.out.printf("\t\t%d. %s\n\t\t%s\n", j++, skill.credentials().name(), skill.credentials().description());
                }
            }
            if (!weapon.passiveSkills().isEmpty()) {
                int j = 1;
                System.out.println("\tPassive Skills:");
                for (PassiveSkill passiveSkill : weapon.passiveSkills()) {
                    System.out.printf("\t\t%d. %s\n\t\t%s\n", j++, passiveSkill.credentials().name(), passiveSkill.credentials().description());
                }
            }
        }
        int selectedId = InputManager.getValidInput(1, weapons.size());
        hero.equip(weapons.get(selectedId - 1));
        System.out.printf("Successfully equipped %s on %s!\n", weapons.get(selectedId - 1).credentials().name(), hero.credentials().name());
    }

    private void chooseOffhand(Entity hero) {
        int i = 1;
        List<Equipment> shields = ResourceManager.getOffhandEquip(hero.credentials().id());
        System.out.println("Choose an off-hand item:");
        for (Equipment off : shields) {
            System.out.printf("%d. %s - %s\n",
                    i++, off.credentials().name(),
                    off.credentials().description());
            if (!off.activeSkills().isEmpty()) {
                int j = 1;
                System.out.println("\tActive Skills:");
                for (ActiveSkill skill : off.activeSkills()) {
                    System.out.printf("\t\t%d. %s\n\t\t%s\n", j++, skill.credentials().name(), skill.credentials().description());
                }
            }
            if (!off.passiveSkills().isEmpty()) {
                int j = 1;
                System.out.println("\tPassive Skills:");
                for (PassiveSkill passiveSkill : off.passiveSkills()) {
                    System.out.printf("\t\t%d. %s\n\t\t%s\n", j++, passiveSkill.credentials().name(), passiveSkill.credentials().description());
                }
            }
        }
        int selectedId = InputManager.getValidInput(1, shields.size());
        hero.equip(shields.get(selectedId - 1));
        System.out.printf("Successfully equipped %s on %s!\n", shields.get(selectedId - 1).credentials().name(), hero.credentials().name());
    }

    private void printHero(Entity hero) {
        System.out.printf("%s [id: %s]\n", hero.credentials().name(),  hero.credentials().id());
        System.out.printf("%s\n", hero.credentials().description());
        System.out.printf("Stats:\n\t- Health: %.0f\n\t- Defense: %.0f\n\t- Attack: %.0f\n\t- Agility: %.0f\n\t- Crit. Rate: %.2f%%\n\t- Crit. Damage: %.2f%%\n",
                hero.maxHp(),
                hero.defense(),
                hero.attackDamage(),
                hero.agility(),
                hero.critRate() * 100d,
                hero.critDamage() * 100d);
        System.out.printf("Ultimate Skill: %s - %s\n", hero.ultimateSkill().credentials().name(), hero.ultimateSkill().credentials().description());
        System.out.println("Equipment:");
        System.out.printf("\tClass: %s (%s)\n\t\t- Defense: %.0f\n\t\t- Agility: %.0f\n",
                hero.getLoadout().getArmor().credentials().name(),
                hero.getLoadout().getArmor().credentials().description(),
                hero.getLoadout().getArmor().mainStat(),
                hero.getLoadout().getArmor().offStat());
        System.out.printf("\tWeapon: %s (%s)\n\t\t- Attack: %.0f\n\t\t- Crit. Rate: %.2f%%\n",
                hero.getLoadout().getSword().credentials().name(),
                hero.getLoadout().getSword().credentials().description(),
                hero.getLoadout().getSword().mainStat(),
                hero.getLoadout().getSword().offStat());
        System.out.printf("\tOff-Hand: %s (%s)\n\t\t- Health: %.0f\n\t\t- Defense: %.0f\n",
                hero.getLoadout().getShield().credentials().name(),
                hero.getLoadout().getShield().credentials().description(),
                hero.getLoadout().getShield().mainStat(),
                hero.getLoadout().getShield().offStat());
        if (!hero.getLoadout().getPassiveSkills().isEmpty()) {
            System.out.println("Active Skills:");
            for (ActiveSkill skill : hero.getLoadout().getActiveSkills()) {
                System.out.printf("\t- %s\n\t%s\n\n",  skill.credentials().name(), skill.credentials().description());
            }
        }
        if (!hero.getLoadout().getActiveSkills().isEmpty()) {
            System.out.println("Passive Skills:");
            for (PassiveSkill skill : hero.getLoadout().getPassiveSkills()) {
                System.out.printf("\t- %s\n\t%s\n\n",  skill.credentials().name(), skill.credentials().description());
            }
        }
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
        int enemyCount = random.nextInt(2, 6);
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
