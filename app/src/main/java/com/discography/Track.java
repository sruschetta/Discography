package com.discography;


public class Track {
    private String title;
    private String album;
    private String coverURL;

    public Track(String title, String album, String coverURL){
        this.title = title;
        this.album = album;
        this.coverURL = coverURL;
    }

    public String getTitle(){
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getCoverURL(){
        return coverURL;
    }
}
