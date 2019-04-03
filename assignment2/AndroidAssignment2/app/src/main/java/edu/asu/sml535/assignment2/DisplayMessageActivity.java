package edu.asu.sml535.assignment2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import edu.asu.sml535.assignment2.MainActivity;
import edu.asu.sml535.assignment2.classifiers.DecisionTreeClassifier;
import edu.asu.sml535.assignment2.classifiers.SVC;
import edu.asu.sml535.assignment2.features.CSVFile;
import edu.asu.sml535.assignment2.R;
import edu.asu.sml535.assignment2.features.FeatureExtractor;

public class DisplayMessageActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        TextView textView = findViewById(R.id.textView1);
        textView.setText(message);
        this.message = message;
    }


    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(View view) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("1", "Uri: " + uri.toString());
                try {
                    processCSVFile(uri);
                } catch (IOException e) {
                    Log.i("1",e.getMessage());
                    new AlertDialog.Builder(getBaseContext().getApplicationContext())
                            .setTitle("Read File Error.")
                            .setMessage("No Proper CSV file found or Error in Classification.")
                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, null)
                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }
    }

    private void processCSVFile(Uri uri) throws  IOException {
        if(uri != null && uri.getPath() != null){
            String extension = uri.getPath().substring(uri.getPath().lastIndexOf("."));
            if(extension.contains("csv")) {

                Log.i("1","Started Processing Data");

                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                101);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted

                    Log.i("1","Started Processing Data." + uri.getPath());

                    File file = new File(FileChooser.getPath(getApplicationContext(),uri));
                    
                    CSVFile csvFile = new CSVFile(new FileInputStream(file));
                    List<String[]> rows = csvFile.read();
                    List<Double> features = FeatureExtractor.extractFeatures(rows);

                    Log.i("1","Read Features. "+features.get(0).toString());

                    int estimation = -1;
                    if(message.toLowerCase().contains("svm")) {
                        Log.i("1","SVM ");

                        try {
                            InputStream data = getAssets().open("data.csv");
                            Log.i("SVC","Reading Vectors");
                            CSVFile csvFile2 = new CSVFile(data);
                            List<String[]> vectorString = csvFile2.read();
                            estimation  = SVC.classify(features,vectorString);
                        }
                        catch (Exception ex) {
                            Log.i("SVC","Reading Failed",ex);
                        }
                    }
                    else if (message.toLowerCase().contains("dtree")) {
                        Log.i("1","DTREE ");
                        estimation = DecisionTreeClassifier.predict(features);
                    }
                    TextView textView = findViewById(R.id.textView2);
                    switch (estimation) {
                        case -1:
                            textView.setText(getString(R.string.error_classification));
                            break;
                        case 0:
                            textView.setText(getString(R.string.classify_about));
                            break;
                        case 1:
                            textView.setText(getString(R.string.classified_father));
                            break;
                    }
                }
            }
        }

    }
}
