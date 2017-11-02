package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.nfc.Tag;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    int sampleRate = 16000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 512;
    private RealDoubleFFT transformer = new RealDoubleFFT(blockSize);

    boolean started = false;
    RecordAudio recordTask;

    MediaRecorder recorder;
	private Button recordButton;

	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	Paint paint0;
	int imgViewWidth;
	int imgViewHeight;

    private class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate, channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, sampleRate,
                    channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0;
                    }
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

	        final double calibration = 15.42137742;

	        canvas.drawColor(Color.BLACK);

	        for (int i = 0; i < toTransform[0].length; i++) {
		        int x = i*imgViewWidth/toTransform[0].length;
		        int upy = imgViewHeight;
		        int downy = (int) (upy - (toTransform[0][i] * upy/2));

		        if (i%(int)(500/calibration) == 0) {
			        canvas.drawLine(x, 0, x, upy, paint0);
		        }
		        canvas.drawLine(x, downy, x, upy, paint);
	        }

	        imageView.invalidate();
            TextView textView;
            textView = (TextView) findViewById(R.id.textView);
            int x = 0;
            double maxY = 0;
	        ArrayList<Integer> array = new ArrayList<>();

            for (int i = 0; i < toTransform[0].length; i++) {
                if(maxY < toTransform[0][i]){
                    x = i;
                    maxY = toTransform[0][i];
                }
            }
            for (int i = 0; i < toTransform[0].length; i++) {
	            if (maxY*2/3 < toTransform[0][i]) {
		            textView.setText("Amplitude: " + Math.round(maxY*100.0)/100.0 + "\nFreq: " + (int)(i*calibration));
		            break;
	            }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    recordButton = (Button) findViewById(R.id.recordButton);
	    recordButton.setTag(0);
	    recordButton.setText(R.string.recodButtonText_record);

	    /*imageView = (ImageView) this.findViewById(R.id.imageView);
	    bitmap = Bitmap.createBitmap(512, 200,
		    Bitmap.Config.ARGB_8888);
	    canvas = new Canvas(bitmap);
	    paint = new Paint();
	    paint.setColor(Color.GREEN);
	    imageView.setImageBitmap(bitmap);

	    paint0 = new Paint();
	    paint0.setColor(Color.WHITE);*/
    }

    public void onWindowFocusChanged(boolean hasFocus) {

	    super.onWindowFocusChanged(hasFocus);
	    if(hasFocus) {
		    imageView = (ImageView) this.findViewById(R.id.imageView);
		    imgViewWidth = imageView.getWidth();
		    imgViewHeight = imageView.getHeight();
		    bitmap = Bitmap.createBitmap(imgViewWidth, imgViewHeight,
			    Bitmap.Config.ARGB_8888);
		    Log.d("ImgView dimensions", "Width: " + imageView.getWidth() + " Height: " + imageView.getHeight());
		    canvas = new Canvas(bitmap);
		    paint = new Paint();
		    paint.setColor(Color.GREEN);
		    imageView.setImageBitmap(bitmap);

		    paint0 = new Paint();
		    paint0.setColor(Color.WHITE);
	    }
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
                recordTask = new RecordAudio();
                started = true;
                recordTask.execute();
		        //record();
		        recordButton.setText(R.string.recordButtonText_stop);
		        v.setTag(1);
	        } else {
		        //recorder.stop();
		        //recorder.reset();
		        //recorder.release();
                started = false;
                recordTask.cancel(true);
		        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
		        recordButton.setText(R.string.recodButtonText_record);
		        v.setTag(0);
	        }
        }
    }
}
