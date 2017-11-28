package com.scarfaiz.cluckinbell;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * This class will link the Backdrop element (that can be anything extending View) with a
 * NestedScrollView (the dependency). Whenever dependecy is moved, the backdrop will be moved too
 * behaving like parallax effect.
 *
 * The backdrop need to be <bold>into</bold> a CoordinatorLayout and <bold>before</bold>
 * {@link BottomSheetBehaviorGoogleMapsLike} in the XML file to get same behavior like Google Maps.
 * It doesn't matter where the backdrop element start in XML, it will be moved following
 * Google Maps's parallax behavior.
 * @param <V>
 */
public class BackdropBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing {@link BottomSheetBehaviorGoogleMapsLike#mPeekHeight}
     * get changed dynamically we get the {@link NestedScrollView} that has
     * "app:layout_behavior=" {@link BottomSheetBehaviorGoogleMapsLike} inside the {@link CoordinatorLayout}
     */
    private WeakReference<BottomSheetBehaviorGoogleMapsLike> mBottomSheetBehaviorRef;
    /**
     * Following {@link #onDependentViewChanged}'s docs mCurrentChildY just save the child Y
     * position.
     */
    private int mCurrentChildY;

    public BackdropBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency instanceof NestedScrollView) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from(dependency);
                return true;
            }
            catch (IllegalArgumentException ignored){}
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef.get() == null)
            getBottomSheetBehavior(parent);
        int collapsedY = dependency.getHeight() - mBottomSheetBehaviorRef.get().getPeekHeight();
        int achorPointY = child.getHeight();
        int lastCurrentChildY = mCurrentChildY;

        if((mCurrentChildY = (int) ((dependency.getY()-achorPointY) * collapsedY / (collapsedY-achorPointY))) <= 0)
            child.setY(mCurrentChildY = 0);
        else
            child.setY(mCurrentChildY);
        return (lastCurrentChildY == mCurrentChildY);
    }

    /**
     * Look into the CoordiantorLayout for the {@link BottomSheetBehaviorGoogleMapsLike}
     * @param coordinatorLayout with app:layout_behavior= {@link BottomSheetBehaviorGoogleMapsLike}
     */
    private void getBottomSheetBehavior(@NonNull CoordinatorLayout coordinatorLayout) {

        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View child = coordinatorLayout.getChildAt(i);

            if (child instanceof NestedScrollView) {

                try {
                    BottomSheetBehaviorGoogleMapsLike temp = BottomSheetBehaviorGoogleMapsLike.from(child);
                    mBottomSheetBehaviorRef = new WeakReference<>(temp);
                    break;
                }
                catch (IllegalArgumentException ignored){}
            }
        }
    }
}