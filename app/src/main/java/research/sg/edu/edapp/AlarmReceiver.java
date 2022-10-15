package research.sg.edu.edapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        StartMasterService(context);
    }

    public void StartMasterService(Context context) {
        Log.d("MasterService","It is starting again");
        Intent intent = new Intent(context,MasterService.class);
        context.startService(intent);

    }
}
