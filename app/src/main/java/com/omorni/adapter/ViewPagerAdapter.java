package com.omorni.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.omorni.R;

/**
 * Created by V on 2/23/2017.
 */

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    Integer[] IMAGES;
    LayoutInflater layoutInflater;


    public ViewPagerAdapter(Context context, Integer[] IMAGES) {
        this.context = context;
        this.IMAGES = IMAGES;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.length;
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = layoutInflater.inflate(R.layout.viewpager_element, view, false);

        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.pager_image);

        imageView.setImageResource(IMAGES[position]);
        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
