package research.sg.edu.edapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import research.sg.edu.edapp.FinalClasses.AppCategory;
import research.sg.edu.edapp.FinalClasses.FeaturesDetails;
import research.sg.edu.edapp.FinalClasses.StatsDetails;

public class PerformRegistration extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    String country_name=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_registration);
        Spinner spinner=findViewById(R.id.spinner_search);

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.Country_list,android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        addListenerOnButton();
    }

    public void addListenerOnButton() {

        Button Registerbtn, Cancelbtn;

        Registerbtn = (Button) findViewById(R.id.button1);
        Cancelbtn = (Button) findViewById(R.id.button2);


        Registerbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.button1) {

                    final EditText et_profession,et_city;

                    RadioGroup rggender, rgage;
                    RadioButton radioAgeButton, radioGenderButton;



                    et_profession=(EditText)findViewById(R.id.Profession);
                    et_city=(EditText)findViewById(R.id.City);




                    et_profession.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                et_profession.clearFocus();
                                et_profession.setCursorVisible(false);
                                et_city.requestFocus();
                                et_city.setCursorVisible(true);
                                return true;
                            }
                            return false;
                        }
                    });




                    et_city.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                et_city.clearFocus();
                                et_city.setCursorVisible(false);
                                return true;
                            }
                            return false;
                        }
                    });

                    rggender = (RadioGroup) findViewById(R.id.radiogenderGroup);
                    rgage = (RadioGroup) findViewById(R.id.radioageGroup);


                    int selectedgender = rggender.getCheckedRadioButtonId();
                    int selectedage = rgage.getCheckedRadioButtonId();


                    String profession=null,city="default";
                    String gender = "Unknown";
                    String age = "Unknown";


                    if(selectedgender >=0) {
                        radioGenderButton = (RadioButton) findViewById(selectedgender);
                        gender=(String)radioGenderButton.getText();
                    }

                    if(selectedage >=0 ) {
                        radioAgeButton = (RadioButton) findViewById(selectedage);
                        age=(String)radioAgeButton.getText();
                    }



                    profession=et_profession.getText().toString();


                    city=et_city.getText().toString();

                    //if(city.isEmpty())
                        //city="None";



                    //StoreUserDetails(name,email);

                    if(gender.isEmpty()||age.isEmpty()||city.isEmpty()){
                        Toast.makeText(PerformRegistration.this, "Please enter your gender,age and email-id", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        StoreCityName(city);
                        StoreRegistrationDetails(gender, age,profession,country_name,city);
                        StoreTrainingFlag(true);
                        StoreRunningStatus(true);

                        //RefillOldData();
                        //NotifyMe();
                        finish();
                        //StartMasterService();
                    }
                }
            }
        });

        Cancelbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button2) {
                    finish();
                    StartMainActivity();
                }
            }
        });
    }

    private void StoreCityName(String city) {
        SharedPreferences npreferences=getApplicationContext().getSharedPreferences("FileName",Context.MODE_PRIVATE);
        SharedPreferences.Editor fileeditor=npreferences.edit();
        fileeditor.putString("email",city);
        fileeditor.apply();
        fileeditor.commit();
    }

    /*public void RefillOldData() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File featureDir = new File(sdCardRoot, getString(R.string.features_file_path));
        File filefolder = new File(String.valueOf(featureDir));
        System.out.println("Test Directory Prints " + featureDir);
        if (filefolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        }) != null) {
            final File[] files = filefolder.listFiles();
            Arrays.sort(files);

            for (File file : files) {
                try {
                    InputStream instream;
                    instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);

                        String line[];
                        String temp = buffreader.readLine();
                        while ((temp = buffreader.readLine()) != null) {
                            line = temp.split(",");
                            StoreStatsDetail(getApplicationContext(), line[7], line[8], line[9]);
                            StoreFeatures(getApplicationContext(), line[0], line[1], line[2], line[3], line[4], line[5], line[6], line[7], line[8], line[9]);
                        }
                        instream.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        Toast.makeText(PerformRegistration.this, "Fill DataBase Complete", Toast.LENGTH_SHORT).show();
    }*/
    /*public void StoreFeatures(Context context,String sessionId, String msi, String rmsi, String sessionLength, String backSpacePer, String splCharPer,
                              String sessionDuration,String appName,String recordTime, String emotion) {
        ContentValues values = new ContentValues();
        values.put(FeaturesDetails.FeaturesEntry.SESSIONID, Integer.parseInt(sessionId));
        values.put(FeaturesDetails.FeaturesEntry.MSI, Float.parseFloat(msi));
        values.put(FeaturesDetails.FeaturesEntry.RMSI, Float.parseFloat(rmsi));
        values.put(FeaturesDetails.FeaturesEntry.SESSIONLEN, Integer.parseInt(sessionLength));
        values.put(FeaturesDetails.FeaturesEntry.BACKSPACEPER, Float.parseFloat(backSpacePer));
        values.put(FeaturesDetails.FeaturesEntry.SPLCHARPER, Float.parseFloat(splCharPer));
        values.put(FeaturesDetails.FeaturesEntry.SESSIONDUR, Float.parseFloat(sessionDuration));
        values.put(FeaturesDetails.FeaturesEntry.APP_NAME, appName);
        values.put(FeaturesDetails.FeaturesEntry.TIMESTAMP, recordTime);
        values.put(FeaturesDetails.FeaturesEntry.EMOTION, getEmoId(emotion));
        Uri uri = context.getContentResolver().insert(FeaturesProvider.CONTENT_URI, values);
    }
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
    }*/
    /*public int getEmoId(String emotion) {
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
            return 3;
        if(AppCategory.social.contains(appName))
            return 4;
        if(AppCategory.entertainment.contains(appName))
            return 5;
        if(AppCategory.surfing.contains(appName))
            return 6;
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
        Date date = null;
        Date tm = null,ta = null,te = null,tn = null;
        try {
            tm = sdf.parse("2016-11-26 03:00:00.000");
            ta = sdf.parse("2016-11-21 11:00:00.000");
            te = sdf.parse("2016-12-27 16:00:00.000");
            tn = sdf.parse("2016-11-25 21:00:00.000");
            date = sdf.parse(recordTime);
            // date = sdf.parse(sdf1.format(date));

            if(timeIsBefore(date,ta) && !timeIsBefore(date,tm)) {
                return 1;
            }
            if(timeIsBefore(date,te) && !timeIsBefore(date,ta)) {
                return 2;
            }
            if(timeIsBefore(date,tn) && !timeIsBefore(date,te)) {
                return 3;
            }
            else {
                return 4;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 5;
    }*/
    /*public static boolean timeIsBefore(Date d1, Date d2) {
        DateFormat f = new SimpleDateFormat("HH:mm:ss");
        return f.format(d1).compareTo(f.format(d2)) < 0;
    }*/

    public void StoreTrainingFlag(boolean flag) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor stat_editor =pref.edit();
        stat_editor.putBoolean(getResources().getString(R.string.sharedpref_training_phase), flag);
        stat_editor.apply();
        stat_editor.commit();
    }

    public void StoreUserDetails(String name, String email){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.user_details_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.user_name), name);
        log_editor.putString(getResources().getString(R.string.user_email_id), email);
        log_editor.apply();
        log_editor.commit();
    }

    public void StoreRegistrationDetails(String gender, String age,String profession,String country,String city){

        String imei_no,version;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //imei_no = telephonyManager.getDeviceId();

        version = Build.VERSION.RELEASE;

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        //File dataDir=new File(getExternalFilesDir(null),"/AffectSense");
        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String registration_file_name = getResources().getString(R.string.registration_file_postfix);

        String registration_dtls=gender+","+age+","+profession+","+country+","+city+","+currentDateandTime+"\n";
        byte[] registration_data = registration_dtls.getBytes();

        File registration_file = new File(dataDir, registration_file_name);
        try {

            FileOutputStream fos;
            fos = new FileOutputStream(registration_file,true);
            fos.write(registration_data);
            fos.close();

            StoreRegistrationFlag(true);
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }

        //move_file(registration_file_name);
    }

    public void StoreRegistrationFlag(boolean flag) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor stat_editor =pref.edit();
        stat_editor.putBoolean(getResources().getString(R.string.sharedpref_registration_flag), flag);
        stat_editor.apply();
        stat_editor.commit();
    }
    public void NotifyMe(){
        Intent intent=new Intent(this,SendNotification.class);
        startService(intent);
    }

    public void StoreRunningStatus(boolean flag){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putBoolean(getResources().getString(R.string.sharedpref_running_status), flag);
        run_editor.apply();
        run_editor.commit();
    }

    public void StartMainActivity() {

        Intent intent = new Intent(PerformRegistration.this, MainActivity.class);
        startActivity(intent);
    }

    public void StartMasterService() {

        Intent intent = new Intent(PerformRegistration.this,MasterService.class);
        this.startService(intent);
    }

    @Override
    public void onBackPressed() {
        StoreRunningStatus(false);
        finish();
        StartMainActivity();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        String item=parent.getItemAtPosition(i).toString();
        country_name=item;


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
