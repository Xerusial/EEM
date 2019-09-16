package edu.hm.eem_library.view;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.StringRes;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.target.Target;

/**
 * A showcaseview, that can be scaled and moved on the target
 */
public class TransformedShowCaseView {

    /**
     * Constructor
     *
     * @param activity calling activity
     * @param target   target view
     * @param text     text do display
     * @param offsetX  x offset of the showcaseview
     * @param offsetY  y offset of the showcaseview
     * @param reduceBy x/y reduction of the showcaseview
     * @return A transformed materialshowcaseview
     */
    public static MaterialShowcaseView getInstance(Activity activity, View target, @StringRes int text, int offsetX, int offsetY, int reduceBy) {
        MaterialShowcaseView scv = new MaterialShowcaseView.Builder(activity)
                .setTarget(target)
                .setContentText(text)
                .setDismissText(android.R.string.ok)
                .build();
        scv.setTarget(new TransformedTarget(target, offsetX, offsetY, reduceBy));
        return scv;
    }

    /**
     * Transformed target
     */
    private static class TransformedTarget implements Target {
        private final View mView;
        private final int offsetX;
        private final int offsetY;
        private final int reduceBy;

        private TransformedTarget(View view, int offsetX, int offsetY, int reduceBy) {
            this.mView = view;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.reduceBy = reduceBy;
        }

        @Override
        public Point getPoint() {
            int[] location = new int[2];
            mView.getLocationInWindow(location);
            int x = location[0] + mView.getWidth() / 2 + offsetX;
            int y = location[1] + mView.getHeight() / 2 + offsetY;
            return new Point(x, y);
        }

        @Override
        public Rect getBounds() {
            int[] l = new int[2];
            mView.getLocationInWindow(l);
            Rect a = new Rect(l[0], l[1], l[0] + mView.getWidth(), l[0] + mView.getHeight());
            a.inset(reduceBy, reduceBy);
            return a;
        }
    }
}
