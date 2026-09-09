package net.kdt.pojavlaunch.lifecycle;

import android.app.Activity;
import android.content.Context;


public interface ContextExecutorTask {
    
    void executeWithActivity(Activity activity);

    
    void executeWithApplication(Context context);
}
