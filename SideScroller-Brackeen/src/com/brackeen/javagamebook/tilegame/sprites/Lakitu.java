package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
 A Grub is a Creature that moves slowly on the ground.
 */
public class Lakitu extends Creature {

    public Lakitu(Animation left, Animation right,
                Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight, true);
        setVelocityY(0.1f);
    }


    public float getMaxSpeed() {
        return 0.3f;
    }

    public int attack(){
        //System.out.println("Trying to attack");
        //if(getAttackTime()>3) {
            return 1;
        //}
        //return 0;
    }

    public boolean isFlying() {
        return isAlive();
    }

    public void collideVertical() {
        setVelocityY(-getVelocityY());
    }
}
