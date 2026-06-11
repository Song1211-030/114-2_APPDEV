package com.example.myapplication;

public class FishData {
    private String name;
    private int imageResId;
    private String rarity;
    private int feedCount;
    private int size;

    public FishData(String name, int imageResId, String rarity) {
        this.name = name;
        this.imageResId = imageResId;
        this.rarity = rarity;
        this.feedCount = 0;
        this.size = 120;
    }

    public String getName() { return name; }
    public int getImageResId() { return imageResId; }
    public String getRarity() { return rarity; }
    public int getFeedCount() { return feedCount; }
    public int getSize() { return size; }

    public void feed() {
        feedCount++;
        size += 10;
    }
}
