package git.str1llax.core.states;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.effects.StatusEffect;
import git.str1llax.core.entities.Entity;
import git.str1llax.core.managers.InputManager;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;
import git.str1llax.core.utils.BattleContext;
import git.str1llax.core.utils.CLinkedList;
import git.str1llax.core.utils.EffectProcessor;
import git.str1llax.core.utils.GameContext;

import java.util.*;

public class BattleState implements GameState {

    public enum TurnOrderMode {
        FIXED_QUEUE,
        DYNAMIC_CSLL
    }

    public enum State {
        INIT,
        ROUND_START,
        PLAYER_MOVE,
        AI_MOVE,
        ROUND_END,
        CHECK_OUTCOME,
        VICTORY,
        DEFEAT
    }

    private final GameContext context;
    private final BattleContext battleContext;
    private final EffectProcessor effectProcessor;
    private final TurnOrderMode turnOrderMode;

    private State state;

    private Queue<Entity> fixedTurnOrder;
    private Iterator<Entity> fixedIterator;

    private CLinkedList<Entity> dynamicTurnOrder;
    private CLinkedList<Entity>.CircularIterator dynamicIterator;

    private Entity activeEntity;

    public BattleState(GameContext context, BattleContext battleContext, TurnOrderMode turnOrderMode) {
        this.context = context;
        this.battleContext = battleContext;
        this.turnOrderMode = turnOrderMode;
        this.effectProcessor = new EffectProcessor();
    }

    @Override
    public void load() {
        state = State.INIT;
        System.out.println("\n=== BATTLE BEGINS ===");
    }

    @Override
    public void update() {
        switch (state) {
            case INIT -> initializeBattle();
            case ROUND_START -> startRound();
            case PLAYER_MOVE -> processPlayer();
            case AI_MOVE -> processAI();
            case ROUND_END -> processResults();
            case CHECK_OUTCOME -> checkOutcome();
            case VICTORY -> win();
            case DEFEAT -> lose();
        }
    }

    private void initializeBattle() {
        for (Entity hero : battleContext.getHeroes()) {
            hero.recalculateStats();
        }
        for (Entity enemy : battleContext.getEnemies()) {
            enemy.recalculateStats();
        }

        if (turnOrderMode == TurnOrderMode.DYNAMIC_CSLL) {
            dynamicTurnOrder = new CLinkedList<>();
            battleContext.getHeroes().forEach(dynamicTurnOrder::add);
            battleContext.getEnemies().forEach(dynamicTurnOrder::add);

            dynamicTurnOrder.sortBy(Comparator.comparingDouble(Entity::agility).reversed());
            dynamicIterator = dynamicTurnOrder.circularIterator();
        }
        state = State.ROUND_START;
    }

    private void startRound() {
        if (turnOrderMode == TurnOrderMode.FIXED_QUEUE) {
            List<Entity> allAlive = new ArrayList<>();
            allAlive.addAll(battleContext.getLivingHeroes());
            allAlive.addAll(battleContext.getLivingEnemies());
            allAlive.sort(Comparator.comparingDouble(Entity::agility).reversed());

            fixedTurnOrder = new LinkedList<>(allAlive);
            fixedIterator = fixedTurnOrder.iterator();
        }

        prepareNextTurn();
    }

    private void prepareNextTurn() {
        if (turnOrderMode == TurnOrderMode.FIXED_QUEUE) {
            if (!fixedIterator.hasNext()) {
                state = State.ROUND_START;
                return;
            }
            activeEntity = fixedIterator.next();
        } else {
            if (!dynamicIterator.hasNext()) {
                state = State.CHECK_OUTCOME;
                return;
            }
            activeEntity = dynamicIterator.next();
        }

        if (activeEntity.isDead()) {
            prepareNextTurn();
            return;
        }

        double previousAgility = activeEntity.agility();

        activeEntity.recalculateStats();
        processStatus(activeEntity);

        if (turnOrderMode == TurnOrderMode.DYNAMIC_CSLL && activeEntity.agility() != previousAgility) {
            dynamicTurnOrder.sortBy(Comparator.comparingDouble(Entity::agility).reversed());
        }

        if (activeEntity.isDead()) {
            state = State.ROUND_END;
            return;
        }

        if (activeEntity.isStunned()) {
            System.out.printf("%s is stunned! Skips move...\n",  activeEntity.credentials().name());
            activeEntity.setStunned(false);
            state = State.ROUND_END;
            return;
        }

        if (battleContext.getHeroes().contains(activeEntity)) {
            state = State.PLAYER_MOVE;
        } else {
            state = State.AI_MOVE;
        }
    }

    private void processStatus(Entity entity) {
        var iterator = entity.statusEffects().iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effectProcessor.processStatusTick(effect, entity, battleContext);
            if (effect.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void processPlayer() {
        renderStats();
        List<ActiveSkill> skills = activeEntity.getLoadout().getActiveSkills();
        if (activeEntity.ultimateSkill() != null) skills.add(activeEntity.ultimateSkill());
        Entity target = null;
        List<Entity> livingEnemies = !activeEntity.getFocusedOn().isEmpty() ? activeEntity.getFocusedOn() : battleContext.getLivingEnemies();
        List<Entity> livingAllies = battleContext.getLivingHeroes();
        List<Entity> deadAllies = battleContext.getHeroes().stream().filter(Entity::isDead).toList();

        System.out.printf("Your turn: %s\n", activeEntity.credentials().name());
        System.out.println("Select skill:");
        for (int i = 0; i < skills.size(); ++i) {
            System.out.printf("%d. %s\n", i+1, skills.get(i).credentials().name());
        }

        int skillChoice = InputManager.getValidInput(1, skills.size()) - 1;
        ActiveSkill chosenSkill = skills.get(skillChoice);
        if ((chosenSkill.skillType() == ActiveSkill.SkillType.ATTACK_ULTIMATE ||
            chosenSkill.skillType() == ActiveSkill.SkillType.SUPPORT_ULTIMATE) &&  !battleContext.isChiliFull()) {
            System.out.println("\nRage Chili is not filled yet!");
            processPlayer();
            return;
        }

        System.out.println("Select target:");
        switch (chosenSkill.skillType()) {
            case ATTACK, ATTACK_ULTIMATE -> {
                if (chosenSkill.skillType() == ActiveSkill.SkillType.ATTACK_ULTIMATE) {
                    battleContext.useChili();
                }
                for (int i = 0; i < livingEnemies.size(); ++i) {
                    System.out.printf("%d. %s (HP: %.2f/%.2f)\n", i+1, livingEnemies.get(i).credentials().name(), livingEnemies.get(i).currentHp(), livingEnemies.get(i).maxHp());
                }
                int targetChoice = InputManager.getValidInput(1, livingEnemies.size()) - 1;
                target = livingEnemies.get(targetChoice);
                effectProcessor.applyPassives(PassiveSkill.TriggerType.ON_DEFENSE, activeEntity, target, battleContext);

            }
            case SUPPORT, SUPPORT_ULTIMATE -> {
                if (chosenSkill.skillType() == ActiveSkill.SkillType.SUPPORT_ULTIMATE) {
                    battleContext.useChili();
                }
                for (int i = 0; i < livingAllies.size(); ++i) {
                    System.out.printf("%d. %s (HP: %.2f/%.2f)\n", i+1, livingAllies.get(i).credentials().name(), livingAllies.get(i).currentHp(), livingAllies.get(i).maxHp());
                }
                int targetChoice = InputManager.getValidInput(1, livingAllies.size()) - 1;
                target = livingAllies.get(targetChoice);
                battleContext.setSelectedAlly(target);
            }
        }

        System.out.printf("%s uses %s on %s!\n", activeEntity.credentials().name(), chosenSkill.credentials().name(), target.credentials().name());
        for (SkillEffect effect : chosenSkill.effects()) {
            effectProcessor.processEffect(effect, activeEntity, target, battleContext);
        }

        state = State.ROUND_END;
    }

    private void processAI() {
        System.out.printf("Enemy turn: %s\n", activeEntity.credentials().name());
        List<Entity> livingHeroes = !activeEntity.getFocusedOn().isEmpty() ? activeEntity.getFocusedOn() : battleContext.getLivingHeroes();
        List<Entity> livingAllies = battleContext.getLivingEnemies();
        List<ActiveSkill> skills = activeEntity.getLoadout().getActiveSkills();
        if (activeEntity.ultimateSkill() != null) skills.add(activeEntity.ultimateSkill());

        ActiveSkill chosenSkill = skills.getFirst();
        Entity target = livingHeroes.getFirst();

        Optional<Entity> weakAlly = livingAllies.stream().filter(e -> (e.currentHp() / e.maxHp() < 0.3d)).findFirst();
        Optional<ActiveSkill> healSkill = skills.stream().filter(s -> s.effects().stream().anyMatch(e ->
                (e.effectType() == SkillEffect.EffectType.HEAL ||
                e.effectType() == SkillEffect.EffectType.HEALTH_BOOST ||
                e.effectType() == SkillEffect.EffectType.PROTECTION) &&
                (e.targetGroup() == SkillEffect.TargetGroup.ALL_ALLIES ||
                e.targetGroup() == SkillEffect.TargetGroup.ALL_DEAD_ALLIES ||
                e.targetGroup() == SkillEffect.TargetGroup.ALL_LIVING_ALLIES ||
                e.targetGroup() == SkillEffect.TargetGroup.MOST_INJURED_ALLY ||
                e.targetGroup() == SkillEffect.TargetGroup.LEAST_INJURED_AlLY ||
                e.targetGroup() == SkillEffect.TargetGroup.SELECTED_ALLY ||
                e.targetGroup() == SkillEffect.TargetGroup.RANDOM_ALLY
                ))).findFirst();
        if (weakAlly.isPresent() && healSkill.isPresent()) {
            chosenSkill = healSkill.get();
            target = weakAlly.get();
        } else {
            Optional<Entity> weakHero = livingHeroes.stream().filter(e -> e.currentHp() / e.maxHp() < 0.2d).findFirst();
            if (weakHero.isPresent()) {
                target = weakHero.get();
                chosenSkill = skills.stream().filter(s -> s.skillType() == ActiveSkill.SkillType.ATTACK || s.skillType() == ActiveSkill.SkillType.ATTACK_ULTIMATE).findFirst().orElse(skills.getFirst());
            } else {
                Optional<ActiveSkill> AOEAttack = skills.stream().filter(s ->
                        (s.skillType() == ActiveSkill.SkillType.ATTACK || s.skillType() == ActiveSkill.SkillType.ATTACK_ULTIMATE) &&
                                s.effects().stream().anyMatch(f -> f.targetGroup() == SkillEffect.TargetGroup.ALL_ENEMIES ||
                                        f.targetGroup()== SkillEffect.TargetGroup.ALL_LIVING_ENEMIES)).findFirst();
                if (AOEAttack.isPresent()) {
                    chosenSkill = AOEAttack.get();
                } else {
                    Optional<ActiveSkill> attackingSkill = skills.stream().filter(s -> s.skillType() == ActiveSkill.SkillType.ATTACK || s.skillType() == ActiveSkill.SkillType.ATTACK_ULTIMATE).findFirst();
                    if (attackingSkill.isPresent()) {
                        chosenSkill = attackingSkill.get();
                        target = livingHeroes.stream().min(Comparator.comparingDouble(Entity::currentHp)).orElse(livingHeroes.get(new Random().nextInt(livingHeroes.size())));
                    } else {
                        target = livingAllies.get(new Random().nextInt(livingAllies.size()));
                    }
                }
            }
        }

        System.out.printf("%s uses %s on %s!\n", activeEntity.credentials().name(), chosenSkill.credentials().name(), target.credentials().name());
        for (SkillEffect effect : chosenSkill.effects()) {
            effectProcessor.processEffect(effect, activeEntity, target, battleContext);
        }
        state = State.ROUND_END;
    }

    private void processResults() {
        for (Entity hero : battleContext.getHeroes()) {
            if (hero.isDead() && battleContext.getLivingEnemies().stream().noneMatch(e -> e.getLoadout().getActiveSkills().stream().anyMatch(s -> s.effects().stream().anyMatch(f -> f.effectType() == SkillEffect.EffectType.REVIVE)))) {
                if (turnOrderMode == TurnOrderMode.FIXED_QUEUE && fixedTurnOrder != null) {
                    fixedIterator.remove();
                } else if (dynamicTurnOrder != null) {
                    dynamicTurnOrder.remove(hero);
                }
            }
        }
        for (Entity enemy : battleContext.getEnemies()) {
            if (enemy.isDead() && !enemy.statusEffects().removeIf(s -> s.getEffect().effectType() == SkillEffect.EffectType.REVIVE)) {
                if (turnOrderMode == TurnOrderMode.FIXED_QUEUE && fixedTurnOrder != null) {
                    fixedTurnOrder.remove(enemy);
                } else if (dynamicTurnOrder != null) {
                    dynamicTurnOrder.remove(enemy);
                }
            }
        }

        state = State.CHECK_OUTCOME;
    }

    private void checkOutcome() {
        if (battleContext.getLivingEnemies().isEmpty()) {
            state = State.VICTORY;
        } else if (battleContext.getLivingHeroes().isEmpty()) {
            state = State.DEFEAT;
        } else {
            prepareNextTurn();
        }
    }

    private void win() {
        System.out.println("YOU WIN! The pigs have been defeated.");
        System.out.println("Press ENTER to return to the battle preparation");
        InputManager.SCANNER.nextLine();
        context.stateManager().pop();
    }

    private void lose() {
        System.out.println("YOU LOSE! The birds were knocked out.");
        System.out.println("Press ENTER to return to the battle preparation");
        InputManager.SCANNER.nextLine();
        context.stateManager().pop();
    }

    private void renderStats() {
        int i = 1;
        System.out.println("\n--- YOUR TEAM ---");
        for (Entity hero : battleContext.getHeroes()) {
            System.out.printf("%d. %s\n\tHP: %.2f/%.2f\n\tDef: %.2f\n\tAD: %.2f\n\tAgility: %.2f\n\tCrit R/D: %.2f%%/%.2f%%\n",
                    i, hero.credentials().name(),
                    hero.currentHp(), hero.maxHp(),
                    hero.defense(),
                    hero.attackDamage(),
                    hero.agility(),
                    hero.critRate() * 100d, hero.critDamage() * 100d);
            ++i;
            if (!hero.statusEffects().isEmpty()) {
                int j = 1;
                System.out.println("\tStatus Effects:");
                for (StatusEffect status : hero.statusEffects()) {
                    System.out.printf("\t\t%d. %s (turns left: %d)\n", j, status.getEffect().credentials().name(), status.left());
                }
            }
        }
        i = 1;
        System.out.println("\n--- ENEMIES ---");
        for (Entity enemy : battleContext.getEnemies()) {
            System.out.printf("%d. %s\n\tHP: %.2f/%.2f\n\tDef: %.2f\n\tAD: %.2f\n\tAgility: %.2f\n\tCrit R/D: %.2f%%/%.2f%%\n",
                    i, enemy.credentials().name(),
                    enemy.currentHp(), enemy.maxHp(),
                    enemy.defense(),
                    enemy.attackDamage(),
                    enemy.agility(),
                    enemy.critRate() * 100d, enemy.critDamage() * 100d);
            ++i;
            if (!enemy.statusEffects().isEmpty()) {
                int j = 1;
                System.out.println("\tStatus Effects:");
                for (StatusEffect status : enemy.statusEffects()) {
                    System.out.printf("\t\t%d. %s (turns left: %d)\n", j, status.getEffect().credentials().name(), status.left());
                }
            }
        }
        System.out.println("\n--- RAGE CHILI ---");
        System.out.printf("\tFullness: %.2f%%\n\n", battleContext.getRageChili() * 100d);
    }

    @Override
    public void render() {

    }

    @Override
    public void unload() {
        if (state == State.VICTORY) {
            System.out.println("Congratulations!");
        } else if (state == State.DEFEAT) {
            System.out.println("Better luck next time!");
        }
    }
}
