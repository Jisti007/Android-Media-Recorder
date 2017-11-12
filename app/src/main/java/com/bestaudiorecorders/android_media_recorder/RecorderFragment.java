package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Ville on 2017/11/12.
 */

public class RecorderFragment extends Fragment {
	MediaRecorder recorder;
	private Button recordButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_recorder, container, false);

		recordButton = (Button) rootView.findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				recordButton_onClick(view);
			}
		});

		recordButton.setTag(0);
		recordButton.setText(R.string.recordButtonText_record);

		return rootView;
	}

	public void record(){
		if (recorder == null) {
			recorder = new MediaRecorder();
		} else {
			stop();
		}
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

		File file = new File(path, "recording_" + formatted + ".3gp");
		recorder.setOutputFile(file.getAbsolutePath());

		try{
			recorder.prepare();
		} catch (Exception e) {
			Log.e("Main", e.getMessage());
		}
		recorder.start();
		Toast.makeText(getActivity(), "Recording to" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
	}

	public void stop() {
		recorder.stop();
		recorder.reset();
		recorder.release();
	}

	public void recordButton_onClick(View v) {
		MainActivity activity = (MainActivity) getActivity();
		if (activity.arePermissionsGranted(
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.RECORD_AUDIO
		)) {
			int status = (Integer) v.getTag();
			if (status == 0) {
				record();
				recordButton.setText(R.string.recordButtonText_stop);
				v.setTag(1);
			} else {
				stop();
				Toast.makeText(getActivity(), "Recording stopped", Toast.LENGTH_SHORT).show();
				recordButton.setText(R.string.recordButtonText_record);
				v.setTag(0);
			}
		}
	}
}
