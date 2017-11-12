package com.bestaudiorecorders.android_media_recorder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

	public SectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		switch(position) {
			case 0: return new PlaybackFragment();
			case 1: return new TunerFragment();
			case 2: return new RecorderFragment();
		}
		return null;
	}

	@Override
	public int getCount() {
		// Show page count
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return "SECTION 1";
			case 1:
				return "SECTION 2";
			case 2:
				return "SECTION 3";
		}
		return null;
	}
}
