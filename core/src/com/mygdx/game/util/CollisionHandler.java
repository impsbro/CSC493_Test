package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.WorldController;
import com.mygdx.game.asset.Assets;
import com.mygdx.game.objects.AbstractGameObject;
import com.mygdx.game.objects.Block;
import com.mygdx.game.objects.Land;
import com.mygdx.game.objects.Player;
import com.mygdx.game.objects.Player.JUMP_STATE;

public class CollisionHandler implements ContactListener
{
    private ObjectMap<Short, ObjectMap<Short, ContactListener>> listeners;
    
    private WorldController world;

    public CollisionHandler(WorldController w)
    {
    	world = w;
        listeners = new ObjectMap<Short, ObjectMap<Short, ContactListener>>();
    }

    public void addListener(short categoryA, short categoryB, ContactListener listener)
    {
        addListenerInternal(categoryA, categoryB, listener);
        addListenerInternal(categoryB, categoryA, listener);
    }

    @Override
    public void beginContact(Contact contact)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
        //Gdx.app.log("CollisionHandler-begin A", "begin");
        
       // processContact(contact);
        
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.beginContact(contact);
        }
    }

    @Override
    public void endContact(Contact contact)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
       // Gdx.app.log("CollisionHandler-end A", "end");
        processContact(contact);
        
        // Gdx.app.log("CollisionHandler-end A", fixtureA.getBody().getLinearVelocity().x+" : "+fixtureA.getBody().getLinearVelocity().y);
        // Gdx.app.log("CollisionHandler-end B", fixtureB.getBody().getLinearVelocity().x+" : "+fixtureB.getBody().getLinearVelocity().y);
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.endContact(contact);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.preSolve(contact, oldManifold);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.postSolve(contact, impulse);
        }
    }

    private void addListenerInternal(short categoryA, short categoryB, ContactListener listener)
    {
        ObjectMap<Short, ContactListener> listenerCollection = listeners.get(categoryA);
        if (listenerCollection == null)
        {
            listenerCollection = new ObjectMap<Short, ContactListener>();
            listeners.put(categoryA, listenerCollection);
        }
        listenerCollection.put(categoryB, listener);
    }

    private ContactListener getListener(short categoryA, short categoryB)
    {
        ObjectMap<Short, ContactListener> listenerCollection = listeners.get(categoryA);
        if (listenerCollection == null)
        {
            return null;
        }
        return listenerCollection.get(categoryB);
    }
    
    private void processContact(Contact contact)
    {
    	Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        AbstractGameObject objA = (AbstractGameObject)fixtureA.getBody().getUserData();
        AbstractGameObject objB = (AbstractGameObject)fixtureB.getBody().getUserData();
        
        if (objA instanceof Player)
        {
        	processPlayerContact(fixtureA, fixtureB);
        }
        else if (objB instanceof Player)
        {
        	processPlayerContact(fixtureB, fixtureA);
        }
    }
    
    private void processPlayerContact(Fixture playerFixture, Fixture objFixture)
    {
    	if (objFixture.getBody().getUserData() instanceof Land)
    	{
    		Player player = (Player)playerFixture.getBody().getUserData();
    	    player.acceleration.y = 0;
    	    player.velocity.y = 0;
    	    player.jumpState = JUMP_STATE.GROUNDED;
    	    playerFixture.getBody().setLinearVelocity(player.velocity);
    	}
    	else if (objFixture.getBody().getUserData() instanceof Block)
    	{
    		// Remove the block update the player's score by 1.
    		world.score++;
    		AudioManager.instance.play(Assets.instance.sounds.pickupCoin);
    		
    		Block block = (Block)objFixture.getBody().getUserData();
    		world.flagForRemoval(block);
    	}
    }
	
}