package com.discography;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TracksRecyclerAdapter.TrackViewHolder>{

    private ArrayList<Track> tracks;
    private ImageLoader imageLoader;

    public static class TrackViewHolder extends RecyclerView.ViewHolder
    {
        public TextView titleView, albumView;
        public NetworkImageView coverView;

        public TrackViewHolder(View view)
        {
            super(view);

            titleView = (TextView) view.findViewById(R.id.song_title);
            albumView = (TextView) view.findViewById(R.id.song_album);

            coverView = (NetworkImageView) view.findViewById(R.id.cover_view);
        }
    }

    public TracksRecyclerAdapter(ArrayList<Track> tracks, ImageLoader imageLoader)
    {
        this.tracks = tracks;
        this.imageLoader = imageLoader;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_layout, parent, false);

        TrackViewHolder trackViewHolder = new TrackViewHolder(view);
        return trackViewHolder;
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.titleView.setText(tracks.get(position).getTitle());
        holder.albumView.setText(tracks.get(position).getAlbum());
        holder.coverView.setImageUrl(tracks.get(position).getCoverURL(), imageLoader);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }
}
