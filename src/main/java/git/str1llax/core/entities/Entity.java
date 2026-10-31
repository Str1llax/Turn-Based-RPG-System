package git.str1llax.core.entities;

import git.str1llax.core.effects.StatusEffect;
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


        currentHp = (maxHp = baseStats.maxHp());
        attackDamage = baseStats.attackDamage();
        defense = baseStats.defense();
        agility = baseStats.agility();

        this.loadout = loadout;
        this.ultimateSkill = ultimateSkill;

        statusEffects = new LinkedList<>();
    }


}
