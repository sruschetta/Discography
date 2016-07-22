package com.discography;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper{

    private static DBManager instance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Discography.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBSong.TABLE_NAME + " (" +
                    DBSong._ID + " INTEGER PRIMARY KEY," +
                    DBSong.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    DBSong.COLUMN_NAME_ALBUM + TEXT_TYPE + COMMA_SEP +
                    DBSong.COLUMN_NAME_COVER_URL + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DBSong.TABLE_NAME;


    public DBManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBManager getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new DBManager(context.getApplicationContext());
        }

        return instance;
    }

    public void fetchTracks(AsyncCallback callback)
    {
        new AsyncFetch(callback).execute();
    }

    public void saveTracks(AsyncCallback callback, JSONArray tracks)
    {
        new AsyncSave(callback).execute(tracks);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(SQL_DELETE_ENTRIES);
        onCreate(database);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public static abstract class DBSong implements BaseColumns {
        public static final String TABLE_NAME = "song";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_ALBUM = "album";
        public static final String COLUMN_NAME_COVER_URL = "cover_url";
    }

    private class AsyncSave extends AsyncTask<JSONArray, Integer, Integer>
    {
        private AsyncCallback callback;


        public AsyncSave (AsyncCallback callback)
        {
            this.callback = callback;
        }

        public int addTracksArrayToDb(JSONArray tracksArray)
        {
            SQLiteDatabase db = instance.getWritableDatabase();

            db.execSQL("delete from "+ DBSong.TABLE_NAME);

            try {

                for (int i = 0; i < tracksArray.length(); i++) {
                    JSONObject track = tracksArray.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put(DBSong.COLUMN_NAME_TITLE, track.getString("trackName"));
                    values.put(DBSong.COLUMN_NAME_ALBUM, track.getString("collectionName"));
                    values.put(DBSong.COLUMN_NAME_COVER_URL, track.getString("artworkUrl100"));

                    db.insert(DBSong.TABLE_NAME, null, values);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return 1;
            }
            finally {
                db.close();
            }
            return 0;
        }


        @Override
        protected Integer doInBackground(JSONArray... params) {

            return addTracksArrayToDb(params[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(integer == 0)
            {
                callback.onSuccess();
            }
            else
            {
                callback.onError();
            }
            super.onPostExecute(integer);
        }
    }


    private class AsyncFetch extends AsyncTask<Void, ArrayList<Track>, ArrayList<Track>>
    {
        private AsyncCallback callback;

        public AsyncFetch (AsyncCallback callback)
        {
            this.callback = callback;
        }

        public ArrayList<Track> fetchAllTracks()
        {
            SQLiteDatabase db = instance.getReadableDatabase();

            String[] projection = {
                    DBSong._ID,
                    DBSong.COLUMN_NAME_TITLE,
                    DBSong.COLUMN_NAME_ALBUM,
                    DBSong.COLUMN_NAME_COVER_URL
            };

            Cursor cursor = db.query(DBSong.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            ArrayList<Track> allTracks = new ArrayList<Track>();

            while (cursor.moveToNext()) {
                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBSong.COLUMN_NAME_TITLE)
                );

                String album = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBSong.COLUMN_NAME_ALBUM)
                );

                String coverUrl = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBSong.COLUMN_NAME_COVER_URL)
                );

                Track track = new Track(title, album, coverUrl);

                allTracks.add(track);
            }
            cursor.close();
            db.close();

            return allTracks;
        }

        @Override
        protected ArrayList<Track> doInBackground(Void... voids) {

            return fetchAllTracks();
        }

        @Override
        protected void onPostExecute(ArrayList<Track> trackList) {
            if(trackList != null)
            {
                callback.onSuccessWithData(trackList);
            }
            else
            {
                callback.onError();
            }
            super.onPostExecute(trackList);
        }
    }

    public interface AsyncCallback
    {
        public void onSuccess();
        public void onSuccessWithData(ArrayList<Track> trackList);
        public void onError();
    }
}
