package research.sg.edu.edapp;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.*;
import research.sg.edu.edapp.FinalClasses.EsmDetail;

public class AppLoggingService extends Service {

    //private static String old_pkg="Dummy_Pkg";

    public AppLoggingService() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class RecentUseComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {

        LogAppName();
        return START_STICKY;
    }

    public void LogAppName() {

        Set<String> strings = new HashSet<String>();
        strings.add("com.google.android.gm");
        strings.add("com.facebook.katana");
        strings.add("com.whatsapp");
        strings.add("com.google.android.talk");
        strings.add(getDefaultSmsAppPackageName(this));

        String app_name;
        String last_app;
        Boolean status=false;
        app_name=getCurrentAppName();
        last_app=RetrieveLastApp();
        System.out.println("Current_app:" + app_name + ",Last App:" + last_app );

        if(!app_name.isEmpty()) {
            if(!last_app.equalsIgnoreCase(app_name)){
                StoreLastApp(app_name);

                if(!last_app.equals("LastApp") && strings.contains(last_app)) {
                    status = true;
                    StoreStatus(status);
                    StoreESMDetail(getApplicationContext(),last_app);
                }
            }
        }
    }

    public String getCurrentAppName(){

        String packagename;
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                packagename = "LockScreen";
            }
            else {
                ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                packagename = componentInfo.getPackageName();
            }
        }
        else {
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                packagename = "LockScreen";
            }
            else {
                packagename=getTopPackage();
            }
        }

        return packagename;

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String getTopPackage(){
        RecentUseComparator mRecentComp=new RecentUseComparator();
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts-1000*10, ts);
        if (usageStats == null || usageStats.size()== 0) {
            return RetrieveLastApp();
        }
        Collections.sort(usageStats, mRecentComp);
        System.out.println("[Current_app]:"+usageStats.get(0).getPackageName());
        return usageStats.get(0).getPackageName();
    }

    public String RetrieveLastApp(){
        String last_app="Dummy";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.applogger_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);
            last_app = pref.getString(getResources().getString(R.string.sharedpref_last_logged_app_name), "LastApp");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return last_app;
    }
    public void StoreLastApp(String app_name){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.sharedpref_last_logged_app_name), app_name);
        log_editor.apply();
        log_editor.commit();
    }
    public void StoreStatus(boolean flag){
        SharedPreferences pref =  getApplicationContext().getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);;
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putBoolean("sharedpref_status", flag);
        log_editor.apply();
        log_editor.commit();
    }
    public void StoreESMDetail(Context context,String esm_app){

        ContentValues values = new ContentValues();
        values.put(EsmDetail.EsmEntry.ESM_APP_NAME, esm_app);
        //values.put(EsmDetail.EsmEntry.ESM_TIMESTAMP, esm_time);

        Uri uri = context.getContentResolver().insert(ESMContentProvider.CONTENT_URI, values);
    }

    public static String getDefaultSmsAppPackageName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Telephony.Sms.getDefaultSmsPackage(context);
        else {
            String defApp = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
            PackageManager pm = context.getApplicationContext().getPackageManager();
            Intent iIntent = pm.getLaunchIntentForPackage(defApp);
            ResolveInfo mInfo = pm.resolveActivity(iIntent,0);
            return mInfo.activityInfo.packageName;

        }
    }

}
