package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer;
    int blockSize = 256;

    boolean started = false;
    RecordAudio recordTask;

    MediaRecorder recorder;

    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                // int bufferSize = AudioRecord.getMinBufferSize(frequency,
                // AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();

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
            Log.d("onProgressUpdate", "Before for loop");
            for (int i = 0; i < toTransform[0].length; i++) {
                textView.setText("Freq at " + i + ": " + toTransform[0][i] * 10);
            }
            // TODO Auto-generated method stub
            // super.onProgressUpdate(values);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        path.mkdirs();
        File file = new File(path, "recording" + Calendar.getInstance().getTime());
        recorder.setOutputFile(file.getAbsolutePath());

        try{
            recorder.prepare();
        } catch (Exception e) {
            Log.e("Main", e.getMessage());
        }
        recorder.start();

        View recordText;
        recordText = findViewById(R.id.recordText);
    }

    public void onClick_record(View v) {
        if (arePermissionsGranted(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )) {
            //record();
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }

    public void onClick_stop(View v) {
        //recorder.stop();
        //recorder.reset();
        //recorder.release();
        recordTask.cancel(true);
    }
}
