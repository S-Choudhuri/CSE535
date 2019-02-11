package com.example.amine.learn2sign;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

import static com.example.amine.learn2sign.LoginActivity.INTENT_ID;
import static com.example.amine.learn2sign.LoginActivity.INTENT_SERVER_ADDRESS;

public class UploadActivity extends AppCompatActivity {

    @BindView(R.id.rv_videos)
    RecyclerView rv_videos;
    @BindView(R.id.tv_filename)
    TextView tv_filename;
    @BindView(R.id.pb_progress)
    ProgressBar progressBar;
    UploadListAdapter uploadListAdapter;
    UploadActivity uploadActivity;
    SharedPreferences sharedPreferences;
    static int upload_number = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.bind(this);
        uploadActivity = this;
        rv_videos.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        sharedPreferences =  this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        File m = new File(Environment.getExternalStorageDirectory().getPath() + "/Learn2Sign");
        if(m.exists()) {
            if(m.isDirectory()) {
                File[] videos =  m.listFiles();
                for(int i=0;i<videos.length;i++) {
                    Log.d("msg",videos[i].getPath());
                }
            }
        }
        uploadListAdapter = new UploadListAdapter(m.listFiles(), this.getApplicationContext());
        rv_videos.setAdapter(uploadListAdapter);


    }
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_upload, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        String id = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(INTENT_ID,"00000000");

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.menu_upload:
                //upload to Server now
                final File[] toUpload = uploadListAdapter.getVideos();
                final boolean[] checked = uploadListAdapter.getChecked();
                String server_ip = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE).getString(INTENT_SERVER_ADDRESS,"10.211.17.171");
                Log.d("msg",server_ip);
                for(int i=0;i<checked.length;i++) {
                    RequestParams params = new RequestParams();
                    if(checked[i]) {
                        params.put("checked",1);
                    } else {
                        params.put("checked",0);
                    }
                        try {
                            params.put("uploaded_file", toUpload[i]);
                            params.put("id",id);

                        } catch(FileNotFoundException e) {}


                        // send request
                        AsyncHttpClient client = new AsyncHttpClient();
                    final int finalI = i;
                    client.post("http://"+server_ip +"/upload_video.php", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                                // handle success response
                                Log.e("msg success",statusCode+"");
                                if(statusCode==200) {
                                    Toast.makeText(UploadActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                    toUpload[finalI].delete();
                                    uploadListAdapter.getVideos()[finalI] = null;
                                    uploadListAdapter.notifyDataSetChanged();

                                    if(checked[finalI]) //video accepted
                                        sharedPreferences.edit().putInt("Number_Accepted",1+sharedPreferences.getInt("Number_Accepted",0)).apply();

                                }
                                else {
                                    Toast.makeText(UploadActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                                // handle failure response
                                Log.e("msg fail",statusCode+"");

                                Toast.makeText(UploadActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

                            }
                            @Override
                            public void onProgress(long bytesWritten, long totalSize) {
                                tv_filename.setText(bytesWritten + " out of " + totalSize);

                                super.onProgress(bytesWritten, totalSize);
                            }


                            @Override
                            public void onStart() {
                                tv_filename.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.VISIBLE);
                                super.onStart();
                            }

                            @Override
                            public void onFinish() {
                                Log.e("msg on finish", upload_number+"");
                                upload_number = upload_number + 1;
                                if(upload_number == checked.length) {
                                    upload_log_file();
                                }
                                tv_filename.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                super.onFinish();
                            }
                        });

                        /*
                        UploadFile uploadFile = new UploadFile();
                        uploadFile.execute(uploadListAdapter.getVideos()[i].getPath());*/

                }


            default:
                return super.onOptionsItemSelected(item);
        }

    }
    public void upload_log_file() {
        upload_number = 0;
        Toast.makeText(this,"Upload to Server", Toast.LENGTH_LONG).show();
        String id = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(INTENT_ID,"00000000");

        String server_ip = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE).getString(INTENT_SERVER_ADDRESS,"10.211.17.171");

        File n = new File(getFilesDir().getPath());
        File f = new File(n.getParent()+"/shared_prefs/" + getPackageName() +".xml");
        AsyncHttpClient client_logs = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("uploaded_file",f);
            params.put("id",id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client_logs.post("http://"+server_ip+"/upload_log_file.php", params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode==200)
                    Toast.makeText(UploadActivity.this, "Done", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(UploadActivity.this, "Log File could not be uploaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(UploadActivity.this, "Log File could not be uploaded", Toast.LENGTH_SHORT).show();

            }
        });


    }
}

