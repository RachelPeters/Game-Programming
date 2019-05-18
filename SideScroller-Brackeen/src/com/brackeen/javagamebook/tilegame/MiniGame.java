package com.brackeen.javagamebook.tilegame;

import com.brackeen.javagamebook.sound.SoundManager;

public class MiniGame {
    private SoundManager soundManager;
    private String backGroundImage;
    private String music;
    private String tileLayout;
    private TileMap map;
    private int score=0;
    private int totalPointsAvailable;
    private boolean isCompleted = false;
    public MiniGame (String bgi, String tl){
        setBackGroundImage(bgi);
        setTileLayout(tl);
    }

    //getters
    public String getBackGroundImage() {
        return backGroundImage;
    }

    public String getMusic() {
        return music;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public String getTileLayout() {
        return tileLayout;
    }

    public TileMap getMap() {
        return map;
    }

    public boolean getIsCompleted(){
        return isCompleted;
    }

    public int getScore(){return score;}

    public int getTotalPointsAvailable(){return totalPointsAvailable;}

    //setters
    public void setBackGroundImage(String backGroundImage) {
        this.backGroundImage = backGroundImage;
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setTileLayout(String tileLayout) {
        this.tileLayout = tileLayout;
    }

    public void setMap(TileMap map) {
        this.map = map;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setScore(int score) { this.score = score; }

    public void setTotalPointsAvailable(int tpa){ this.totalPointsAvailable = tpa;}
    //methods


}
