package research.sg.edu.edapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class SendNotification extends Service{


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Send Notification has started");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        StartNotification();
        return START_STICKY;
    }

    private void startForegroundService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Send Notification has destroyed");
        //CancelAlarm();
    }

    public void CancelAlarm(){
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        manager.cancel(pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void StartNotification() {
        SharedPreferences mPreferences;
        int count=0;
        System.out.println("I am before");
        Log.d("SendNotification:", "Sending notification ");
        mPreferences=getApplicationContext().getSharedPreferences("CounterFile",Context.MODE_PRIVATE);
        count=mPreferences.getInt("TimeCounter",-1);
        // check existence of variable TimeCounter
        if(count==-1) {
            SharedPreferences.Editor mEditor = mPreferences.edit();
            mEditor.putInt("TimeCounter", 0);
            // counter for uploading data
            //mEditor.putInt("UploadCounter",0);
            System.out.println("We are here to update time counter and upload counter");
            WriteFile("We are here to update time counter and upload counter");
            mEditor.apply();
            mEditor.commit();
        }
        System.out.println("Time counter already exist");
        WriteFile("Time counter already exist");
        Intent intent = new Intent(SendNotification.this, NotificationSevice.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 10*60* 1000, pendingIntent);
        //manager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 15*60*1000, 15*60*1000, pendingIntent);

        //manager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 15*60*1000,15*60*1000, pendingIntent);
        System.out.println("I am after");
    }

    private void WriteFile(String text) {
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


}

