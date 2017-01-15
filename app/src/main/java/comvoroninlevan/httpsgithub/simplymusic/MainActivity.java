package comvoroninlevan.httpsgithub.simplymusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.support.design.widget.TabLayout;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private boolean isShuffle;
    private boolean isRepeat;
    private int SongPosition;
    private double currentTime;
    private double totalTime;
    private boolean isPlay = false;

    SeekBar seekBar;
    ImageButton playPause;
    IntentFilter filter;
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
                Intent playPauseAction = new Intent("comvoroninlevan.httpsgithub.simplymusic.MAIN_ACTION_PLAY");
                sendBroadcast(playPauseAction);
                if (mediaPlayerService.mediaPlayer.isPlaying()) {
                    playPause.setImageResource(R.drawable.play);
                } else {
                    playPause.setImageResource(R.drawable.pause);
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
}
