package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by K1697 on 9.11.2017.
 */

public class TunerFragment extends Fragment {

	RecordAudio recordTask;
	private Button recordButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_tuner, container, false);

		recordButton = (Button) rootView.findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClick_record(view);
			}
		});

		recordButton.setTag(0);
		recordButton.setText(R.string.recordButtonText_record);

		return rootView;
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

	public void onClick_record(View v) {

		MainActivity activity = (MainActivity) getActivity();

		if (activity.arePermissionsGranted(
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.RECORD_AUDIO
		)) {
			int status = (Integer) v.getTag();
			if (status == 0) {
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
				Toast.makeText(getActivity(), "Recording stopped", Toast.LENGTH_SHORT).show();
				recordButton.setText(R.string.recordButtonText_record);
				v.setTag(0);
			}
		}
	}
}

