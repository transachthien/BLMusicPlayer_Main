package com.example.blmusicplayer;



import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements  MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{
    public MediaPlayer player;
    private ArrayList<Song> songlist;
    private int curpos;
    private final IBinder binder = new MusicBinder();
    private boolean iscomplete = false;
    public boolean shuffle = false;
    public boolean replay = false;

    public void onCreate(){
        super.onCreate();
        curpos = 0;
        player = new MediaPlayer();
        initPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void initPlayer(){
        player.reset();
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        //player.reset();
    }

    public void setSongList(ArrayList<Song> songs){
        songlist = songs;
        curpos = -1;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        if(replay){

        }
        else if(shuffle){
            Random  ran = new Random();
            curpos = ran.nextInt(songlist.size());
        }
        else{
            curpos++;
            if (curpos == songlist.size())
                curpos = 0;
        }
        try {
            startPlaying();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void startPlaying() throws IOException {
        player.reset();
        Song playSong = songlist.get(curpos);
        long currSong = playSong.id;
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepare();


    }

    public void playPrev() throws IOException {
        if(shuffle)
        {
            Random  ran = new Random();
            curpos = ran.nextInt(songlist.size());
        }
        else {
            curpos--;
            if (curpos < 0)
                curpos = songlist.size() - 1;
        }
        startPlaying();
    }

    public void playNext() throws IOException {
        if(shuffle)
        {
            Random  ran = new Random();
            curpos = ran.nextInt(songlist.size());
        }
        else {
            curpos++;
            if (curpos == songlist.size())
                curpos = 0;
        }
        startPlaying();
    }
    public int getCurpos() { return this.curpos; }
    public void setCurpos(int pos){
        curpos = pos;
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public int pausePlayer(){
        player.pause();
        return curpos;
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public int go(){
        player.start();
        return  curpos;
    }
    public  void setShuffle()
    {
        if(shuffle == false) shuffle=true;
        else shuffle =false;
    }
    public void setReplay()
    {
        if(replay == false) replay = true;
        else  replay = false;
    }
}
