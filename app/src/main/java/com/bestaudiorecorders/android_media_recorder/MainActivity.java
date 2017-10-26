package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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

    int sampleRate = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 256;
    private RealDoubleFFT transformer = new RealDoubleFFT(blockSize);

    boolean started = false;
    RecordAudio recordTask;

    MediaRecorder recorder;
	private Button recordButton;

    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d("doInBackground", "start in background");
            try {
                // int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                // AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                        channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate,
                        channelConfiguration, audioEncoding, bufferSize);
                Log.d("doInBackground", "initialization success");

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();
                Log.d("doInBackground", "starting success");

                // started = true; hopes this should true before calling
                // following while loop

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            blockSize);

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed
                        // 16
                    }                                       // bit
                    transformer.ft(toTransform);
                    publishProgress(toTransform);
                }
                Log.d("doInBackground", "Loop success");

                audioRecord.stop();

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            TextView textView;
            textView = (TextView) findViewById(R.id.textView);
            int x = 0;
            double maxY = 0;
            for (int i = 0; i < toTransform[0].length; i++) {
                if(maxY < toTransform[0][i]){
                    x = i;
                    maxY = toTransform[0][i];
                }
            }
            textView.setText("Strongest freq: " + x*15);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    recordButton = (Button) findViewById(R.id.recordButton);
	    recordButton.setTag(0);
	    recordButton.setText("Record");
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
        path.mkdirs();

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
                recordTask = new RecordAudio();
                started = true;
                recordTask.execute();
		        //record();
		        recordButton.setText("Stop");
		        v.setTag(1);
	        } else {
		        //recorder.stop();
		        //recorder.reset();
		        //recorder.release();
                started = false;
                recordTask.cancel(true);
		        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
		        recordButton.setText("Record");
		        v.setTag(0);
	        }
        }
    }
}
