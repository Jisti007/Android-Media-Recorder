package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

/**
 * Created by K1697 on 9.11.2017.
 */

public class PlaybackFragment extends Fragment {

	private TextView currentFileView;
	final int PICKFILE_RESULT_CODE = 0;
	Uri activeFile;
	private Button playActiveFileButton;
	private Button getFileButton;

	static MediaPlayer mediaPlayer;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_playback, container, false);


		currentFileView = (TextView) rootView.findViewById(R.id.textView2);
		currentFileView.setText(R.string.label_noFileSelected);

		getFileButton = (Button) rootView.findViewById(R.id.getFileButton);
		getFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFile(view);
			}
		});

		playActiveFileButton = (Button) rootView.findViewById(R.id.playActiveFile);
		playActiveFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				playActiveFile(view);
			}
		});
		playActiveFileButton.setTag(0);
		playActiveFileButton.setText(R.string.playButtonText_play);


		activeFile = null;

		return rootView;
	}

	public void getFile(View v) {
		MainActivity activity = (MainActivity) getActivity();

		if (activity.arePermissionsGranted(
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
			Toast.makeText(getActivity(), "Something went wrong, please allow file reading for the app", Toast.LENGTH_SHORT).show();
		}
	}

	public void playActiveFile(View v) {

		int status = (Integer) v.getTag();

		if (activeFile != null) {

			if (status == 0) {

				try {
					mediaPlayer = MediaPlayer.create(getActivity(), activeFile);
					mediaPlayer.start();

					playActiveFileButton.setText(R.string.playButtonText_stop);
					v.setTag(1);
				} catch (Exception e) {
					Log.e("playActiveFile:", e.toString());
					Toast.makeText(getActivity(), "Failed to open file " + activeFile, Toast.LENGTH_SHORT).show();
				}
			} else {
				try {

					mediaPlayer.reset();
					mediaPlayer.stop();
					//mediaPlayer.release();
					//mediaPlayer=null;

					Toast.makeText(getActivity(), "Playback stopped", Toast.LENGTH_SHORT).show();
					playActiveFileButton.setText(R.string.playButtonText_play);
					v.setTag(0);

				} catch (Exception e) {
					Log.e("Mediaplayer", "x*D " + e.toString());
					Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			Toast.makeText(getActivity(), "Please select a file", Toast.LENGTH_SHORT).show();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Fix no activity available
		if (data == null)
			return;
		switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				if (resultCode == RESULT_OK) {
					//Get active audio file name
					Uri returnUri = data.getData();
					Cursor returnCursor = getActivity().getContentResolver().query(returnUri, null, null, null, null);
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
