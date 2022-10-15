package research.sg.edu.edapp;

/**
 * Created by weirdmyth on 28/10/16.
 */


import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Environment;

import android.telephony.TelephonyManager;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class BuildModelService extends IntentService {

    private int count=0,savedcount=0;
    public BuildModelService() {
        super("BuildModelService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
       /* PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLockForBuildingModelTag");
        wakeLock.acquire();*/

        Intent notificationIntent = new Intent(this, MasterService.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true).setContentTitle("Emotion Detection")
                .setContentText("Building Model...")
                .setColor(Color.RED)
                .setPriority(Notification.PRIORITY_MAX)
                //.setVibrate(new long[] { 100, 250 })
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);

        System.out.println("[BuildModelService]: BuildModelService is running**");

        buildModel();

        StartKLDService();

        System.out.println("[BuildModelService]: BuildModelService Completed**");
        stopForeground(true);

       // wakeLock.release();
    }

    public void StartKLDService() {
        System.out.println("[BuildModelService]: KLD Service is running");
        Intent intent = new Intent(BuildModelService.this,CalculateKLD.class);
        this.startService(intent);
    }

    private void buildModel() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File featuresDir = new File(sdCardRoot, getString(R.string.features_file_path));
        File logDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!featuresDir.exists()) {
            featuresDir.mkdirs();
        }

        final SharedPreferences counter = getSharedPreferences("counter", Context.MODE_PRIVATE);
        final SharedPreferences.Editor countedit = counter.edit();

        count = counter.getInt("counter",savedcount) + 1;
        countedit.putInt("counter",count).commit();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null,iFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        float batteryPct = level*100/(float)scale;

        String outtext = count + ","+currentDateandTime+","+batteryPct+",";

        File filefolder = new File(String.valueOf(featuresDir));
        if (filefolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        }) != null) {
            final File[] files = filefolder.listFiles();
            Arrays.sort(files);
            Instances data = null;
            data = setAttributes();

            int flag = 0;

            Random rn = new Random();
            int randomNum = 0;
            String lastMood = getMoodFromIndex(randomNum);
            for (File file : files) {
                if(file.isDirectory())
                    continue;
                else if (file != null) {
                    System.out.println(file);
                    InputStream instream;
                    try {
                        instream = new FileInputStream(file);
                        if (instream != null) {
                            InputStreamReader inputreader = new InputStreamReader(instream);
                            BufferedReader buffreader = new BufferedReader(inputreader);
                            String line[];
                            String temp;
                            temp = buffreader.readLine();
                            while ((temp = buffreader.readLine()) != null) {
                                line = temp.split(",");
                                double[] values = new double[]{Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3]),
                                        Double.parseDouble(line[4]), Double.parseDouble(line[5]), Double.parseDouble(line[6]), getMoodIndex(lastMood), getMoodIndex(line[9])};
                                Instance instance = new DenseInstance(1, values);
                                System.out.println(instance);
                                data.add(instance);
                                lastMood = line[9];
                            }
                            instream.close();
                        }
                        move_file(file.getName());
                    }
                    catch (Exception e) {
                        TopExceptionHandler(e);
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(data);
            StoreLastMood(lastMood);

            try {
                if(data == null || data.numInstances() <= Integer.parseInt(getString(R.string.trainData_threshold))) {
                    if(data == null)
                        System.out.println("Build Model Failed....Data is Null");
                    else
                        System.out.println("Build Model Failed...." + data.numInstances());
                    System.out.println("Build Model Failed...Train Data is Less than threshold");
                    return;
                }

                data.setClassIndex(data.numAttributes() - 1);

                RandomForest rf = new RandomForest();
                System.out.println("[BuildModelService]: Model Training Start");
                //System.out.println(data);
                rf.buildClassifier(data);
                System.out.println("[BuildModelService]: Model Build Completed");

                File modelDir = new File(sdCardRoot, getString(R.string.model_file_path));

                if(!modelDir.exists()) {
                    modelDir.mkdirs();
                }

                System.out.println("Model Address:"+modelDir);
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String imei_no = (String) telephonyManager.getDeviceId();

                String model_file_ctr = RetrieveModelFileCtr();
                String modelPath = modelDir + "/" + imei_no + "_" + model_file_ctr + "_" + getString(R.string.model_file_postfix);

                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelPath));
                System.out.println("Model Writing:"+modelPath);
                out.writeObject(rf);
                System.out.println("Model Saved");
                out.flush();
                System.out.println("Out Flush");
                out.close();

                //Increase Model Ctr
                int ctr = Integer.parseInt(model_file_ctr) + 1;
                model_file_ctr = String.valueOf(ctr);
                model_file_ctr = String.format("%06d", Integer.parseInt(model_file_ctr));
                StoreModelFileCtr(model_file_ctr);

                outtext = outtext+data.numInstances()+",";

                String currentDateandTime2 = sdf.format(new Date());

                level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
                float batteryPct2 = level*100/(float)scale;

                long diff = 0;

                Date starttime = sdf.parse(currentDateandTime);
                Date endtime = sdf.parse(currentDateandTime2);
                diff = (endtime.getTime() - starttime.getTime());

                float batteryDiff = batteryPct - batteryPct2;

                outtext = outtext+currentDateandTime2+","+batteryPct2+","+diff+","+batteryDiff+"\n";

                String buildModel_log = getString(R.string.log_file_path) + getString(R.string.buildModel_log_file_name);
                byte[] buildModel_data = outtext.getBytes();

                File buildModel_file = new File(logDir, buildModel_log);
                FileOutputStream fos;
                fos = new FileOutputStream(buildModel_file,true);
                fos.write(buildModel_data);
                fos.close();
            } catch (Exception e) {
                TopExceptionHandler(e);
                e.printStackTrace();
            }
        }

        else {
            String error_log = getString(R.string.log_file_path) + getString(R.string.error_log_file_name);
            String errout = currentDateandTime+" Build Model Failed (No Train Data File Exists)\n";
            byte[] error_data = errout.getBytes();

            File error_file = new File(logDir, error_log);
            try {
                FileOutputStream fos;
                fos = new FileOutputStream(error_file,true);
                fos.write(error_data);
                fos.close();
            }
            catch (IOException e) {
            }
            System.out.println("Build Model Failed...Train Data is Null");
            return;
        }
    }

    public void move_file(String file_name){

        File sdCardRoot = Environment.getExternalStorageDirectory();

        File featuresDir = new File(sdCardRoot, getResources().getString(R.string.features_file_path));;
        File tobeuploadedDir = new File(sdCardRoot, getResources().getString(R.string.archive_features_file_path));

        if(!tobeuploadedDir.exists()) {
            tobeuploadedDir.mkdirs();
        }

        File sourceLocation = new File(featuresDir, file_name);
        File targetLocation = new File(tobeuploadedDir, file_name);

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

            //Now delete the mood file from the DataFiles location
            if(sourceLocation.exists()) {
                sourceLocation.delete();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMoodFromIndex(int randomNum) {
        switch(randomNum) {
            case 0: return "Happy";
            case 1: return "Sad";
            case 2: return "Stressed";
            case 3: return "Relaxed";
        }
        return "";
    }

    public String RetrieveLastMood(){
        String ctr="Happy";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.last_mood), "Happy");
            System.out.println("[ExtractFeatures]: Last Mood Retreive : " + ctr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ctr;
    }

    public void StoreLastMood(String lastMood) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        System.out.println("[BuildModel]: Last Mood Stored : " + lastMood);
        log_editor.putString(getResources().getString(R.string.last_mood), lastMood);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveModelFileCtr() {

        String ctr="000000";

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.model_file_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoreModelFileCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.model_file_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    private double getMoodIndex(String str) {
        switch(str) {
            case "Happy": return 0;
            case "Sad": return 1;
            case "Stressed": return 2;
            case "Relaxed": return 3;
        }
        return -1;
    }

    private Instances setAttributes(){
        Instances data;
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        // - numeric
        // TODO: MINE Add features here as well ? Perhaps not. This is for final model building
        atts.add(new Attribute("MSI",0));
        atts.add(new Attribute("RMSI",1));
        atts.add(new Attribute("SessionLength",2));
        atts.add(new Attribute("BackSpacePercentage",3));
        atts.add(new Attribute("SplCharPercentage",4));
        atts.add(new Attribute("SessionDuration",5));
        // - nominal
        ArrayList<String> attVals2 = new ArrayList<String>();
        attVals2.add("Happy");
        attVals2.add("Sad");
        attVals2.add("Stressed");
        attVals2.add("Relaxed");
        atts.add(new Attribute("LastMood", attVals2, 6));

        ArrayList<String> attVals = new ArrayList<String>();
        attVals.add("Happy");
        attVals.add("Sad");
        attVals.add("Stressed");
        attVals.add("Relaxed");
        atts.add(new Attribute("MoodState", attVals, 7));

        // 2. create Instances object
        data = new Instances("TapFeaturesRelation", atts, 0);

        //System.out.println(data);

        return data;
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
