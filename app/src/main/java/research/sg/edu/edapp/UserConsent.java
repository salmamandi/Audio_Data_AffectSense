package research.sg.edu.edapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;


//import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dropbox.core.android.Auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class UserConsent extends AppCompatActivity {

    Button Consent_btn;

    public static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1;
    public static final int CAMERA_PERMISSION=111;
    public static final int PHONE_STATE_PERMISSION=100;
    public static final int REQUEST_CODE_MULTIPLE_PERMISSIONS=123;
    public static final int REQUEST_AUDIO_PERMISSION_CODE=101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_consent);
        final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        Consent_btn=(Button)findViewById(R.id.usr_ok);
        Consent_btn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.usr_ok) {
                    //String id = Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
                    //System.out.println("[KeyboardActivity]: Default IME"+id);
                    String packageName =getApplicationContext().getPackageName();
                    // uncomment below when need to track application usage in phone
                    //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !hasPermission()){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                        //Toast.makeText(UserConsent.this, "Please Enable Usage Access Permissions", Toast.LENGTH_SHORT).show();
                        // uncomment below lines when need to track application usage in phone
                        //startActivityForResult(
                                //new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                                //MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
                        if(ContextCompat.checkSelfPermission(UserConsent.this,Manifest.permission.READ_PHONE_STATE)+ ContextCompat.checkSelfPermission(UserConsent.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED ) {
                            //ActivityCompat.requestPermissions(UserConsent.this,new String[]{Manifest.permission.READ_PHONE_STATE},PHONE_STATE_PERMISSION);
                            //ActivityCompat.requestPermissions(UserConsent.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
                            //requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.CAMERA},REQUEST_CODE_MULTIPLE_PERMISSIONS);
                            if(ContextCompat.checkSelfPermission(UserConsent.this, RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
                                //requestPermissions(new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE},REQUEST_AUDIO_PERMISSION_CODE);
                                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO, WRITE_EXTERNAL_STORAGE},REQUEST_CODE_MULTIPLE_PERMISSIONS);

                        }

                        }
                        //if(ContextCompat.checkSelfPermission(UserConsent.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                            //ActivityCompat.requestPermissions(UserConsent.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
                        //}
                        if(!pm.isIgnoringBatteryOptimizations(packageName)){
                            Intent intent = new Intent();
                            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("package:" + packageName));
                            System.out.println("optimized section");
                            startActivity(intent);
                        }

                    }

                    /*else if(!id.contains("research.sg.edu.edapp")) {
                        Toast.makeText(UserConsent.this, "Please Enable & Make 'AffectSense Keyboard' as Default", Toast.LENGTH_SHORT).show();
                        ShowKeyBoards();
                    }*/
                    //else {
                    Consent_btn.setEnabled(false);
                    Consent_btn.setVisibility(View.INVISIBLE);

                    //RegisterUser();}

                    //RegisterUser();


                    LoginDropbox();
                    //StartServices();
                    finish();


                }



        });
        //StartServices();
        //finish();
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean hasPermission() {
            try {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getApplicationContext().getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
                return (mode == AppOpsManager.MODE_ALLOWED);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CODE_MULTIPLE_PERMISSIONS:{
                Map<String,Integer> perms=new HashMap<String, Integer>();
                perms.put(Manifest.permission.READ_PHONE_STATE,PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO,PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE,PackageManager.PERMISSION_GRANTED);
                for(int i=0; i<permissions.length;i++)
                    perms.put(permissions[i],grantResults[i]);
                if(perms.get(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED ){
                  System.out.println("All permissions Granted");
                }
                else{
                    System.out.println("Some permission is denied");
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        //if(requestCode==CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //System.out.println("Camera Permission Granted");
        //}
    }

    public void getAccessToken() {
        System.out.println("i am in getaccesstoken");
        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            System.out.println("I got accesstoken");
            SharedPreferences prefs = getSharedPreferences("com.example.valdio.dropboxintegration", Context.MODE_PRIVATE);
            prefs.edit().putString("access-token", accessToken).apply();

            //Proceed to MainActivity
            //Intent intent = new Intent(UserConsent.this, MainActivity.class);
            //startActivity(intent);
        }
        System.out.println("I didn't get accesstoken");
    }


    public void ShowKeyBoards() {
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> InputMethods = imeManager.getEnabledInputMethodList();
        imeManager.showInputMethodPicker();


        String id = Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
        System.out.println("[KeyboardActivity]: Default IME"+id);

/*        Intent intent = new Intent(UserConsent.this, KeyboardActivity.class);
        startActivity(intent);*/
    }

    public void RegisterUser() {
        if(!IsAlreadyRegistered()) {
            Intent intent = new Intent(UserConsent.this, PerformRegistration.class);
            startActivity(intent);
        }
        else {
            StoreRunningStatus(true);
        }
    }

    public void LoginDropbox(){
        if(!IsAlreadyRegistered()) {
            Intent intent = new Intent(UserConsent.this, LoginDropboxxActivity.class);
            startActivity(intent);
        }
        else{
            StoreRunningStatus(true);
        }
    }

    public void StoreRunningStatus(boolean flag){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putBoolean(getResources().getString(R.string.sharedpref_running_status), flag);
        run_editor.apply();
        run_editor.commit();
    }

    public boolean IsAlreadyRegistered(){

        boolean registration_flag= false;
        SharedPreferences mood_pref=null;

        //Sharedpreference based Registration Status

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.moodrecorder_pkg), Context.CONTEXT_IGNORE_SECURITY);
            mood_pref = con.getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_PRIVATE);

            registration_flag = mood_pref.getBoolean(getResources().getString(R.string.sharedpref_registration_flag), false);
            System.out.println("Registration Status:" + registration_flag);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return registration_flag;
    }

    public void StartServices() {
        if(IsAlreadyRegistered()){
            Log.d("StartServices:","Started first time");
            SetAlarms();
        }

    }

    public void SetAlarms() {

        int common_interval=1000 * Integer.parseInt(getResources().getString(R.string.common_interval));

        Intent alarmIntent = new Intent(UserConsent.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(UserConsent.this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

            manager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+common_interval,pendingIntent);
        }
        else {

            manager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+common_interval,pendingIntent);
        }

    }
}
