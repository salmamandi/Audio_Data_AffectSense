package research.sg.edu.edapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import research.sg.edu.edapp.ExtAudioRecorder;

public class AudioRecorder extends AppCompatActivity {
    // Initializing all variables..
    private TextView startTV, stopTV,statusTV,textread,speakTV;
    private Chronometer simpleChronometer;
    // creating a variable for medi recorder object class.
    private MediaRecorder mRecorder;
    // creating a variable for mediaplayer class
    private MediaPlayer mPlayer;
    // string variable is created for storing a file name
    private static String mFileName = null;
    private static ExtAudioRecorder instance;
    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 101;
    public static String read=null;
    public static String typing_session_no,popUpTimeStamp,moodRecordTimestamp;
    public static int mood_id;

    public static int flag=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);
        // initialize all variables with their layout items.
        statusTV = findViewById(R.id.idTVstatus);
        startTV = findViewById(R.id.btnRecord);
        speakTV=findViewById(R.id.btnSpeak);
        stopTV = findViewById(R.id.btnStop);
        textread=findViewById(R.id.textstream);
        simpleChronometer=(Chronometer) findViewById(R.id.simpleChronometer);
        // setting invisible the timer
        simpleChronometer.setVisibility(View.INVISIBLE);
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        Intent intent = getIntent();
        read= intent.getStringExtra("text_to_speak");
        typing_session_no=intent.getStringExtra("Typing_session_no");
        popUpTimeStamp=intent.getStringExtra("popup_timestamp");
        mood_id=intent.getIntExtra("mood_ID",100);
        moodRecordTimestamp=intent.getStringExtra("mood_record_timestamp");
        textread.setText(read);
        startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start recording method will
                // start the recording of audio.
                if(flag==0){

                }
                else {
                    flag = 1;
                    startRecording();
                }
            }
        });

        stopTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause Recording method will
                // pause the recording of audio.
                if(flag==0)
                    simpleChronometer.stop();
                pauseRecording();
                //for send back the flag
                RecordTapInfo(flag,typing_session_no,popUpTimeStamp,mood_id,moodRecordTimestamp);

            }
        });

        //writing action when clicking on recording speak button
        speakTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(flag==1){

                }
                else {
                    flag = 0;
                    simpleChronometer.setVisibility(View.VISIBLE);
                    simpleChronometer.start();
                    startRecording();
                }
            }
        });
    }

    private void RecordTapInfo(int flag, String typing_session_no, String popUpTimeStamp, int mood_id, String moodRecordTimestamp) {
        File tap_file;
        String tap_file_name;
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        if(!dataDir.exists()){
            dataDir.mkdirs();
        }
        String string_t="";
        try {
            tap_file_name = getResources().getString(R.string.tap_file_postfix);
            tap_file = new File(dataDir, tap_file_name);
            string_t=typing_session_no+","+popUpTimeStamp+","+mood_id+","+ moodRecordTimestamp +","+flag+"\n";
            try{
                FileOutputStream fos = new FileOutputStream(tap_file,true);
                fos.write(string_t.getBytes());
                fos.close();
            }catch(Exception e) {
                //Log.d("EXCEPTION", e.getMessage());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void pauseRecording() {
          //this lines are for recording wav file
          /*instance.release();

          statusTV.setText("Recording Play Stopped");*/
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        speakTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        mRecorder.stop();
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
        statusTV.setText("Recording Stopped");
    }


    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record nd store the audio.
        if (CheckPermissions()) {
            // setbackgroundcolor method will change
            // the background color of text view.
            if(flag==1) {
                stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
                startTV.setBackgroundColor(getResources().getColor(R.color.gray));

            }
            else if(flag==0){
                stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
                speakTV.setBackgroundColor(getResources().getColor(R.color.gray));
            }
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "AffectSense/saved_audio");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("audio", "failed to create audio directory");
                    //return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());

            String mediaFile;
            //mediaFile = new File(mediaStorageDir.getPath() + File.separator
            //+ "sound_" + timeStamp + ".wav");
            mFileName = mediaStorageDir.getPath() + "/" + "sound_" + timeStamp + ".3gp";
            System.out.println("print name of directory:" + mFileName);


            //written this code block for recording wav file
            /*instance = ExtAudioRecorder.getInstanse(false);
            instance.setOutputFile(mediaFile);
            instance.prepare();
            System.out.println("state of recorder:"+instance.getState());*/
            /*while(instance.getState()==ExtAudioRecorder.State.READY||instance.getState()==ExtAudioRecorder.State.RECORDING) {
                instance.CalltoRecord();
                System.out.println("state of recorder:"+instance.getState());
            }*/
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // the output format of the audio.
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            // audio encoder for our recorded audio.
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // output file location for our recorded audio
            mRecorder.setOutputFile(mFileName);
            try {
                // below method will prepare
                // our audio recorder class
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            mRecorder.start();
            statusTV.setText("Recording Started");



        }
        else{
            RequestPermissions();
        }

        }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;

    }


}
