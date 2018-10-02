package ashad.app.torch.flashlight.whatsappvideo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final File ExternalStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            + "/YOUR_FOLDER_NAME");
    private static final String DIR_NAME = "DASHRecorder";
    Button btn;
    Uri uri;
    String realPath;
    private String fileName;
    private Uri videoUri;
    private String outputPath;
    private Intent videoIntent;
    private ProgressBar segmentProgressBar;
    private ProgressBar uploadProgressBar;
    int gallery=0;
    private TextView segmentProgressView;
    private TextView uploadProgressView;
    //Keeping FileObserver global else it will be garbage collected
    private FileObserver observer;
    private String getSegmentFolder(String innerFolderName) {
        String folderName = DIR_NAME + "/segments/" + innerFolderName;
        File segmentFolder = new File(ExternalStorageDir, folderName);
        segmentFolder.mkdirs();

        return segmentFolder.getPath() + "/";
    }

    /**
     * Convenience method to generate the path where to store the video
     * recording.
     * @return Uri where the recording is to be saved
     */
    @SuppressLint("SimpleDateFormat")
    private Uri getOutputMediaFileUri() {
        String folderName = DIR_NAME + "/video/" ;
        fileName = "DASH_Video_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(new Date());
        String fileNameWithExt = fileName + ".mp4";

        File videoFolder = new File(ExternalStorageDir, folderName);
        videoFolder.mkdirs();

        File video = new File(videoFolder, fileNameWithExt);
        Uri uriSavedImage = Uri.fromFile(video);
        return uriSavedImage;
    }


    /**
     * Makes a call to video segmentation in the form of
     * asynchronous task.
     */
    private void segmentVideo() {
        //Segment the video in splits of 3 seconds
        Log.i("DASH","Inside segmentVideo()");
        outputPath = getSegmentFolder(fileName);
        Log.i("DASH", "Path where segments have to be saved is " + outputPath);

        SplitVideo obj = new SplitVideo(segmentProgressBar, segmentProgressView);
        obj.execute(realPath, outputPath, "30.0");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn=findViewById(R.id.button);
        segmentProgressBar = (ProgressBar) findViewById(R.id.segmentProgress);
        segmentProgressView = (TextView) findViewById(R.id.segmentTextView);
        uploadProgressView = (TextView) findViewById(R.id.uploadTextView);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Intent.ACTION_PICK);
                i.setType("video/*");
                startActivityForResult(i,gallery);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery)
        {
            uri=data.getData();
           realPath = ImageFilePath.getPath(MainActivity.this, data.getData());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(),uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInmillisec = Long.parseLong( time );
            long duration = timeInmillisec / 1000;
            long hours = duration / 3600;
            long minutes = (duration - hours * 3600) / 60;
            long seconds = duration - (hours * 3600 + minutes * 60);
            Log.i("lengthofvideobro"," is urio "+uri);
            Log.i("lengthofvideobro"," is time  "+time);
            Log.i("lengthofvideobro"," is "+timeInmillisec);
            Log.i("lengthofvideobro"," is "+duration);
            Log.i("lengthofvideobro"," is "+hours);
            Log.i("lengthofvideobro"," is "+minutes);
            Log.i("lengthofvideobro"," is "+seconds);
            segmentVideo();
            getOutputMediaFileUri();

            Log.i("lengthofvideobro"," is "+getOutputMediaFileUri());
        }
    }
}
