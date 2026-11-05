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
        this.maxHp = getInitialHp();
        this.attackDamage = getInitialAttack();
        this.defense = getInitialDefense();
        this.agility = getInitialAgility();
        this.critRate = getInitialCritRate();
        this.critDamage = getInitialCritDamage();

        for (PassiveSkill skill : this.loadout.getPassiveSkills()) {
            if (skill.triggerType() == PassiveSkill.TriggerType.CONSTANT) {
                for (SkillEffect effect : skill.effects()) {
                    EffectProcessor.applyConstantStatModifier(effect, this);
                }
            }
        }
        for (StatusEffect status : this.statusEffects) {
            if (status.getEffect().executionType() == SkillEffect.ExecutionType.CONSTANT) {
                EffectProcessor.applyConstantStatModifier(status.getEffect(), this);
            }
        }
    }

    public void reset() {
        this.statusEffects.clear();
        this.focusedOn.clear();

        this.immune = false;
        this.stunned = false;
        this.spiky = false;

        recalculateStats();
        this.currentHp = this.maxHp;
    }

    public double getInitialHp() {
        double hp = baseStats.maxHp();
        if (loadout.getShield() != null) {
            hp += loadout.getShield().mainStat();
        }
        return Math.max(hp, 1d);
    }

    public double getInitialDefense() {
        double def = baseStats.defense();
        if (loadout.getShield() != null) {
            def += loadout.getShield().offStat();
        }
        if (loadout.getArmor() != null) {
            def += loadout.getShield().offStat();
        }
        return Math.max(def, 0d);
    }

    public double getInitialAttack() {
        double attack = baseStats.attackDamage();
        if (loadout.getSword() != null) {
            attack += loadout.getSword().mainStat();
        }
        return Math.max(attack, 1d);
    }

    public double getInitialAgility() {
        double agility = baseStats.agility();
        if (loadout.getArmor() != null) {
            agility += loadout.getArmor().offStat();
        }
        return Math.max(agility, 1d);
    }

    public double getInitialCritRate() {
        double cr = 0d;
        if (loadout.getSword() != null) {
            cr += loadout.getSword().offStat() * 0.01;
        }
        return Math.max(cr, 0d);
    }

    public double getInitialCritDamage() {
        double cd = 1d;
        //TODO add stat
        return Math.max(cd, 1d);
    }

    public Loadout equip(Equipment item) {
        this.loadout.equip(item);
        recalculateStats();
        return this.loadout;
    }

    public void heal(double amount) {
        this.currentHp = Math.min(this.maxHp, currentHp + amount);
    }

    public void receiveDamage(double amount) {
        //APPLY ON_DEFENSE passives somehow
        if (isDead() || this.immune) return;

        double damageMultiplier = 1d - (this.defense / (this.defense + 300));
        double finalDamage = amount * damageMultiplier;

        System.out.printf("%s took %.2f damage\n", credentials.name(), finalDamage);
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
            if (eff3ct.getEffect().equals(effect.getEffect())) {
                eff3ct.setTurns(effect.maxDuration());
                return;
            }
        }
        this.statusEffects.add(effect);
    }

    public LinkedList<StatusEffect> statusEffects() {
        return statusEffects;
    }

    public Credentials credentials() {
        return credentials;
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

    public void focus(Entity entity) {
        if (this.focusedOn.contains(entity)) return;
        this.focusedOn.add(entity);
    }

    public List<Entity> getFocusedOn() {
        return focusedOn;
    }

    public boolean isFocused() {
        return !focusedOn.isEmpty();
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
