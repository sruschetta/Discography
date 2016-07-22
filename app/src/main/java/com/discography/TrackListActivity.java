package com.discography;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;

import java.util.ArrayList;

public class TrackListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private int numberOfSongs = 50;
    private final String defaultArtist = "Daft punk";
    private final String prefs_name = "preferences";

    private SharedPreferences preferences;
    private final static int requestId = 101;
    public final static String intentParam = "currentArtist";
    private View loaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);

        preferences = getSharedPreferences(prefs_name, Context.MODE_PRIVATE);

        loaderView = findViewById(R.id.loader_view);

        //Recycler View setting up

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (preferences.getBoolean("firstLaunch", true)) {

            preferences.edit().putString("currentArtist", defaultArtist).commit();

            fetchSongs();

            preferences.edit().putBoolean("firstLaunch", false).commit();
        }
        else {
            populateRecyclerView();
        }

        updateTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(intentParam, preferences.getString("currentArtist", ""));
                startActivityForResult(intent, requestId);
                return true;

            case R.id.action_refresh:
                fetchSongs();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == requestId && resultCode == Activity.RESULT_OK)
        {
            preferences.edit().putString("currentArtist", data.getExtras().getString(intentParam)).commit();

            updateTitle();
            fetchSongs();
        }
    }

    public void updateTitle() {
        String artist = preferences.getString("currentArtist", null);
        setTitle(artist.substring(0, 1).toUpperCase() + artist.substring(1));
    }

    public void populateRecyclerView() {

        DBManager.getInstance(getApplicationContext()).fetchTracks(new DBManager.AsyncCallback() {
                                                                       @Override
                                                                       public void onSuccess() {
                                                                           loaderView.setVisibility(View.GONE);
                                                                       }

                                                                       @Override
                                                                       public void onSuccessWithData(ArrayList<Track> trackList) {
                                                                           ImageLoader imageLoader = APIManager.getInstance(getApplicationContext()).getImageLoader();

                                                                           TracksRecyclerAdapter adapter = new TracksRecyclerAdapter(trackList, imageLoader);
                                                                           recyclerView.swapAdapter(adapter, true);
                                                                           loaderView.setVisibility(View.GONE);
                                                                       }

                                                                       @Override
                                                                       public void onError() {
                                                                           Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                                                                           loaderView.setVisibility(View.GONE);
                                                                       }
                                                                   }
        );
    }

    public void fetchSongs() {

        String artist = preferences.getString("currentArtist", null);

        loaderView.setVisibility(View.VISIBLE);

        APIManager.getInstance(this).fetchSongsForArtist(artist, numberOfSongs, new APIListener() {
            @Override
            public void apiSuccess(JSONArray tracks) {

                if(tracks.length() > 0) {

                    DBManager dbManager = DBManager.getInstance(getApplicationContext());

                    dbManager.saveTracks(new DBManager.AsyncCallback() {
                        @Override
                        public void onSuccess() {
                            populateRecyclerView();
                        }

                        @Override
                        public void onSuccessWithData(ArrayList<Track> trackList) {
                            loaderView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                            loaderView.setVisibility(View.GONE);
                        }
                    }, tracks);

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No results to show", Toast.LENGTH_SHORT).show();
                }
                loaderView.setVisibility(View.GONE);
            }
        });
    }
}
