package NG.InputHandling.Controllers;

import NG.Entities.Entity;
import NG.Entities.MovingEntity;

/**
 * @author Geert van Ieperen. Created on 23-7-2018.
 */
public class HunterAI extends RocketAI {
    private static final float SHOOT_ACCURACY = 0.05f;

    private final Entity actualTarget;

    /**
     * a controller that tries to collect powerups and hunt down the player
     * @param user         the entity that is controlled by this controller
     * @param target       the target entity that this projectile tries to hunt down
     * @param maximumSpeed the assumed (and preferably over-estimated) maximum speed of the given jet
     */
    public HunterAI(MovingEntity user, Entity target, float maximumSpeed) {
        super(user, target, maximumSpeed, 10f);
        this.actualTarget = this.target;
    }

    @Override
    public void update() {

        if (target == null) {
//            target = getClosestPowerup(actualTarget);
            target = actualTarget;

        } else if (!target.equals(actualTarget)) {
            target = actualTarget;
        }

        super.update();
    }

    @Override
    public boolean primaryFire() {
        // do check for having offensive abilities
        return xVec.dot(vecToTarget) > (1 - SHOOT_ACCURACY);
    }
}
