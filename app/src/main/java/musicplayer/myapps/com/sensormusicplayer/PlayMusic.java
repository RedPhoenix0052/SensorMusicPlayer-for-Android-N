package musicplayer.myapps.com.sensormusicplayer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PlayMusic extends Service implements SensorEventListener {
    static MediaPlayer mediaPlayer;

    static int maxDuration;
    int currentDuration;

    static Thread progressSeekBar;

    SensorManager sensorManager;
    Sensor mySensor1;
    Sensor mySensor2;
    boolean isWait = false;
    int count = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        mySensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mySensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mySensor1, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mySensor2, sensorManager.SENSOR_DELAY_NORMAL);

        mediaPlayer = MediaPlayer.create(this, MainActivity.currentPath);
        maxDuration = 0;

        progressSeekBar = new Thread() {
            @Override
            public void run() {
                currentDuration = 0;
                while (currentDuration <= maxDuration) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentDuration = mediaPlayer.getCurrentPosition();
                    if (PlayerActivity.seekBar != null)
                        PlayerActivity.seekBar.setProgress(currentDuration);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        sensorManager.unregisterListener(this);
    }

    void start() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(PlayMusic.this, MainActivity.currentPath);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(PlayMusic.this, MainActivity.currentPath);
            mediaPlayer.start();
        }
        maxDuration = mediaPlayer.getDuration();

        if (PlayerActivity.seekBar != null) {
            PlayerActivity.seekBar.setMax(mediaPlayer.getDuration());
            PlayerActivity.seekBar.setProgress(0);
        }
        stopWhenFinished();
    }

    void stopWhenFinished() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (MainActivity.index >= MainActivity.songPath.size() - 1) {
                    MainActivity.index = 0;
                } else {
                    MainActivity.index++;
                }

                MainActivity.currentPath = MainActivity.songPath.get(MainActivity.index);
                MainActivity.textView.setText(MainActivity.songList.get(MainActivity.index));

                if (PlayerActivity.seekBar == null) {
                    start();
                } else {
                    maxDuration = mediaPlayer.getDuration();
                    PlayerActivity.seekBar.setProgress(maxDuration);
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float[] x = sensorEvent.values;

            if (x[0] > 1.0) {
                if (isWait) {
                    isWait = false;
                } else {

                    if (!PlayMusic.mediaPlayer.isPlaying()) {
                        PlayMusic.mediaPlayer.start();
                        isWait = true;
                        if (PlayerActivity.toggleButton != null) {
                            PlayerActivity.toggleButton.setChecked(false);
                        }
                        MainActivity.minibutton.setBackgroundResource(R.drawable.img2);
                        MainActivity.playing = true;
                    } else {
                        PlayMusic.mediaPlayer.pause();
                        isWait = true;
                        if (PlayerActivity.toggleButton != null) {
                            PlayerActivity.toggleButton.setChecked(true);
                        }
                        MainActivity.minibutton.setBackgroundResource(R.drawable.img3);
                        MainActivity.playing = false;
                    }
                }
            }
        }
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] v = sensorEvent.values;

            if (mediaPlayer != null) {
                if (v[0] <= -9.0) {
                    if (count == 1) {
                        if (MainActivity.index >= MainActivity.songPath.size() - 1) {
                            MainActivity.index = 0;
                        } else {
                            MainActivity.index++;
                        }

                        MainActivity.currentPath = MainActivity.songPath.get(MainActivity.index);
                        MainActivity.textView.setText(MainActivity.songList.get(MainActivity.index));
                        start();
                        PlayerActivity.textView3.setText(MainActivity.songList.get(MainActivity.index));
                        setImage();
                        progressSeekBar.start();
                        PlayerActivity.toggleButton.setChecked(false);
                        MainActivity.minibutton.setBackgroundResource(R.drawable.img2);
                        count = 0;
                    }
                } else if (v[0] >= 9.0) {
                    if (count == 1) {
                        if (MainActivity.index <= 0) {
                            MainActivity.index = MainActivity.songPath.size() - 1;
                        } else {
                            MainActivity.index--;
                        }

                        MainActivity.currentPath = MainActivity.songPath.get(MainActivity.index);
                        MainActivity.textView.setText(MainActivity.songList.get(MainActivity.index));
                        start();
                        PlayerActivity.textView3.setText(MainActivity.songList.get(MainActivity.index));
                        setImage();
                        progressSeekBar.start();
                        PlayerActivity.toggleButton.setChecked(false);
                        MainActivity.minibutton.setBackgroundResource(R.drawable.img2);
                        count = 0;
                    }
                } else if (v[0] <= (2.0) && v[0] >= (-2.0)) {
                    count = 1;
                }
            }
        }
    }

    public void setImage() {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, MainActivity.currentPath);

        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            PlayerActivity.imageView.setImageBitmap(bitmap);
        } else {
            PlayerActivity.imageView.setImageResource(R.drawable.img);
        }
        mmr.release();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
