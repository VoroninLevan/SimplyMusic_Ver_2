package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Levan on 26.01.2017.
 */

public class DetailSong extends AppCompatActivity {

    ImageView albumArt;
    TextView songTitle;
    TextView songArtist;
    ImageButton shuffle;
    ImageButton skipPrevious;
    ImageButton playPause;
    ImageButton skipNext;
    ImageButton repeat;
    SeekBar seekBar;
    private MediaPlayerService mediaPlayerService = MediaPlayerService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_song);

        albumArt = (ImageView)findViewById(R.id.albumArtDetail);
        songTitle = (TextView)findViewById(R.id.titleDetail);
        songArtist = (TextView)findViewById(R.id.artistDetail);
        shuffle = (ImageButton)findViewById(R.id.shuffle);
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shuffleAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SHUFFLE");
                sendBroadcast(shuffleAction);
            }
        });
        skipPrevious = (ImageButton)findViewById(R.id.skipPreviousDetail);
        skipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent skipPreviousAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_PREVIOUS");
                sendBroadcast(skipPreviousAction);
            }
        });
        playPause = (ImageButton)findViewById(R.id.playPauseDetail);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayerService.localArrayList != null) {
                    if (mediaPlayerService.mediaPlayer.isPlaying()) {
                        Intent playPauseAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.MAIN_ACTION_PLAY");
                        sendBroadcast(playPauseAction);
                        playPause.setImageResource(R.drawable.play);
                    } else {
                        Intent playPauseAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.MAIN_ACTION_PLAY");
                        sendBroadcast(playPauseAction);
                        playPause.setImageResource(R.drawable.pause);
                    }
                }
            }
        });
        skipNext = (ImageButton)findViewById(R.id.skipNextDetail);
        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent skipNextAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_NEXT");
                sendBroadcast(skipNextAction);
            }
        });
        repeat = (ImageButton)findViewById(R.id.repeat);
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent repeatAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_REPEAT");
                sendBroadcast(repeatAction);
            }
        });
        seekBar = (SeekBar)findViewById(R.id.seekBarDetail);
    }
}
