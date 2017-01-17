package pt.cmg.sweranker.pt.cmg.sweranker.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Carlos on 17/01/2017.
 */

public class SweRankerUtils {

    /**
     * Converts dp sizes to actual pixels.
     * As this is completely dependant on the current device (because of pixel density, naturally),
     * there is the need for a context.
     *
     * @param dp
     * @return
     */
    public static int convertDpToPixels(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
