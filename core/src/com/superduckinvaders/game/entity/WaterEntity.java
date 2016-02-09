package com.superduckinvaders.game.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.superduckinvaders.game.Round;

/**
 * Created by james on 09/02/16.
 */
public class WaterEntity extends PhysicsEntity {

    public WaterEntity(Round parent, float x, float y, float width, float height) {
        super(parent, x, y);
        this.width = width;
        this.height = height;
        createBody(BodyDef.BodyType.StaticBody, WATER_BITS, ALL_BITS, NO_GROUP, true);
    }

    @Override
    public void beginContact(PhysicsEntity other, Contact contact){
        if (other instanceof Player){
            contact.setEnabled(false);
            ((Player) other).waterBlockCount++;
        }
    }

    @Override
    public void endContact(PhysicsEntity other, Contact contact){
        if (other instanceof Player){
            ((Player) other).waterBlockCount--;
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch) {}

}
