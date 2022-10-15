package research.sg.edu.edapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      StartUploadService(context);
    }

    private void StartUploadService(Context context) {
     Log.d("UploadAlarm:","Starts again");
     SimpleDateFormat sdf= new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSS");
     WriteFile("UploadAlarm Starts Again:"+sdf.format(new Date()));
     Intent intent= new Intent(context,UploadService.class);
     context.startService(intent);
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
