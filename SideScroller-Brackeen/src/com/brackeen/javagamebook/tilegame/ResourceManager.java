package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.tilegame.sprites.*;


/**
    The ResourceManager class loads and manages tile Images and
    "host" Sprites used in the game. Game Sprites are cloned from
    "host" Sprites.
*/
public class ResourceManager {

    private ArrayList tiles;
    private int currentMap = 0;
    private GraphicsConfiguration gc;

    // host sprites used for cloning
    private Sprite playerSprite;
    private Sprite musicSprite;
    private Sprite coinSprite;
    private Sprite goalSprite;
    private Sprite grubSprite;
    private Sprite flySprite;
    private Sprite lakituSprite;
    private Sprite levelSprite;

    public int iHeight;
    public int iWidth;

    /**
        Creates a new ResourceManager with the specified
        GraphicsConfiguration.
    */
    public ResourceManager(GraphicsConfiguration gc) {
        this.gc = gc;
        loadTileImages();
        loadCreatureSprites();
        loadPowerUpSprites();
    }

/*  The following constructor added by P. Mohan on February 8, 2019 */

    public ResourceManager() {
        //this.gc = gc;			// don't worry about graphics for now
        loadTileImages();
        loadCreatureSprites();
        loadPowerUpSprites();
    }

    public Sprite getPlayerSprite(){
        return playerSprite;
    }


    /**
        Gets an image from the images/ directory.
    */
    public Image loadImage(String name) {
	String filename = "C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\images\\" + name;
        //String filename = "images/" + name;

            File file = new File(filename);
            if (!file.exists()) {
System.out.println("Image file could not be opened: " + filename);
            }
else
System.out.println("Image file opened: " + filename);

        return new ImageIcon(filename).getImage();
    }


    public Image getMirrorImage(Image image) {
        return getScaledImage(image, -1, 1);
    }


    public Image getFlippedImage(Image image) {
        return getScaledImage(image, 1, -1);
    }


    private Image getScaledImage(Image image, float x, float y) {

        // set up the transform
        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate(
            (x-1) * image.getWidth(null) / 2,
            (y-1) * image.getHeight(null) / 2);

        // create a transparent (not translucent) image
        Image newImage = gc.createCompatibleImage(
            image.getWidth(null),
            image.getHeight(null),
            Transparency.BITMASK);

        // draw the transformed image
        Graphics2D g = (Graphics2D)newImage.getGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }

    public TileMap loadWorld() {
        TileMap map = null;
        String worldFile = "C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\maps\\world1" + ".txt";
        try {
            map = loadMap(worldFile);

        } catch (IOException ex) {
            System.out.println ("Could not find map to load" + worldFile);
        }
        return map;
    }

    public TileMap loadNextMap() {
        TileMap map = null;
        while (map == null) {
            currentMap++;
   	    String mapFile = "C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\maps\\map" + currentMap + ".txt";
            try {		
		map = loadMap(mapFile);
/*
                map = loadMap(
                    "maps/map" + currentMap + ".txt");
*/
            }
            catch (IOException ex) {
		System.out.println ("Could not find map to load" + mapFile);
                if (currentMap == 1) {
                    // no maps to load!
                    return null;
                }
                currentMap = 0;
                map = null;
            }
        }

        return map;
    }


    public TileMap reloadMap(String map) {

        try {
            return loadMap(map);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public TileMap loadMap(String filename)
        throws IOException
    {
        System.out.println("LOADING MAP: "+ filename);
        ArrayList lines = new ArrayList();
        int width = 0;
        int height = 0;

        // read every line in the text file into the list
        BufferedReader reader = new BufferedReader(
            new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
                width = Math.max(width, line.length());
            }
        }

        // parse the lines to create a TileEngine
        height = lines.size();
        TileMap newMap = new TileMap(width, height);
        for (int y=0; y<height; y++) {
            String line = (String)lines.get(y);
            for (int x=0; x<line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, (Image)tiles.get(tile));
                }

                // check if the char represents a sprite
                else
                {
                    addIndividualSprite(newMap,ch,x,y);
                }
            }
        }

        // add the player to the map
        Sprite player = (Sprite)playerSprite.clone();
        player.setX(TileMapRenderer.tilesToPixels(3));
        player.setY(0);
        newMap.setPlayer(player);

        return newMap;
    }

    public void addIndividualSprite(TileMap newMap, char ch, int x, int y){

        if (ch == 'o') {
            addSprite(newMap, coinSprite, x, y);
        }
        else if (ch == '!') {
            addSprite(newMap, musicSprite, x, y);
        }
        else if (ch == '*') {
            addSprite(newMap, goalSprite, x, y);
        }
        else if (ch == '1') {
            addSprite(newMap, grubSprite, x, y);
        }
        else if (ch == '2') {
            addSprite(newMap, flySprite, x, y);
        }
        else if (ch == '3') {
            addSprite(newMap, levelSprite, x, y);
        }
        else if (ch == '4') {
            addSprite(newMap, lakituSprite, x, y);
        }
    }

    private void addSprite(TileMap map,
        Sprite hostSprite, int tileX, int tileY)
    {
        if (hostSprite != null) {
            // clone the sprite from the "host"
            Sprite sprite = (Sprite)hostSprite.clone();

            // center the sprite
            sprite.setX(
                TileMapRenderer.tilesToPixels(tileX) +
                (TileMapRenderer.tilesToPixels(1) -
                sprite.getWidth()) / 2);

            // bottom-justify the sprite
            sprite.setY(
                TileMapRenderer.tilesToPixels(tileY + 1) -
                sprite.getHeight());

            // add it to the map
            map.addSprite(sprite);
        }
    }


    // -----------------------------------------------------------
    // code for loading sprites and images
    // -----------------------------------------------------------


    public void loadTileImages() {
        // keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ directory
        tiles = new ArrayList();
        char ch = 'A';
        while (true) {
            String name = "tile_" + ch + ".png";
            File file = new File("C:\\Users\\rache\\Documents\\UWI\\Year 3\\Y3S2\\Game Programming\\2019-03-19\\SideScroller-Brackeen\\images\\" + name);
            if (!file.exists()) {
System.out.println("Image file could not be opened: " + name);
                break;
            }
else
System.out.println("Image file opened: " + name);
            tiles.add(loadImage(name));
            ch++;
        }
        Image i = (Image)tiles.get(0);
        iWidth = i.getWidth(null);
        iHeight = i.getHeight(null);
    }


    public void loadCreatureSprites() {

        Image[][] images = new Image[4][];

        // load left-facing images
        images[0] = new Image[] {
            loadImage("Run1.png"),
            loadImage("Run2.png"),
            loadImage("Run3.png"),
            loadImage("fly1.png"),
            loadImage("fly2.png"),
            loadImage("fly3.png"),
            loadImage("grub1.png"),
            loadImage("grub2.png"),
                loadImage("lakitu1.png"),
                loadImage("lakitu2.png"),
        };

        images[1] = new Image[images[0].length];
        images[2] = new Image[images[0].length];
        images[3] = new Image[images[0].length];
        for (int i=0; i<images[0].length; i++) {
            // right-facing images
            images[1][i] = getMirrorImage(images[0][i]);
            // left-facing "dead" images
            images[2][i] = getFlippedImage(images[0][i]);
            // right-facing "dead" images
            images[3][i] = getFlippedImage(images[1][i]);
        }

        // create creature animations
        Animation[] playerAnim = new Animation[4];
        Animation[] flyAnim = new Animation[4];
        Animation[] grubAnim = new Animation[4];
        Animation[] lakituAnim = new Animation[4];

        for (int i=0; i<4; i++) {
            playerAnim[i] = createPlayerAnim(
                images[i][0], images[i][1], images[i][2]);
            flyAnim[i] = createFlyAnim(
                images[i][3], images[i][4], images[i][5]);
            grubAnim[i] = createGrubAnim(
                images[i][6], images[i][7]);
            lakituAnim[i] = createLakituAnim(
                    images[i][8],images[i][9]);
        }

        // create creature sprites
        playerSprite = new Player(playerAnim[0], playerAnim[1],
            playerAnim[2], playerAnim[3]);
        flySprite = new Fly(flyAnim[0], flyAnim[1],
            flyAnim[2], flyAnim[3]);
        grubSprite = new Grub(grubAnim[0], grubAnim[1],
            grubAnim[2], grubAnim[3]);
        lakituSprite = new Lakitu(lakituAnim[0], lakituAnim[1],
                lakituAnim[2], lakituAnim[3]);
System.out.println("loadCreatureSprites successfully executed.");

    }


    private Animation createPlayerAnim(Image player1,
        Image player2, Image player3)
    {
        Animation anim = new Animation();
        anim.addFrame(player1, 250);
        anim.addFrame(player2, 150);
        anim.addFrame(player1, 150);
        anim.addFrame(player2, 150);
        anim.addFrame(player3, 200);
        anim.addFrame(player2, 150);
        return anim;
    }


    private Animation createFlyAnim(Image img1, Image img2,
        Image img3)
    {
        Animation anim = new Animation();
        anim.addFrame(img1, 50);
        anim.addFrame(img2, 50);
        anim.addFrame(img3, 50);
        anim.addFrame(img2, 50);
        return anim;
    }


    private Animation createGrubAnim(Image img1, Image img2) {
        Animation anim = new Animation();
        anim.addFrame(img1, 250);
        anim.addFrame(img2, 250);
        return anim;
    }

    private Animation createLakituAnim(Image img1, Image img2) {
        Animation anim = new Animation();
        anim.addFrame(img1, 750);
        anim.addFrame(img2, 750);
        return anim;
    }


    private void loadPowerUpSprites() {
        // create "goal" sprite
        Animation anim = new Animation();
        anim.addFrame(loadImage("heart1.png"), 150);
        anim.addFrame(loadImage("heart2.png"), 150);
        anim.addFrame(loadImage("heart3.png"), 150);
        anim.addFrame(loadImage("heart2.png"), 150);
        goalSprite = new PowerUp.Goal(anim);

        // create "star" sprite
        anim = new Animation();
        anim.addFrame(loadImage("star1.png"), 100);
        anim.addFrame(loadImage("star2.png"), 100);
        anim.addFrame(loadImage("star3.png"), 100);
        anim.addFrame(loadImage("star4.png"), 100);
        coinSprite = new PowerUp.Star(anim);

        // create "music" sprite
        anim = new Animation();
        anim.addFrame(loadImage("music1.png"), 150);
        anim.addFrame(loadImage("music2.png"), 150);
        anim.addFrame(loadImage("music3.png"), 150);
        anim.addFrame(loadImage("music2.png"), 150);
        musicSprite = new PowerUp.Music(anim);

        anim = new Animation();
        anim.addFrame(loadImage("leve11.png"), 150);
        anim.addFrame(loadImage("level2.png"), 150);
        anim.addFrame(loadImage("level3.png"), 150);
        levelSprite = new PowerUp.Level(anim);

System.out.println("loadPowerUpSprites successfully executed.");
    }

}
