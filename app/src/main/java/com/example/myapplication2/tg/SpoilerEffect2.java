package com.example.myapplication2.tg;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author a.s.korchagin
 */
public class SpoilerEffect2 {

    public static FrameLayout makeTextureViewContainer(ViewGroup rootView) {
        FrameLayout container = new FrameLayout(rootView.getContext()) {
            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                return false;
            }
        };
        rootView.addView(container);
        return container;
    }
}
