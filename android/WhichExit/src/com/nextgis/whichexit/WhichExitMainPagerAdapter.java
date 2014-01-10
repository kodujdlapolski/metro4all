package com.nextgis.whichexit;

import java.util.Locale;

import com.google.android.gms.maps.SupportMapFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;


public class WhichExitMainPagerAdapter extends FragmentPagerAdapter {

	Context mContext;
	SparseArray<Fragment> fragmentCache;
	
	public WhichExitMainPagerAdapter(Context pContext) {
		super(((FragmentActivity) pContext).getSupportFragmentManager());
		mContext = pContext;
		fragmentCache = new SparseArray<Fragment>(getCount());
	}

	@Override
	public Fragment getItem(int position) {

		if(fragmentCache.get(position) != null) {
			return fragmentCache.get(position);
		}

		switch(position) {
		case 0: {
			Fragment mf = new SupportMapFragment();
			fragmentCache.put(position, mf);
			return mf;
		}
		case 1: {
			Fragment fragment = new PoiListFragment();
			fragmentCache.put(position, fragment);
			return fragment;
		}
		case 2: {
			Fragment fragment = new NearestExitsListFragment();
			fragmentCache.put(position, fragment);
			return fragment;
		}
		default: {
			throw new IllegalStateException("Invalid position");
		}
		}

	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return mContext.getString(R.string.map).toUpperCase(l);
		case 1:
			return mContext.getString(R.string.poi_list).toUpperCase(l);
		case 2:
			return mContext.getString(R.string.exits).toUpperCase(l);
		}
		return null;
	}

}
