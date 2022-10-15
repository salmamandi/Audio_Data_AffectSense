package research.sg.edu.edapp;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;


public class NotificationSevice extends BroadcastReceiver {

    private int count,upload_count;
    File sdCardRoot = Environment.getExternalStorageDirectory();
    StatFs stat=new StatFs(sdCardRoot.getPath());
    StatFs stat2= new StatFs(new File(sdCardRoot,"/AffectSense").getPath());



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        //StorageManager sm=context.getSystemService(StorageManager.class);// for accessing storage
        //StorageManager storageManager= (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);// for folder storage
        int storage_taken;
        System.out.println("File path=" + sdCardRoot.getPath());
        System.out.println("AffectSense path=" + new File(sdCardRoot, "/AffectSense").getPath());
        //
        //NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        try {
            Context con1 = context.createPackageContext("research.sg.edu.edapp", CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con1.getSharedPreferences("CounterFile", Context.MODE_MULTI_PROCESS);
            count = pref.getInt("TimeCounter", -1);
            //upload_count=pref.getInt("UploadCounter", 0);
            System.out.println("Count=" + count);
            WriteFile("Count:" + count);
            //System.out.println("UploadCount=" + upload_count);
            //WriteFile("UploadCount:"+upload_count);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (count < 8) {
            count += 1;
            SaveSharedPreferences(count, "TimeCounter", context);
        } else {
            System.out.println("do nothing");
            if (myKM.isKeyguardLocked()) {
                // it is locked
                System.out.println("screen is locked");
                WriteFile("Screen is locked");


            } else {
                WriteFile("Screen is unlocked:call NotificationStart");
                //it is not locked
                System.out.println("screen is unlocked");
                // storage percent taken by our folder
                //storage_taken=GetStorageTaken();
                storage_taken = numfiles();
                NotificationStart(storage_taken, context);

            }

        }

        try {
            if (numfiles() >= 2) {
                WriteFile("Uploading: sending data");
                if (isConnectingtoInternet(context)) {
                    WriteFile("Calling upload service");
                    Util.scheduleUpload(context);
                    // uncomment the blow three lines if salma's approach does not work
                    //Intent intent1 = new Intent(context, UploadService.class);
                    //intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //context.startService(intent1);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error in calculation");
            WriteFile("Error in calculation");
            if(isConnectingtoInternet(context)) {
                Intent intent1 = new Intent(context, UploadService.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(intent1);
            }
        }
    }
    private float GetStorageTaken() {
        float space_percent;
        // total storage
        float InternalTotalSpace=(float)getInternalTotalSpace(stat);
        System.out.println("Total space in storage="+InternalTotalSpace);
        // total taken
        float ExternalTotalSpace=(float)getDirectorySize(new File(sdCardRoot,"/AffectSense"));
        System.out.println("Total space in Affectsense external storage="+ExternalTotalSpace);
        space_percent=(float)((ExternalTotalSpace/InternalTotalSpace)*100.0);
        System.out.println(" space percentage="+space_percent);
        System.out.println("space percenatage long="+(ExternalTotalSpace/InternalTotalSpace)*100.0);
        return space_percent;
    }

    private boolean isConnectingtoInternet(Context context) {
        boolean N_status=false;
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) {
                System.out.println(" list of active network is null");
                WriteFile("list of active network is null");
                return N_status;
            }
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            System.out.println(" list of active network is not null");
            WriteFile("list of active network is not null");
            if(actNw!=null)
                System.out.println("Network capability is not null");
                WriteFile("Network capability is not null");

                N_status= (actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)));
                WriteFile("Network status:"+Boolean.toString(N_status));
                System.out.println("Network status:"+Boolean.toString(N_status));
                return N_status;
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            System.out.println("Network is available");
            WriteFile("Network is available");
            N_status=(nwInfo != null && nwInfo.isConnected());
            WriteFile("Network status:"+Boolean.toString(N_status));
            System.out.println("Network status:"+Boolean.toString(N_status));
            return N_status;
        }

        }



    private void SaveSharedPreferences(int count, String timeCounter, Context context) {
        SharedPreferences mPreferences;
        mPreferences = context.getSharedPreferences("CounterFile", Context.MODE_PRIVATE);
        WriteFile("updating variable "+timeCounter+" to"+String.valueOf(count));
        SharedPreferences.Editor mEditor = mPreferences.edit();
        mEditor.putInt(timeCounter, count);
        mEditor.apply();
        mEditor.commit();
    }

    private void check(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String PopupTime = sdf.format(new Date());
        System.out.println("PopUpTime=" + PopupTime);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 2);
        System.out.println(sdf.format(now.getTime()));
        Intent intent = new Intent(context, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void NotificationStart(int storage_taken, Context context) {
        String Channel_ID = "12345";
        String PopUpTimestamp;
        //Bundle b=new Bundle();

        System.out.println("Hi! I am in Notification");
        WriteFile("Hi! I am in Notification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Firing Reminder";
            String description = "pop-up and Make a sound";
            System.out.println("Notification can be sent");
            WriteFile("Notification can be sent");
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Channel_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Channel_ID);
        Intent notificationIntent = new Intent(context, MoodRecorder.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        PopUpTimestamp = sdf.format(new Date());
        System.out.println("Time=" + PopUpTimestamp);
        //b.putString("AppName",getApplicationName(context));
        //b.putString("PopUpTimeStamp",sdf.format(new Date()));
        notificationIntent.putExtra("PopUpTimeStamp", sdf.format(new Date()));
        System.out.println("sending number of files="+storage_taken);
        //notificationIntent.putExtra("StoragePercent",storage_taken);
        //notificationIntent.putExtra("Information",b);


        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Excuse me!")
                .setContentText("Record your mood")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL);

        builder.setContentIntent(contentIntent);
        //NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());


    }

    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public void CaptureImage(Context context) {
        Log.d("CaptureImage:", "It is starting");
        Intent intent = new Intent(context, snap_before.class);
        context.startActivity(intent);
    }
    public void WriteFile(String text){
        File sdCardRoot = Environment.getExternalStorageDirectory();

        File logFile=new File(sdCardRoot,"/AffectSense/logfile.txt");

        if(!logFile.exists()){
            try{
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            BufferedWriter buf= new BufferedWriter(new FileWriter(logFile,true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getInternalTotalSpace(StatFs stat)    {
        //Get total Bytes
        System.out.println("Block size="+ this.stat.getBlockSizeLong());
        System.out.println("Block count="+ this.stat.getBlockCountLong());
        System.out.println(this.stat.toString());
        long bytesTotal = (this.stat.getBlockSizeLong() * this.stat.getBlockCountLong());
        return bytesTotal;
    }
    public static String convertBytes (long size){
        long Kb = 1  * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        if (size <  Kb)                 return floatForm(        size     ) + " byte";
        if (size >= Kb && size < Mb)    return floatForm((double)size / Kb) + " KB";
        if (size >= Mb && size < Gb)    return floatForm((double)size / Mb) + " MB";
        if (size >= Gb && size < Tb)    return floatForm((double)size / Gb) + " GB";
        if (size >= Tb && size < Pb)    return floatForm((double)size / Tb) + " TB";
        if (size >= Pb && size < Eb)    return floatForm((double)size / Pb) + " PB";
        if (size >= Eb)                 return floatForm((double)size / Eb) + " EB";

        return "anything...";
    }
    public static String floatForm (double d)    {
        return new DecimalFormat("#.##").format(d);
    }
    public static long getDirectorySize(File dir) {
        long length = 0;
        File[] files = dir.listFiles();
        //System.out.println(files);
        if (files != null) {
            for (File file : files) {
                if (file.isFile())
                    length += file.length();
                else
                    length += getDirectorySize(file);
            }
        }
        //System.out.println("directory="+dir+",length="+length);
        return length;


    }

    public int numfiles(){
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dir=new File(sdCardRoot,"/AffectSense");
        int num_files=0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isFile()) {
                    num_files = getDirectoryFiles(file);
                }
            }
        }
        System.out.println("number of file="+Integer.toString(num_files));
        WriteFile("number of file="+Integer.toString(num_files));
        return num_files;
    }


    private int getDirectoryFiles(File dir) {
        File[] files = dir.listFiles();
        int num_files=0;
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    num_files += 1;
                }
            }
        }
        return num_files;
    }

    public Boolean CalculateSpace(){
        Boolean check_status=false;
        // total space in storage
        long InternalTotalSpace=getInternalTotalSpace(stat);
        System.out.println("Internal total space="+String.valueOf(InternalTotalSpace)+"bytes");
        // can use up to this limit
        long Spacelimit= (long) (InternalTotalSpace*.02);
        System.out.println("Space limit="+String.valueOf(Spacelimit));
        // already occupied
        //long InternalOccupiedSpace=getDirectorySize(sdCardRoot);
        //System.out.println("Total space in primary external storage="+InternalOccupiedSpace);
        //Total space taken by our folder
        long ExternalTotalSpace=getDirectorySize(new File(sdCardRoot,"/AffectSense"));
        System.out.println("Total space in Affectsense external storage="+ExternalTotalSpace);
        if(ExternalTotalSpace>=Spacelimit){
            check_status=true;
            return check_status;
        }
        return check_status;

    }


}