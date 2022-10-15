package research.sg.edu.edapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification_broadcast extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationStart(context);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void NotificationStart(Context context) {
        String Channel_ID = "12345";
        String PopUpTimestamp;
        //Bundle b=new Bundle();

        System.out.println("Hi! I am in Notification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Firing Reminder";
            String description = "pop-up and Make a sound";

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

}