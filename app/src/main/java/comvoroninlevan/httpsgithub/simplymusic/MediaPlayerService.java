package comvoroninlevan.httpsgithub.simplymusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Levan on 13.12.2016.
 */

public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static volatile MediaPlayerService instance = null;

    public MediaPlayerService() {
    }

    public static MediaPlayerService getInstance() {
        if (instance == null) {
            synchronized (MediaPlayerService.class) {
                if (instance == null) {
                    instance = new MediaPlayerService();
                }
            }
        }
        return instance;
    }

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    public MediaPlayer mediaPlayer;
    private int songPosition;
    private Uri playlistUri;
    private SongArrayList list = SongArrayList.getInstance();
    private AudioManager audioManager;
    // TODO HANDLE isShuffle, isRepeat
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    public ArrayList<Long> localArrayList;
    private boolean check = false;

    private String currentTitle;
    private String currentArtist;
    private Bitmap albumArt;
    private String currentArt;

    private RemoteViews notificationView;
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    private int NOTIFICATION_ID = 1;
    private int FOREGROUND_NOTIFICATION = 100;

    private final IBinder iBinder = new MusicBinder();

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int changeFocus) {
            if (changeFocus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    changeFocus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mediaPlayer.pause();
            } else if (changeFocus == AudioManager.AUDIOFOCUS_GAIN) {
                mediaPlayer.start();
            } else if (changeFocus == AudioManager.AUDIOFOCUS_LOSS) {
                pausePlayer();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        builder = new Notification.Builder(this);
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.ACTION_PLAY");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.MAIN_ACTION_PLAY");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_NEXT");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_PREVIOUS");
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.ACTION_CANCEL_NOTIFICATION");
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startForeground(FOREGROUND_NOTIFICATION, builder.build());
        if (mediaPlayer.isPlaying()) {
            Intent setMax = new Intent("comvoroninlevan.httpsgithub.simplymusic.ON_START_COMMAND");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(setMax);
        }
        return Service.START_STICKY;
    }

    public void setList() {

        localArrayList = new ArrayList<>(list.longList);
    }

    public class MusicBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    public void playSong() {

        playlistUri = null;
        check = true;

        mediaPlayer.reset();
        long id = localArrayList.get(songPosition);
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
            setTitleArtist(id);
            albumArt = getAlbumId(getApplicationContext(), id);
            setTitleArtistAlbumArtMainActivity(getApplicationContext());
        }
    }

    public void playSongFromPlaylist(Uri currentPlaylist) {

        check = true;

        mediaPlayer.reset();
        long id = localArrayList.get(songPosition);
        playlistUri = currentPlaylist;
        Uri playlist = Uri.withAppendedPath(playlistUri, "members");
        Uri uri = ContentUris.withAppendedId(playlist, id);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
        setTitleArtistPlaylist(id);
        albumArt = null;
        setTitleArtistAlbumArtMainActivity(getApplicationContext());
    }

    public void setSong(int songPos) {
        songPosition = songPos;
    }

    private void pausePlayer() {

        if (mediaPlayer != null) {
            mediaPlayer.pause();
            pauseIntent(getApplicationContext());
            pauseForNotification();
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    //______________________________________________________________________________________________
    //_________________BROADCAST_INTENT_TO_SET_ARTIST_TITLE_ALBUM_ART_IN_MAIN_ACTIVITY______________

    public void setTitleArtistAlbumArtMainActivity(Context context){

        Intent sendDataToMainActivity = new Intent("comvoroninlevan.httpsgithub.simplymusic.SONG_INFO");
        sendDataToMainActivity.putExtra("Title", currentTitle);
        sendDataToMainActivity.putExtra("Artist", currentArtist);
        if(playlistUri == null) {
            sendDataToMainActivity.putExtra("AlbumArt", currentArt);
        } else {
            currentArt = null;
            sendDataToMainActivity.putExtra("AlbumArt", currentArt);
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(sendDataToMainActivity);
    }

    //______________________________________________________________________________________________
    //____________________________METHODS_TO_FIND_AND_SET_TITLE_ARTIST_ALBUM_ART____________________

    private void setTitleArtist(long id) {

        String selection = MediaStore.Audio.Media._ID + " =?";
        String currentId = String.valueOf(id);
        String[] selectionArgs = {currentId};
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null);

        if (cursor.moveToFirst()) {

            currentTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            currentArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        }
        cursor.close();
    }

    private void setTitleArtistPlaylist(long id) {

        Uri playlist = Uri.withAppendedPath(playlistUri, "members");
        Uri uri = ContentUris.withAppendedId(playlist, id);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.moveToFirst()) {

            currentTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
            currentArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
        }
        cursor.close();

    }

    private Bitmap getAlbumId(Context context, long id) {

        Bitmap albumArt = null;
        String selection = MediaStore.Audio.Media._ID + " = " + id + "";
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                        MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID},
                selection, null, null);

        if (cursor.moveToFirst()) {
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            albumArt = getAlbumArt(context, albumId);
        }
        cursor.close();
        return albumArt;
    }

    private Bitmap getAlbumArt(Context context, long albumId) {

        Bitmap albumArt = null;
        String selection = MediaStore.Audio.Albums._ID + " = " + albumId + "";
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, selection, null, null);

        if (cursor.moveToFirst()) {

            int art = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

            currentArt = cursor.getString(art);
            albumArt = BitmapFactory.decodeFile(currentArt);
        }
        cursor.close();
        return albumArt;
    }

    //____________________________Playback Controls_________________________________________________

    public void playIntent(Context context) {
        Intent playIntent = new Intent("comvoroninlevan.httpsgithub.simplymusic.BUTTON_ACTION_PLAY");
        LocalBroadcastManager.getInstance(context).sendBroadcast(playIntent);
    }

    public void pauseIntent(Context context) {
        Intent pauseIntent = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_PAUSE");
        LocalBroadcastManager.getInstance(context).sendBroadcast(pauseIntent);
    }

    public void pauseForNotification(){
        if (notificationView != null) {
            if (currentApiVersion < 24) {
                notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                builder.setContent(notificationView);
                //notificationManager.notify(NOTIFICATION_ID, builder.build());
                startForeground(FOREGROUND_NOTIFICATION, builder.build());
            } else {
                notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                builder.setCustomContentView(notificationView);
                //notificationManager.notify(NOTIFICATION_ID, builder.build());
                startForeground(FOREGROUND_NOTIFICATION, builder.build());
            }
        }
    }

    //______________________________________________________________________________________________
    //_____________________________________SEEK_BAR_HANDLING________________________________________

    public double getDuration() {
        return mediaPlayer.getDuration();
    }

    public double getCurrentTime() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public boolean checkPlayer() {
        return check;
    }

    public static void setMaxDuration(Context context, int totalTime) {
        Intent intent = new Intent("comvoroninlevan.httpsgithub.simplymusic.SET_MAX_DURATION");
        intent.putExtra("TOTAL_TIME", totalTime);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //______________________________________________________________________________________________
    //_____________________________________NOTIFICATION_____________________________________________
    private void setNotification() {

        notificationView = new RemoteViews(getPackageName(), R.layout.notification_controller);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Intents for broadcastReceiver
        Intent playIntent = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_PLAY");
        Intent skipNextIntent = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_NEXT");
        Intent skipPreviousIntent = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_PREVIOUS");
        Intent cancelNotification = new Intent("comvoroninlevan.httpsgithub.simplymusic.ACTION_CANCEL_NOTIFICATION");
        PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(this, 100, playIntent, 0);
        PendingIntent pendingSkipNextIntent = PendingIntent.getBroadcast(this, 100, skipNextIntent, 0);
        PendingIntent pendingSkipPreviousIntent = PendingIntent.getBroadcast(this, 100, skipPreviousIntent, 0);
        PendingIntent pendingCancelNotification = PendingIntent.getBroadcast(this, 100, cancelNotification, 0);

        notificationView.setOnClickPendingIntent(R.id.skipPreviousNotification, pendingSkipPreviousIntent);
        notificationView.setOnClickPendingIntent(R.id.playPauseNotification, pendingPlayIntent);
        notificationView.setOnClickPendingIntent(R.id.skipNextNotification, pendingSkipNextIntent);
        notificationView.setOnClickPendingIntent(R.id.cancelNotification, pendingCancelNotification);
        notificationView.setTextViewText(R.id.titleNotification, currentTitle);
        notificationView.setTextViewText(R.id.artistNotification, currentArtist);
        if (albumArt != null) {
            notificationView.setImageViewBitmap(R.id.albumArtNotification, albumArt);
        } else {
            notificationView.setImageViewResource(R.id.albumArtNotification, R.drawable.placeholder);
        }


        if (currentApiVersion < 24) {
            builder.setContentIntent(pendingNotificationIntent)
                    .setContent(notificationView)
                    .setSmallIcon(R.drawable.play)
                    .setOngoing(true);
        } else {
            builder.setContentIntent(pendingNotificationIntent)
                    .setCustomContentView(notificationView)
                    .setSmallIcon(R.drawable.play)
                    .setOngoing(true);
        }
        startForeground(FOREGROUND_NOTIFICATION, builder.build());
    }

    private void cancelNotification() {
        stopForeground(true);
    }

    //______________________________________________________________________________________________
    //_______________________________________BROADCAST_RECEIVER_____________________________________
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.ACTION_PLAY")) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    if (currentApiVersion < 24) {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                        builder.setContent(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    } else {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                        builder.setCustomContentView(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    }
                    pauseIntent(getApplicationContext());
                } else {
                    int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mediaPlayer.start();
                        if (currentApiVersion < 24) {
                            notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                            builder.setContent(notificationView);
                            startForeground(FOREGROUND_NOTIFICATION, builder.build());
                        } else {
                            notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                            builder.setCustomContentView(notificationView);
                            startForeground(FOREGROUND_NOTIFICATION, builder.build());
                        }
                        playIntent(getApplicationContext());
                    }
                }
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_NEXT")) {
                if (localArrayList != null) {
                    if (currentApiVersion < 24) {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                        builder.setContent(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    } else {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                        builder.setCustomContentView(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    }

                    songPosition++;
                    if (songPosition == localArrayList.size() - 1) {
                        songPosition = 0;
                    }
                    if (playlistUri == null) {
                        playSong();
                    } else {
                        playSongFromPlaylist(playlistUri);
                    }
                }
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.ACTION_SKIP_PREVIOUS")) {
                if (localArrayList != null) {
                    if (currentApiVersion < 24) {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                        builder.setContent(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    } else {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                        builder.setCustomContentView(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    }

                    songPosition--;
                    if (songPosition < 0) {
                        songPosition = localArrayList.size() - 2;
                    }
                    if (playlistUri == null) {
                        playSong();
                    } else {
                        playSongFromPlaylist(playlistUri);
                    }
                }
            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.ACTION_CANCEL_NOTIFICATION")) {

                check = false;
                mediaPlayer.stop();
                cancelNotification();
                pauseIntent(getApplicationContext());

            } else if (action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.MAIN_ACTION_PLAY")) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    if (notificationView != null) {
                        if (currentApiVersion < 24) {
                            notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                            builder.setContent(notificationView);
                            startForeground(FOREGROUND_NOTIFICATION, builder.build());
                        } else {
                            notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                            builder.setCustomContentView(notificationView);
                            startForeground(FOREGROUND_NOTIFICATION, builder.build());
                        }
                    }
                } else {
                    int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mediaPlayer.start();
                        if (notificationView != null) {
                            if (currentApiVersion < 24) {
                                notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                                builder.setContent(notificationView);
                                startForeground(FOREGROUND_NOTIFICATION, builder.build());
                            } else {
                                notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.pause);
                                builder.setCustomContentView(notificationView);
                                startForeground(FOREGROUND_NOTIFICATION, builder.build());
                            }
                        }
                    }
                }
            } else if (action.equalsIgnoreCase(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                mediaPlayer.pause();
                if (notificationView != null) {
                    if (currentApiVersion < 24) {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                        builder.setContent(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    } else {
                        notificationView.setImageViewResource(R.id.playPauseNotification, R.drawable.play);
                        builder.setCustomContentView(notificationView);
                        startForeground(FOREGROUND_NOTIFICATION, builder.build());
                    }
                }
                pauseIntent(getApplicationContext());
            }
        }
    };



    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //mediaPlayer.stop();
        //mediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        if (isShuffle) {
            Random random = new Random();
            songPosition = random.nextInt(localArrayList.size());
        } else if (isRepeat) {
            songPosition += 0;
        } else {
            songPosition++;
        }

        if (songPosition == localArrayList.size() - 1) {
            songPosition = 0;
        }

        if (playlistUri != null) {
            playSongFromPlaylist(playlistUri);
        } else {
            playSong();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        int totalTime = ((int) getDuration());
        setMaxDuration(getApplicationContext(), totalTime);
        playIntent(getApplicationContext());
        setNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        cancelNotification();
    }
}
