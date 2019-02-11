package com.example.amine.learn2sign;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder>
{
    private File[] videos;
    Context context;
    private boolean[] checked;
    public UploadListAdapter(File[] videos, Context context){
        this.videos = videos;
        this.context = context;
        checked = new boolean[videos.length];
    }




    @NonNull
    @Override
    public UploadListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.row_layout, viewGroup, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull UploadListAdapter.ViewHolder viewHolder, int i) {

        if(videos[i]!=null) {

            viewHolder.position = i;
            Uri uri = Uri.parse(videos[i].getPath());
            viewHolder.vv_video.setVideoURI(uri);
            if(!viewHolder.vv_video.isPlaying())
                viewHolder.vv_video.start();

            String filename=videos[i].getPath().substring(videos[i].getPath().lastIndexOf("/")+1);
            viewHolder.tv_title.setText(filename);
            Log.e("msg",viewHolder.ischecked+"");
            if(checked[i]) {
                checked[i] = true;
                viewHolder.cb_check.setChecked(true);

            }
            else {
                checked[i] = false;
                viewHolder.cb_check.setChecked(false);

            }

        } else {
            viewHolder.vv_video.setVisibility(View.GONE);
            viewHolder.cb_check.setVisibility(View.GONE);
            viewHolder.tv_title.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return videos.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        CheckBox cb_check;
        boolean ischecked;
        int position;
        public VideoView vv_video;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vv_video = (VideoView) itemView.findViewById(R.id.vv_video);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            cb_check = (CheckBox) itemView.findViewById(R.id.cb_check);
            cb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    ischecked= b;

                    checked[position] = ischecked;

                }
            });
            vv_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                   if(videos[position]==null)
                   {
                       vv_video.setVisibility(View.GONE);
                       cb_check.setVisibility(View.GONE);
                       tv_title.setVisibility(View.GONE);
                   } else {

                       vv_video.start();
                   }
                }
            });
            vv_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vv_video.start();
                }
            });
        }
    }

    public boolean[] getChecked() {
        return checked;
    }

    public File[] getVideos() {
        return videos;
    }

}
