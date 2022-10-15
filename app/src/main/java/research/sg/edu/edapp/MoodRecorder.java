package research.sg.edu.edapp;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import research.sg.edu.edapp.FinalClasses.EsmDetail;


public class MoodRecorder extends AppCompatActivity {

    private RadioGroup radioMoodGroup;
    private RadioButton radioMoodButton;
    private Button btnRecordMood;
    private Button btnTakePic;
    public static int flag=-1;
    private static String tap_ctr="000000";
    public static String PopUpTimeStamp;
    private static int storage_taken;
    private String ApplicationName;
    private static final String TAG = "MoodRecorder";
    private TextureView textureView;
    private Boolean mclose=false;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    //for recording audio
    private SqliteTableHelper tbhelper;






    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref=null;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.mood_popup);
        //TextView ThetextView=(TextView) findViewById(R.id.textView_1);
        Intent intent = getIntent();
       //Bundle b=intent.getBundleExtra("Information");
       //ApplicationName=b.getString("AppName");
       //PopUpTimeStamp=b.getString("PopUpTimeStamp");
        PopUpTimeStamp=intent.getStringExtra("PopUpTimeStamp");
        storage_taken=intent.getIntExtra("StoragePercent",0);

        //for printing audio text
        tbhelper=new SqliteTableHelper(MoodRecorder.this);

        // uncomment the below lines if want to print folat value upto two decimal
        //BigDecimal num=new BigDecimal(storage_taken).setScale(2, RoundingMode.HALF_UP);
        //float final_num=num.floatValue();
        //ThetextView.setText(String.valueOf(final_num)+"% storage has been consumed. Review the photos before it reaches to 2%.");
        //



        //ThetextView.setText(Integer.toString(storage_taken)+" photos are captured. Review the photos before it reaches to 4.");
        System.out.println("PopUpTimeStamp="+PopUpTimeStamp);
        WriteFile("PopUpTimeStamp="+PopUpTimeStamp);

       //System.out.println("ApplicationName="+ApplicationName);


        try {
            /*Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.keyboard_pkg), Context.CONTEXT_IGNORE_SECURITY);
            pref = con.getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);

            SharedPreferences.Editor seditor = pref.edit();
            seditor.putBoolean(getResources().getString(R.string.sharedpref_mood_rdy_to_record), false);
            seditor.apply();
            seditor.commit();*/
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.keyboard_pkg), Context.CONTEXT_IGNORE_SECURITY);
            pref = con.getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor log_editor = pref.edit();
            log_editor.putBoolean("sharedpref_status", false);
            log_editor.apply();
            log_editor.commit();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        addListenerOnButton(PopUpTimeStamp);
    }

    public void addListenerOnButton(final String popUpTimeStamp) {

        radioMoodGroup = (RadioGroup) findViewById(R.id.radioMoodGroup);
        btnRecordMood = (Button) findViewById(R.id.btnRecordMood);
        /*textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);*/

        /*btnTakePic = (Button) findViewById(R.id.btnTakePic);
        assert btnTakePic != null;
        btnTakePic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        // automatic image capture at 1st 4th and 7th second of popup.
        final Handler handler = new Handler();
        final Runnable runnable  = new Runnable(){
            int counter=0;
            @Override
            public void run() {
                btnTakePic.performClick();
                if(counter<2) {
                    System.out.println("Runnable count"+ counter);
                    handler.postDelayed(this, 1000);
                    counter++;
                }
            }
        };

        handler.postDelayed(runnable, 1000);*/



        btnRecordMood.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                int selectedId = radioMoodGroup.getCheckedRadioButtonId();
                int rand_no=0;
                Random rand = new Random();
                try {
                    radioMoodButton = (RadioButton) findViewById(selectedId);
                    //StartExtractFeaturesService();
                    //blocking it to stop on-device feature calculation

                    SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                    String esm_time = sdf.format(new Date());

                    //StoreESMDetail(getApplicationContext(),LastAppName,esm_time);
                    StoreESMTime(esm_time);
                    //handler.removeCallbacks(runnable);// add by salma, for image capture case

                    //method to record audio
                    int mood_id=getMoodId(radioMoodButton);
                    int rand_len=0;
                    String table_name;
                    if(mood_id==2)
                    {
                        rand_len=getResources().getStringArray(R.array.happy_text).length;
                        rand_no=rand.nextInt(rand_len);
                        table_name="Happy";
                        RecordAudio(tbhelper.readText(table_name,rand_no),popUpTimeStamp,radioMoodButton);


                    }
                    else if(mood_id==-2) {
                        rand_len=getResources().getStringArray(R.array.sad_text).length;
                        rand_no=rand.nextInt(rand_len);
                        table_name="Sad";
                        RecordAudio(tbhelper.readText(table_name,rand_no),popUpTimeStamp,radioMoodButton);

                    }
                    else if (mood_id==1) {

                        rand_len=getResources().getStringArray(R.array.stress_text).length;
                        rand_no=rand.nextInt(rand_len);
                        table_name="Stressed";
                        RecordAudio(tbhelper.readText(table_name,rand_no),popUpTimeStamp,radioMoodButton);

                    }

                    else if(mood_id==0) {
                        rand_len=getResources().getStringArray(R.array.relax_text).length;
                        rand_no=rand.nextInt(rand_len);
                        table_name="Relaxed";
                        RecordAudio(tbhelper.readText(table_name,rand_no),popUpTimeStamp,radioMoodButton);

                    }
                    else if(mood_id==-99) {
                        Log.e(TAG, "view vanished");
                        finish();
                    }

                    Log.e(TAG,"view vanished");
                    RecordTapMood(radioMoodButton,popUpTimeStamp);
                    finish();
                }
                catch(Exception e) {
                    Toast.makeText(MoodRecorder.this,"Please select your emotion", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void RecordAudio(String speak,String s, RadioButton radioMoodButton) {
        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        Intent audio_intent=new Intent(MoodRecorder.this,AudioRecorder.class);
        audio_intent.putExtra("text_to_speak",speak);
        audio_intent.putExtra("Typing_session_no",RetrieveTypingSession());
        audio_intent.putExtra("popup_timestamp",s);
        audio_intent.putExtra("mood_ID",getMoodId(radioMoodButton));
        audio_intent.putExtra("mood_record_timestamp",sdf.format(new Date()));
        startActivity(audio_intent);



    }


    private void DoSomething() {
        String PROVIDER_NAME = "research.sg.edu.edapp.kb.KbContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);
        ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);
        try{
            int deleted_rows = CR.delete(CONTENT_URI, null, null);
            System.out.println("Number of deleted entries:" +deleted_rows);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void StoreESMDetail(Context context,String esm_app,String esm_time){

        ContentValues values = new ContentValues();
        values.put(EsmDetail.EsmEntry.ESM_APP_NAME, esm_app);
        values.put(EsmDetail.EsmEntry.ESM_TIMESTAMP, esm_time);

        Uri uri = context.getContentResolver().insert(ESMContentProvider.CONTENT_URI, values);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(MoodRecorder.this,"Please select your emotion", Toast.LENGTH_SHORT).show();
    }

    public void StartExtractFeaturesService() {
        System.out.println("[MoodRecorder]: ExtractFeaturesService is running");

        Intent intent = new Intent(MoodRecorder.this,ExtractFeaturesService.class);
        intent.putExtra("isTestPhase",0);
        this.startService(intent);
    }

    public void RecordTapMood(RadioButton radioMoodButton, String popUpTimeStamp) throws PackageManager.NameNotFoundException {

        SharedPreferences mPreferences;
        String typing_session_no;



        try {
            typing_session_no=RetrieveTypingSession();
            int session_no=(Integer.parseInt(typing_session_no)+1) % 999999;
            typing_session_no=String.valueOf(session_no);
            typing_session_no=String.format("%06d", Integer.parseInt(typing_session_no));
            StoretypingSession(typing_session_no);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mPreferences=getApplicationContext().getSharedPreferences("CounterFile",Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor=mPreferences.edit();
        mEditor.putInt("TimeCounter",0);
        mEditor.apply();
        mEditor.commit();
    }

    public int getMoodId(RadioButton radioMoodButton) {

        int mood_id=-99;
        String mood_string;

        mood_string = (String)radioMoodButton.getText();


        switch(mood_string)
        {
            case "Sad or Depressed": mood_id=-2;
                break;
            case "Happy or Excited": mood_id = 2;
                break;
            case "Stressed": mood_id = 1;
                break;
            case "Relaxed": mood_id = 0;
                break;
            case "No Response": mood_id = -99;
                break;

        }
        return mood_id;
    }

    public void StoreTapCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.tap_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveTapCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.tap_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoretypingSession(String session_no){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.typing_session_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.typing_session_no), session_no);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveTypingSession(){

        String session="000001";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.typing_session_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.typing_session_sharedpref_file), Context.MODE_MULTI_PROCESS);

            session = pref.getString(getResources().getString(R.string.typing_session_no), "000001");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return session;
    }

    public void StoreESMTime(String esm_time){

        System.out.println("write esm time="+ esm_time);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor = pref.edit();
        log_editor.putString("sharedpref_ESM", esm_time);
        log_editor.apply();
        log_editor.commit();

    }

    /*TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //  Toast.makeText(MoodRecorder.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            //   Toast.makeText(MoodRecorder.this, "Image Captured" , Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    /*protected void takePicture() {


        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int mSensorOrientation =  characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,getOrientation(rotation,mSensorOrientation) );



            final File file = getOutputMediaFile();





            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //  Toast.makeText(MoodRecorder.this, "Image Captured", Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                    mclose=true;// salma update to get rid from session clossing problem
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }*/
    /*protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MoodRecorder.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }*/
    /*private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[1];      //getting front camera id, 0=rear, 1= front.
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            //


            //
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MoodRecorder.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        if(!mclose) { // salma add this if loop with mclose to get rid of session closing problem
            System.out.println("we came in update preview");// salma add this line extra to see
            try {
                cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }*/
    /*crea@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MoodRecorder.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        //startBackgroundThread();
        //uncomment when camera is needed (salma)
        /* if (textureView.isAvailable()) {
            openCamera();

        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }*/
    }
    @Override
    protected void onPause() {

        Log.e(TAG, "onPause");
        //closeCamera();
        //super.onPause();
        //stopBackgroundThread();//comment out by salma
        super.onPause();
    }

    protected void onStop() {

        super.onStop();

    }

    private static File getOutputMediaFile() throws PackageManager.NameNotFoundException {

        //uncomment the below line if there is any problem
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "AffectSense/saved_images");
        //For dropbox

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("mcamera", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }



    private int getOrientation(int rotation, int mSensorOrientation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
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



}
