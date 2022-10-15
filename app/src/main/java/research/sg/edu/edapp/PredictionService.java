package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PredictionService extends IntentService {

    private String testDir = "Test_Tap/";

    private static String tap_ctr="000000";

    public PredictionService() {
        super("PredictionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            RecordTapMood();

            StartExtractFeaturesService();
        }
    }

    public void StartExtractFeaturesService() {
        System.out.println("[MoodRecorder]: ExtractFeaturesService is running");
        //  WriteAlarmFiringTime(5);

        Intent intent = new Intent(PredictionService.this,ExtractFeaturesService.class);
        intent.putExtra("isTestPhase",1);
        this.startService(intent);
    }

    public void RecordTapMood() {

        String imei_no;
        String moodRecordTimestamp, tap_file_name;
        String typing_session_no;

        File tap_file;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = telephonyManager.getDeviceId();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        moodRecordTimestamp = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File tapDataDir = new File(sdCardRoot, getResources().getString(R.string.tap_file_path));

        if(!tapDataDir.exists()) {
            tapDataDir.mkdirs();
        }

        String string_t="";

        String PROVIDER_NAME = "research.sg.edu.edapp.kb.KbContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);
        ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);

        try {
            Cursor tCursor = CR.query(CONTENT_URI, null, null, null, null);

            typing_session_no=RetrieveTypingSession();
            tap_ctr=RetrieveTapCtr();

            tap_file_name = imei_no + "_" + tap_ctr + getResources().getString(R.string.tap_file_postfix);
            tap_file = new File(tapDataDir, tap_file_name);

            if(tap_file.exists()) {
                tap_file.delete();
            }

            //process_meta_data_typing(tCursor,typing_session_no,getMoodId(radioMoodButton));

            tCursor.moveToFirst();
            while (!tCursor.isAfterLast()) {

                string_t="";
                //string_t=typing_session_no + ","+ tCursor.getString(1) + "," + tCursor.getString(2)+ "," + tCursor.getString(3) +"," + getMoodId(radioMoodButton) + "," + getUsrExpId(usrexpbtn) + "," + moodRecordTimestamp +"\n";
                string_t=typing_session_no + ","+ tCursor.getString(1) + "," + tCursor.getString(2)+ "," + tCursor.getString(3) + ",Mood,"  + moodRecordTimestamp +"\n";
                System.out.println(typing_session_no + "," + tCursor.getLong(0) + "," + tCursor.getString(1) + "," + tCursor.getString(2) + "," + tCursor.getString(3) + "," + moodRecordTimestamp);

                try{
                    FileOutputStream fos = new FileOutputStream(tap_file,true);
                    fos.write(string_t.getBytes());
                    fos.close();
                }catch(Exception e) {
                    //Log.d("EXCEPTION", e.getMessage());
                }

                //System.out.println(tCursor.getLong(0) + "," + tCursor.getString(1) + "," + tCursor.getString(2) + "," + tCursor.getString(3));
                tCursor.moveToNext();
            }
            tCursor.close();



            //  int tap_file_size = Integer.parseInt(String.valueOf(tap_file.length() / 1024));
//
            //          int tap_file_size_threshold = Integer.parseInt(getResources().getString(R.string.tap_file_size_limit));
            //        System.out.println("Tap File Size:" + tap_file_size + ", Tap File Threshold:" + tap_file_size_threshold );

            //      if (tap_file_size > tap_file_size_threshold) {
            int ctr = (Integer.parseInt (tap_ctr) + 1) % 999999 ;
            tap_ctr=String.valueOf(ctr);
            tap_ctr=String.format("%06d", Integer.parseInt(tap_ctr));
            //String.format("%05d", Integer.parseInt(mood_ctr));
            //    }

            int session_no=(Integer.parseInt(typing_session_no)+1) % 999999;
            typing_session_no=String.valueOf(session_no);
            typing_session_no=String.format("%06d", Integer.parseInt(typing_session_no));

            StoreTapCtr(tap_ctr);
            StoretypingSession(typing_session_no);

            int deleted_rows = CR.delete(CONTENT_URI, null, null);
            System.out.println("Number of deleted entries:" +deleted_rows);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Store additional features
        //mean_itd, #backspace_key,#splsymbol_key,#touch_count,#erased_text_length,typ_dur,time_of_day
    }

    public void StoreTapCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.tap_ctr_test), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveTapCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.tap_ctr_test), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoretypingSession(String session_no){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.typing_session_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.typing_session_no_test), session_no);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveTypingSession(){

        String session="000001";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.typing_session_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.typing_session_sharedpref_file), Context.MODE_MULTI_PROCESS);

            session = pref.getString(getResources().getString(R.string.typing_session_no_test), "000001");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return session;
    }

}
