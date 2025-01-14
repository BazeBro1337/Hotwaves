package net.bzbr.hotwaves.common.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;

public class PlayerTrackingGoal extends Goal {

    protected final MobEntity mob;
    protected final LivingEntity target;
    protected final double speed;
    protected EntityNavigation pather;
    protected int timeToRecalcPath;
    private boolean targetIsFar;

    public PlayerTrackingGoal(MobEntity mob, LivingEntity target, double speed) {
        this.mob = mob;
        this.target = target;
        this.speed = speed;
        this.pather = mob.getNavigation();
        this.mob.setTarget(this.target);
        timeToRecalcPath = mob.getRandom().nextInt(20);
    }

    @Override
    public boolean shouldContinue(){

        return this.targetIsFar;
    }

    @Override
    public void start(){
        this.targetIsFar = target.distanceTo(this.mob) >= 8D;
    }

    @Override
    public boolean canStart() {

        return target != null && target.isAlive() && mob.getTarget() == null;
    }

    @Override
    public void tick() {
        if (timeToRecalcPath-- <= 0) {
            timeToRecalcPath = 200;
            this.targetIsFar = target.distanceTo(this.mob) >= 8D;
            pather = mob.getNavigation();
            pather.startMovingTo(target, speed);
        }
    }
}
