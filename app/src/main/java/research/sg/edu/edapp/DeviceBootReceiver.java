package research.sg.edu.edapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class DeviceBootReceiver extends BroadcastReceiver {
    public DeviceBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i=new Intent(context,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            StoreRunningStatus(context,true);
            Toast.makeText(context, "AffectSense Restarted..", Toast.LENGTH_SHORT).show();
            //RestoreServices(context);
        }
    }

    public void RestoreServices(Context context){
        Intent intent = new Intent(context,MainActivity.class);


        //RestoreNotification(context);
        context.startService(intent);
        StoreRunningStatus(context,true);

    }

    public void StoreRunningStatus(Context context,boolean flag){

        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putBoolean(context.getResources().getString(R.string.sharedpref_running_status), flag);
        run_editor.apply();
        run_editor.commit();
    }
    public void RestoreNotification(Context context){
        Intent intent1=new Intent(context,SendNotification.class);
        context.startService(intent1);
    }

}
