package musicplayer.myapps.com.sensormusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    static ToggleButton toggleButton;
    Button bt_next, bt_prev, bt_ff, bt_rew;
    static SeekBar seekBar;
    static ImageView imageView;
    static TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        textView3 = findViewById(R.id.textView3);
        imageView = findViewById(R.id.imageView);
        toggleButton = findViewById(R.id.toggleButton);
        seekBar = findViewById(R.id.seekBar);
        bt_next = findViewById(R.id.button2);
        bt_prev = findViewById(R.id.button);
        bt_ff = findViewById(R.id.button4);
        bt_rew = findViewById(R.id.button3);

        textView3.setText(MainActivity.songList.get(MainActivity.index));

        setImage();

        toggleButtonFunction();

        bt_next.setOnClickListener(this);
        bt_prev.setOnClickListener(this);
        bt_rew.setOnClickListener(this);
        bt_ff.setOnClickListener(this);

        seekBar.setMax(PlayMusic.maxDuration);
        PlayMusic.progressSeekBar.start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seek, int progress, boolean b) {
                if (seekBar.getProgress() == PlayMusic.mediaPlayer.getDuration()) {
                    Intent i = new Intent(PlayerActivity.this, PlayMusic.class);
                    textView3.setText(MainActivity.songList.get(MainActivity.index));
                    setImage();
                    startService(i);

                    PlayMusic.progressSeekBar.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlayMusic.mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        seekBar = null;
        finish();
    }

    void toggleButtonFunction() {
        if (PlayMusic.mediaPlayer != null) {
            if (PlayMusic.mediaPlayer.isPlaying()) {
                toggleButton.setChecked(false);
            } else
                toggleButton.setChecked(true);
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (PlayMusic.mediaPlayer.isPlaying()) {
                        PlayMusic.mediaPlayer.pause();
                        MainActivity.minibutton.setBackgroundResource(R.drawable.img3);
                        MainActivity.playing = false;
                    }
                } else {
                    if (!PlayMusic.mediaPlayer.isPlaying()) {
                        PlayMusic.mediaPlayer.start();
                        MainActivity.minibutton.setBackgroundResource(R.drawable.img2);
                        MainActivity.playing = true;
                    }
                }
            }
        });
    }

    public void setImage() {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, MainActivity.currentPath);
        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.img);
        }
        mmr.release();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button4:
                PlayMusic.mediaPlayer.seekTo(PlayMusic.mediaPlayer.getCurrentPosition() + 10000);
                break;

            case R.id.button3:
                PlayMusic.mediaPlayer.seekTo(PlayMusic.mediaPlayer.getCurrentPosition() - 5000);
                break;

            case R.id.button2:
                if (MainActivity.index >= MainActivity.songPath.size() - 1) {
                    MainActivity.index = 0;
                } else {
                    MainActivity.index++;
                }

                MainActivity.currentPath = MainActivity.songPath.get(MainActivity.index);
                MainActivity.textView.setText(MainActivity.songList.get(MainActivity.index));
                Intent next = new Intent(this, PlayMusic.class);
                textView3.setText(MainActivity.songList.get(MainActivity.index));
                setImage();
                startService(next);
                PlayMusic.progressSeekBar.start();
                toggleButton.setChecked(false);
                break;

            case R.id.button:
                if (MainActivity.index <= 0) {
                    MainActivity.index = MainActivity.songPath.size() - 1;
                } else {
                    MainActivity.index--;
                }

                MainActivity.currentPath = MainActivity.songPath.get(MainActivity.index);
                MainActivity.textView.setText(MainActivity.songList.get(MainActivity.index));
                Intent prev = new Intent(this, PlayMusic.class);
                textView3.setText(MainActivity.songList.get(MainActivity.index));
                setImage();
                startService(prev);
                PlayMusic.progressSeekBar.start();
                toggleButton.setChecked(false);
                break;
        }
    }
}