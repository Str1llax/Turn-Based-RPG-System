package git.str1llax.core.utils;

import git.str1llax.core.effects.SkillEffect;
import git.str1llax.core.effects.StatusEffect;
import git.str1llax.core.entities.Entity;
import git.str1llax.core.skills.PassiveSkill;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class EffectProcessor {
    private final Random random = new Random();

    public void processEffect(SkillEffect effect, Entity caster, Entity primaryTarget, BattleContext context) {
        if (effect.triggerChance() != null && random.nextDouble() > effect.triggerChance()) {
            return;
        }

        List<Entity> targets = resolveTargets(effect.targetGroup(), caster, primaryTarget, context);

        for  (Entity target : targets) {
            if (target.isDead() && effect.effectType() != SkillEffect.EffectType.REVIVE) {
                continue;
            }

            double calculatedValue = calculateScaledValue(effect, caster, target, context);

            switch (effect.executionType()) {
                case INSTANT -> applyInstant(effect, caster, target, context, calculatedValue); // Execute general effect logic
                case DOT -> applyDoT(effect, caster, target, context, calculatedValue); // Add to the target effect as status with DoT logic
                case CHARGE -> applyCharge(effect, caster, target, context, calculatedValue); // Add to the target effect as status with Charge logic
                case CONSTANT -> applyConstant(effect, caster, target, context, calculatedValue); // Execute stat modifier logic or add effect as constant status (but don't stack)
            }
        }
    }

    private List<Entity> resolveTargets(SkillEffect.TargetGroup targetGroup, Entity caster, Entity primaryTarget, BattleContext context) {
        List<Entity> allies = context.getHeroes().contains(caster) ? context.getHeroes() : context.getEnemies();
        List<Entity> enemies = context.getHeroes().contains(caster) ? context.getEnemies() : context.getHeroes();
        return switch(targetGroup) {
            case SELF -> List.of(caster);
            case TARGET -> primaryTarget != null ? List.of(primaryTarget) : List.of();
            case SELECTED_ALLY -> context.getSelectedAlly() != null ? List.of(context.getSelectedAlly()) : List.of();
            case ALL_ENEMIES -> enemies;
            case ALL_ALLIES -> allies;
            case MOST_INJURED_ALLY -> allies.stream().filter(Predicate.not(Entity::isDead)).min(Comparator.comparingDouble(Entity::currentHp)).map(List::of).orElse(List.of());
            case MOST_INJURED_ENEMY -> enemies.stream().filter(Predicate.not(Entity::isDead)).min(Comparator.comparingDouble(Entity::currentHp)).map(List::of).orElse(List.of());
            case LEAST_INJURED_AlLY -> allies.stream().filter(Predicate.not(Entity::isDead)).max(Comparator.comparingDouble(Entity::currentHp)).map(List::of).orElse(List.of());
            case LEAST_INJURED_ENEMY -> enemies.stream().filter(Predicate.not(Entity::isDead)).max(Comparator.comparingDouble(Entity::currentHp)).map(List::of).orElse(List.of());
            case ALL_LIVING_ALLIES -> allies.stream().filter(Predicate.not(Entity::isDead)).toList();
            case ALL_LIVING_ENEMIES -> enemies.stream().filter(Predicate.not(Entity::isDead)).toList();
            case ALL_DEAD_ALLIES -> allies.stream().filter(Entity::isDead).toList();
            case ALL_DEAD_ENEMIES -> enemies.stream().filter(Entity::isDead).toList();
            case RANDOM_ALLY -> {
                List<Entity> livingAlly = allies.stream().filter(Predicate.not(Entity::isDead)).toList();
                yield livingAlly.isEmpty() ? List.of() : List.of(livingAlly.get(random.nextInt(livingAlly.size())));
            }
            case RANDOM_ENEMY -> {
                List<Entity> livingEnemy = enemies.stream().filter(Predicate.not(Entity::isDead)).toList();
                yield livingEnemy.isEmpty() ? List.of() : List.of(livingEnemy.get(random.nextInt(livingEnemy.size())));
            }
        };
    }

    private double calculateScaledValue(SkillEffect effect, Entity caster, Entity target, BattleContext context) {
        if (effect.statMultiplier() == null) return 0d;
        List<Entity> allies = context.getHeroes().contains(caster) ? context.getHeroes() : context.getEnemies();
        List<Entity> enemies = context.getHeroes().contains(caster) ? context.getEnemies() : context.getHeroes();
        return switch(effect.scaleType()) {
            case FLAT -> effect.statMultiplier();
            case DAMAGE_DEALT -> effect.statMultiplier() * context.getLastDamage();
            case HEAL_RECEIVED -> effect.statMultiplier() * context.getLastHeal();
            case CASTER_MAX_HP -> effect.statMultiplier() * caster.maxHp();
            case CASTER_CURRENT_HP -> effect.statMultiplier() * caster.currentHp();
            case CASTER_LOST_HP -> effect.statMultiplier() * (caster.maxHp() - caster.currentHp());
            case CASTER_CURRENT_HP_PERCENT -> caster.currentHp() * 100d / caster.maxHp() * effect.statMultiplier();
            case CASTER_LOST_HP_PERCENT -> (caster.maxHp() - caster.currentHp()) * 100d / caster.maxHp() * effect.statMultiplier();
            case CASTER_ATTACK -> effect.statMultiplier() * caster.attackDamage();
            case CASTER_DEFENSE -> effect.statMultiplier() * caster.defense();
            case CASTER_AGILITY -> effect.statMultiplier() * caster.agility();
            case TARGET_MAX_HP -> effect.statMultiplier() * target.maxHp();
            case TARGET_CURRENT_HP -> effect.statMultiplier() * target.currentHp();
            case TARGET_LOST_HP -> effect.statMultiplier() * (target.maxHp() - target.currentHp());
            case TARGET_CURRENT_HP_PERCENT -> target.currentHp() * 100d / target.maxHp() * effect.statMultiplier();
            case TARGET_LOST_HP_PERCENT -> (target.maxHp() - target.currentHp()) / target.maxHp() * 100d * effect.statMultiplier();
            case TARGET_ATTACK -> effect.statMultiplier() * target.attackDamage();
            case TARGET_DEFENSE -> effect.statMultiplier() * target.defense();
            case TARGET_AGILITY -> effect.statMultiplier() * target.agility();
            case PER_EACH_ALLY_DOWN -> effect.statMultiplier() * (allies.stream().filter(Entity::isDead).count());
            case PER_EACH_ENEMY_DOWN -> effect.statMultiplier() * (enemies.stream().filter(Entity::isDead).count());
        };
    }

    private void applyInstant(SkillEffect effect, Entity caster, Entity target, BattleContext context, double value) {
        switch (effect.effectType()) {
            case DAMAGE -> {
                //apply ON_ATTACK passives
                applyPassives(PassiveSkill.TriggerType.ON_ATTACK, caster, target, context);
                double damage = random.nextDouble() < caster.critRate() ? value * caster.critDamage() : value;
                if (target.defender() != null) {
                    System.out.printf("%s defends %s\n", target.defender().credentials().name(), target.credentials().name());
                    target.defender().receiveDamage(damage);
                } else {
                    target.receiveDamage(damage);
                }
                context.setLastDamage(damage);
                context.addRageChili(damage * 0.1d);

                if (target.isSpiky()) {
                    List<StatusEffect> thorns = target.statusEffects().stream().filter(s -> s.getEffect().effectType() == SkillEffect.EffectType.THORN).toList();
                    double reflectedDamage = 0d;
                    for (StatusEffect thorn : thorns) {
                        reflectedDamage += thorn.getEffect().scaleType() == SkillEffect.ScaleType.DAMAGE_DEALT ? damage * thorn.value() : thorn.value();
                    }
                    caster.receiveDamage(reflectedDamage);
                    System.out.printf("%s reflected %.2f damage back to %s\n", target.credentials().name(), reflectedDamage, caster.credentials().name());
                }
            }
            case HEAL -> {
                applyPassives(PassiveSkill.TriggerType.ON_HEAL, caster, target, context);
                target.heal(value);
                context.setLastHeal(value);
                System.out.printf("%s healed %.2f\n", target.credentials().name(), value);
            }
            case HEALTH_BOOST -> target.modifyMaxHpBy(value);
            case FOCUS_SELF -> target.focus(caster);
            case FOCUS_TARGET -> target.focus(context.getSelectedAlly());
            case PUT_SELF -> target.setDefender(caster);
            case PUT_TARGET -> target.setDefender(context.getSelectedAlly());
            case THORN -> target.setSpiky(true);
            case PROTECTION -> target.modifyDefenseBy(value);
            case WEAKNESS -> target.modifyAttackBy(-value);
            case STRENGTH -> target.modifyAttackBy(value);
            case SPEED -> target.modifyAgilityBy(value);
            case SLOWNESS -> target.modifyAgilityBy(-value);
            case STUN -> target.setStunned(true);
            case DISPEL_BUFFS -> target.statusEffects().removeIf(s ->
                    s.getEffect().effectType() == SkillEffect.EffectType.HEAL ||
                    s.getEffect().effectType() == SkillEffect.EffectType.HEALTH_BOOST ||
                    s.getEffect().effectType() == SkillEffect.EffectType.THORN ||
                    s.getEffect().effectType() == SkillEffect.EffectType.PROTECTION ||
                    s.getEffect().effectType() == SkillEffect.EffectType.STRENGTH ||
                    s.getEffect().effectType() == SkillEffect.EffectType.SPEED ||
                    s.getEffect().effectType() == SkillEffect.EffectType.IMMUNE ||
                    s.getEffect().effectType() == SkillEffect.EffectType.REVIVE ||
                    s.getEffect().effectType() == SkillEffect.EffectType.MODIFY_CRIT_RATE ||
                    s.getEffect().effectType() == SkillEffect.EffectType.PUT_SELF ||
                    s.getEffect().effectType() == SkillEffect.EffectType.PUT_TARGET ||
                    s.getEffect().effectType() == SkillEffect.EffectType.MODIFY_CRIT_DAMAGE
            );
            case DISPEL_DEBUFFS -> target.statusEffects().removeIf(s ->
                    s.getEffect().effectType() == SkillEffect.EffectType.DAMAGE ||
                    s.getEffect().effectType() == SkillEffect.EffectType.FOCUS_SELF ||
                    s.getEffect().effectType() == SkillEffect.EffectType.FOCUS_TARGET ||
                    s.getEffect().effectType() == SkillEffect.EffectType.WEAKNESS ||
                    s.getEffect().effectType() == SkillEffect.EffectType.SLOWNESS ||
                    s.getEffect().effectType() == SkillEffect.EffectType.STUN
            );
            case IMMUNE -> target.setImmune(true);
            case REVIVE -> {
                if (target.isDead()) target.heal(target.maxHp() * (value > 0d ? value : 1d));
            }
            case MODIFY_CRIT_RATE -> target.modifyCritRateBy(value);
            case MODIFY_CRIT_DAMAGE -> target.modifyCritDamageBy(value);
        }
    }

    private void applyDoT(SkillEffect effect, Entity caster, Entity target, BattleContext context, double value) {
        target.addStatusEffect(new StatusEffect(caster, effect, value));
    }

    private void applyCharge(SkillEffect effect, Entity caster, Entity target, BattleContext context, double value) {
        target.setCharging(true);
        target.addStatusEffect(new StatusEffect(caster, effect, value));
    }

    private void applyConstant(SkillEffect effect, Entity caster, Entity target, BattleContext context, double value) {
        applyInstant(effect, caster, target, context, value);
        target.addStatusEffect(new StatusEffect(caster, effect, value));
    }

    public void processStatusTick(StatusEffect effect, Entity owner, BattleContext context) {
        SkillEffect skillEffect = effect.getEffect();

        switch (skillEffect.executionType()) {
            case DOT, INSTANT, CONSTANT -> {
                effect.tick();
                List<Entity> targets = resolveTargets(skillEffect.targetGroup(), effect.caster(), owner, context);
                for (Entity target : targets) {
                    applyInstant(skillEffect, effect.caster(), target, context, effect.value());
                }
            }
            case CHARGE -> {
                effect.tick();
                if (effect.isExpired()) {
                    List<Entity> targets = resolveTargets(skillEffect.targetGroup(), effect.caster(), owner, context);
                    for (Entity target : targets) {
                        applyInstant(skillEffect, effect.caster(), target, context, effect.value());
                    }
                }
            }
        }
    }

    public void applyPassives(PassiveSkill.TriggerType type, Entity caster, Entity target, BattleContext context) {
        for (PassiveSkill passiveSkill : caster.getLoadout().getPassiveSkills()) {
            if (passiveSkill.triggerType() == type) {
                processPassive(passiveSkill, caster, target, context);
            }
        }
    }

    public void processPassive(PassiveSkill skill, Entity owner, Entity target, BattleContext context) {
        boolean triggers = shouldTriggerPassive(skill, owner, context);
        if (triggers) {
            for (SkillEffect effect : skill.effects()) {
                processEffect(effect, owner, target, context);
            }
        }
    }

    private boolean shouldTriggerPassive(PassiveSkill skill, Entity owner, BattleContext context) {
        if (skill.condition() == null) return true;
        PassiveSkill.PassiveCondition condition = skill.condition();
        return switch (condition.conditionType()) {
            case HP_BELOW_PERCENT -> owner.currentHp() / owner.maxHp() < condition.threshold();
            case HP_ABOVE_PERCENT -> owner.currentHp() / owner.maxHp() > condition.threshold();
            case DAMAGE_MORE_THAN -> context.getLastDamage() > condition.threshold();
            case DAMAGE_LESS_THAN -> context.getLastDamage() < condition.threshold();
            case ON_DEATH -> owner.isDead();
            case RAGE_CHILI_BELOW -> context.getRageChili() < condition.threshold();
            case RAGE_CHILI_ABOVE -> context.getRageChili() > condition.threshold();
        };
    }

    public static void processConstantEffects(SkillEffect effect, Entity target) {
        switch (effect.effectType()) {
            case HEALTH_BOOST ->  target.modifyMaxHpBy(target.getInitialHp() * effect.statMultiplier());
            case PROTECTION -> target.modifyDefenseBy(target.getInitialDefense() * effect.statMultiplier());
            case STRENGTH -> target.modifyAttackBy(target.getInitialAttack() * effect.statMultiplier());
            case WEAKNESS -> target.modifyAttackBy(-target.getInitialAttack() * effect.statMultiplier());
            case SPEED -> target.modifyAgilityBy(target.getInitialAgility() * effect.statMultiplier());
            case SLOWNESS ->  target.modifyAgilityBy(-target.getInitialAgility() * effect.statMultiplier());
            case MODIFY_CRIT_RATE -> target.modifyCritRateBy(effect.statMultiplier());
            case MODIFY_CRIT_DAMAGE -> target.modifyCritDamageBy(effect.statMultiplier());
            case DISPEL_BUFFS -> target.statusEffects().removeIf(s ->
                    s.getEffect().effectType() == SkillEffect.EffectType.HEAL ||
                            s.getEffect().effectType() == SkillEffect.EffectType.HEALTH_BOOST ||
                            s.getEffect().effectType() == SkillEffect.EffectType.THORN ||
                            s.getEffect().effectType() == SkillEffect.EffectType.PROTECTION ||
                            s.getEffect().effectType() == SkillEffect.EffectType.STRENGTH ||
                            s.getEffect().effectType() == SkillEffect.EffectType.SPEED ||
                            s.getEffect().effectType() == SkillEffect.EffectType.IMMUNE ||
                            s.getEffect().effectType() == SkillEffect.EffectType.REVIVE ||
                            s.getEffect().effectType() == SkillEffect.EffectType.MODIFY_CRIT_RATE ||
                            s.getEffect().effectType() == SkillEffect.EffectType.PUT_SELF ||
                            s.getEffect().effectType() == SkillEffect.EffectType.PUT_TARGET ||
                            s.getEffect().effectType() == SkillEffect.EffectType.MODIFY_CRIT_DAMAGE
            );
            case DISPEL_DEBUFFS -> target.statusEffects().removeIf(s ->
                    s.getEffect().effectType() == SkillEffect.EffectType.DAMAGE ||
                            s.getEffect().effectType() == SkillEffect.EffectType.FOCUS_SELF ||
                            s.getEffect().effectType() == SkillEffect.EffectType.FOCUS_TARGET ||
                            s.getEffect().effectType() == SkillEffect.EffectType.WEAKNESS ||
                            s.getEffect().effectType() == SkillEffect.EffectType.SLOWNESS ||
                            s.getEffect().effectType() == SkillEffect.EffectType.STUN
            );
            case THORN -> target.setSpiky(true);
            case STUN -> target.setStunned(true);
            case IMMUNE -> target.setImmune(true);
            default -> {}
        }
    }
}
