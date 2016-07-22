package com.discography;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class APIManager {

    /*--- Attributes ---*/
    private static APIManager instance;
    private static Context context;

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private String apiUrl = "https://itunes.apple.com/search?term=";
    private String params = "&entity=song&limit=";


    /*--- Constructors ---*/

    public APIManager(Context context)
    {
        this.context = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache(){
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

    }

    /*--- Functions ---*/

    public static synchronized APIManager getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new APIManager(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue()
    {
        if(requestQueue == null)
        {
            requestQueue = new Volley().newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public ImageLoader getImageLoader()
    {
        return imageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    public void fetchSongsForArtist(String artistName, int numberOfSongs, final APIListener listener){

        try {
            String requestURL = (apiUrl + URLEncoder.encode(artistName, "UTF-8") + params + numberOfSongs);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, requestURL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        listener.apiSuccess(response.getJSONArray("results"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Unexpected error", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Unexpected error", Toast.LENGTH_SHORT).show();
                }
            });

            requestQueue.add(jsonObjectRequest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
