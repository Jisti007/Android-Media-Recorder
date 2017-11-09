package com.bestaudiorecorders.android_media_recorder;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by K1697 on 9.11.2017.
 */

public class MenuActivity extends AppCompatActivity{


	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
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

}

