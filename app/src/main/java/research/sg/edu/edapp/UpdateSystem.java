package research.sg.edu.edapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateSystem extends AppCompatActivity {

    Button btn1,btn2;
    TextView tv;
    ProgressBar pbHeaderProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_system);

        tv = (TextView) findViewById(R.id.textView10);
        btn1 = (Button) findViewById(R.id.button6);
        btn2 = (Button) findViewById(R.id.button7);
        pbHeaderProgress = (ProgressBar) findViewById(R.id.progressBar);
        pbHeaderProgress.setVisibility(View.GONE);
        tv.setText("");
        btn1.setVisibility(View.GONE);
        btn2.setText("Check For Updates");
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: MINE Handle updates
                checkForUpdateByVersionCode(getString(R.string.affectsense_server_uri)+getString(R.string.version_file));
            }
        });
    }

    private String TAG = "UpdateChecker";

    public boolean checkForUpdateByVersionCode(String url) {
        boolean isConnected = false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            TopExceptionHandler(e);
            Toast.makeText(UpdateSystem.this, "Not able to connect", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(isConnected) {
            int versionCode = getVersionCode();
            int readCode = 0;
            if (versionCode >= 0) {
                try {
                    readCode = Integer.parseInt(readFile(url));
                    // Check if update is available.
                    if (readCode > versionCode) {
                        File sdCardRoot = Environment.getExternalStorageDirectory();
                        File file = new File(sdCardRoot, getString(R.string.data_file_path));
                        File outputFile = new File(file, "update.apk");
                        if(readLastUpdateVersionDownloaded()==readCode && outputFile.exists()) {
                            afterDownloadSet();
                        }
                        else {
                            storeLastUpdateVersionDownloaded(readCode);
                            tv.setText("Update Available.\nClick Download Now to download them.");
                            btn1.setVisibility(View.VISIBLE);
                            btn1.setText("Download Now");
                            btn2.setText("Download Later");
                            btn1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    File sdCardRoot = Environment.getExternalStorageDirectory();
                                    File file = new File(sdCardRoot, getString(R.string.data_file_path));
                                    File outputFile = new File(file, "update.apk");
                                    if(outputFile.exists()){
                                        outputFile.delete();
                                    }
                                    tv.setText("Downloading Updates...\nPlease Wait :)");
                                    btn1.setVisibility(View.GONE);
                                    btn2.setVisibility(View.GONE);

                                    new AsyncTask<Void, Void, Void>() {
                                        protected void onPreExecute() {
                                            // TODO Auto-generated method stub
                                            super.onPreExecute();
                                            pbHeaderProgress.setVisibility(View.VISIBLE);
                                            btn1.setVisibility(View.GONE);
                                            btn2.setVisibility(View.GONE);
                                        }

                                        protected Void doInBackground(Void... params) {
                                            try {
                                                URL url = new URL(getString(R.string.affectsense_server_uri) + getString(R.string.update_app_page));
                                                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                                                c.setRequestMethod("GET");
                                                c.setDoOutput(true);
                                                c.connect();

                                                File sdCardRoot = Environment.getExternalStorageDirectory();
                                                File file = new File(sdCardRoot, getString(R.string.data_file_path));

                                                File outputFile = new File(file, "update.apk");
                                                if(outputFile.exists()){
                                                    outputFile.delete();
                                                }

                                                FileOutputStream fos = new FileOutputStream(outputFile);
                                                InputStream is = c.getInputStream();

                                                byte[] buffer = new byte[1024];
                                                int len1 = 0;
                                                while ((len1 = is.read(buffer)) != -1) {
                                                    fos.write(buffer, 0, len1);
                                                }
                                                fos.close();
                                                is.close();
                                            } catch (Exception e) {
                                                Log.e("UpdateAPP", "Update error! " + e.getMessage());
                                            }
                                            return null;
                                        }
                                        protected void onPostExecute(Void result) {
                                            pbHeaderProgress.setVisibility(View.GONE);
                                            afterDownloadSet();
                                        }
                                    }.execute();
                                }
                            });
                            btn2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                        }
                        System.out.println("We Have Updates");
                        return true;
                    }
                    else {
                        tv.setText("AffectSense is up-to-date.");
                        btn1.setVisibility(View.GONE);
                        btn2.setVisibility(View.VISIBLE);
                        btn2.setText("Close");
                        btn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                        System.out.println("we Dont have updates");
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid number online"); //Something wrong with the file content
                }
            } else {
                Log.e(TAG, "Invalid version code in app"); //Invalid version code
            }
        } else {
            Toast.makeText(UpdateSystem.this,"App update check failed. No internet connection available",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void afterDownloadSet() {
        tv.setText("Update Downloaded.\nClick Update Now to install them.");
        btn1.setVisibility(View.VISIBLE);
        btn2.setVisibility(View.VISIBLE);
        btn1.setText("Update Now");
        btn2.setText("Update Later");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdCardRoot = Environment.getExternalStorageDirectory();
                File file = new File(sdCardRoot, getString(R.string.data_file_path));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(Uri.fromFile(new File(file + "/update.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                finish();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public String readFile(String url) {
        String result;
        InputStream inputStream;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            inputStream = new URL(url).openStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            result = bufferedReader.readLine();
            return result;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid URL");
        } catch (IOException e) {
            Log.e(TAG, "There was an IO exception");
        }
        Log.e(TAG, "There was an error reading the file");
        return "Problem reading the file";
    }
    public int getVersionCode() {
        int code;
        try {
            code = getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionCode;
            return code; // Found the code!
        } catch (PackageManager.NameNotFoundException e) {
            TopExceptionHandler(e);
            Log.e(TAG, "Version Code not available"); // There was a problem with the code retrieval.
        } catch (NullPointerException e) {
            TopExceptionHandler(e);
            Log.e(TAG, "Context is null");
        }

        return -1; // There was a problem.
    }
    public void storeLastUpdateVersionDownloaded(int version){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putInt(getResources().getString(R.string.last_update_version), version);
        run_editor.apply();
        run_editor.commit();
    }
    public int readLastUpdateVersionDownloaded(){
        int running_status = getVersionCode();
        SharedPreferences mood_pref=null;
        //Sharedpreference based Running Status
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.moodrecorder_pkg), Context.CONTEXT_IGNORE_SECURITY);
            mood_pref = con.getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);

            running_status = mood_pref.getInt(getResources().getString(R.string.last_update_version),getVersionCode());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return running_status;
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
