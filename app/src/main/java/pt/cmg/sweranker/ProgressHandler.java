package pt.cmg.sweranker;

import android.os.Handler;
import android.os.Looper;

public abstract class ProgressHandler extends Handler {

    public ProgressHandler(Looper looper) {
        super(looper);
    }

    public abstract void startProgress();

    public abstract void startProgressAction(String text, int maxProgressValue);

    public abstract void updateProgressAction(int progressIncrement);

    public abstract void terminateProgress();
}
