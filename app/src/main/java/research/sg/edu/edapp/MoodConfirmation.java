package research.sg.edu.edapp;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import research.sg.edu.edapp.FinalClasses.AppCategory;
import research.sg.edu.edapp.FinalClasses.StatsDetails;
import research.sg.edu.edapp.ModelClasses.FeatureDataFileClass;

public class MoodConfirmation extends AppCompatActivity {

    ArrayList<FeatureDataFileClass> featureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_confirmation);
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); */

        final String mood = getSessionMood();

        TextView tv = (TextView) findViewById(R.id.textView5);
        ImageView imgV = (ImageView) findViewById(R.id.imageView);

        tv.setText("You are predicted as "+mood+" now");

        if(mood.equals("Happy")){
            imgV.setImageResource(R.drawable.happy);
        }
        else if(mood.equals("Sad")){
            imgV.setImageResource(R.drawable.sad);
        }
        else if(mood.equals("Stressed")){
            imgV.setImageResource(R.drawable.stressed);
        }
        else if(mood.equals("Relaxed")){
            imgV.setImageResource(R.drawable.relaxed);
        }

        Button agree = (Button) findViewById(R.id.button4);
        Button nAgree = (Button) findViewById(R.id.button5);

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveFeatures(mood);
                getContentResolver().delete(ConfirmFeaturesProvider.CONTENT_URI, null, null);
                finish();
            }
        });

        nAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoodConfirmation.this, MoodRecordPopUp.class);
                startActivityForResult(intent,2);
            }
        });

    }

    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2) {
            String mood = data.getStringExtra("mood");
            if(!(mood.equals("") || mood.equals("No Response"))) {
                SaveFeatures(mood);
                getContentResolver().delete(ConfirmFeaturesProvider.CONTENT_URI, null, null);
            }
            finish();
        }
    }

    private String getSessionMood() {
        String mood="";
        featureList = new ArrayList<FeatureDataFileClass>();
        FeatureDataFileClass featureEntry = null;

        String PROVIDER_NAME = "research.sg.edu.edapp.ConfirmFeaturesProvider";
        String URL = "content://" + PROVIDER_NAME + "/EmoConfirmFeatures";
        Uri CONTENT_URI = Uri.parse(URL);
        ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);

        int happyCount=0,sadCount=0,stressedCount=0,relaxedCount=0;
        List<String> featureEmotions = new ArrayList<String>();

        try {
            Cursor tCursor = CR.query(CONTENT_URI, null, null, null, null);
            tCursor.moveToFirst();
            while (!tCursor.isAfterLast()) {
                // TODO: MINE get features ADDED. CHECK IF RECEIVING FEATURES PROPERLY
                System.out.println( tCursor.getString(1)    //sessionId
                        + "," + tCursor.getString(2)        //msi
                        + "," + tCursor.getString(3)        //rmsi
                        + "," + tCursor.getString(4)        //sessionLength
                        + "," + tCursor.getString(5)        //backspace
                        + "," + tCursor.getString(6)        //specialchar
                        + "," + tCursor.getString(7)        //sessionDuration
                        + "," + tCursor.getString(8)        //appName
                        + "," + tCursor.getString(9)        //recordTime
                        + "," + tCursor.getString(10)       //Emotion

                        + "," + tCursor.getString(11)       // mpressure
                        + "," + tCursor.getString(12)       // mvelocity
                        + "," + tCursor.getString(13));     // mswipeduration

                featureEntry = new FeatureDataFileClass();

                featureEntry.setSessionId(tCursor.getString(1));
                featureEntry.setMsi(tCursor.getString(2));
                featureEntry.setRmsi(tCursor.getString(3));
                featureEntry.setSessionLength(tCursor.getString(4));
                featureEntry.setBackspace_percentage(tCursor.getString(5));
                featureEntry.setSplchar_percentage(tCursor.getString(6));
                featureEntry.setSessionDuration(tCursor.getString(7));
                featureEntry.setAppName(tCursor.getString(8) );
                featureEntry.setRecordTime(tCursor.getString(9));
                featureEntry.setMoodState(getMood(tCursor.getString(10)));
                featureEmotions.add(getMood(tCursor.getString(10)));

                featureEntry.setmpressure(tCursor.getString(11) );
                featureEntry.setmvelocity(tCursor.getString(12));
                featureEntry.setmswipeDuration(tCursor.getString(13));

                if(getMood(tCursor.getString(10)).equals("Happy")) {
                    happyCount++;
                }
                else if(getMood(tCursor.getString(10)).equals("Sad")) {
                    sadCount++;
                }
                else if(getMood(tCursor.getString(10)).equals("Stressed")) {
                    stressedCount++;
                }
                else if(getMood(tCursor.getString(10)).equals("Relaxed")) {
                    relaxedCount++;
                }
                tCursor.moveToNext();
            }
            tCursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return findMood(happyCount,sadCount,stressedCount,relaxedCount,featureEmotions);
    }
    public String findMood(int a, int b, int c, int d, List<String> featureEmotions) {
        String[] mood = new String[4];
        if (a >= b && a >= c && a >= d)
            mood[0]="Happy";
        else if (b >= a && b >= c && b >= d)
            mood[1]="Sad";
        else if (c >= a && c >= b && c >= d)
            mood[2]="Stressed";
        else if (d >= b && d >= c && d >= a)
            mood[3]="Relaxed";

        for(int i=featureEmotions.size()-1;i>=0;i--)
            for(int j=0;j<4;j++)
                if(featureEmotions.get(i).equals(mood[j])) {
                    return mood[j];
                }
        return "";
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

    public void SaveFeatures(String mood) {
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
            fileData = "SessionId,MSI,RMSI,SessionLength,BackSpacePercentage,SplCharPercentage,SessionDuration,AppName,RecordTime,MoodState,ActualState,mPressure,mVelocity,mSwipeDuration\n";
            byte[] file_data = fileData.getBytes();

            try {
                fos = new FileOutputStream(featureDataFile, true);
                fos.write(file_data);
                fos.close();
            } catch (IOException e) {
                //Log.e("Exception", "File write failed: " + e.toString());
            }
        }

        FeatureDataFileClass featureEntry = null;
        for (int i = 0; i < featureList.size(); i++) {
            featureEntry = featureList.get(i);
            // TODO MINE File storage filetype ADDED
            fileData = featureEntry.getSessionId() + "," + featureEntry.getMsi() + "," + featureEntry.getRmsi() + "," + featureEntry.getSessionLength() + "," +
                    featureEntry.getBackspace_percentage() + "," + featureEntry.getSplchar_percentage() + "," + featureEntry.getSessionDuration() + "," +
                    featureEntry.getAppName() + "," + featureEntry.getRecordTime() + "," + featureEntry.getMoodState()+ "," + mood +
                    "," + featureEntry.getmpressure() + "," + featureEntry.getmvelocity() + "," + featureEntry.getmswipeDuration() + "\n";

            System.out.println("Filedata: "+ fileData);
            byte[] file_data = fileData.getBytes();

            try {
                fos = new FileOutputStream(featureDataFile, true);
                fos.write(file_data);
                fos.close();
            } catch (IOException e) {
                //Log.e("Exception", "File write failed: " + e.toString());
            }

            //Storing Train Stats
            StoreStatsDetail(getApplicationContext(), featureEntry.getAppName(), featureEntry.getRecordTime(), mood);
        }

        //Maintain Features File Size & CTR
        int feature_file_size = Integer.parseInt(String.valueOf(featureDataFile.length() / 1024));
        int feature_file_size_threshold = Integer.parseInt(getResources().getString(R.string.feature_file_size_limit));
        if (feature_file_size > feature_file_size_threshold) {
            int ctr = Integer.parseInt(feature_file_ctr) + 1;
            feature_file_ctr = String.valueOf(ctr);
            feature_file_ctr = String.format("%06d", Integer.parseInt(feature_file_ctr));
            StoreFeatureFileCtr(getResources().getString(R.string.ground_truth_feature_file_ctr),feature_file_ctr);
        }
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
            System.out.println("Exception Handler Starts");
            StackTraceElement[] arr = e.getStackTrace();
            String report = e.toString()+"\n\n";
            report += "--------- Stack Trace ---------\n\n";
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
            report += "-------------------------------\n\n";

// If the exception was thrown in a background thread inside
// AsyncTask, then the actual exception can be found with getCause
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
// ...
            }
            System.out.println("Exception Handler Exits");
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
            System.out.println("Exception Handler Starts");
            StackTraceElement[] arr = e.getStackTrace();
            String report = e.toString()+"\n\n";
            report += "--------- Stack Trace ---------\n\n";
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
            report += "-------------------------------\n\n";

// If the exception was thrown in a background thread inside
// AsyncTask, then the actual exception can be found with getCause
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
// ...
            }
            System.out.println("Exception Handler Exits");
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

}
