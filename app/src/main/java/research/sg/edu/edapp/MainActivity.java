package research.sg.edu.edapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RemoteException;




import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.dropbox.core.DbxException;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.example.expresstoast.MyToast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import research.sg.edu.edapp.FinalClasses.AppCategory;
import research.sg.edu.edapp.FinalClasses.FeaturesDetails;
import research.sg.edu.edapp.FinalClasses.StatsDetails;
import research.sg.edu.edapp.data.model.DropboxClient;
import research.sg.edu.edapp.data.model.UploadTask;

import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {

    Button ShowKb, Stopbtn, Statbtn, Validatebtn;

    Boolean isDeveloper = false;
    List<String> filesListInDir = new ArrayList<String>();

    final static private String PREF_KEY_SHORTCUT_ADDED = "PREF_KEY_SHORTCUT_ADDED";

    String ACCESS_TOKEN;
    String zipDirName;
    TelephonyManager tm;
    String imei="";
    private static String email="";
    private static long file_limit=26214400;
    private static String zip_file_name="";
    private static final String TAG=MainActivity.class.getName();
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        setContentView(R.layout.activity_main);

        ShowKb=(Button)findViewById(R.id.button1);
        Stopbtn=(Button)findViewById(R.id.button2);

        Statbtn=(Button)findViewById(R.id.button3);
        //Validatebtn=(Button)findViewById(R.id.button4);
        Stopbtn.setVisibility(View.GONE);
        Statbtn.setVisibility(View.GONE);



        Log.i(TAG,"in method create");
        SharedPreferences ratePrefs = getSharedPreferences("FirstUpdate", 0);
        if (!ratePrefs.getBoolean("FirstTimeTask", false)) {
            // Do update you want here
            createShortcutIcon();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            SharedPreferences.Editor edit = ratePrefs.edit();
            edit.putBoolean("FirstTimeTask", true);
            edit.commit();
        }

        CreateActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"in method onStart");
    }


    protected void onResume() {

        super.onResume();
        //File sdCardRoot = Environment.getExternalStorageDirectory();
        //File directory=new File(sdCardRoot,getResources().getString(R.string.affectsense_file_path));
        //deleteFolder(directory);
        Log.i(TAG,"in method onResume");
        // this commented section is related to review images
        /*if(isAppRunning() && IsAlreadyRegistered()){
            Validatebtn.setOnClickListener(new View.OnClickListener(){
                public  void onClick(View v){
                    Intent intent = new Intent(MainActivity.this, activity_images.class);
                    startActivity(intent);
                }
            });
        }*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"in method onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"in method onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"in method onDestroy");
    }

    private void delete_Folder(File directory) {
        File[] files=directory.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    delete_Folder(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CreateActivity() {
        if (!isAppRunning()) {
            final Activity myActivity = this;
            Stopbtn.setEnabled(false);
            ShowKb.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "Please Allow These Permissions", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(myActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        CreateFolders();

                        ShowKb.setEnabled(false);
                        Stopbtn.setEnabled(true);

                        ReceiveConsent();

                        NotifyMe();
                        // cann't set second alarm. Cause it will not be called
                        //SetAlarmUpload();
                        finish();
                    }
                }

            });
            Stopbtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    System.out.println("Stopping the application and all associated services");
                    ShowKb.setEnabled(true);
                    Stopbtn.setEnabled(false);

                    StopServices();
                    StoreRunningStatus(false);
                    CreateActivity();
                }
            });

            /*if(!IsAlreadyRegistered()) {
                Statbtn.setEnabled(false);
            }
            else {
                Statbtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DisplayDashboard();
                    }
                });
            }*/

        }
        /*else if(isAppRunning()){
            ShowKb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        //CreateFolders();

                        ShowKb.setEnabled(false);
                        Stopbtn.setEnabled(true);

                        //ReceiveConsent();


                    StartServices();
                    Log.d("StartServices:","has started");
                    NotifyMe();
                    finish();
                    }


            });
        }*/
        else {
            if (!IsAlreadyRegistered()) {
                Stopbtn.setEnabled(false);
                Statbtn.setEnabled(false);
                ShowKb.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        ShowKb.setEnabled(false);
                        Stopbtn.setEnabled(true);

                        ReceiveConsent();
                        NotifyMe();
                        finish();
                    }
                });
            }


        else{
            // uncomment the below line if needed--salma
            //ShowKb.setEnabled(false);
            ShowKb.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    // after restart notification will start with help of this method
                    NotifyMe();
                }
            });

            // this commented section related to photo review activity
            /*Validatebtn.setOnClickListener(new View.OnClickListener(){
                public  void onClick(View v){
                    Intent intent = new Intent(MainActivity.this, activity_images.class);
                    startActivity(intent);
                }
                });*/
            //Stopbtn.setOnClickListener(new View.OnClickListener() {
                //public void onClick(View v) {
                    //System.out.println("Stopping the application and all associated services");

                    //ShowKb.setEnabled(true);
                    //Stopbtn.setEnabled(false);

                    //StopServices();
                    //StoreRunningStatus(false);
                    //CreateActivity();
                //}
            //});

            //Statbtn = (Button) findViewById(R.id.button3);
            //Statbtn.setOnClickListener(new View.OnClickListener() {
                //@Override
                //public void onClick(View v) {
                    //DisplayDashboard();
                //}
            //});


        }
    }

       /* try {
            SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format));
            Date myDate = new Date();
            Date newDate = new Date(myDate.getTime() - 172800000L); // 2 * 24 * 60 * 60 * 1000

            Uri CONTENT_URI = ConfirmFeaturesProvider.CONTENT_URI;
            ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);

            String where = "date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") >= date(?) and date(" + FeaturesDetails.FeaturesEntry.TIMESTAMP + ") <= date(?)";
            Cursor tCursor = CR.query(CONTENT_URI,
                    new String[]{"DISTINCT " + FeaturesDetails.FeaturesEntry.TIMESTAMP, FeaturesDetails.FeaturesEntry.EMOTION},
                    where, new String[]{format.format(newDate), format.format(myDate)}, null);

            if(tCursor.getCount() == 0) {
                Validatebtn.setVisibility(View.GONE);
            }
            else {
                Validatebtn.setVisibility(View.VISIBLE);
                Validatebtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ValidatePredictions.class);
                        intent.putExtra("OpenFromMainActivity",1);
                        startActivity(intent);
                    }
                });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/


    }

    private void SetAlarmUpload() {
        System.out.println("Setting upload alarm");
        WriteFile("Setting upload alarm");
        Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis((System.currentTimeMillis()));
        calendar.set(Calendar.HOUR_OF_DAY,1);
        calendar.set(Calendar.MINUTE,10);
        Intent intent=new Intent(MainActivity.this,UploadAlarm.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);


    }

    Menu mOptionsMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.instruction){
            Intent intent = new Intent(this, Instruction_data.class);
            startActivity(intent);
            return true;

        }
        else if (id == R.id.zip_file) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    zip_file();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(MainActivity.this,"File is compressed",Toast.LENGTH_SHORT).show();
                            MyToast.applause(MainActivity.this,"File is compressed",Toast.LENGTH_LONG);
                        }
                    });
                }
            }).start();



            //deletecontent();

            return true;
        }
        else if (id == R.id.mail) {
            /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            alertDialogBuilder.setView(inflater.inflate(R.layout.contact_us, null))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return true;*/
            // salma uncomment the below sendmail when we want to mail data
            SendMail();


            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void deletecontent() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File directory=new File(sdCardRoot,getResources().getString(R.string.affectsense_file_path));
        delete_Folder(directory);
    }

    private void SendMail() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        //String zipDirName ="file:"+sdCardRoot+"/AffectSense.zip";
        //File newfile = new File(sdCardRoot,"/AffectSense.zip");
        File newfile = new File(sdCardRoot,"/"+zip_file_name);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        //intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(zipDirName));
        Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this,getApplicationContext().getPackageName()+".fileprovider", newfile);
        //this.grantUriPermission(getApplicationContext().getPackageName(),outputFileUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"salmamandi@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Data Collection_"+timeStamp+"_"+email);
        intent.putExtra(Intent.EXTRA_TEXT, "Sending the data file.");
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM,outputFileUri);
        //startActivity(Intent.createChooser(intent, "Send Email"));
        try {
            Intent chooser= Intent.createChooser(intent, "Send Email");
            List<ResolveInfo> resInfoList=this.getPackageManager().queryIntentActivities(chooser,PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                this.grantUriPermission(packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            //startActivity(Intent.createChooser(intent, "Send Email"));
            startActivity(chooser);
            finish();
            Log.i("Finished sending email", "");


        }
        catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void zip_file() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File directory=new File(sdCardRoot,getResources().getString(R.string.affectsense_file_path));
        //File[] fList = directory.listFiles();
        /*for (File file : fList){
            System.out.println(file.getName());
        }*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        try {
            Context con=getApplicationContext().createPackageContext("research.sg.edu.edapp", CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref1=con.getSharedPreferences("FileName",Context.MODE_PRIVATE);
            email=pref1.getString("email","None");
            System.out.println("email id:"+email);
            WriteFile("email id:"+email);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //String zipDirName =sdCardRoot+"/AffectSense.zip";
        // delete previous file
        if(!zip_file_name.isEmpty()) {
            File file2 = new File(sdCardRoot, "/" + zip_file_name);
            file2.delete();
        }
        zip_file_name=email+"_" + timeStamp + ".zip";
        String zipDirName =sdCardRoot+"/"+zip_file_name;
        System.out.println("zip directory name="+ zipDirName);
        ZipDirectory(directory, zipDirName);



    }




    private void ZipDirectory(File directory, String zipDirName) {
        populateFilesList(directory);
        File sdCardRoot = Environment.getExternalStorageDirectory();
        try {
            FileOutputStream fos = new FileOutputStream(zipDirName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesListInDir){
                System.out.println("Zipping "+filePath);
                ZipEntry ze = new ZipEntry(filePath.substring(directory.getAbsolutePath().length()+1, filePath.length()));
                //System.out.println(filePath.substring(directory.getAbsolutePath().length()+1, filePath.length()));
                zos.putNextEntry(ze);
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);

                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
            for(String filePath:filesListInDir){
                if(filePath.endsWith(".jpg")) {
                    File f = new File(filePath);
                    f.delete();
                }
            }
            filesListInDir.clear();
            //Toast.makeText(MainActivity.this,"File is compressed",Toast.LENGTH_SHORT).show();

        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void populateFilesList(File directory) {
        File[] fList = directory.listFiles();
        long length=0;
        for (File file : fList)
            if(file.isFile()) {
                if((length+file.length())<file_limit){
                    filesListInDir.add(file.getAbsolutePath());
                    length += file.length();
                    System.out.println(file.getAbsolutePath());
                }
            }else {
                populateFilesList(file);
            }

        }





    public void ChangeTrainPhaseThreshold() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View promptsView = inflater.inflate(R.layout.set_trainphase_threshold, null);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Change Train Phase Threshold")
                .setMessage("Current Threshold : " + RetrieveTrainPhaseThreshold());

        final TextView tv = (TextView) promptsView.findViewById(R.id.textView11);
        final EditText et = (EditText) promptsView.findViewById(R.id.editText2);

        tv.setText("Enter value more than 10");

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String et_text = et.getText().toString().trim();
                        StoreTrainPhaseThreshold(Integer.parseInt(et_text));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        final Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(false);
        et.addTextChangedListener(new TextWatcher() {
            private void handleText() {
                // Grab the button
                if(et.getText().length() == 0) {
                    okButton.setEnabled(false);
                } else {
                    okButton.setEnabled(true);
                }

                String et_text = et.getText().toString().trim();
                if(!(et_text.isEmpty() || et_text.length() == 0 || et_text.equals("") || et_text == null)) {
                    int value = Integer.parseInt(et_text);
                    if (value >= 10)
                        okButton.setEnabled(true);
                    else
                        okButton.setEnabled(false);
                }
                else
                    okButton.setEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                handleText();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }
        });
    }

    public int RetrieveTrainPhaseThreshold(){
        String ctr=getString(R.string.default_trainphase_threshold);
        String ctr1=getString(R.string.default_trainphase_threshold);

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.mood_count_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getString(R.string.trainphase_threshold_ctr), ctr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(ctr == null || ctr.trim().equals("null"))
            return Integer.parseInt(ctr1);
        return Integer.parseInt(ctr);
    }

    public void StoreTrainPhaseThreshold(int value){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_count_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getString(R.string.trainphase_threshold_ctr), String.valueOf(value));
        log_editor.apply();
        log_editor.commit();
    }

    public void StartExtractFeaturesService(int i) {
        System.out.println("[MainActivity]: ExtractFeaturesService is running");
        //  WriteAlarmFiringTime(5);
        Intent intent = new Intent(MainActivity.this,ExtractFeaturesService.class);
        intent.putExtra("isTestPhase",i);
        this.startService(intent);
    }

    public void StoreFeaturesCount(String feature_variable,int value){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_count_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(feature_variable, String.valueOf(value));
        log_editor.apply();
        log_editor.commit();
    }

    public void StoreCtr(String ctr,int value){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(ctr, String.valueOf(value));
        log_editor.apply();
        log_editor.commit();
    }

    /*public void StartDeveloperMode() {
        MenuItem action_fill_features = mOptionsMenu.findItem(R.id.action_fill_features);
        MenuItem action_empty_features = mOptionsMenu.findItem(R.id.action_empty_features);
        MenuItem action_see_features = mOptionsMenu.findItem(R.id.action_see_features);

        MenuItem action_build_model = mOptionsMenu.findItem(R.id.action_build_model);
        MenuItem action_developerMode = mOptionsMenu.findItem(R.id.action_developerMode);
        MenuItem action_set_trainthreshold = mOptionsMenu.findItem(R.id.action_set_trainthreshold);

        MenuItem action_fill_data = mOptionsMenu.findItem(R.id.action_fill_data);
        MenuItem action_test_data = mOptionsMenu.findItem(R.id.action_test_data);
        MenuItem action_clear_counts = mOptionsMenu.findItem(R.id.action_clear_counts);

        if(!isDeveloper) {
            Intent intent = new Intent(MainActivity.this, AskPassword.class);
            startActivityForResult(intent,1);
        }
        else {
            action_developerMode.setTitle(getString(R.string.onDeveloper));
            action_build_model.setVisible(false);

            action_fill_features.setVisible(false);
            action_empty_features.setVisible(false);
            action_see_features.setVisible(false);

            action_set_trainthreshold.setVisible(false);

            action_fill_data.setVisible(false);
            action_test_data.setVisible(false);
            action_clear_counts.setVisible(false);

            isDeveloper = false;
        }
    }*/

    // Call Back method  to get the Message form other Activity
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==1) {
            if(data.getBooleanExtra("password",false)) {
                isDeveloper = true;
                MenuItem action_fill_features = mOptionsMenu.findItem(R.id.action_fill_features);
                MenuItem action_empty_features = mOptionsMenu.findItem(R.id.action_empty_features);
                MenuItem action_see_features = mOptionsMenu.findItem(R.id.action_see_features);
                MenuItem action_build_model = mOptionsMenu.findItem(R.id.action_build_model);
                MenuItem action_developerMode = mOptionsMenu.findItem(R.id.action_developerMode);

                MenuItem action_set_trainthreshold = mOptionsMenu.findItem(R.id.action_set_trainthreshold);

                MenuItem action_fill_data = mOptionsMenu.findItem(R.id.action_fill_data);
                MenuItem action_test_data = mOptionsMenu.findItem(R.id.action_test_data);
                MenuItem action_clear_counts = mOptionsMenu.findItem(R.id.action_clear_counts);

                action_developerMode.setTitle(R.string.offDeveloper);

                action_build_model.setVisible(true);
                action_set_trainthreshold.setVisible(true);

                action_empty_features.setVisible(true);
                action_see_features.setVisible(true);

                action_fill_data.setVisible(true);
                action_test_data.setVisible(true);
                action_clear_counts.setVisible(true);
            }
        }
    }*/

    public void StartBuildModelService() {
        System.out.println("[MasterService]: BuildModelService is running");
        Intent intent = new Intent(MainActivity.this,BuildModelService.class);
        this.startService(intent);
    }

    public void fillDatabase(int i) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //String imei_no = (String) telephonyManager.getDeviceId();
        String feature_file_ctr = RetrieveFeatureFileCtr();
        String tap_file_name =  feature_file_ctr + getResources().getString(R.string.features_file_postfix);
        String swipe_file_name = feature_file_ctr + "_Swipe_Features_Label.txt";
        File tapDataFile = new File(sdCardRoot, getString(R.string.features_file_path) + tap_file_name);
        // TODO: Maybe store all features in the same file
        File swipeDataFile = new File(sdCardRoot, getString(R.string.features_file_path) + swipe_file_name);

        InputStream instream;

        try {
            instream = new FileInputStream(tapDataFile);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line[];
                String temp=buffreader.readLine();
                while ((temp = buffreader.readLine()) != null) {
                    line = temp.split(",");
                    if(i==1)
                        StoreStatsDetail(getApplicationContext(),line[7],line[8],line[9]);
                    else {
                        StoreFeatures(getApplicationContext(),line[0],line[1],line[2],line[3],line[4],line[5],line[6],line[7],line[8],line[9], line[10], line[11], line[12]);
                    }
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
        Toast.makeText(MainActivity.this, "Fill DataBase Complete", Toast.LENGTH_SHORT).show();
    }

    public void StoreFeatures(Context context,String sessionId, String msi, String rmsi, String sessionLength, String backSpacePer, String splCharPer,
                                                        String sessionDuration,String appName,String recordTime, String emotion,
                                                        String pressure, String velocity, String duration) {
        ContentValues values = new ContentValues();
        // TODO: MINE F value store ADDED
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

        values.put(FeaturesDetails.FeaturesEntry.PRESSURE, Float.parseFloat(pressure));
        values.put(FeaturesDetails.FeaturesEntry.VELOCITY, Float.parseFloat(velocity));
        values.put(FeaturesDetails.FeaturesEntry.SWIPEDURATION, Float.parseFloat(duration));

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
    }

    public static boolean timeIsBefore(Date d1, Date d2) {
        DateFormat f = new SimpleDateFormat("HH:mm:ss");
        return f.format(d1).compareTo(f.format(d2)) < 0;
    }

    public String RetrieveFeatureFileCtr(){
        String ctr="000000";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.feature_file_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ctr;
    }

    public boolean isAppRunning(){
        boolean running_status= false;
        SharedPreferences mood_pref=null;
        //Sharedpreference based Running Status
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.moodrecorder_pkg), Context.CONTEXT_IGNORE_SECURITY);
            mood_pref = con.getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);

            running_status = mood_pref.getBoolean(getResources().getString(R.string.sharedpref_running_status), false);
            System.out.println("App Running Status:" + running_status);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return running_status;
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

    private void createShortcutIcon(){
        // Checking if ShortCut was already added
        SharedPreferences sharedPreferences1 = getPreferences(MODE_PRIVATE);
        boolean shortCutWasAlreadyAdded = sharedPreferences1.getBoolean(PREF_KEY_SHORTCUT_ADDED, false);
        if (shortCutWasAlreadyAdded) return;

        Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "AffectSense 2.1");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);

        // Remembering that ShortCut was already added
        SharedPreferences.Editor editor = sharedPreferences1.edit();
        editor.putBoolean(PREF_KEY_SHORTCUT_ADDED, true);
        editor.commit();
    }

    public void CreateFolders() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        System.out.println(sdCardRoot);
        //String uniqueFolder= UUID.randomUUID().toString()+"/";
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        //File dataDir=new File(getExternalFilesDir(null),"/AffectSense");
        System.out.println(dataDir);
        String uniqueString= UUID.randomUUID().toString();
        System.out.println("Unique string="+uniqueString);
        //File logDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path) + getResources().getString(R.string.log_file_path));
        //File featuresDir = new File(sdCardRoot, getString(R.string.features_file_path));

        if(!dataDir.exists()) {
            if(dataDir.mkdirs()) {
                System.out.println("Data dir has created");
            }else{
                System.out.println("Data dir cannot be created");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

       /* if(!logDir.exists()) {
            logDir.mkdirs();
        }
        if(!featuresDir.exists()) {
            featuresDir.mkdirs();
        }*/
    }

    public void ReceiveConsent() {
        Intent intent = new Intent(MainActivity.this, UserConsent.class);
        startActivity(intent);
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

    public void StopServices() {

        Intent intent = new Intent(MainActivity.this,MasterService.class);
        this.stopService(intent);

    }

    public void DisplayDashboard() {
        Intent intent = new Intent(MainActivity.this, StatsType.class);
        startActivity(intent);
    }
    public void StartServices() {
        if(IsAlreadyRegistered()){

            SetAlarms();
        }

    }
    public void SetAlarms() {

        int common_interval=1000 * Integer.parseInt(getResources().getString(R.string.common_interval));

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

            manager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+common_interval,pendingIntent);
        }
        else {

            manager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+common_interval,pendingIntent);
        }

    }
    public void WriteFile(String text) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File logFile = new File(sdCardRoot, "/AffectSense/logfile.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}