package comvoroninlevan.httpsgithub.simplymusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.support.design.widget.TabLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean isShuffle;
    private boolean isRepeat;
    private int SongPosition;
    private double currentTime;
    private double totalTime;
    private boolean isPlay = false;

    private SeekBar seekBar;
    private ImageButton playPause;
    private ImageView imagePlayer;
    private TextView title;
    private TextView artist;
    private IntentFilter filter;
    private Handler handler = new Handler();

    private MediaPlayerService mediaPlayerService = MediaPlayerService.getInstance();
    private Intent intent;
    private boolean musicBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        imagePlayer = (ImageView)findViewById(R.id.imagePlayer);
        imagePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toDetailSong = new Intent(MainActivity.this, DetailSong.class);
                startActivity(toDetailSong);
            }
        });
        title = (TextView)findViewById(R.id.title);
        artist = (TextView)findViewById(R.id.artist);

        ImageButton skipPrevious = (ImageButton) findViewById(R.id.skipPrevious);
        skipPrevious.setImageResource(R.drawable.skip_previous);
        skipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent skipPreviousAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_PREVIOUS");
                sendBroadcast(skipPreviousAction);
            }
        });
        playPause = (ImageButton) findViewById(R.id.playPause);
        playPause.setImageResource(R.drawable.play);
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
        ImageButton skipNext = (ImageButton) findViewById(R.id.skipNext);
        skipNext.setImageResource(R.drawable.skip_next);
        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent skipNextAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_NEXT");
                sendBroadcast(skipNextAction);
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean userInput) {
                if (userInput) mediaPlayerService.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        filter = new IntentFilter();
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.BUTTON_ACTION_PLAY");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.ACTION_PAUSE");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.SET_MAX_DURATION");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.ON_START_COMMAND");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.SONG_INFO");
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.SET_MAX_DURATION")) {
                int totalTime = intent.getIntExtra("TOTAL_TIME", 0);
                seekBar.setMax(totalTime);
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.BUTTON_ACTION_PLAY")) {
                if (playPause != null) {
                    playPause.setImageResource(R.drawable.pause);
                }
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.ON_START_COMMAND")) {
                setMax();
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.ACTION_PAUSE")) {
                if (playPause != null) {
                    playPause.setImageResource(R.drawable.play);
                }
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.SONG_INFO")){
                String songTitle = intent.getStringExtra("Title");
                String songArtist = intent.getStringExtra("Artist");
                String songArt = intent.getStringExtra("AlbumArt");
                title.setText(songTitle);
                artist.setText(songArtist);
                Bitmap art = BitmapFactory.decodeFile(songArt);
                if(art != null){
                    imagePlayer.setImageBitmap(art);
                } else {
                    imagePlayer.setImageResource(R.drawable.placeholder);
                }
            }
        }
    };

    private Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayerService.checkPlayer()) {
                currentTime = mediaPlayerService.getCurrentTime();
                seekBar.setProgress((int) currentTime);
            }

            handler.postDelayed(this, 100);
        }
    };

    private void setMax() {
        totalTime = mediaPlayerService.getDuration();
        seekBar.setMax((int) totalTime);
        playPause.setImageResource(R.drawable.pause);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MusicBinder musicBinder = (MediaPlayerService.MusicBinder) iBinder;
            mediaPlayerService = musicBinder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
        if (mediaPlayerService.checkPlayer()) {
            mediaPlayerService.setTitleArtistAlbumArtMainActivity(getApplicationContext());
            totalTime = mediaPlayerService.getDuration();
            seekBar.setMax((int) totalTime);
            playPause.setImageResource(R.drawable.pause);
        }
        handler.postDelayed(timeUpdater, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        handler.removeCallbacks(timeUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent permissionGranted = new Intent("comvoroninlevan.httpsgithub.simplymusic.PERMISSION_GRANTED");
                    sendBroadcast(permissionGranted);

                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
