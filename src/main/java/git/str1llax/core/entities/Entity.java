package git.str1llax.core.entities;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.effects.StatusEffect;
import git.str1llax.core.equipment.Equipment;
import git.str1llax.core.equipment.Loadout;
import git.str1llax.core.skills.ActiveSkill;
import git.str1llax.core.skills.PassiveSkill;
import git.str1llax.core.utils.BaseStats;
import git.str1llax.core.utils.Credentials;
import git.str1llax.core.utils.EffectProcessor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Entity {
    private final Credentials credentials;

    private final BaseStats baseStats;

    private double maxHp;
    private double currentHp;
    private double attackDamage;
    private double defense;
    private double agility;
    private double critRate;
    private double critDamage;

    private final Loadout loadout;
    private final ActiveSkill ultimateSkill;

    private final LinkedList<StatusEffect> statusEffects;

    private final List<Entity> focusedOn;
    private boolean immune;
    private boolean stunned;
    private boolean spiky;

    public Entity(Credentials credentials, BaseStats baseStats, Loadout loadout, ActiveSkill ultimateSkill) {
        this.credentials = credentials;
        this.baseStats = baseStats;
        this.loadout = loadout != null ? loadout : new Loadout();
        this.ultimateSkill = ultimateSkill;
        this.statusEffects = new LinkedList<>();
        this.focusedOn = new ArrayList<>();

        this.critRate = 0d;
        this.critDamage = 1d;

        this.immune = false;
        this.stunned = false;
        this.spiky = false;

        recalculateStats();
        this.currentHp = this.maxHp;
    }

    public Entity(Entity blueprint) {
        this.credentials = blueprint.credentials;
        this.baseStats = blueprint.baseStats;
        this.loadout = new Loadout();
        this.statusEffects = new LinkedList<>();
        this.focusedOn = new ArrayList<>();

        if (blueprint.loadout.getArmor() != null) this.loadout.equip(blueprint.loadout.getArmor());
        if (blueprint.loadout.getSword() != null) this.loadout.equip(blueprint.loadout.getSword());
        if (blueprint.loadout.getShield() != null) this.loadout.equip(blueprint.loadout.getShield());

        this.critRate = 0d;
        this.critDamage = 1d;

        this.immune = false;
        this.stunned = false;
        this.spiky = false;

        this.ultimateSkill = blueprint.ultimateSkill;
        recalculateStats();
        this.currentHp = this.maxHp;
    }

    public void recalculateStats() {
        double bonusDF = 0;
        double bonusHP = 0;
        double bonusAD = 0;
        double bonusAG = 0;
        double bonusCR = 0;
        double bonusCD = 0;

        // Sword = Attack Damage + Crit. Rate
        if (loadout.getSword() != null) {
            bonusAD += loadout.getSword().mainStat();
            bonusCR += loadout.getSword().offStat() * 0.01;
        }
        // Shield = HP + Defense
        if (loadout.getShield() != null) {
            bonusHP += loadout.getShield().mainStat();
            bonusDF += loadout.getShield().offStat();
        }
        // Armor = Defense + Agility
        if (loadout.getArmor() != null) {
            bonusDF += loadout.getArmor().mainStat();
            bonusAG += loadout.getArmor().offStat();
        }

        for (PassiveSkill skill : this.loadout.getPassiveSkills()) {
            if (skill.triggerType() == PassiveSkill.TriggerType.CONSTANT) {
                for (SkillEffect effect : skill.effects()) {
                    EffectProcessor.applyConstantStatModifier(effect, this);
                }
            }
        }
        for (StatusEffect effect : this.statusEffects) {
            if (effect.getEffect().executionType() == SkillEffect.ExecutionType.CONSTANT) {
                EffectProcessor.applyConstantStatModifier(effect.getEffect(), this);
            }
        }

        this.maxHp = baseStats.maxHp() + bonusHP;
        this.attackDamage = baseStats.attackDamage() + bonusAD;
        this.defense = baseStats.defense() +  bonusDF;
        this.agility = baseStats.agility() + bonusAG;
        this.critRate = Math.max(0d, bonusCR);
        this.critDamage = 1d + bonusCD;
    }

    public void reset() {
        this.statusEffects.clear();
        this.focusedOn.clear();

        this.critRate = 0d;
        this.critDamage = 1d;

        this.immune = false;
        this.stunned = false;
        this.spiky = false;

        recalculateStats();
        this.currentHp = this.maxHp;
    }

    public void equip(Equipment item) {
        this.loadout.equip(item);
        recalculateStats();
    }

    public void heal(double amount) {
        this.currentHp = Math.max(this.maxHp, currentHp + amount);
    }

    public void receiveDamage(double amount) {
        if (isDead() || this.immune) return;

        double damageMultiplier = 1d - (this.defense / (this.defense + 300));
        double finalDamage = amount * damageMultiplier;

        this.currentHp = Math.max(0d, this.currentHp - finalDamage);
    }

    public boolean isDead() {
        return this.currentHp <= 0d;
    }

    public void modifyMaxHpBy(double value) {
        this.maxHp = Math.max(1d, this.maxHp + value);
        this.currentHp += Math.max(1d, this.currentHp + value);
    }

    public void modifyAttackBy(double value) {
        this.attackDamage = Math.max(1d, this.attackDamage + value);
    }

    public void modifyDefenseBy(double value) {
        this.defense = Math.max(1d, this.defense + value);
    }

    public void modifyAgilityBy(double value) {
        this.agility = Math.max(1d, this.agility + value);
    }

    public void modifyCritRateBy(double value) {
        this.critRate = Math.max(0d, this.critRate + value);
    }

    public void modifyCritDamageBy(double value) {
        this.critDamage = Math.max(1d, this.critDamage + value);
    }

    public void addStatusEffect(StatusEffect effect) {
        for (StatusEffect eff3ct : this.statusEffects) {
            if (eff3ct.getEffect().credentials().id().equals(effect.getEffect().credentials().id())) {
                eff3ct.setTurns(effect.maxDuration());
                return;
            }
        }
        this.statusEffects.add(effect);
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

    public double critRate() {
        return critRate;
    }

    public double critDamage() {
        return critDamage;
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

    public void focus(Entity entity) {
        if (this.focusedOn.contains(entity)) return;
        this.focusedOn.add(entity);
    }

    public List<Entity> getFocusedOn() {
        return focusedOn;
    }

    public void clearFocus() {
        this.focusedOn.clear();
    }

    public void setImmune(boolean value) {
        this.immune = value;
    }

    public boolean isImmune() {
        return immune;
    }

    public void setStunned(boolean value) {
        this.stunned = value;
    }

    public boolean isStunned() {
        return stunned;
    }

    public void setSpiky(boolean value) {
        this.spiky = value;
    }

    public boolean isSpiky() {
        return spiky;
    }

}
