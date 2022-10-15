package research.sg.edu.edapp;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import research.sg.edu.edapp.data.model.DropboxClient;
import research.sg.edu.edapp.data.model.UploadTask;

public class UploadJob extends JobService {
    String ACCESS_TOKEN;
    List<String> filesListInDir = new ArrayList<String>();
    String zipDirName;
    TelephonyManager tm;
    String imei="";
    private static String email="";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        System.out.println("I am going to start Uploading");

        ACCESS_TOKEN = retrieveAccessToken();
        if (ACCESS_TOKEN != null) {
            System.out.println("Access Token is not null");
            WriteFile("Access Token is not null");
            //mApi=new DropboxAPI(new AndroidAuthSession(appKeyPair,ACCESS_TYPE,ACCESS_TOKEN));
            uploadcall();
            System.out.println("Upload is complete");
            WriteFile("Upload is complete");
            jobFinished(jobParameters,false);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        System.out.println("Job has stopped");
        WriteFile("Job has stopped");
        return true;
    }

    private String retrieveAccessToken() {
        //check if ACCESS_TOKEN is stored on previous app launches
        SharedPreferences prefs = getSharedPreferences("research.sg.edu.valdio.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found");
            WriteFile("AccessToken Status:No token found");
            return null;
        } else {
            //accessToken already exists
            Log.d("AccessToken Status:", "Token exists");
            WriteFile("AccessToken Status:Token exists");
            return accessToken;
        }
    }

    private void uploadcall() {

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File directory=new File(sdCardRoot,getResources().getString(R.string.affectsense_file_path));
        // zip the file and return the zip file name
        zipDirName=zip_file();
        // uploading files, not zip file
        //upload(directory);
        // uploading zip file
        upload_zip(zipDirName);
        // delete save images
        //deleteStorage();
        // save shared preference
        //SaveSharedPreferences();
        // delete zip file
        //boolean delete_satus=delete_zip(zipDirName);
        //System.out.println("Zip file deleted="+delete_satus);

    }

    private void SaveSharedPreferences() {
        SharedPreferences mPreferences;
        mPreferences=getApplicationContext().getSharedPreferences("CounterFile",Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor mEditor=mPreferences.edit();
        mEditor.putInt("UploadCounter",0);
        mEditor.apply();
        mEditor.commit();
    }

    private void upload_zip(String zipDirName) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File newfile = new File(sdCardRoot,zipDirName);
        System.out.println("file name:"+newfile.getName());
        System.out.println("Uri:"+ Uri.fromFile(newfile));
        new UploadTask(DropboxClient.getClient(ACCESS_TOKEN), Uri.fromFile(newfile), getApplicationContext(), newfile.getName(),email).execute();


    }
    private void deleteStorage() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File directory=new File(sdCardRoot,"/AffectSense/");
        deleteFolder(directory);
    }

    private void deleteFolder(File directory) {
        File[] files=directory.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFromDirectory(f);
                }
            }
        }
    }

    private void deleteFromDirectory(File f) {
        File[] files=f.listFiles();
        if(files!=null) {
            for(File fin: files) {
                fin.delete();
            }
        }
    }

    private String zip_file() {
        // first create the zip file name
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File directory=new File(sdCardRoot,getResources().getString(R.string.affectsense_file_path));
        String zip_filename="";
        //String email="";
        // get the email id from shared preference file
        try {
            Context con=getApplicationContext().createPackageContext("research.sg.edu.edapp", CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref1=con.getSharedPreferences("FileName",Context.MODE_PRIVATE);
            email=pref1.getString("email","None");
            System.out.println("email id:"+email);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // this date will be coded in zip file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        // check permission for reading phone state is given or not
        int PermisI= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        try {
            if (PermisI == PackageManager.PERMISSION_GRANTED) {

                tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                imei = tm.getDeviceId().toString();
                System.out.println("imei no:" + tm.getDeviceId());
                System.out.println("IMEI no:" + imei);
                zip_filename = "Affect_" + imei + "_" + timeStamp + ".zip";
            }
        }catch(Exception e) {
            zip_filename = email+"_" + timeStamp + ".zip";
        }

        /*if (imei.length() != 0) {
            zip_filename = "Affect_" + imei + "_" + timeStamp + ".zip";
        } else {
            zip_filename = "Affect_" + timeStamp + ".zip";
        }*/
        String zipDirName=sdCardRoot+"/"+zip_filename;
        System.out.println("Zip file name:"+zipDirName);
        // zip the file
        ZipDirectory(directory, zipDirName);
        return zip_filename;
    }

    private void ZipDirectory(File directory, String zipDirName) {
        populateFilesList(directory);
        try{
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
            System.out.println("File is zipped");
            // clear array list
            filesListInDir.clear();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void populateFilesList(File directory) {
        File[] fList = directory.listFiles();
        for (File file : fList)
            if(file.isFile()) {
                filesListInDir.add(file.getAbsolutePath());
                System.out.println(file.getAbsolutePath());
            }else {
                populateFilesList(file);
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
