package musicplayer.myapps.com.sensormusicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    LinearLayout musicPanel;
    static TextView textView;
    ListView playListView;
    static Button minibutton;

    public static final int req_code = 101;

    static List<String> songList = new ArrayList();
    static List<Uri> songPath = new ArrayList();

    ArrayAdapter<String> songAdapter;

    static Uri currentPath;
    static int index = 0;
    static boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playListView = findViewById(R.id.playlist);
        musicPanel = findViewById(R.id.musicPanel);
        textView = findViewById(R.id.textView);
        minibutton = findViewById(R.id.minibutton);

        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, req_code);
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, req_code);
        } else {
            createMusicList();
            playerButtonPanel();
            textView.setText(songList.get(index));
            minibuttonFunction();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songAdapter.clear();
    }

    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();

        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String colSongName = MediaStore.Audio.Media.TITLE;
        String colSongArtist = MediaStore.Audio.Media.ARTIST;
        String colSongLocation = MediaStore.Audio.Media.DATA;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, colSongName + "\tASC");

        if (songCursor != null) {
            int songTitle = songCursor.getColumnIndex(colSongName);
            int songArtist = songCursor.getColumnIndex(colSongArtist);
            int songLocation = songCursor.getColumnIndex(colSongLocation);

            while (songCursor.moveToNext()) {
                String title = songCursor.getString(songTitle);
                String artist = songCursor.getString(songArtist);
                Uri path = Uri.parse(songCursor.getString(songLocation));

                songList.add(title + "\n" + artist);
                songPath.add(path);
            }
        }
    }

    void createMusicList() {
        getMusic();
        songAdapter = new ArrayAdapter<String>(this, R.layout.listtextview, R.id.textView2, songList);
        playListView.setAdapter(songAdapter);

        playListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentPath = songPath.get(i);

                showPlayerActivity();
                playMusicService();

                musicPanel.setVisibility(View.VISIBLE);
                textView.setText(songList.get(i));
                minibutton.setBackgroundResource(R.drawable.img2);
                playing = true;
                index = i;
            }
        });
    }

    void playerButtonPanel() {
        if (PlayMusic.mediaPlayer != null) {
            musicPanel.setVisibility(View.VISIBLE);
        } else {
            musicPanel.setVisibility(View.GONE);
        }

        musicPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlayerActivity();
            }
        });
    }

    void minibuttonFunction() {
        if (playing) {
            minibutton.setBackgroundResource(R.drawable.img2);
        } else {
            minibutton.setBackgroundResource(R.drawable.img3);
        }

        minibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayMusic.mediaPlayer.isPlaying()) {
                    PlayMusic.mediaPlayer.pause();
                    playing = false;
                    minibutton.setBackgroundResource(R.drawable.img3);
                } else {
                    PlayMusic.mediaPlayer.start();
                    playing = true;
                    minibutton.setBackgroundResource(R.drawable.img2);
                }
            }
        });
    }

    void playMusicService() {
        Intent intent = new Intent(MainActivity.this, PlayMusic.class);
        startService(intent);
    }

    void showPlayerActivity() {
        Intent player = new Intent(MainActivity.this, PlayerActivity.class);
        startActivity(player);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == req_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    createMusicList();
                    playerButtonPanel();
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}