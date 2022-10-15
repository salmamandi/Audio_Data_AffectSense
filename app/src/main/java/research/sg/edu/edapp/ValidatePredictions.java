package research.sg.edu.edapp;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;

import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import research.sg.edu.edapp.FinalClasses.AppCategory;
import research.sg.edu.edapp.FinalClasses.FeaturesDetails;
import research.sg.edu.edapp.FinalClasses.StatsDetails;

public class ValidatePredictions extends AppCompatActivity {

    String[] timeStampList;
    String[] emotionList;
    SimpleDateFormat format;
    SimpleDateFormat sdf;

    int OpenFromMainActivity=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valid_predictions);

        Bundle extras = getIntent().getExtras();
        OpenFromMainActivity = extras.getInt("OpenFromMainActivity");

        format = new SimpleDateFormat(getResources().getString(R.string.time_format));
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");

        final ListView lvItems = (ListView) findViewById(R.id.myListView);

        Date myDate = new Date();
        Date newDate = new Date(myDate.getTime() - 172800000L); // 2 * 24 * 60 * 60 * 1000

        Uri CONTENT_URI;

        CONTENT_URI = ConfirmFeaturesProvider.CONTENT_URI;
        ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);

        String where = "date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") >= date(?) and date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") <= date(?)";
        /*Cursor tCursor = CR.query(CONTENT_URI,
                  new String[] {" "+ FeaturesDetails.FeaturesEntry.TIMESTAMP, FeaturesDetails.FeaturesEntry.EMOTION},
               null, null, FeaturesDetails.FeaturesEntry.TIMESTAMP + " DESC");*/
        Cursor tCursor = null;
        try {
            tCursor = CR.query(CONTENT_URI,
                    new String[] {"DISTINCT "+ FeaturesDetails.FeaturesEntry.TIMESTAMP, FeaturesDetails.FeaturesEntry.EMOTION},
                    where, new String[]{format.format(newDate), format.format(myDate)}, null);

            tCursor.moveToFirst();
            timeStampList = new String[tCursor.getCount()];
            emotionList = new String[tCursor.getCount()];
            int i = 0;

            while (!tCursor.isAfterLast()) {
                Date date = format.parse(tCursor.getString(0));
                timeStampList[i] = sdf.format(date);
                emotionList[i] = getMood(tCursor.getString(1));
                tCursor.moveToNext();
                i++;
            }
            tCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            TopExceptionHandler(e);
        }
        final CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), timeStampList, emotionList);
        lvItems.setAdapter(customAdapter);

        Button btn = (Button) findViewById(R.id.button10);
        final Button btn2 = (Button) findViewById(R.id.button12);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "";
                // get the value of selected answers from custom adapter
                for (int i = 0; i < CustomAdapter.selectedAnswers.size(); i++) {
                    message = message + "\n" + (i + 1) + " " + CustomAdapter.selectedAnswers.get(i);
                }
                System.out.println(message);
                // display the message on screen with the help of Toast.
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                SaveFeatures();

                setTime=1;
                long confirm_popup_interval = 1000 * 60 * 60 * Integer.parseInt(getResources().getString(R.string.confirm_popup_interval));
                Date curr_time=new Date();
                Date next_popup_time = new Date(curr_time.getTime() + confirm_popup_interval);
                System.out.println("[ValidatePredictions]:Current : "+sdf.format(curr_time)+" ___ Next : "+sdf.format(next_popup_time));
                store_time(getString(R.string.confirm_popup_timestamp), next_popup_time);

                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(OpenFromMainActivity == 0) {
                    setTime=1;
                    long confirm_popup_interval = 1000 * 60 * 60 * Integer.parseInt(getResources().getString(R.string.confirm_popup_later_interval));
                    Date curr_time = new Date();
                    Date next_popup_time = new Date(curr_time.getTime() + confirm_popup_interval);
                    store_time(getString(R.string.confirm_popup_timestamp), next_popup_time);
                }
                finish();
            }
        });
    }

    int backpress=0;
    int setTime=0;
    @Override
    public void onBackPressed() {
        if (backpress==1) {
            super.onBackPressed();
            if(OpenFromMainActivity == 0) {
                setTime=1;
                long confirm_popup_interval = 1000 * 60 * 60 * Integer.parseInt(getResources().getString(R.string.confirm_popup_later_interval));
                Date curr_time = new Date();
                Date next_popup_time = new Date(curr_time.getTime() + confirm_popup_interval);
                store_time(getString(R.string.confirm_popup_timestamp), next_popup_time);
            }
            finish();
            return;
        }

        backpress = (backpress + 1);
        final Toast toast = Toast.makeText(getApplicationContext(), "Press Back again to Exit",Toast.LENGTH_SHORT);
        toast.show();

        new CountDownTimer(1000, 500) {
            public void onTick(long millisUntilFinished) {toast.show();}
            public void onFinish() {toast.cancel();}
        }.start();
    }

    public void onStop() {
        super.onStop();
        if(setTime == 0 && OpenFromMainActivity == 0) {
            setTime=1;
            long confirm_popup_interval = 1000 * 60 * 60 * Integer.parseInt(getResources().getString(R.string.confirm_popup_later_interval));
            Date curr_time = new Date();
            Date next_popup_time = new Date(curr_time.getTime() + confirm_popup_interval);
            store_time(getString(R.string.confirm_popup_timestamp), next_popup_time);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(setTime == 0 && OpenFromMainActivity == 0) {
            setTime=1;
            long confirm_popup_interval = 1000 * 60 * 60 * Integer.parseInt(getResources().getString(R.string.confirm_popup_later_interval));
            Date curr_time = new Date();
            Date next_popup_time = new Date(curr_time.getTime() + confirm_popup_interval);
            store_time(getString(R.string.confirm_popup_timestamp), next_popup_time);
        }
    }

    private void SaveFeatures() {
        System.out.println("Save Features");
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.ground_truth_features_file_path));

        if(!dataDir.exists())
            dataDir.mkdirs();

        //Writing Features to a File
        FileOutputStream fos;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei_no = telephonyManager.getDeviceId();

        String feature_file_ctr = RetrieveFeatureFileCtr(getResources().getString(R.string.ground_truth_feature_file_ctr));
        String feature_file_name = imei_no + "_" + feature_file_ctr + getResources().getString(R.string.features_file_postfix);
        File featureDataFile = new File(dataDir, feature_file_name);

        String fileData = "";

        if(!featureDataFile.exists()) {
            // TODO MINE F ADDED
            // fileData = "SessionId,MSI,RMSI,SessionLength,BackSpacePercentage,SplCharPercentage,SessionDuration,AppName,RecordTime,MoodState,ActualState\n";
            fileData = "SessionId,MSI,RMSI,SessionLength,BackSpacePercentage,SplCharPercentage,SessionDuration,AppName,RecordTime,MoodState,ActualState,Pressure,Velocity,SwipeDuration\n";
            byte[] file_data = fileData.getBytes();

            try {
                fos = new FileOutputStream(featureDataFile, true);
                fos.write(file_data);
                fos.close();
            } catch (IOException e) {
                //Log.e("Exception", "File write failed: " + e.toString());
            }
        }

        try {
            Date myDate = new Date();
            Date newDate = new Date(myDate.getTime() - 172800000L); // 2 * 24 * 60 * 60 * 1000

            Uri CONTENT_URI = ConfirmFeaturesProvider.CONTENT_URI;
            ContentProviderClient CR = getApplicationContext().getContentResolver().acquireContentProviderClient(CONTENT_URI);
            Cursor tCursor = null;


            tCursor = CR.query(CONTENT_URI, null,
                    "date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") >= date(?) and date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") <= date(?)",
                    new String[]{format.format(newDate), format.format(myDate)}, null);

            tCursor.moveToFirst();

            int i = 0;
            while (!tCursor.isAfterLast()) {
                Date date = format.parse(tCursor.getString(9));
                String timeStamp = sdf.format(date);
                while(i<CustomAdapter.selectedAnswers.size() && CustomAdapter.selectedAnswers.get(i).equals("No Response")) {
                    while(!tCursor.isAfterLast() && timeStamp.equals(timeStampList[i])) {
                        tCursor.moveToNext();
                        if(!tCursor.isAfterLast()) {
                            date = format.parse(tCursor.getString(9));
                            timeStamp = sdf.format(date);
                        }
                    }
                    i++;
                }
                if(i<CustomAdapter.selectedAnswers.size() && timeStamp.equals(timeStampList[i])) {
                    //System.out.println(timeStamp+" "+CustomAdapter.selectedAnswers.get(i));
                    while(!tCursor.isAfterLast() && timeStamp.equals(timeStampList[i])) {
                        if(!tCursor.isAfterLast()) {
                            date = format.parse(tCursor.getString(9));
                            timeStamp = sdf.format(date);
                            System.out.println(timeStamp + " " + CustomAdapter.selectedAnswers.get(i));

                            // TODO MINE F fileData ADDED
                            fileData = tCursor.getString(1) + "," + tCursor.getString(2) + "," + tCursor.getString(3) + "," + tCursor.getString(4)
                                    + "," + tCursor.getString(5) + "," + tCursor.getString(6) + "," + tCursor.getString(7) + "," +
                                    tCursor.getString(8) + "," + tCursor.getString(9) + "," + tCursor.getString(10)+ "," + CustomAdapter.selectedAnswers.get(i) +
                                    tCursor.getString(11) + "," + tCursor.getString(12)+ ","+ tCursor.getString(13)+ ","  + "\n";

                            System.out.println(fileData);

                            byte[] file_data = fileData.getBytes();

                            try {
                                fos = new FileOutputStream(featureDataFile, true);
                                fos.write(file_data);
                                fos.close();
                            } catch (IOException e) {
                                //Log.e("Exception", "File write failed: " + e.toString());
                            }

                            //Storing Train Stats
                            StoreStatsDetail(getApplicationContext(), tCursor.getString(8), tCursor.getString(9), CustomAdapter.selectedAnswers.get(i));
                        }
                        tCursor.moveToNext();
                    }
                    String where = "date("+FeaturesDetails.FeaturesEntry.TIMESTAMP + ") = date(?)";
                    System.out.println(getContentResolver().delete(ConfirmFeaturesProvider.CONTENT_URI, where, new String[] {format.format(sdf.parse(timeStamp))}));
                    i++;
                }
            }
            tCursor.close();

            String where = "date("+ FeaturesDetails.FeaturesEntry.TIMESTAMP + ") < date(?)";
            System.out.println(getContentResolver().delete(ConfirmFeaturesProvider.CONTENT_URI, where, new String[] {format.format(newDate)}));

            //Maintain Features File Size & CTR
            int feature_file_size = Integer.parseInt(String.valueOf(featureDataFile.length() / 1024));
            int feature_file_size_threshold = Integer.parseInt(getResources().getString(R.string.feature_file_size_limit));
            if (feature_file_size > feature_file_size_threshold) {
                int ctr = Integer.parseInt(feature_file_ctr) + 1;
                feature_file_ctr = String.valueOf(ctr);
                feature_file_ctr = String.format("%06d", Integer.parseInt(feature_file_ctr));
                StoreFeatureFileCtr(getResources().getString(R.string.ground_truth_feature_file_ctr),feature_file_ctr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            TopExceptionHandler(e);
        }
    }

    public void store_time(String time_variable, Date value) {
        SharedPreferences eda_pref;
        eda_pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor eda_editor =eda_pref.edit();
        eda_editor.putString(time_variable, convert_to_string(value));
        eda_editor.apply();
        eda_editor.commit();
    }
    public String convert_to_string(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String date_string = sdf.format(date);
        return date_string;
    }

    public void StoreFeatureFileCtr(String ctr_variable, String ctr){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(ctr_variable, ctr);
        log_editor.apply();
        log_editor.commit();
    }
    public String RetrieveFeatureFileCtr(String ctr_variable){
        String ctr="000000";
        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(ctr_variable, "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    //Store Stats to Content Provider
    public void StoreStatsDetail(Context context,String appName,String recordTime, String emotion){

        ContentValues values = new ContentValues();
        System.out.println("Stats Detail -->> " + appName + "_" + recordTime + "_" + findAppCat(appName) + "_" + isWeekend(recordTime) + "_" +
                findDaySession(recordTime) + "_" + emotion);
        values.put(StatsDetails.StatsEntry.APP_NAME, appName);
        values.put(StatsDetails.StatsEntry.TIMESTAMP, recordTime);
        values.put(StatsDetails.StatsEntry.APP_CAT, findAppCat(appName));
        values.put(StatsDetails.StatsEntry.WEEKEND, isWeekend(recordTime));
        values.put(StatsDetails.StatsEntry.DAYSESSION, findDaySession(recordTime));
        values.put(StatsDetails.StatsEntry.EMOTION, getEmoId(emotion));
        Uri uri = context.getContentResolver().insert(StatsProvider.CONTENT_URI, values);
    }
    public int getEmoId(String emotion) {
        switch(emotion) {
            case "Sad": return -2;
            case "Happy": return 2;
            case "Stressed": return 1;
            case "Relaxed": return 0;
        }
        return 3;
    }
    public int findAppCat (String appName) {
        if(AppCategory.email.contains(appName))
            return 1;
        if(AppCategory.im.contains(appName))
            return 2;
        if(AppCategory.social.contains(appName))
            return 3;
        if(AppCategory.entertainment.contains(appName))
            return 4;
        if(AppCategory.surfing.contains(appName))
            return 5;
        return 0;
    }
    public int isWeekend (String recordTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = null;
        try {
            date = sdf.parse(recordTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int temp = calendar.get(Calendar.DAY_OF_WEEK);
        if(temp == 1 || temp == 7)
            return 1;
        return 0;
    }
    public int findDaySession (String recordTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        int hours = 0;
        try {
            Date date = sdf.parse(recordTime);
            SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
            String sTime=sdf1.format(date);
            hours = Integer.parseInt(sTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int tm = 3,ta = 11,te = 16,tn = 21;

        if(hours>=tm && hours<ta) {
            return 1;
        }
        if(hours>=ta && hours<te) {
            return 2;
        }
        if(hours>=te && hours<tn) {
            return 3;
        }
        else {
            return 4;
        }
    }

    public String getMood(String moodInt) {
        switch(moodInt) {
            case "-2": return "Sad";
            case "2": return "Happy";
            case "1": return "Stressed";
            case "0": return "Relaxed";
        }
        return "";
    }

    public void TopExceptionHandler(Exception e){
        System.out.println("Exception Handler Starts");
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        report += sdf.format(date);
        report += "--------- Stack Trace ---------\n\n";
        for (int i=0; i<arr.length; i++) {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File logDir = new File(sdCardRoot, "/AffectSense/DataFiles/Log/");
        String ExceptionTraceFileName = "ExceptionTrace.log";
        File ExceptionTraceFile = new File(logDir, ExceptionTraceFileName);
        System.out.println("Exception Handler Writing Into File");
        System.out.println(ExceptionTraceFile);
        try {
            FileOutputStream trace = new FileOutputStream(ExceptionTraceFile, true);
            trace.write(report.getBytes());
            trace.close();
        } catch(IOException ioe) {
        }
        System.out.println("Exception Handler Exits");
    }

}
