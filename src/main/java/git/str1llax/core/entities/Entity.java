package git.str1llax.core.entities;

import git.str1llax.core.effects.StatusEffect;
import git.str1llax.core.equipment.Equipment;
import git.str1llax.core.equipment.Loadout;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.utils.BaseStats;
import git.str1llax.core.utils.Credentials;

import java.util.LinkedList;

public class Entity {
    private final Credentials credentials;

    private final BaseStats baseStats;

    private double maxHp;
    private double currentHp;
    private double attackDamage;
    private double defense;
    private double agility;

    private final Loadout loadout;
    private final ActiveSkill ultimateSkill;

    private final LinkedList<StatusEffect> statusEffects;

    public Entity(Credentials credentials, BaseStats baseStats, Loadout loadout, ActiveSkill ultimateSkill) {
        this.credentials = credentials;
        this.baseStats = baseStats;
        this.loadout = loadout != null ? loadout : new Loadout();
        this.ultimateSkill = ultimateSkill;
        this.statusEffects = new LinkedList<>();

        recalculateStats();
        this.currentHp = this.maxHp;
    }

    public Entity(Entity blueprint) {
        this.credentials = blueprint.credentials;
        this.baseStats = blueprint.baseStats;
        this.loadout = new Loadout();
        this.statusEffects = new LinkedList<>();

        if (blueprint.loadout.getArmor() != null) this.loadout.equip(blueprint.loadout.getArmor());
        if (blueprint.loadout.getSword() != null) this.loadout.equip(blueprint.loadout.getSword());
        if (blueprint.loadout.getShield() != null) this.loadout.equip(blueprint.loadout.getShield());

        this.ultimateSkill = blueprint.ultimateSkill;
        recalculateStats();
        this.currentHp = this.maxHp;
    }

    public void recalculateStats() {
        double defenseBonus = 0;
        double hpBonus = 0;
        double attackBonus = 0;

        if (loadout.getSword() != null) {
            attackBonus += loadout.getSword().mainStat();
            hpBonus += loadout.getSword().offStat();
        }
        if (loadout.getShield() != null) {
            hpBonus += loadout.getShield().mainStat();
            defenseBonus += loadout.getShield().offStat();
        }
        if (loadout.getArmor() != null) {
            hpBonus += loadout.getArmor().mainStat();
            defenseBonus += loadout.getArmor().offStat();
        }

        //TODO Passives bonus

        this.maxHp = baseStats.maxHp() + hpBonus;
        this.attackDamage = baseStats.attackDamage() + attackBonus;
        this.defense = baseStats.defense() +  defenseBonus;
        this.agility = baseStats.agility();
    }

    public void equip(Equipment item) {
        this.loadout.equip(item);
        recalculateStats();
    }

    public Credentials credentials() {
        return credentials;
    }

    public BaseStats baseStats() {
        return baseStats;
    }

    public double maxHp() {
        return maxHp;
    }

    public double currentHp() {
        return currentHp;
    }

    public double attackDamage() {
        return attackDamage;
    }

    public double defense() {
        return defense;
    }

    public double agility() {
        return agility;
    }

    public Loadout getLoadout() {
        return loadout;
    }

    public ActiveSkill ultimateSkill() {
        return ultimateSkill;
    }

    public LinkedList<StatusEffect> statusEffects() {
        return statusEffects;
    }

}
