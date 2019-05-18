package com.brackeen.javagamebook.tilegame;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;


public class Game extends GameCore{

    private static final AudioFormat PLAYBACK_FORMAT =
            new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;
    private Point pointCache = new Point();
    private ResourceManager resourceManager;
    private TileMapRenderer renderer;

    private SoundManager soundManager;
    private MidiPlayer midiPlayer;
    private Sound prizeSound;
    private Sound boopSound;

    private TileMap worldMap;
    private TileMap currentMap;
    private InputManager inputManager;
    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction moveUp;
    private GameAction moveDown;
    private GameAction jump;
    private GameAction exit;
    private GameAction startLevel;
    private GameManager gameManager;
    private ArrayList<MiniGame> miniGames = new ArrayList<MiniGame>();
    private int currentMinigame=0;
    public int points = 0;
    private String worldBackground= "worldBackground.png";
    private String currentBackgroundImage;
    public static void main(String[] args) {
        new Game().run();
    }
/*
    public void run(){
        resourceManager = new ResourceManager(screen.getFullScreenWindow().getGraphicsConfiguration());
        GameManager gm = new GameManager();
        gm.run();
    }
 */
    public ScreenManager init1() {
        super.init();
        return screen;
    }


    public void init() {
        super.init();
        initInput();
        // start resource manager
        resourceManager = new ResourceManager(
                screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
                resourceManager.loadImage(worldBackground));

        loadMiniGames();
        // load first map
        worldMap = resourceManager.loadWorld();
        currentMap = worldMap;

        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\sounds\\prize.wav");
        boopSound = soundManager.getSound("C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\sounds\\boop2.wav");

        midiPlayer = new MidiPlayer();
        Sequence sequence =
                midiPlayer.getSequence("C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\sounds\\world.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
    }

    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                    !sequencer.getTrackMute(DRUM_TRACK));
        }
    }

    private int getCurrentMinigame(){
        return currentMinigame;
    }

    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        moveUp = new GameAction("moveUp");
        moveDown = new GameAction("moveDown");
        jump = new GameAction("jump",GameAction.DETECT_INITAL_PRESS_ONLY);
        startLevel = new GameAction("startLevel");
        exit = new GameAction("exit",GameAction.DETECT_INITAL_PRESS_ONLY);

        inputManager = new InputManager(screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(moveUp, KeyEvent.VK_UP);
        inputManager.mapToKey(moveDown, KeyEvent.VK_DOWN);
        inputManager.mapToKey(jump,KeyEvent.VK_SPACE);
        inputManager.mapToKey(startLevel, KeyEvent.VK_A);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
    }

    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {stop();}

        Player player = (Player)currentMap.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            //float velocityY=player.getVelocityY();
            if (!currentMap.equals(worldMap)) {
                if (moveLeft.isPressed()) {velocityX -= player.getMaxSpeed();}
                if (moveRight.isPressed()) {velocityX += player.getMaxSpeed();}
                if (jump.isPressed()) {player.jump(false);}
            }
            if(currentMap.equals(worldMap)) {
                float velocityY = 0;
                if (moveLeft.isPressed()) {velocityX-=player.getMaxSpeed();}
                if (moveRight.isPressed()) { velocityX+=player.getMaxSpeed(); }
                if (moveUp.isPressed()) {velocityY -= player.getMaxSpeed();}
                if (moveDown.isPressed()) {velocityY += player.getMaxSpeed();}
                if(startLevel.isPressed()){System.out.println("start level");}
                player.setVelocityY(velocityY);
            }

            player.setVelocityX(velocityX);

        }

    }

    public Point getTileCollision(Sprite sprite,
                                  float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
                toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
                toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= currentMap.getWidth() ||
                        currentMap.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }

    public void loadMiniGames(){

        String currBGI;
        String currTL;
        for (int i=0;i<7;i++){
            currBGI = "background" + i + ".png";
            currTL = "C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\maps\\map" + i + ".txt";
            miniGames.add(new MiniGame(currBGI,currTL)) ;
        }
    }


    /**
     Updates the creature, applying gravity for creatures that
     aren't flying, and checks collisions.
     */
    private void updateCreatureWorld(Creature creature,
                                long elapsedTime)
    {

        // apply gravity
        if (!creature.isFlying() || creature instanceof Lakitu) {
            creature.setVelocityY(creature.getVelocityY());
        }
        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        if (!getScreenCollisionX(newX, creature.getHeight())) {
            creature.setX(newX);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        if (!getScreenCollisionY(newY, creature.getHeight())) {
            creature.setY(newY);
        }
        try {
            if (creature instanceof Player) {
                Sprite s = getSpriteCollision(currentMap.getPlayer());
                //System.out.println(s);
                if (s != null) {
                    acquirePowerUp((PowerUp) s);
                }
            }
    }catch(Exception e){
            //System.out.println(e);
        }

    }

    private void updateCreature(Creature creature,
                                long elapsedTime)
    {

        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                    GRAVITY * elapsedTime);
        }

        //if creature can attack, perform the attack
        if (creature.isCanAttack()){
            //int sprite = creature.attack();
            //resourceManager.addIndividualSprite(currentMap,(char)(sprite+ '0'), (int)creature.getX(),(int)creature.getY());

        }
        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
                getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                        TileMapRenderer.tilesToPixels(tile.x) -
                                creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                        TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                        TileMapRenderer.tilesToPixels(tile.y) -
                                creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                        TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }

    }

    public void checkPlayerCollision(Player player,
                                     boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
                soundManager.play(boopSound);
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            }
            else {
                // player dies!
                player.setState(Creature.STATE_DYING);
                try {
                    currentMap = resourceManager.loadMap(miniGames.get(currentMinigame).getTileLayout());
                    miniGames.get(currentMinigame).setScore(0);

                }
                catch (IOException io){
                    System.out.println(io);
                    currentMap = worldMap;
                }

            }
        }
    }

    public boolean getScreenCollisionX(float Y, int h){
        if (Y<0 || Y+1.5*h>=resourceManager.iWidth* currentMap.getWidth())
            return true;
        return false;
    }

    public boolean getScreenCollisionY(float Y, int h){
        if (Y<0 || Y+1.5*h>=resourceManager.iHeight* currentMap.getHeight())
            return true;
        return false;
    }


    public void draw(Graphics2D g) {
        if(currentMap == worldMap) {
            renderer.draw(g, currentMap, screen.getWidth(), screen.getHeight(), points);
        }else {
            renderer.draw(g, currentMap, screen.getWidth(), screen.getHeight(), miniGames.get(currentMinigame).getScore());
        }
    }


    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            //System.out.println("No COllision");
            return false;
        }
        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            //System.out.println("No COllision");
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            //System.out.println("No COllision");
            return false;
        }
        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
                s2x < s1x + s1.getWidth() &&
                s1y < s2y + s2.getHeight() &&
                s2y < s1y + s1.getHeight());
    }

    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = currentMap.getSprites();
        while (i.hasNext()) {

            Sprite otherSprite = (Sprite)i.next();
            //System.out.println(otherSprite instanceof PowerUp.Level);
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }

    public void update(long elapsedTime) {

        if (!midiPlayer.getSequencer().isOpen()){
            midiPlayer.close();
            midiPlayer = new MidiPlayer();
            Sequence sequence =
                    midiPlayer.getSequence("C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\sounds\\world.midi");
            midiPlayer.play(sequence, true);
            toggleDrumPlayback();
        }

        Creature player = (Creature)currentMap.getPlayer();

        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            currentMap = resourceManager.reloadMap(miniGames.get(currentMinigame).getTileLayout());
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);
        // update player
        if (currentMap.equals(worldMap)) {
            updateCreatureWorld(player, elapsedTime);
        }else {
            updateCreature(player, elapsedTime);
        }
        player.update(elapsedTime);

        Iterator i = currentMap.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);

                }
            }
            // normal update
            sprite.update(elapsedTime);

        }

        //System.out.println(m);
        //m.clear();
    }

    public void acquirePowerUp(PowerUp powerUp) {
        if(!currentMap.equals(worldMap)) {
            currentMap.removeSprite(powerUp);
        }
        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
            if(!currentMap.equals(worldMap)){
                miniGames.get(currentMinigame).setScore(miniGames.get(currentMinigame).getScore()+5);
            }
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            midiPlayer.close();
            midiPlayer = new MidiPlayer();
            Sequence sequence =
                    midiPlayer.getSequence("C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\sounds\\world.midi");
            midiPlayer.play(sequence, true);
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // go back to world map
            MiniGame mg = miniGames.get(currentMinigame);
            mg.setCompleted(true);
            points +=mg.getScore();
            renderer.setBackground(resourceManager.loadImage(worldBackground));
            currentMap = worldMap;
        }
        else if (powerUp instanceof PowerUp.Level) {
            int level = ((PowerUp.Level)powerUp).getLevel(); //level = 1..x
            MiniGame mg = miniGames.get(level-1);
            if (mg.getIsCompleted()){//if this is the first time starting the level or it has not been completed
                if(startLevel.isPressed()){//if the user wants to start this game
                    try {
                        renderer.setBackground(resourceManager.loadImage(mg.getBackGroundImage()));
                        currentMap = resourceManager.loadMap(mg.getTileLayout());

                    }
                    catch (IOException io){
                        currentMinigame=0;
                        System.out.println(io);
                    }
                }
            }else {
                currentMinigame = level-1;
                try {
                    renderer.setBackground(resourceManager.loadImage(mg.getBackGroundImage()));
                    currentMap = resourceManager.loadMap(mg.getTileLayout());
                    Iterator i = currentMap.getSprites();
                    while (i.hasNext()) {
                        Sprite sprite = (Sprite) i.next();
                        if (sprite instanceof PowerUp.Star) {
                            mg.setTotalPointsAvailable(mg.getTotalPointsAvailable()+5);
                        }
                    }
                } catch (IOException io) {
                    currentMinigame = 0;
                    System.out.println(io);
                }
            }
        }
    }
}