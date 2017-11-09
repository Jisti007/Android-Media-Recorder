package com.bestaudiorecorders.android_media_recorder;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by K1697 on 9.11.2017.
 */

public class MainActivity extends AppCompatActivity{


	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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

