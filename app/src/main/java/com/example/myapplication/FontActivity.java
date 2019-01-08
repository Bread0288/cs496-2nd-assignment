package com.example.myapplication;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontActivity extends AppCompatActivity {
        private static Typeface mTypeface;

        @Override
        public void setContentView(int layoutResID) {
            super.setContentView(layoutResID);

            if (FontActivity.mTypeface == null)
                FontActivity.mTypeface = Typeface.createFromAsset(getAssets(), "font/NanumSquareRoundR.ttf");

            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            setGlobalFont(root);
        }

        void setGlobalFont(ViewGroup root) {
            for (int i = 0; i < root.getChildCount(); i++) {
                View child = root.getChildAt(i);
                if (child instanceof TextView)
                    ((TextView)child).setTypeface(mTypeface);
                else if (child instanceof ViewGroup)
                    setGlobalFont((ViewGroup)child);
            }
    }
}
