package git.str1llax.core.utils;

import git.str1llax.core.entities.Entity;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class BattleContext {
    private final LinkedList<Entity> heroes;
    private final LinkedList<Entity> enemies;

    private double rageChili;

    private Entity selectedAlly;
    private double lastDamageDealt;
    private double lastHeal;

    public BattleContext(List<Entity> heroes, List<Entity> enemies) {
        this.heroes = new LinkedList<>(heroes);
        this.enemies = new LinkedList<>(enemies);
        rageChili = 0d;
        lastDamageDealt = 0d;
        lastHeal = 0d;
    }

    public List<Entity> getHeroes() {
        return heroes;
    }

    public List<Entity> getEnemies() {
        return enemies;
    }

    public List<Entity> getLivingHeroes() {
        return heroes.stream().filter(Predicate.not(Entity::isDead)).toList();
    }

    public List<Entity> getLivingEnemies() {
        return enemies.stream().filter(Predicate.not(Entity::isDead)).toList();
    }

    public void addHero(Entity hero) {
        this.heroes.add(hero);
    }

    public void addEnemy(Entity enemy) {
        this.enemies.add(enemy);
    }

    public void addRageChili(double amount) {
        rageChili = Math.min(1d, rageChili + amount / 100d);
    }

    public double getRageChili() {
        return rageChili;
    }

    public boolean isChiliFull() {
        return rageChili == 1d;
    }

    public void setLastDamage(double lastDamage) {
        this.lastDamageDealt = lastDamage;
    }

    public double getLastDamage() {
        return lastDamageDealt;
    }

    public void setSelectedAlly(Entity selectedAlly) {
        this.selectedAlly = selectedAlly;
    }

    public Entity getSelectedAlly() {
        return selectedAlly;
    }

    public void  setLastHeal(double lastHeal) {
        this.lastHeal = lastHeal;
    }

    public double getLastHeal() {
        return lastHeal;
    }

    public void useChili() {
        this.rageChili = 0d;
    }
}
