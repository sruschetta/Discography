package com.discography;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String artist = getIntent().getExtras().getString(TrackListActivity.intentParam);

        editText = (EditText) findViewById(R.id.settings_artist_edit_text);
        editText.setText(artist);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(TrackListActivity.intentParam, editText.getText().toString());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
