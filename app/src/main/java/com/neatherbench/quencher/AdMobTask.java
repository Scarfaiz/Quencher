package com.neatherbench.quencher;

import android.os.AsyncTask;

public class AdMobTask extends AsyncTask {

    private final int delay;
    private  AdMobTask.AsyncResponse delegate = null;
    private boolean result = false;

    public AdMobTask (int delay, AsyncResponse delegate)
    {
        this.delay = delay;
        this.delegate = delegate;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return 0;
    }
    protected void onPostExecute(boolean result) {
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(boolean output);
    }
}
