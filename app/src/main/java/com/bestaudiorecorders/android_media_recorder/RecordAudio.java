package com.bestaudiorecorders.android_media_recorder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class RecordAudio extends AsyncTask<Void, double[], Void> {
	private MainActivity activity;

	private int blockSize = 512;
	private RealDoubleFFT transformer = new RealDoubleFFT(blockSize);
	private ImageView imageView;
	private int imgViewWidth;
	private int imgViewHeight;
	private Canvas canvas;
	private Paint paint;
	private Paint paint0;

	private boolean started = true;

	RecordAudio(MainActivity activity) {
		this.activity = activity;
		imageView = (ImageView) activity.findViewById(R.id.imageView);
		imgViewWidth = imageView.getWidth();
		imgViewHeight = imageView.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(imgViewWidth, imgViewHeight,
			Bitmap.Config.ARGB_8888);
		Log.d("ImgView dimensions", "Width: " + imageView.getWidth() + " Height: " + imageView.getHeight());
		canvas = new Canvas(bitmap);
		imageView.setImageBitmap(bitmap);
		paint = new Paint();
		paint.setColor(Color.GREEN);
		paint0 = new Paint();
		paint0.setColor(Color.WHITE);
	}

	void stop() {
		started = false;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			int sampleRate = 16000;
			int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
			int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
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
			int bottomy = imgViewHeight;
			int topy = (int) (bottomy - toTransform[0][i]);

			if (i%(int)(500/calibration) == 0) {
				canvas.drawLine(x, 0, x, bottomy, paint0);
			}
			canvas.drawLine(x, topy, x, bottomy, paint);
		}

		imageView.invalidate();
		TextView textView;
		textView = (TextView) activity.findViewById(R.id.textView);
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
