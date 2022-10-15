package research.sg.edu.edapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Util {
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleUpload(Context context){
        ComponentName serviceComponent=new ComponentName(context,UploadJob.class);
        JobInfo.Builder builder=new JobInfo.Builder(0,serviceComponent);
        JobScheduler jobScheduler=context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }
}
