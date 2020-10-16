package com.hostcart.socialbot.job;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.hostcart.socialbot.model.constants.LastSeenStates;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.JobSchedulerSingleton;
import com.hostcart.socialbot.utils.MyApp;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SetLastSeenJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        int lastSeenState = SharedPreferencesManager.getLastSeenState();
        if (MyApp.isBaseActivityVisible() && lastSeenState != LastSeenStates.ONLINE) {
            FireManager.setOnlineStatus();
        } else if (!MyApp.isBaseActivityVisible() && lastSeenState != LastSeenStates.LAST_SEEN) {
            FireManager.setLastSeen();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public static void schedule(Context context) {
        ComponentName component = new ComponentName(context, SetLastSeenJob.class);

        JobInfo.Builder builder = new JobInfo.Builder(JobIds.JOB_ID_SET_LAST_SEEN, component)
                .setPersisted(true)
                .setPeriodic(TimeUnit.MINUTES.toMillis(5))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);


        JobSchedulerSingleton.getInstance().schedule(builder.build());
    }

}
