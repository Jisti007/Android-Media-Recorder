package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	RecordAudio recordTask;
    MediaRecorder recorder;
	private Button recordButton;
	private TextView currentFileView;
	final int PICKFILE_RESULT_CODE = 0;
	Uri activeFile;
	private Button playActiveFileButton;

	static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    recordButton = (Button) findViewById(R.id.recordButton);
	    recordButton.setTag(0);
	    recordButton.setText(R.string.recodButtonText_record);

	    currentFileView = (TextView) findViewById(R.id.textView2);
	    currentFileView.setText("No file selected");

		playActiveFileButton = (Button) findViewById(R.id.playActiveFile);
	    playActiveFileButton.setTag(0);
	    playActiveFileButton.setText("Play active file");

	    activeFile = null;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
    }

    public boolean arePermissionsGranted(String... permissions) {
        boolean permissionsGranted = true;

        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false;
                    break;
                }
            }
        }
        if (!permissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        return permissionsGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean recordPermissionGranted = false;
        boolean writePermissionGranted = false;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                switch (permissions[i]) {
                    case Manifest.permission.RECORD_AUDIO:
                        recordPermissionGranted = true;
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        writePermissionGranted = true;
                        break;
                }
            }
        }
        if (recordPermissionGranted && writePermissionGranted) {
            //record();
        }
    }

    public void record(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
	    if (!path.mkdirs()) {
		    Log.e("record", "Failed to create path.");
	    }

	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
	    String formatted = format1.format(cal.getTime());
	    //default saving path: storage/emulated/0/Documents

        File file = new File(path, "recording_" + formatted);
        recorder.setOutputFile(file.getAbsolutePath());

        try{
            recorder.prepare();
        } catch (Exception e) {
            Log.e("Main", e.getMessage());
        }
        //recorder.start();
	    Toast.makeText(this, "Recording to" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    public void onClick_record(View v) {
        if (arePermissionsGranted(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )) {
            int status =(Integer) v.getTag();
	        if(status == 0) {
                recordTask = new RecordAudio(this);
               // started = true;
                recordTask.execute();
		        //record();
		        recordButton.setText(R.string.recordButtonText_stop);
		        v.setTag(1);
	        } else {
		        //recorder.stop();
		        //recorder.reset();
		        //recorder.release();
                //started = false;
		        recordTask.stop();
                recordTask.cancel(true);
		        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
		        recordButton.setText(R.string.recodButtonText_record);
		        v.setTag(0);
	        }
        }
    }

    public void onClick_getFile(View v) {
	    if (arePermissionsGranted(
		    Manifest.permission.READ_EXTERNAL_STORAGE
	    )) {
		    Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
		    fileintent.setType("audio/*");
		    try {
			    startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
		    } catch (ActivityNotFoundException e) {
			    Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
		    }

		    } else {
			    Toast.makeText(this, "Something went wrong, please allow file reading for the app", Toast.LENGTH_SHORT).show();
		    }
    }

	public void onClick_playActiveFile(View v) {

		int status = (Integer) v.getTag();

		if (activeFile != null) {

			if (status == 0) {

				try {
					mediaPlayer = MediaPlayer.create(this, activeFile);
					mediaPlayer.start();

					playActiveFileButton.setText("Stop playback");
					v.setTag(1);
				} catch (Exception e) {
					Log.e("onClick_playActiveFile:", e.toString());
					Toast.makeText(this, "Failed to open file " + activeFile, Toast.LENGTH_SHORT).show();
				}
			} else {
				try {

					mediaPlayer.reset();
					mediaPlayer.stop();
					//mediaPlayer.release();
					//mediaPlayer=null;

					Toast.makeText(this, "Playback stopped", Toast.LENGTH_SHORT).show();
					playActiveFileButton.setText("Play active file");
					v.setTag(0);

				} catch (Exception e) {
					Log.e("Mediaplayer", "x*D " + e.toString());
					Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
		}
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Fix no activity available
		if (data == null)
			return;
		switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				if (resultCode == RESULT_OK) {
					//Get active audio file name
					Uri returnUri = data.getData();
					Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
					int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					returnCursor.moveToFirst();
					currentFileView.setText("Active file: " + returnCursor.getString(nameIndex) );

					//FilePath is your file as a string
					String FilePath = data.getData().getPath();
					activeFile = data.getData();
					}
		}
	}

}
