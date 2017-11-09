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

		}
		return null;
	}

	@Override
	public int getCount() {
		// Show page count
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return "SECTION 1";
			case 1:
				return "SECTION 2";
		}
		return null;
	}
}
