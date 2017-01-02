package com.group2.team.project2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.facebook.login.LoginManager;
import com.group2.team.project2.event.BResultEvent;
import com.group2.team.project2.fragment.ATabFragment;
import com.group2.team.project2.fragment.BTabFragment;
import com.group2.team.project2.fragment.CTabFragment;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private ImageView imageView;

    private PhotoViewAttacher attacher;
    private int prevOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        imageView = (ImageView) findViewById(R.id.main_imageView);
        imageView.setBackgroundColor(Color.rgb(0, 0, 0));
        attacher = new PhotoViewAttacher(imageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EventBus.getInstance().post(BResultEvent.create(requestCode, resultCode, data));
    }

    @Override
    public void onBackPressed() {
        if (imageView.getVisibility() == View.INVISIBLE)
            super.onBackPressed();
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(prevOption);
            }
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_logout:
                LoginManager.getInstance().logOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }
        return true;
    }

    public void setImageView(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            prevOption = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);
        attacher.update();
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//            switch (position) {
//                case 0:
//                    return ATabFragment.newInstance();
//                case 1:
//                    return BTabFragment.newInstance();
//                case 2:
//                    return CTabFragment.newInstance();
//            }
//            return null;
            return CTabFragment.newInstance();
        }

        @Override
        public int getCount() {
//            return 3;
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "A";
                case 1:
                    return "B";
                case 2:
                    return "C";
            }
            return null;
        }
    }
}
