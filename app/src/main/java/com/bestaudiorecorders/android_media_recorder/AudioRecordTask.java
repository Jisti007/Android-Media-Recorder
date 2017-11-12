package com.bestaudiorecorders.android_media_recorder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.corp.productivity.specialprojects.android.fft.RealDoubleFFT;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.encode.BitOutputStream;
import io.nayuki.flac.encode.FlacEncoder;
import io.nayuki.flac.encode.RandomAccessFileOutputStream;
import io.nayuki.flac.encode.SubframeEncoder;

class AudioRecordTask extends AsyncTask<Void, double[], Void> {
	private TunerFragment activity;

	private int sampleRate = 16000;
	private int blockSize = 512;
	private RealDoubleFFT transformer = new RealDoubleFFT(blockSize);
	private ArrayList<Integer> samples;

	private ImageView imageView;
	private int imgViewWidth;
	private int imgViewHeight;
	private Canvas canvas;
	private Paint paint;
	private Paint paint0;

	private boolean started = true;

	AudioRecordTask(TunerFragment activity) {
		this.activity = activity;
		imageView = (ImageView) activity.getView().findViewById(R.id.imageView);
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
			int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
			int audioEncoding = AudioFormat.ENCODING_PCM_FLOAT;
			int bufferSize = AudioRecord.getMinBufferSize(
				sampleRate, channelConfiguration, audioEncoding);

			AudioRecord audioRecord = new AudioRecord(
				MediaRecorder.AudioSource.MIC, sampleRate,
				channelConfiguration, audioEncoding, bufferSize);

			float[] buffer = new float[blockSize];
			double[] toTransform = new double[blockSize];
			samples = new ArrayList<>();

			audioRecord.startRecording();

			while (started) {
				int bufferReadResult = audioRecord.read(buffer, 0, blockSize, AudioRecord.READ_BLOCKING);
				for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
					toTransform[i] = (double) buffer[i];
					samples.add((int)(buffer[i] * 1000));
				}
				transformer.ft(toTransform);
				publishProgress(toTransform);
			}

			audioRecord.stop();
			save();

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
			int topy = (int) (bottomy - toTransform[0][i] * bottomy);

			if (i%(int)(500/calibration) == 0) {
				canvas.drawLine(x, 0, x, bottomy, paint0);
			}
			canvas.drawLine(x, topy, x, bottomy, paint);
		}

		imageView.invalidate();
		TextView textView;
		textView = activity.getView().findViewById(R.id.textView);
		double maxY = 0;

		for (int i = 0; i < toTransform[0].length; i++) {
			if(maxY < toTransform[0][i]){
				maxY = toTransform[0][i];
			}
		}
		for (int i = 0; i < toTransform[0].length; i++) {
			if (maxY*2/3 < toTransform[0][i]) {
				textView.setText(String.format(Locale.getDefault(), "%s%.2f\nFreq: %.2f",
					activity.getString(R.string.amplitude),
					maxY,
					i * calibration
				));
				break;
			}
		}
	}

	private void save() {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		if (!path.exists() && !path.mkdirs()) {
			Log.e("record", "Failed to create path.");
			return;
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
		String formatted = format1.format(cal.getTime());
		//default saving path: storage/emulated/0/Documents

		File file = new File(path, "recording_" + formatted + ".flac");
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.setLength(0);  // Truncate an existing file
			BitOutputStream out = new BitOutputStream(
				new BufferedOutputStream(new RandomAccessFileOutputStream(raf)));
			out.writeInt(32, 0x664C6143);

			// Populate and write the stream info structure
			StreamInfo info = new StreamInfo();
			info.sampleRate = sampleRate;
			info.numChannels = 1;//samples.size();
			int sampleDepth = 32;
			info.sampleDepth = sampleDepth;
			info.numSamples = samples.size();
			int[][] sampleArray = new int[1][samples.size()];
			for (int i = 0; i < samples.size(); i++) {
				sampleArray[0][i] = samples.get(i);
			}
			info.md5Hash = StreamInfo.getMd5Hash(sampleArray, sampleDepth);
			info.write(true, out);

			// Encode all frames
			new FlacEncoder(info, sampleArray, blockSize, SubframeEncoder.SearchOptions.SUBSET_BEST, out);
			out.flush();

			// Rewrite the stream info metadata block, which is
			// located at a fixed offset in the file by definition
			raf.seek(4);
			info.write(true, out);
			out.flush();
		} catch (IOException e) {
			Log.e("record", "Could not open file for reading and/or writing.");
		}
	}
}
