package research.sg.edu.edapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import android.telephony.TelephonyManager;


import android.provider.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.*;
import android.os.Build.VERSION_CODES;
import android.app.NotificationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.*;

import research.sg.edu.edapp.FinalClasses.EsmDetail;
import research.sg.edu.edapp.FinalClasses.FeaturesDetails;

import static java.lang.Integer.parseInt;

public class MasterService extends Service {

    static long old_time, old_time_f, old_time_a, old_time_s, old_time_b;

    public MasterService() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        System.out.println("[MasterService] getting started...");
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        System.out.println("[StopService] getting stopped...");

        CancelAlarm();
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();


        check_and_fire_service();

        WriteAlarmFiringTime(0);

        wakeLock.release();

        SetAlarmAgain();


        /*try {
            SendNotification();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/


        return START_STICKY;
    }

    public void SetAlarmAgain() {

        long currentTimeMillis = System.currentTimeMillis();
        // TODO: Setting firing up interval here 1000* instead of 1* REVERTED
        int common_interval = 1000 * parseInt(getResources().getString(R.string.common_interval));
        // int common_interval = 1 * Integer.parseInt(getResources().getString(R.string.common_interval));

        long nextUpdateTimeMillis = currentTimeMillis + common_interval;
        Log.d("SetAlarmAgain:","set alarm again");
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            manager.set(AlarmManager.RTC_WAKEUP, nextUpdateTimeMillis, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, nextUpdateTimeMillis, pendingIntent);
        }

    }

    public void CancelAlarm() {
        Log.d("CancelAlarm:","Cancelling masterservice alarm");
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        manager.cancel(pendingIntent);
    }

    public void SendNotification() throws ParseException {
        //long currentTimeMillis = System.currentTimeMillis();
        long interval = 1;
        //Calendar cal=Calendar.getInstance();
        //cal.add(Calendar.MINUTE,4);
        //long nextUpdateTime=currentTimeMillis+interval;
        //Calendar cal = Calendar.getInstance();
        //Date date=cal.getTime();
        //DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        //Date date = new Date();
        //Date StartTime=new Date(date.getTime()+interval);

        //String StartTime=dateFormat.format(cal.getTime());
        //System.out.println("StartTime="+StartTime);
        //String []arrOfString=StartTime.split(":",0);
        //System.out.println("Hour="+arrOfString[0]+",Minute="+arrOfString[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 50);
        //calendar.set(Calendar.SECOND, 20);
        System.out.println("I am before");

        Intent intent = new Intent(MasterService.this, NotificationSevice.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 10*60* 1000, pendingIntent);
        System.out.println("I am after");
    }


    public void WriteAlarmFiringTime(int idx) {

        String imei_no, service_name = "[MasterService]:";
        long curr_time;

        long diff;

        curr_time = System.currentTimeMillis();
        diff = (curr_time - old_time) / 1000;

        old_time = curr_time;

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //imei_no = (String) telephonyManager.getDeviceId();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String alarm_file_ctr = RetrieveAlarmFileCtr();

        String alarm_file_name = alarm_file_ctr + "_Alarm_timing.txt";

        if(idx==1){
            service_name="[FileUpload]:";

            diff=(curr_time-old_time_f)/1000;
            old_time_f=curr_time;
        }
        else if(idx==2){
            service_name="[Prediction Starts]:";

            diff=(curr_time-old_time_a)/1000;
            old_time_a=curr_time;
        }
        else if(idx==3){
            service_name="[Mood Recorder Fire]:";

            diff=(curr_time-old_time_s)/1000;
            old_time_s=curr_time;
        }
        else if(idx==4){
            service_name="[Building Model]:";

            diff=(curr_time-old_time_b)/1000;
            old_time_b=curr_time;
        }
        else if(idx==5){
            service_name="[App Logging]:";

            diff=(curr_time-old_time_b)/1000;
            old_time_b=curr_time;
        }
        else if(idx==6){
            service_name="[Validate Predictions]:";

            diff=(curr_time-old_time_b)/1000;
            old_time_b=curr_time;
        }

        //String registration_dtls=service_name+"Alarm at:"+currentDateandTime+","+diff+"\n";
        String alarm_dtls=service_name+","+diff+"\n";
        byte[] alarm_data = alarm_dtls.getBytes();

        /*File alarm_file = new File(dataDir, alarm_file_name);
        try {

            FileOutputStream fos;
            fos = new FileOutputStream(alarm_file,true);
            fos.write(alarm_data);
            fos.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }*/

        //int alarm_file_size = parseInt(String.valueOf(alarm_file.length() / 1024));

        //int alarm_file_size_threshold = parseInt(getResources().getString(R.string.alarm_file_size_limit));
        //System.out.println("Feature File Size:" + alarm_file_size + ", Feature File Threshold:" + alarm_file_size_threshold);

        /*if (alarm_file_size > alarm_file_size_threshold) {
            int ctr = (parseInt(alarm_file_ctr) + 1) % 10;
            alarm_file_ctr = String.valueOf(ctr);
            move_file(alarm_file_name);
        }*/
        int ctr = (parseInt(alarm_file_ctr) + 1) % 10;
        alarm_file_ctr = String.valueOf(ctr);
        StoreAlarmFileCtr(alarm_file_ctr);
    }

    public void check_and_fire_service() {

        String mood_log_timestamp=null, prediction_timestamp = null,upload_data_timestamp=null,app_log_timestamp=null,confirm_popup_timestamp=null;
        Date mood_log_time, prediction_time,upload_data_time,app_log_time,confirm_popup_time;

        Date curr_time=new Date();

        // App Logging Service
        // TODO: Changing logging interval from 1000 * to 1 * , parseInt 10 to 1 REVERTED
        long app_log_interval=1000 * parseInt(getResources().getString(R.string.app_logging_interval));
        // long app_log_interval=1 * 1;

        app_log_timestamp=read_time(getString(R.string.app_log_timestamp));
        app_log_time=convert_to_date(app_log_timestamp);
        if(find_time_diff(curr_time,app_log_time)>=0){
            Date next_app_log_time;
            WriteAlarmFiringTime(5);
            StartAppLoggingService();
            next_app_log_time=new Date(curr_time.getTime()+app_log_interval);
            store_time(getString(R.string.app_log_timestamp),next_app_log_time);
            //status=RetrieveStatus();
            //System.out.println("status value="+status);
            //Check_and_fire_mood_recorder_me();
        }
        //WriteAlarmFiringTime(5);
        //StartAppLoggingService();
        MyFunction();


        /*if(isTrainingPhase()) {
            // Mood Logging activity
            // TODO: Time REVERTED
            long mood_log_interval = 1000 * parseInt(getResources().getString(R.string.mood_interval));
            // long mood_log_interval = 1 * Integer.parseInt("1");
            mood_log_timestamp = read_time(getString(R.string.mood_log_timestamp));
            mood_log_time = convert_to_date(mood_log_timestamp);
            if (find_time_diff(curr_time, mood_log_time) >= 0) {
                Date next_mood_log_time;
                Check_and_fire_mood_recorder(1);
                next_mood_log_time = new Date(curr_time.getTime() + mood_log_interval);
                store_time(getString(R.string.mood_log_timestamp), next_mood_log_time);
            }
        }
        else {
            long prediction_interval = 1000 * parseInt(getResources().getString(R.string.mood_interval));
            prediction_timestamp = read_time(getString(R.string.prediction_timestamp));
            prediction_time = convert_to_date(prediction_timestamp);
            if (find_time_diff(curr_time, prediction_time) >= 0) {
                Date next_prediction_time;
                Check_and_fire_mood_recorder(2);
                next_prediction_time = new Date(curr_time.getTime() + prediction_interval);
                store_time(getString(R.string.prediction_timestamp), next_prediction_time);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");

            confirm_popup_timestamp = read_time(getString(R.string.confirm_popup_timestamp));
            confirm_popup_time = convert_to_date(confirm_popup_timestamp);

            System.out.println("Validate Predictions... Time Left : "+find_time_diff(curr_time, confirm_popup_time));
            System.out.println("[MasterService]:Current : "+sdf.format(curr_time)+" ___ Next : "+sdf.format(confirm_popup_time));
            if (find_time_diff(curr_time, confirm_popup_time) >= 0) {
                WriteAlarmFiringTime(6);
                Check_and_Fire_ValidatePredictions();
            }
        }*/

        //Upload Service
        /*upload_data_timestamp = read_time(getString(R.string.upload_data_timestamp));
        upload_data_time = convert_to_date(upload_data_timestamp);
        System.out.println("Upload Data... Time Left : "+find_time_diff(curr_time, upload_data_time));
        if (find_time_diff(curr_time, upload_data_time) >= 0) {
            WriteAlarmFiringTime(1);
            StartUploadDataService();
        }*/
    }
    public void MyFunction(){
        String mood_log_timestamp=null;
        Date mood_log_time;
        Date curr_time=new Date();

        // Mood Logging activity
        // TODO: Time REVERTED
        long mood_log_interval = 1000 * parseInt(getResources().getString(R.string.mood_interval));
        // long mood_log_interval = 1 * Integer.parseInt("1");
        mood_log_timestamp = read_time(getString(R.string.mood_log_timestamp));
        mood_log_time = convert_to_date(mood_log_timestamp);
        if (find_time_diff(curr_time, mood_log_time) >= 0) {
            Date next_mood_log_time;

            try {
                Check_and_fire_mood_recorder_me();
            } catch (ParseException e) {
                e.printStackTrace();
            }


            next_mood_log_time = new Date(curr_time.getTime() + mood_log_interval);
            store_time(getString(R.string.mood_log_timestamp), next_mood_log_time);
        }


    }

    private boolean RetrieveStatus() {
        Boolean status=false;
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.applogger_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);
            //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            status = pref.getBoolean("sharedpref_status", status);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    private void Check_and_fire_mood_recorder_me() throws ParseException {
        boolean status=false;
        String CurrentAppName=null,LastAppName=null;
        String last_esm_fire_time=null;

        System.out.println("I am here");
        status=RetrieveStatus();
        System.out.println("status value="+status);

        if(status==true) {
            CurrentAppName = getCurrentAppName();

            System.out.println("Current App : " + CurrentAppName);

            if (!CurrentAppName.isEmpty()) {

                    if (CurrentAppName.equalsIgnoreCase("LockScreen")) {
                    // Screen is locked, do not fire the ESM
                    System.out.println("[MasterService]Screen locked, Event-based ESM not Fired...");
                } else if (CurrentAppName.equalsIgnoreCase("research.sg.edu.edapp")) {
                    // User yet to record emotion, do not fire the ESM
                    System.out.println("[MasterService]User yet to record emotion, Event-based ESM not Fired...");
                } else {
                    last_esm_fire_time = RetrieveESMTime();
                    System.out.println("[MasterService]:" + last_esm_fire_time);
                    if (last_esm_fire_time == null || last_esm_fire_time.trim().equals("null")) {
                        System.out.println("[MasterService]: Going to invoke mood recorder");

                        WriteAlarmFiringTime(3);
                        InvokeMoodRecorderMe();


                            /*SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                            String esm_time = sdf.format(new Date());
                            StoreESMDetail(getApplicationContext(), LastAppName, esm_time);*/
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                        Date last_esm_time = sdf.parse(last_esm_fire_time);
                        Date curr_time = new Date();
                        System.out.println("Current Time=" + convert_to_string(curr_time));
                        System.out.println("last_esm_time is retrieved=" + convert_to_string(last_esm_time));
                        //Log.d(SyncStateContract.Constants.LOG,"Difference since last ESM:"+find_time_diff(curr_time, last_esm_time));
                        if (check_elapsed_time_since_last_probe(curr_time, last_esm_time) >= parseInt(getApplicationContext().getResources().getString(R.string.esm_app_min_window))) {
                            System.out.println("[MasterService]: Going to invoke mood recorder");

                            WriteAlarmFiringTime(3);
                            InvokeMoodRecorderMe();

                                /*String esm_time = sdf.format(new Date());
                                StoreESMDetail(getApplicationContext(), LastAppName, esm_time);*/
                        }
                    }


                }
            }
        }





    }

    public boolean isTrainingPhase() {
        SharedPreferences eda_pref;
        boolean bool = true;

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.sharedpref_pkg), Context.CONTEXT_IGNORE_SECURITY);
            eda_pref = con.getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
            bool = eda_pref.getBoolean(getResources().getString(R.string.sharedpref_training_phase), true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    public void Check_and_Fire_ValidatePredictions() {
        String CurrentAppName=getCurrentAppName();
        if (CurrentAppName.equalsIgnoreCase("LockScreen")) {
            // Screen is locked, do not fire the ESM
            System.out.println("[MasterService]Screen locked, Validate Predictions not Fired...");
        }
        else if (CurrentAppName.equalsIgnoreCase("research.sg.edu.edapp")) {
            // User yet to record emotion, do not fire the ESM
            System.out.println("[MasterService]Main Activity Open, Validate Predictions not Fired...");
        }
        else {
            try {
                SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format));
                Date myDate = new Date();
                Date newDate = new Date(myDate.getTime() - 172800000L); // 2 * 24 * 60 * 60 * 1000

                Uri CONTENT_URI = ConfirmFeaturesProvider.CONTENT_URI;
                ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);

                String where = "date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") >= date(?) and date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") <= date(?)";
                Cursor tCursor = CR.query(CONTENT_URI,
                        new String[]{"DISTINCT " + FeaturesDetails.FeaturesEntry.TIMESTAMP, FeaturesDetails.FeaturesEntry.EMOTION},
                        where, new String[]{format.format(newDate), format.format(myDate)}, null);

                if(tCursor.getCount() != 0) {
                    InvokeValidatePredictions();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    public void InvokeValidatePredictions() {
        Intent openActivity = new Intent("research.sg.edu.edapp.ValidatePredictions");
        openActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openActivity.putExtra("OpenFromMainActivity",0);
        getApplicationContext().startActivity(openActivity);
    }

    public void Check_and_fire_mood_recorder(int x) {
        int esm_min_txt_length;

        String CurrentAppName=null,LastAppName=null;
        String last_esm_fire_time=null;

        String PROVIDER_NAME = "research.sg.edu.edapp.kb.KbContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);

        esm_min_txt_length= parseInt(getApplicationContext().getResources().getString(R.string.esm_min_txt_length));

        ContentProviderClient CR = getApplicationContext().getContentResolver().acquireContentProviderClient(CONTENT_URI);

        try {

            Cursor tCursor = CR.query(CONTENT_URI, null, null, null, null);

            if(tCursor.getCount()>esm_min_txt_length) {

                CurrentAppName=getCurrentAppName();

                tCursor.moveToLast();
                LastAppName=tCursor.getString(1);
                System.out.println("Last App : "+LastAppName+" && Current App : "+CurrentAppName);
                tCursor.close();

                if(CurrentAppName.equalsIgnoreCase(LastAppName)) {
                    System.out.println("[MasterService]:Same app, do not invoke mood recorder");
                }
                else if(x==2) {
                    last_esm_fire_time = RetrieveESMDetail(getApplicationContext());
                    System.out.println("[MasterService]:"+last_esm_fire_time);
                    if (last_esm_fire_time == null || last_esm_fire_time.trim().equals("null")) {
                        System.out.println("[MasterService]: Going to invoke mood recorder");

                        StartPredictionService();

                        SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                        String esm_time = sdf.format(new Date());
                        StoreESMDetail(getApplicationContext(), LastAppName, esm_time);
                    }
                    else {
                        SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                        Date last_esm_time = sdf.parse(last_esm_fire_time);
                        Date curr_time = new Date();
                        //Log.d(SyncStateContract.Constants.LOG,"Difference since last ESM:"+find_time_diff(curr_time, last_esm_time));
                        if (check_elapsed_time_since_last_probe(curr_time, last_esm_time) > parseInt(getApplicationContext().getResources().getString(R.string.esm_app_min_window))) {
                            System.out.println("[MasterService]: Going to invoke mood recorder");
                            //WriteAlarmFiringTime(3);
                            StartPredictionService();

                            //SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.time_format));
                            String esm_time = sdf.format(new Date());
                            StoreESMDetail(getApplicationContext(), LastAppName, esm_time);
                        }
                    }
                }
                else {
                    if (CurrentAppName.equalsIgnoreCase("LockScreen")) {
                        // Screen is locked, do not fire the ESM
                        System.out.println("[MasterService]Screen locked, Event-based ESM not Fired...");
                    }
                    else if (CurrentAppName.equalsIgnoreCase("research.sg.edu.edapp")) {
                        // User yet to record emotion, do not fire the ESM
                        System.out.println("[MasterService]User yet to record emotion, Event-based ESM not Fired...");
                    }
                    else {
                        last_esm_fire_time = RetrieveESMDetail(getApplicationContext());
                        System.out.println("[MasterService]:"+last_esm_fire_time);
                        if (last_esm_fire_time == null || last_esm_fire_time.trim().equals("null")) {
                            System.out.println("[MasterService]: Going to invoke mood recorder");

                            WriteAlarmFiringTime(3);
                            InvokeMoodRecorder(LastAppName);

                            /*SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                            String esm_time = sdf.format(new Date());
                            StoreESMDetail(getApplicationContext(), LastAppName, esm_time);*/
                        }
                        else {
                            SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                            Date last_esm_time = sdf.parse(last_esm_fire_time);
                            Date curr_time = new Date();
                            //Log.d(SyncStateContract.Constants.LOG,"Difference since last ESM:"+find_time_diff(curr_time, last_esm_time));
                            if (check_elapsed_time_since_last_probe(curr_time, last_esm_time) > parseInt(getApplicationContext().getResources().getString(R.string.esm_app_min_window))) {
                                System.out.println("[MasterService]: Going to invoke mood recorder");
                                WriteAlarmFiringTime(3);
                                InvokeMoodRecorder(LastAppName);

                                /*String esm_time = sdf.format(new Date());
                                StoreESMDetail(getApplicationContext(), LastAppName, esm_time);*/
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void InvokeMoodRecorder(String LastAppName) {
        Intent openMainActivity = new Intent("research.sg.edu.edapp.MoodRecorder");
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openMainActivity.putExtra("LastAppName",LastAppName);
        getApplicationContext().startActivity(openMainActivity);
    }
    public void InvokeMoodRecorderMe(){
        Intent openMainActivity = new Intent("research.sg.edu.edapp.MoodRecorder");
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SimpleDateFormat sdf=new SimpleDateFormat(getApplicationContext().getString(R.string.time_format));
        openMainActivity.putExtra("PopUpTimeStamp",sdf.format(new Date()));
        getApplicationContext().startActivity(openMainActivity);
    }

    public void StartAppLoggingService() {
        System.out.println("[MasterService]: AppLoggingService is running");
        WriteAlarmFiringTime(1);
        Intent intent = new Intent(MasterService.this,AppLoggingService.class);
        this.startService(intent);
    }
    public void StartPredictionService() {
        System.out.println("[MasterService]: PredictionService is running");
        WriteAlarmFiringTime(2);
        Intent intent = new Intent(MasterService.this,PredictionService.class);
        this.startService(intent);
    }
    public void StartUploadDataService() {
        System.out.println("[MasterService]: UploadDataService is running");
        //Toast.makeText(this,"Model Building Starts",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MasterService.this,UploadData.class);
        this.startService(intent);
    }

    public void move_file(String file_name) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        File tobeuploadedDir = new File(sdCardRoot, getResources().getString(R.string.oldAlarm_file_path));

        if(!tobeuploadedDir.exists()) {
            tobeuploadedDir.mkdirs();
        }

        File sourceLocation = new File(dataDir, file_name);
        File targetLocation = new File(tobeuploadedDir, file_name);

        if(targetLocation.exists()) {
            targetLocation.delete();
        }

        try {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            if(sourceLocation.exists()) {
                sourceLocation.delete();
            }
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String read_time(String time_variable){
        SharedPreferences eda_pref;
        String time=null;
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.sharedpref_pkg), Context.CONTEXT_IGNORE_SECURITY);
            eda_pref = con.getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
            time = eda_pref.getString(time_variable, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(time==null){
            time = "2000-01-01 00:00:00.000";
        }
        return time;
    }
    public void store_time(String time_variable, Date value) {
        SharedPreferences eda_pref;
        eda_pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor eda_editor =eda_pref.edit();
        eda_editor.putString(time_variable, convert_to_string(value));
        eda_editor.apply();
        eda_editor.commit();
    }

    public Date convert_to_date(String time){

        Date date=null;

        SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format));
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }
    public String convert_to_string(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String date_string = sdf.format(date);
        return date_string;
    }

    public float find_time_diff (Date dt1, Date dt2){

        float diff = dt1.getTime() - dt2.getTime();
        return diff;
    }

    public float check_elapsed_time_since_last_probe (Date dt1, Date dt2) {

        float diff = dt1.getTime() - dt2.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        float diff_in_min = diff / minutesInMilli;

        System.out.println("Time Diff in Minutes:"+diff_in_min);

        return diff_in_min;
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

    public void StoreAlarmFileCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.alarm_file_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }
    public String RetrieveAlarmFileCtr(){
        String ctr="0";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.alarm_file_ctr), "0");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ctr;
    }

    /********************************/
    /* ESM Detail related functions */
    /********************************/

    public void StoreESMDetail(Context context,String esm_app,String esm_time){

        ContentValues values = new ContentValues();
        values.put(EsmDetail.EsmEntry.ESM_APP_NAME, esm_app);
        values.put(EsmDetail.EsmEntry.ESM_TIMESTAMP, esm_time);

        Uri uri = context.getContentResolver().insert(ESMContentProvider.CONTENT_URI, values);
    }
    public String RetrieveESMDetail(Context context) {
        String last_esm_fire_time=null;
        String PROVIDER_NAME = "research.sg.edu.edapp.ESMContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);

        ContentProviderClient CR = context.getContentResolver().acquireContentProviderClient(CONTENT_URI);
        try {
            Cursor eCursor = CR.query(CONTENT_URI, null, null, null, null);

            if(eCursor.getCount()>0) {
                eCursor.moveToLast();
                last_esm_fire_time=eCursor.getString(2);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return last_esm_fire_time;
    }
    public String RetrieveESMTime(){
        SharedPreferences eda_pref;
        String time=null;
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.sharedpref_pkg), Context.CONTEXT_IGNORE_SECURITY);
            eda_pref = con.getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
            time = eda_pref.getString("sharedpref_ESM", null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        /*if(time==null){
            time = "2000-01-01 00:00:00.000";
        }*/
        return time;

    }

    public void StoreStatus(boolean flag) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor = pref.edit();
        log_editor.putBoolean("sharedpref_status", flag);
        log_editor.apply();
        log_editor.commit();
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
    /***************************************/
    /* End of ESM Detail related functions */
    /***************************************/
}
