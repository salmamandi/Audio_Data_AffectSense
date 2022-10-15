package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import research.sg.edu.edapp.FinalClasses.AppCategory;
import research.sg.edu.edapp.FinalClasses.FeaturesDetails;
import research.sg.edu.edapp.FinalClasses.StatsDetails;
import research.sg.edu.edapp.ModelClasses.BaseDataFileClass;
import research.sg.edu.edapp.ModelClasses.FeatureDataFileClass;
import weka.classifiers.trees.RandomForest;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ExtractFeaturesService extends IntentService {

    private int isTestPhase = 0;
    private String testCtr = "";
    private String testDir = "";
    private static String sessionId_ctr ="000001";

    public ExtractFeaturesService() {
        super("ExtractFeaturesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        isTestPhase = extras.getInt("isTestPhase");

        if(isTestPhase == 1) {
            testCtr = "Test";
            testDir = "Test_Tap/";
        }

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.tap_file_path));

        File filefolder = new File(String.valueOf(dataDir));
        final File[] files = filefolder.listFiles();
        Arrays.sort(files);

        for (File tap_file : files) {
            if(!tap_file.isDirectory()) {
                ArrayList<BaseDataFileClass> mylist;
                MyLoadArchive(tap_file);
                //mylist = CleaningData(tap_file);
                //CalculateITDs(mylist);
            }
        }
    }

    private void MyLoadArchive(File tap_file) {
        String tap_file_name = tap_file.getName();
        move_file(tap_file_name);
    }

    //Load Tap Files, Clean Data & Save To A List
    private ArrayList<BaseDataFileClass> CleaningData(File tap_file) {

        ArrayList<BaseDataFileClass> list = new ArrayList<BaseDataFileClass>();

        String tap_file_name = tap_file.getName();
        System.out.println("Tap File Name : " + tap_file_name);
        System.out.println("Tap File : " + tap_file);
        InputStream instream;

        // open the file for reading
        try {
            instream = new FileInputStream(tap_file);

            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line[];
                String temp;
                BaseDataFileClass entry;
                while ((temp = buffreader.readLine()) != null) {
                    line = temp.split(",");
                    // TODO: MINE add entries here BaseData
                    entry = new BaseDataFileClass();
                    entry.setSessionId(line[0]);
                    entry.setAppName(line[1]);
                    entry.setTapTime(line[2]);
                    entry.setTapKey(line[3]);

                    entry.setMoodState(line[4]);
                    entry.setRecordTime(line[5]);

                    // need to calculate mean pressure, vel, duration
                    entry.setPressure(line[6]);
                    entry.setVelocity(line[7]);
                    entry.setSwipeDuration(line[8]);

                    list.add(entry);
                }
                instream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        move_file(tap_file_name);

        ArrayList<BaseDataFileClass> newlist = new ArrayList<BaseDataFileClass>();

        if(!list.isEmpty()) {
            BaseDataFileClass newentry;
            BaseDataFileClass entry;

            int count=0;
            // TODO: FEATURE ADDITION  BASEDATA done
            String sessionId,appName,tapTime,tapKey,moodState = null,recordTime, pressure, velocity, swipeDuration;
            System.out.println("[Reading List Again]");
            Date tapt,recordt;
            for (int i = 0; i < list.size(); i++) {

                sessionId_ctr=readSessionId();
                entry = list.get(i);
                // BASEDATA
                sessionId=entry.getSessionId();
                appName=entry.getAppName();
                tapTime=entry.getTapTime();
                tapKey=entry.getTapKey();
                recordTime=entry.getRecordTime();
                pressure = entry.getPressure();
                velocity = entry.getVelocity();
                swipeDuration = entry.getSwipeDuration();

                //if(isTestPhase != 1) {
                    moodState = entry.getMoodState();
                    if (moodState.equalsIgnoreCase("-99")) {
                        continue;
                    }
                //}
                
                if (appName.equalsIgnoreCase("Dummy_Pkg")) {
                    continue;
                }

                newentry = new BaseDataFileClass();
                newentry.setSessionId(sessionId_ctr);
                newentry.setAppName(appName);
                newentry.setTapTime(tapTime);
                newentry.setTapKey(tapKey);
                newentry.setRecordTime(recordTime);
                newentry.setPressure(pressure);
                newentry.setVelocity(velocity);
                newentry.setSwipeDuration(swipeDuration);

                //if(isTestPhase!=1) {
                    newentry.setMoodState(moodState);
                //}

                newlist.add(newentry);
                count++;
                //System.out.println(newentry.getSessionId()+","+newentry.getTapKey()+","+newentry.getAppName()+","+newentry.getTapTime()+","+newentry.getRecordTime());
                if(i != list.size()-1) {
                    BaseDataFileClass e2 = list.get(i + 1);
                    if (!appName.equalsIgnoreCase(e2.getAppName()) || !sessionId.equalsIgnoreCase(e2.getSessionId())) {
                        tapt=convert_to_date(tapTime);
                        recordt=convert_to_date(recordTime);
                        if (count < Integer.parseInt(getResources().getString(R.string.smallSessionThreshold)) ||
                                find_time_diff(recordt,tapt) >= Integer.parseInt(getResources().getString(R.string.recordTimeOut))*60*60*1000) {
                            /*if(count < Integer.parseInt(getResources().getString(R.string.smallSessionThreshold)))
                                System.out.println("1. Reject Small "+ count);
                            else
                                System.out.println("1. Expired Sessions " + tapt + " ___ " + recordt);*/
                            while (count != 0) {
                                newlist.remove(newlist.size() - count);
                                count--;
                            }
                        }
                        else {
                            int ctr2 = Integer.parseInt(sessionId_ctr) + 1;
                            sessionId_ctr = String.format("%06d", Integer.parseInt(String.valueOf(ctr2)));
                            storeSessionId(sessionId_ctr);
                        }
                        count=0;
                    }
                }

                else {
                    tapt=convert_to_date(tapTime);
                    recordt=convert_to_date(recordTime);
                    if (count < Integer.parseInt(getResources().getString(R.string.smallSessionThreshold)) ||
                            find_time_diff(recordt,tapt) >= Integer.parseInt(getResources().getString(R.string.recordTimeOut))*60*60*1000) {
/*                        if(count < Integer.parseInt(getResources().getString(R.string.smallSessionThreshold)))
                            System.out.println("2. Reject Small "+ count);
                        else
                            System.out.println("2. Expired Sessions " + tapt + " ___ " + recordt);*/
                        while (count != 0) {
                            newlist.remove(newlist.size() - count);
                            count--;
                        }
                    }
                    else {
                        int ctr2 = Integer.parseInt(sessionId_ctr) + 1;
                        sessionId_ctr = String.format("%06d", Integer.parseInt(String.valueOf(ctr2)));
                        storeSessionId(sessionId_ctr);
                    }
                    count = 0;
                }
            }
        }
        return newlist;
    }

    //Process List, Extract Features, Predict** & Save Features To A File
    // TODO: Resolve how to compute ITD if tapped after swype
    private void CalculateITDs(ArrayList<BaseDataFileClass> mylist) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.features_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        BaseDataFileClass e1;
        BaseDataFileClass e2;

        Date t1,t2;

        if(!mylist.isEmpty()) {

            ArrayList<FeatureDataFileClass> featureList = new ArrayList<FeatureDataFileClass>();
            FeatureDataFileClass featureEntry = null;

            e1 = mylist.get(0);

            String sessionMood = e1.getMoodState();
            float ssi = 0;
            int no_itds = 0;

            int session_length = 1, no_splchar=0, no_backspace = 0;

            // TODO: Logging mean PVD
            float mpressure = Float.valueOf(e1.getPressure()), mvelocity = Float.valueOf(e1.getVelocity());
            float mswipeDuration = Float.valueOf(e1.getSwipeDuration());
            int keyAscii = Integer.parseInt(e1.getTapKey());
            if(keyAscii==-5)
                no_backspace += 1;
            else if(!(keyAscii>=48 && keyAscii<=57) && !(keyAscii>=65 && keyAscii<=90) && !(keyAscii>=97 && keyAscii<=122))
                no_splchar += 1;

            Date sDur2, sDur1 = convert_to_date(e1.getTapTime());
            List<Float> myITDs = new ArrayList<Float>();

            // String typeTime, swipeTime, modPressure, sdPressure, sdVelocity, isSwipe;
            // TODO: FEATURE ADDITION RESUME HERE
            //Extracting Features
            for (int i = 1; i < mylist.size(); i++) {
                e2 = mylist.get(i);

                mpressure += Float.valueOf(e2.getPressure());
                mvelocity += Float.valueOf(e2.getVelocity());
                mswipeDuration += Float.valueOf(e2.getSwipeDuration());

                //MSI Calculcations
                if(e1.getSessionId().equals(e2.getSessionId())) {
                    t1 = convert_to_date(e1.getTapTime());
                    t2 = convert_to_date(e2.getTapTime());
                    float diff = find_time_diff(t2, t1)/1000;

                    if(diff<= Integer.parseInt(getString(R.string.longTap_threshold))) {
                        myITDs.add(diff);
                        ssi += diff;
                        no_itds += 1;
                    }

                    session_length += 1;
                    keyAscii = Integer.parseInt(e2.getTapKey());
                    if(keyAscii==-5)
                        no_backspace += 1;
                    else if(!(keyAscii>=48 && keyAscii<=57) && !(keyAscii>=65 && keyAscii<=90) && !(keyAscii>=97 && keyAscii<=122))
                        no_splchar +=1;
                }

                //RMSI Calculcations
                if(!e1.getSessionId().equals(e2.getSessionId()) || i==mylist.size()-1) {
                    if(i==mylist.size()-1)
                        sDur2 = convert_to_date(e1.getTapTime());
                    else
                        sDur2 = convert_to_date(e2.getTapTime());

                    featureEntry = new FeatureDataFileClass();

                    if(no_itds>=40) {
                        float msi = ssi / no_itds;
                        float sd = calculateSD(myITDs, msi, no_itds);

                        float threshold = msi + 3 * sd;

                        ssi = 0;
                        for (int j = 0; j < myITDs.size(); j++) {
                            if (threshold < myITDs.get(j)) {
                                myITDs.remove(j);
                                j--;
                                no_itds--;
                                continue;
                            }
                            ssi += myITDs.get(j);
                        }

                        if (no_itds >= 40) {
                            msi = ssi/no_itds;
                            mpressure = mpressure/no_itds;
                            mvelocity = mvelocity/no_itds;
                            mswipeDuration = mswipeDuration/no_itds;

                            // TODO: MINE ITD related features continued
                            //float sessionDur = find_time_diff(sDur2, sDur1)/1000;
                            //featureEntry.setSessionDuration(String.format("%06f",Float.parseFloat(String.valueOf(find_time_diff(sDur2, sDur1)/1000))));
                            featureEntry.setSessionDuration(String.format("%06f", Float.parseFloat(String.valueOf(ssi))));
                            featureEntry.setBackspace_percentage(String.format("%03f", Float.parseFloat(String.valueOf((float)no_backspace*100/session_length))));
                            featureEntry.setSplchar_percentage(String.format("%03f", Float.parseFloat(String.valueOf((float)no_splchar*100/session_length))));
                            featureEntry.setSessionLength(String.valueOf(session_length));
                            featureEntry.setSessionId(e1.getSessionId());
                            featureEntry.setAppName(e1.getAppName());
                            featureEntry.setRecordTime(e1.getRecordTime());
                            featureEntry.setmpressure(String.format("%06f", Float.parseFloat(String.valueOf(mpressure))));
                            featureEntry.setmvelocity(String.format("%06f", Float.parseFloat(String.valueOf(mvelocity))));
                            featureEntry.setmswipeDuration(String.format("%06f", Float.parseFloat(String.valueOf(mswipeDuration))));

                            featureEntry.setMsi(String.format("%06f", msi));

                            featureEntry.setMoodState(e1.getMoodState());

                            Instances data;
                            ArrayList<Attribute> atts = new ArrayList<Attribute>();
                            // - numeric
                            atts.add(new Attribute("RMSI",0));

                            // 2. create Instances object
                            data = new Instances("RMSI Values", atts, 0);
                            for (int j = 0; j < myITDs.size(); j++) {
                                double[] values = new double[] {myITDs.get(j)};
                                Instance instance = new DenseInstance(1,values);
                                data.add(instance);
                            }
                            //System.out.println(instance);
                            //System.out.println(data);

                            SimpleKMeans kmeans = new SimpleKMeans();

                            kmeans.setSeed(10);
                            kmeans.setPreserveInstancesOrder(true);

                            try {
                                kmeans.setNumClusters(2);
                                kmeans.buildClusterer(data);
                                int[] assignments = kmeans.getAssignments();
                                int j = 0;
                                int c1 = 0, c2 = 0;
                                float itd1 = 0, itd2 = 0;

                                Collections.sort(myITDs);

                                for (int clusterNum : assignments) {
                                    if (clusterNum == 0) {
                                        c1++;
                                        itd1 += Float.parseFloat(String.valueOf(data.instance(j).value(0)));
                                    } else if (clusterNum == 1) {
                                        c2++;
                                        itd2 += Float.parseFloat(String.valueOf(data.instance(j).value(0)));
                                    }
                                    j++;
                                }

                                float mitd1 = itd1 / c1, mitd2 = itd2 / c2;
                                int z = (int) (myITDs.size() * 0.8);
                                float ritd = 0;

                                if (c1 > c2) {
                                    if (mitd1 > mitd2) {
                                        //Take bottom 80% from list (Major Cluster has more ITDs)
                                        for (int k = myITDs.size() - 1; k >= myITDs.size() - z; k--) {
                                            ritd += myITDs.get(k);
                                        }
                                    } else {
                                        //Take top 80% from list (Minor Cluster has more ITDs)
                                        for (int k = 0; k < z; k++) {
                                            ritd += myITDs.get(k);
                                        }
                                    }
                                } else {
                                    if (mitd2 > mitd1) {
                                        //Take bottom 80% from list (Major Cluster has more ITDs)
                                        for (int k = myITDs.size() - 1; k >= myITDs.size() - z; k--) {
                                            ritd += myITDs.get(k);
                                        }
                                    } else {
                                        //Take top 80% from list (Minor Cluster has more ITDs)
                                        for (int k = 0; k < z; k++) {
                                            ritd += myITDs.get(k);
                                        }
                                    }
                                }

                                float rmsi = ritd / z;
                                featureEntry.setRmsi(String.format("%06f", rmsi));
                            } catch (Exception e) {
                                e.printStackTrace();
                                TopExceptionHandler(e);
                            }
                            // TODO: new features mean printed
                            System.out.println("Feature Entry "+featureList.size()+":"+featureEntry.getMsi()+","+featureEntry.getRmsi()+","+featureEntry.getSessionLength()
                                    +","+featureEntry.getSessionDuration()+","+featureEntry.getBackspace_percentage()+","+featureEntry.getSplchar_percentage()+","+featureEntry.getAppName()+","+featureEntry.getMoodState()
                                     +","+ featureEntry.getmpressure()+","+featureEntry.getmvelocity()+","+featureEntry.getmswipeDuration());
                            featureList.add(featureEntry);
                        }
                    }
                    ssi = 0;
                    no_itds = 0;
                    myITDs.clear();

                    sDur1 = convert_to_date(e2.getTapTime());

                    session_length = 1;
                    no_backspace = 0;
                    no_splchar = 0;
                    keyAscii = Integer.parseInt(e2.getTapKey());
                    if(keyAscii==-5)
                        no_backspace += 1;
                    else if(!(keyAscii>=48 && keyAscii<=57) && !(keyAscii>=65 && keyAscii<=90) && !(keyAscii>=97 && keyAscii<=122))
                        no_splchar +=1;
                }
                e1=mylist.get(i);
            }

            if(featureList.size()>0) {

                //List<String> featureEmotions = new ArrayList<String>();
                if(isTestPhase == 1) {
                    //Predict Features
                    predictData(featureList);
                }

                if(isTestPhase != 1) {
                    //Maintain Train Features Count
                    int featuresCount = RetrieveFeaturesCount(getString(R.string.train_features_count));
                    featuresCount += featureList.size();
                    StoreFeaturesCount(getString(R.string.train_features_count), featuresCount);

                    //Maintain Count for Each Features (Train Phase)
                    int count = RetrieveFeaturesCount(getStringResourceByName(getMood(sessionMood).trim().toLowerCase() + "_features_count"));
                    count += featureList.size();
                    StoreFeaturesCount(getStringResourceByName(getMood(sessionMood).trim().toLowerCase() + "_features_count"), count);

                    //Writing Features to a File
                    FileOutputStream fos;
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String imei_no = telephonyManager.getDeviceId();

                    String feature_file_ctr = RetrieveFeatureFileCtr();
                    String feature_file_name = imei_no + "_" + feature_file_ctr + getResources().getString(R.string.features_file_postfix);
                    File featureDataFile = new File(dataDir, feature_file_name);

                    String fileData = "";

                    if (!featureDataFile.exists()) {
                        // TODO; MINE ITD part 3 and 4 on line 475
                        fileData = "SessionId,MSI,RMSI,SessionLength,BackSpacePercentage,SplCharPercentage,SessionDuration,AppName,RecordTime,MoodState, mPressure, mVelocity, mSwipeDuration\n";
                        byte[] file_data = fileData.getBytes();

                        try {
                            fos = new FileOutputStream(featureDataFile, true);
                            fos.write(file_data);
                            fos.close();
                        } catch (IOException e) {
                            //Log.e("Exception", "File write failed: " + e.toString());
                        }
                    }

                    for (int i = 0; i < featureList.size(); i++) {
                        featureEntry = featureList.get(i);
                        fileData = featureEntry.getSessionId() + "," + featureEntry.getMsi() + "," + featureEntry.getRmsi() + "," + featureEntry.getSessionLength()
                                + "," + featureEntry.getBackspace_percentage() + "," + featureEntry.getSplchar_percentage() + "," + featureEntry.getSessionDuration() + "," +
                                featureEntry.getAppName() + "," + featureEntry.getRecordTime() + "," + getMood(featureEntry.getMoodState()) +
                                "," + featureEntry.getmpressure() + "," + featureEntry.getmvelocity() + "," + featureEntry.getmswipeDuration() + "\n";

                        byte[] file_data = fileData.getBytes();

                        try {
                            fos = new FileOutputStream(featureDataFile, true);
                            fos.write(file_data);
                            fos.close();
                        } catch (IOException e) {
                            //Log.e("Exception", "File write failed: " + e.toString());
                        }

                        //Storing Train Features
                        // TODO: Added here
                        StoreFeatures(getApplicationContext(), featureEntry.getSessionId(), featureEntry.getMsi(), featureEntry.getRmsi(),
                                featureEntry.getSessionLength(), featureEntry.getBackspace_percentage(), featureEntry.getSplchar_percentage(),
                                featureEntry.getSessionDuration(), featureEntry.getAppName(), featureEntry.getRecordTime(), getMood(featureEntry.getMoodState()),
                                featureEntry.getmpressure(), featureEntry.getmvelocity(), featureEntry.getmswipeDuration());
                        //Storing Train Stats
                        StoreStatsDetail(getApplicationContext(), featureEntry.getAppName(), featureEntry.getRecordTime(), getMood(featureEntry.getMoodState()));
                    }

                    int flag=0;
                    //Maintain Features File Size & CTR
                    int feature_file_size = Integer.parseInt(String.valueOf(featureDataFile.length() / 1024));
                    int feature_file_size_threshold = Integer.parseInt(getResources().getString(R.string.feature_file_size_limit));
                    if (feature_file_size > feature_file_size_threshold) {
                        int ctr = Integer.parseInt(feature_file_ctr) + 1;
                        feature_file_ctr = String.valueOf(ctr);
                        feature_file_ctr = String.format("%06d", Integer.parseInt(feature_file_ctr));
                        flag=1;
                        StoreFeatureFileCtr(feature_file_ctr);
                    }

                    //Checking TrainPhase Exit Condition
                    int trainData = RetrieveFeaturesCount(getString(R.string.train_features_count));
                    if (trainData > RetrieveTrainPhaseThreshold()) {
                        ExitTrainingPhase();
                        StartBuildModelService();

                        if(flag==0) {
                            int ctr = Integer.parseInt(feature_file_ctr) + 1;
                            feature_file_ctr = String.valueOf(ctr);
                            feature_file_ctr = String.format("%06d", Integer.parseInt(feature_file_ctr));
                            StoreFeatureFileCtr(feature_file_ctr);
                        }
                    }
                }
            }
        }
    }

    //Get Value of String Resource By Their Name
    private String getStringResourceByName(String aString) {
        String packageName = "research.sg.edu.edapp";
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    //Predict Mood For a List of Features & Return List of Emotions Corresponding to Features List
    private void predictData(ArrayList<FeatureDataFileClass> featureList) {
        List<String> featureEmotions = new ArrayList<String>();
        String[] lastModelEmotions = new String[featureList.size()];

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        File modelDir = new File(sdCardRoot, getResources().getString(R.string.model_file_path));

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));

        final File[] modelFiles = modelDir.listFiles();
        Arrays.sort(modelFiles);
        double[][] result = new double[featureList.size()][modelFiles.length];

        FeatureDataFileClass featureEntry;
        Instances data = setAttributes();
        String lastMood = RetrieveLastMood();

        //Prepare Instances
        for (int i = 0; i < featureList.size(); i++) {
            featureEntry = featureList.get(i);
            double[] values = new double[]{Double.parseDouble(featureEntry.getMsi()), Double.parseDouble(featureEntry.getRmsi()), Double.parseDouble(featureEntry.getSessionLength()),
                    Double.parseDouble(featureEntry.getBackspace_percentage()), Double.parseDouble(featureEntry.getSplchar_percentage()), Double.parseDouble(featureEntry.getSessionDuration()), getMoodIndex(lastMood), 0};
            Instance instance = new DenseInstance(1, values);
            //System.out.println("Instance " + (i + 1) + " " + instance);
            data.add(instance);
        }
        data.setClassIndex(data.numAttributes() - 1);

        int j=0;
        int[] happyCounter = new int[featureList.size()];
        int[] sadCounter = new int[featureList.size()];
        int[] stressedCounter = new int[featureList.size()];
        int[] relaxedCounter = new int[featureList.size()];

        for (File modelFile : modelFiles) {
            //Load Model Object
            RandomForest rf = null;
            String modelPath = modelFile.getAbsolutePath();
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelPath));
                rf = (RandomForest) in.readObject();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                TopExceptionHandler(e);
            }

            //Start Prediction for Each Feature Row
            for (int i = 0; i < featureList.size(); i++) {
                //Classify Instance Using Model
                double pred = 0;
                try {
                    pred = rf.classifyInstance(data.instance(i));
                    result[i][j] = pred;

                    if(pred == 0) {
                        happyCounter[i]++;
                    }
                    else if(pred == 1) {
                        sadCounter[i]++;
                    }
                    else if(pred == 2) {
                        stressedCounter[i]++;
                    }
                    else if(pred == 3) {
                        relaxedCounter[i]++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    TopExceptionHandler(e);
                    continue;
                }
                System.out.println("Instance " + (i + 1) + " " + data.instance(i));
                System.out.println("=>> Predict : " + data.classAttribute().value((int) pred) + " with model =>> " + modelFile.getName());
            }
            j++;
        }

        FileOutputStream fos;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei_no = telephonyManager.getDeviceId();

        File predictionsDir = new File(sdCardRoot, getResources().getString(R.string.prediction_file_path));

        if(!predictionsDir.exists())
            predictionsDir.mkdirs();

        String feature_file_ctr = RetrieveFeatureFileCtr();
        String feature_file_name = imei_no + "_" + feature_file_ctr + getResources().getString(R.string.features_file_postfix);
        File featureDataFile = new File(predictionsDir, feature_file_name);

        String fileData = "";

        if (!featureDataFile.exists()) {
            fileData = "SessionId,MSI,RMSI,SessionLength,BackSpacePercentage,SplCharPercentage,SessionDuration,AppName,RecordTime,MoodState,mPressure,mVelocity,mSwipeDuration\n";
            byte[] file_data = fileData.getBytes();

            try {
                fos = new FileOutputStream(featureDataFile, true);
                fos.write(file_data);
                fos.close();
            } catch (IOException e) {
                //Log.e("Exception", "File write failed: " + e.toString());
            }
        }

        int happyCount=0,sadCount=0,stressedCount=0,relaxedCount=0;
        List<String> maxEmotions = new ArrayList<String>();

        for (int i = 0; i < featureList.size(); i++) {
            if (happyCounter[i] >= sadCounter[i] && happyCounter[i] >= stressedCounter[i] && happyCounter[i] >= relaxedCounter[i])
                maxEmotions.add("Happy");
            else if (sadCounter[i] >= happyCounter[i] && sadCounter[i] >= stressedCounter[i] && sadCounter[i] >= relaxedCounter[i])
                maxEmotions.add("Sad");
            else if (stressedCounter[i] >= happyCounter[i] && stressedCounter[i] >= sadCounter[i] && stressedCounter[i] >= relaxedCounter[i])
                maxEmotions.add("Stressed");
            else if (relaxedCounter[i] >= sadCounter[i] && relaxedCounter[i] >= stressedCounter[i] && relaxedCounter[i] >= happyCounter[i])
                maxEmotions.add("Relaxed");

            int k = (int)(Math.random() * maxEmotions.size());
            featureEmotions.add(maxEmotions.get(k));

            int count=0;
            featureEntry = featureList.get(i);
            featureEntry.setMoodState(featureEmotions.get(i));

            //Store Features for Last Model
            StoreFeatures(getApplicationContext(), featureEntry.getSessionId(), featureEntry.getMsi(), featureEntry.getRmsi(),
                    featureEntry.getSessionLength(), featureEntry.getBackspace_percentage(), featureEntry.getSplchar_percentage(),
                    featureEntry.getSessionDuration(), featureEntry.getAppName(), featureEntry.getRecordTime(), data.classAttribute().value((int) result[i][modelFiles.length-1]),
                    "0.0","0.0","0.0"
                    /**/);
            // TODO: FEATURES here to be added in training, not sure how
            //Save Prediction Features
            fileData = featureEntry.getSessionId() + "," + featureEntry.getMsi() + "," + featureEntry.getRmsi() + "," + featureEntry.getSessionLength()
                    + "," + featureEntry.getBackspace_percentage() + "," + featureEntry.getSplchar_percentage() + "," + featureEntry.getSessionDuration() + "," +
                    featureEntry.getAppName() + "," + featureEntry.getRecordTime() + "," + featureEntry.getMoodState() + "," + "0.0,0.0,0.0" + "\n";

            byte[] file_data = fileData.getBytes();

            try {
                fos = new FileOutputStream(featureDataFile, true);
                fos.write(file_data);
                fos.close();
            } catch (IOException e) {
            }

            ////Maintain Count for Each Features (Test Phase)
            count = RetrieveFeaturesCount(getStringResourceByName(featureEmotions.get(i).toLowerCase().trim()+"_test_features_count"));
            count += 1;
            StoreFeaturesCount(getStringResourceByName(featureEmotions.get(i).toLowerCase().trim()+"_test_features_count"),count);

            String currTime = sdf.format(new Date());
            String countout = featureEmotions.get(i) + "," + String.valueOf(count);

            //Checking For Retraining Condition
            if(featureEmotions.get(i).equals("Happy")) {
                countout += "," + RetrieveFeaturesCount(getString(R.string.happy_features_count)) +"," + currTime+"\n";
                System.out.println("Happy Count : " +RetrieveFeaturesCount(getString(R.string.happy_features_count)) +", Test Count"+ count);
                happyCount++;
                if(RetrieveFeaturesCount(getString(R.string.happy_features_count)) < count && RetrieveFeaturesCount(getString(R.string.happy_features_count))!=0) {
                    findRetraining(0,2,featureEntry.getSessionId());        //moodIndex for KLD, moodId
                }
            }
            else if(featureEmotions.get(i).equals("Sad")) {
                countout += "," + RetrieveFeaturesCount(getString(R.string.sad_features_count)) +"," + currTime+"\n";
                System.out.println("Sad Count : " +RetrieveFeaturesCount(getString(R.string.sad_features_count)) +", Test Count : "+ count);
                sadCount++;
                if(RetrieveFeaturesCount(getString(R.string.sad_features_count)) < count && RetrieveFeaturesCount(getString(R.string.sad_features_count))!=0) {
                    findRetraining(1,-2,featureEntry.getSessionId());        //moodIndex for KLD, moodId
                }
            }
            else if(featureEmotions.get(i).equals("Stressed")) {
                countout += "," + RetrieveFeaturesCount(getString(R.string.stressed_features_count)) +"," + currTime+"\n";
                System.out.println("Stressed Count : " +RetrieveFeaturesCount(getString(R.string.stressed_features_count)) +", Test Count"+ count);
                stressedCount++;
                if(RetrieveFeaturesCount(getString(R.string.stressed_features_count)) < count && RetrieveFeaturesCount(getString(R.string.stressed_features_count))!=0) {
                    findRetraining(2,1,featureEntry.getSessionId());        //moodIndex for KLD, moodId
                }
            }
            else if(featureEmotions.get(i).equals("Relaxed")) {
                countout += "," + RetrieveFeaturesCount(getString(R.string.relaxed_features_count)) +"," + currTime+"\n";
                System.out.println("Relaxed Count : " +RetrieveFeaturesCount(getString(R.string.relaxed_features_count)) +", Test Count"+ count);
                relaxedCount++;
                if(RetrieveFeaturesCount(getString(R.string.relaxed_features_count)) < count && RetrieveFeaturesCount(getString(R.string.relaxed_features_count))!=0) {
                    findRetraining(3,0,featureEntry.getSessionId());        //moodIndex for KLD, moodId
                }
            }

            String count_log = getString(R.string.log_file_path) + getString(R.string.testcount_log_file_name);
            byte[] count_data = countout.getBytes();

            File prediction_file = new File(dataDir, count_log);
            try {
                fos = new FileOutputStream(prediction_file,true);
                fos.write(count_data);
                fos.close();
            }
            catch (IOException e) {
                //Log.e("Exception", "File write failed: " + e.toString());
            }
        }

        String LastMood=findMood(happyCount,sadCount,stressedCount,relaxedCount,featureEmotions);
        StoreLastMood(LastMood);

        //Store Confirm Features
        for (int i = 0; i < featureList.size(); i++) {
            featureEntry = featureList.get(i);
            // TODO; MINE ITD part 5
            StoreConfirmFeatures(getApplicationContext(), featureEntry.getSessionId(), featureEntry.getMsi(), featureEntry.getRmsi(),
                    featureEntry.getSessionLength(), featureEntry.getBackspace_percentage(), featureEntry.getSplchar_percentage(),
                    featureEntry.getSessionDuration(), featureEntry.getAppName(), featureEntry.getRecordTime(), LastMood,
                    "0.0","0.0","0.0");
        }

        //Maintain Features File Size & CTR
        int feature_file_size = Integer.parseInt(String.valueOf(featureDataFile.length() / 1024));
        int feature_file_size_threshold = Integer.parseInt(getResources().getString(R.string.feature_file_size_limit));
        if (feature_file_size > feature_file_size_threshold) {
            int ctr = Integer.parseInt(feature_file_ctr) + 1;
            feature_file_ctr = String.valueOf(ctr);
            feature_file_ctr = String.format("%06d", Integer.parseInt(feature_file_ctr));
            StoreFeatureFileCtr(feature_file_ctr);
        }
    }

    public void StoreLastMood(String lastMood) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        System.out.println("[ExtractFeatures]: Last Mood Stored : " + lastMood);
        log_editor.putString(getResources().getString(R.string.last_mood), lastMood);
        log_editor.apply();
        log_editor.commit();
    }
    public double getMoodIndex(String str) {
        switch(str) {
            case "Happy": return 0;
            case "Sad": return 1;
            case "Stressed": return 2;
            case "Relaxed": return 3;
        }
        return -1;
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

    public String findMood(int a, int b, int c, int d, List<String> featureEmotions) {
        String[] mood = new String[4];
        if (a >= b && a >= c && a >= d)
            mood[0]="Happy";
        else if (b >= a && b >= c && b >= d)
            mood[1]="Sad";
        else if (c >= a && c >= b && c >= d)
            mood[2]="Stressed";
        else if (d >= b && d >= c && d >= a)
            mood[3]="Relaxed";

        for(int i=featureEmotions.size()-1;i>=0;i--)
            for(int j=0;j<4;j++)
                if(featureEmotions.get(i).equals(mood[j])) {
                    return mood[j];
                }
        return "";
    }

    //Initialize Instances With Attributes & Class
    private Instances setAttributes(){
        Instances data;
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        // - numeric
        // TODO: MINE features part 100000
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
        atts.add(new Attribute("mPressure", 8));
        atts.add(new Attribute("mVelocity", 9));
        atts.add(new Attribute("mSwipeDuration", 10));

        // 2. create Instances object
        data = new Instances("TapFeaturesRelation", atts, 0);

        //System.out.println(data);

        return data;
    }

    //Get List of Features (Train or Test) for a Mood
    public ArrayList<FeatureDataFileClass> FetchLists(Context context, int moodId, int isTest) {

        ArrayList<FeatureDataFileClass>featureMood1 = new ArrayList<FeatureDataFileClass>();

        try {

            Cursor tCursor;
            if(isTest==0) {
                ContentProviderClient CR = context.getContentResolver().acquireContentProviderClient(FeaturesProvider.CONTENT_URI);

                tCursor = CR.query(FeaturesProvider.CONTENT_URI, new String[]{FeaturesDetails.FeaturesEntry.MSI, FeaturesDetails.FeaturesEntry.RMSI,
                                FeaturesDetails.FeaturesEntry.SESSIONLEN, FeaturesDetails.FeaturesEntry.BACKSPACEPER, FeaturesDetails.FeaturesEntry.SPLCHARPER,
                                FeaturesDetails.FeaturesEntry.SESSIONDUR, FeaturesDetails.FeaturesEntry.EMOTION,
                                FeaturesDetails.FeaturesEntry.PRESSURE, FeaturesDetails.FeaturesEntry.VELOCITY,
                                FeaturesDetails.FeaturesEntry.SWIPEDURATION},
                        FeaturesDetails.FeaturesEntry.EMOTION + " = ?", new String[]{String.valueOf(moodId)}, null);
            }
            else {
                ContentProviderClient CR = context.getContentResolver().acquireContentProviderClient(TestFeaturesProvider.CONTENT_URI);

                tCursor = CR.query(TestFeaturesProvider.CONTENT_URI, new String[]{FeaturesDetails.FeaturesEntry.MSI, FeaturesDetails.FeaturesEntry.RMSI,
                                FeaturesDetails.FeaturesEntry.SESSIONLEN, FeaturesDetails.FeaturesEntry.BACKSPACEPER, FeaturesDetails.FeaturesEntry.SPLCHARPER,
                                FeaturesDetails.FeaturesEntry.SESSIONDUR, FeaturesDetails.FeaturesEntry.EMOTION,
                                FeaturesDetails.FeaturesEntry.PRESSURE, FeaturesDetails.FeaturesEntry.VELOCITY,
                                FeaturesDetails.FeaturesEntry.SWIPEDURATION},
                        FeaturesDetails.FeaturesEntry.EMOTION + " = ?", new String[]{String.valueOf(moodId)}, null);
            }
            tCursor.moveToFirst();

            FeatureDataFileClass featureEntry = null;
            while (!tCursor.isAfterLast()) {
                featureEntry = new FeatureDataFileClass();

                featureEntry.setMsi(tCursor.getString(0));
                featureEntry.setRmsi(tCursor.getString(1));
                featureEntry.setSessionLength(tCursor.getString(2));
                featureEntry.setBackspace_percentage(tCursor.getString(3));
                featureEntry.setSplchar_percentage(tCursor.getString(4));
                featureEntry.setSessionDuration(tCursor.getString(5));
                featureEntry.setMoodState(getMood(String.valueOf(moodId)));
                featureEntry.setmpressure(tCursor.getString(6));
                featureEntry.setmvelocity(tCursor.getString(7));
                featureEntry.setmswipeDuration(tCursor.getString(8));
                featureMood1.add(featureEntry);

                tCursor.moveToNext();
            }
            tCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureMood1;
    }
    //Find whether retraining is required or not
    public void findRetraining(int moodIndex, int moodId, String sessionID) {
        System.out.println("Checking for Retraining");
        FeatureDataFileClass e1;

        double[][] KLD = new double[4][6];
        KLD = RetrieveKldMatrix();

        ArrayList<FeatureDataFileClass> featureMood1;
        featureMood1 = FetchLists(getApplicationContext(),moodId,0);

        ArrayList<FeatureDataFileClass> testfeatureMood1;
        testfeatureMood1 = FetchLists(getApplicationContext(),moodId,1);

        double[] f1 = new double[featureMood1.size()];
        double[] f2 = new double[featureMood1.size()];
        double[] f3 = new double[featureMood1.size()];
        double[] f4 = new double[featureMood1.size()];
        double[] f5 = new double[featureMood1.size()];
        double[] f6 = new double[featureMood1.size()];

        double[] f11 = new double[featureMood1.size()];
        double[] f12 = new double[featureMood1.size()];
        double[] f13 = new double[featureMood1.size()];
        double[] f14 = new double[featureMood1.size()];
        double[] f15 = new double[featureMood1.size()];
        double[] f16 = new double[featureMood1.size()];


        for(int i=0;i<featureMood1.size();i++) {
            e1 = featureMood1.get(i);
            f11[i] = Double.parseDouble(e1.getMsi());
            f12[i] = Double.parseDouble(e1.getRmsi());
            f13[i] = Double.parseDouble(e1.getSessionLength());
            f14[i] = Double.parseDouble(e1.getBackspace_percentage());
            f15[i] = Double.parseDouble(e1.getSplchar_percentage());
            f16[i] = Double.parseDouble(e1.getSessionDuration());
        }

        int reqFeatures = featureMood1.size();

        int i=0;

        Collections.shuffle(testfeatureMood1);

        while(reqFeatures!=0) {
            //System.out.print(reqFeatures + " 1--->> ");
            //System.out.println(testfeatureMood1.size()-reqFeatures);
            // TODO: MINE F
            e1 = testfeatureMood1.get(testfeatureMood1.size()-reqFeatures);
            f1[i] = Double.parseDouble(e1.getMsi());
            f2[i] = Double.parseDouble(e1.getRmsi());
            f3[i] = Double.parseDouble(e1.getSessionLength());
            f4[i] = Double.parseDouble(e1.getBackspace_percentage());
            f5[i] = Double.parseDouble(e1.getSplchar_percentage());
            f6[i] = Double.parseDouble(e1.getSessionDuration());
            reqFeatures--;
            i++;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currTime = sdf.format(new Date());

        String outtext=sessionID+","+currTime+","+getMood(String.valueOf(moodId));

        int count = 0;
        int count2 = 0;
        if( KLD[moodIndex][0] < klDivergence(f1,f11) ){
            count++;
            count2++;
            outtext+= ",Msi("+ KLD[moodIndex][0] + "," + klDivergence(f1,f11) +")";
            System.out.println("Msi Wants Retraining");
        }
        if( KLD[moodIndex][1] < klDivergence(f2,f12) ){
            count++;
            count2++;
            outtext+= ",RMsi("+ KLD[moodIndex][1] + "," + klDivergence(f2,f12) +")";
            System.out.println("RMsi Wants Retraining");
        }
        if( KLD[moodIndex][2] < klDivergence(f3,f13) ){
            count++;
            outtext+= ",SLeng("+ KLD[moodIndex][2] + "," + klDivergence(f3,f13) +")";
            System.out.println("SessionLength Wants Retraining");
        }
        if( KLD[moodIndex][3] < klDivergence(f4,f14) ){
            count++;
            outtext+= ",BackS("+ KLD[moodIndex][3] + "," + klDivergence(f4,f14) +")";
            System.out.println("Backspace Wants Retraining");
        }
        if( KLD[moodIndex][4] < klDivergence(f5,f15) ){
            count++;
            outtext+= ",SplC("+ KLD[moodIndex][4] + "," + klDivergence(f5,f15) +")";
            System.out.println("Splchar Wants Retraining");
        }
        if( KLD[moodIndex][5] < klDivergence(f6,f16) ) {
            count++;
            outtext+= ",SDur("+ KLD[moodIndex][5] + "," + klDivergence(f6,f16) +")";
            System.out.println("SessionDuration Wants Retraining");
        }

        if(count>=3 || count2==2) {
            outtext+= ",Needed\n";
            EnterTrainingPhase();
        }
        else
            outtext+= ",NotNeeded\n";

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        String retraining_log = getString(R.string.log_file_path) + getString(R.string.retraining_log_file_name);
        byte[] retraining_data = outtext.getBytes();

        File prediction_file = new File(dataDir, retraining_log);
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(prediction_file,true);
            fos.write(retraining_data);
            fos.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
        System.out.println(outtext);
    }
    public static double sum(double...values) {
        double result = 0;
        for (double value:values)
            result += value;
        return result;
    }
    public static double klDivergence(double[] p1, double[] p2) {

        final double log2 = Math.log(2);
        double klDiv = 0.0;

        double s1 = sum(p1);
        double s2 = sum(p2);

        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] == 0) { continue; }
            if (p2[i] == 0.0) { continue; } // Limin

            klDiv += (p1[i]/s1) * Math.log( (p1[i]/s1) / (p2[i]/s2) );
        }

        return klDiv
                ; // moved this division out of the loop -DM
    }
    public double[][] RetrieveKldMatrix(){

        String kld_str="";
        double[][] kld = new double[4][6];

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.kld_vector_file), Context.MODE_MULTI_PROCESS);

            kld_str = pref.getString(getResources().getString(R.string.kld_vector), "0");

            String[] kldStrArr = kld_str.split(",");

            int k=0;
            for(int i=0;i<4;i++) {
                for(int j=0;j<6;j++) {
                    kld[i][j] = Double.parseDouble(kldStrArr[k++]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return kld;

    }

    //Exit from Training Phase
    public void ExitTrainingPhase() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putBoolean(getResources().getString(R.string.sharedpref_training_phase), false);
        log_editor.apply();
        log_editor.commit();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExtractFeaturesService.this,"Training Phase Ends",Toast.LENGTH_SHORT).show();
            }
        });

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File logDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        String train_log = getString(R.string.log_file_path) + getString(R.string.trainPhase_log_file_name);
        //String errout = currentDateandTime+" Build Model Failed (No Train Data File Exists)\n";
        String out = currentDateandTime+" Train Phase End"+"\n";

        byte[] file_data = out.toString().getBytes();

        File error_file = new File(logDir, train_log);
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(error_file,true);
            fos.write(file_data);
            fos.close();
        }
        catch (IOException e1) {
            //Log.e("Exception", "File write failed: " + e1.toString());
        }
        System.out.println("Training Phase End");
    }

    //Call BuildModelService
    public void StartBuildModelService() {
        System.out.println("[MasterService]: BuildModelService is running");
        //Toast.makeText(this,"Model Building Starts",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ExtractFeaturesService.this,BuildModelService.class);
        this.startService(intent);
    }

    //Enter to Training Phase
    public void EnterTrainingPhase() {

        StoreFeaturesCount(getString(R.string.train_features_count),0);

        StoreFeaturesCount(getString(R.string.happy_features_count),0);
        StoreFeaturesCount(getString(R.string.sad_features_count),0);
        StoreFeaturesCount(getString(R.string.stressed_features_count),0);
        StoreFeaturesCount(getString(R.string.relaxed_features_count),0);

        StoreFeaturesCount(getString(R.string.happy_test_features_count),0);
        StoreFeaturesCount(getString(R.string.sad_test_features_count),0);
        StoreFeaturesCount(getString(R.string.stressed_test_features_count),0);
        StoreFeaturesCount(getString(R.string.relaxed_test_features_count),0);

        getContentResolver().delete(ConfirmFeaturesProvider.CONTENT_URI, null, null);
        getContentResolver().delete(FeaturesProvider.CONTENT_URI, null, null);
        getContentResolver().delete(TestFeaturesProvider.CONTENT_URI, null, null);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putBoolean(getResources().getString(R.string.sharedpref_training_phase), true);
        log_editor.apply();
        log_editor.commit();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExtractFeaturesService.this,"Training Phase Starts",Toast.LENGTH_SHORT).show();            }
        });


        File sdCardRoot = Environment.getExternalStorageDirectory();
        File logDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        String train_log = getString(R.string.log_file_path) + getString(R.string.trainPhase_log_file_name);
        //String errout = currentDateandTime+" Build Model Failed (No Train Data File Exists)\n";
        String out = currentDateandTime+" Train Phase Start"+"\n";

        byte[] file_data = out.toString().getBytes();

        File error_file = new File(logDir, train_log);
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(error_file,true);
            fos.write(file_data);
            fos.close();
        }
        catch (IOException e1) {
            //Log.e("Exception", "File write failed: " + e1.toString());
        }
        System.out.println("Training Phase Start");
    }

    public String getMood(String moodInt) {
        switch(moodInt) {
            case "-2": return "Sad";
            case "2": return "Happy";
            case "1": return "Stressed";
            case "0": return "Relaxed";
        }
        return "";
    }

    //Store Stats to Content Provider
    public void StoreStatsDetail(Context context,String appName,String recordTime, String emotion){

        ContentValues values = new ContentValues();
        /*System.out.println("Stats Detail -->> " + appName + "_" + recordTime + "_" + findAppCat(appName) + "_" + isWeekend(recordTime) + "_" +
                                                                                                           findDaySession(recordTime) + "_" + emotion);*/
        values.put(StatsDetails.StatsEntry.APP_NAME, appName);
        values.put(StatsDetails.StatsEntry.TIMESTAMP, recordTime);
        values.put(StatsDetails.StatsEntry.APP_CAT, findAppCat(appName));
        values.put(StatsDetails.StatsEntry.WEEKEND, isWeekend(recordTime));
        values.put(StatsDetails.StatsEntry.DAYSESSION, findDaySession(recordTime));
        values.put(StatsDetails.StatsEntry.EMOTION, getEmoId(emotion));
        Uri uri = context.getContentResolver().insert(StatsProvider.CONTENT_URI, values);
    }

    //Store Features to Content Provider
    public void StoreFeatures(Context context,String sessionId, String msi, String rmsi, String sessionLength, String backSpacePer, String splCharPer,
                              String sessionDuration,String appName,String recordTime, String emotion,
                              String mpressure, String mvelocity, String mswipeduration) {
        ContentValues values = new ContentValues();
        // TODO: MINE F
        values.put(FeaturesDetails.FeaturesEntry.SESSIONID, Integer.parseInt(sessionId));
        values.put(FeaturesDetails.FeaturesEntry.MSI, Float.parseFloat(msi));
        values.put(FeaturesDetails.FeaturesEntry.RMSI, Float.parseFloat(rmsi));
        values.put(FeaturesDetails.FeaturesEntry.SESSIONLEN, Integer.parseInt(sessionLength));
        values.put(FeaturesDetails.FeaturesEntry.BACKSPACEPER, Float.parseFloat(backSpacePer));
        values.put(FeaturesDetails.FeaturesEntry.SPLCHARPER, Float.parseFloat(splCharPer));
        values.put(FeaturesDetails.FeaturesEntry.SESSIONDUR, Float.parseFloat(sessionDuration));
        values.put(FeaturesDetails.FeaturesEntry.APP_NAME, appName);
        values.put(FeaturesDetails.FeaturesEntry.TIMESTAMP, recordTime);
        values.put(FeaturesDetails.FeaturesEntry.EMOTION, getEmoId(emotion));
        values.put(FeaturesDetails.FeaturesEntry.PRESSURE, Float.parseFloat(mpressure));
        values.put(FeaturesDetails.FeaturesEntry.VELOCITY, Float.parseFloat(mvelocity));
        values.put(FeaturesDetails.FeaturesEntry.SWIPEDURATION, Float.parseFloat(mswipeduration));
        Uri uri;
        if(isTestPhase != 1)
            uri = context.getContentResolver().insert(FeaturesProvider.CONTENT_URI, values);
        else
            uri = context.getContentResolver().insert(TestFeaturesProvider.CONTENT_URI, values);
    }

    //Store Confirm Features to Content Provider
    public void StoreConfirmFeatures(Context context,String sessionId, String msi, String rmsi, String sessionLength, String backSpacePer, String splCharPer,
                              String sessionDuration,String appName,String recordTime, String emotion,
                                     String mpressure, String mvelocity, String mswipeduration) {
        ContentValues values = new ContentValues();
        // TODO: MINE F
        values.put(FeaturesDetails.FeaturesEntry.SESSIONID, Integer.parseInt(sessionId));
        values.put(FeaturesDetails.FeaturesEntry.MSI, Float.parseFloat(msi));
        values.put(FeaturesDetails.FeaturesEntry.RMSI, Float.parseFloat(rmsi));
        values.put(FeaturesDetails.FeaturesEntry.SESSIONLEN, Integer.parseInt(sessionLength));
        values.put(FeaturesDetails.FeaturesEntry.BACKSPACEPER, Float.parseFloat(backSpacePer));
        values.put(FeaturesDetails.FeaturesEntry.SPLCHARPER, Float.parseFloat(splCharPer));
        values.put(FeaturesDetails.FeaturesEntry.SESSIONDUR, Float.parseFloat(sessionDuration));
        values.put(FeaturesDetails.FeaturesEntry.APP_NAME, appName);
        values.put(FeaturesDetails.FeaturesEntry.TIMESTAMP, recordTime);
        values.put(FeaturesDetails.FeaturesEntry.EMOTION, getEmoId(emotion));
        values.put(FeaturesDetails.FeaturesEntry.PRESSURE, Float.parseFloat(mpressure));
        values.put(FeaturesDetails.FeaturesEntry.VELOCITY, Float.parseFloat(mvelocity));
        values.put(FeaturesDetails.FeaturesEntry.SWIPEDURATION, Float.parseFloat(mswipeduration));
        Uri uri = context.getContentResolver().insert(ConfirmFeaturesProvider.CONTENT_URI, values);
    }

    public int getEmoId(String emotion) {
        switch(emotion) {
            case "Sad": return -2;
            case "Happy": return 2;
            case "Stressed": return 1;
            case "Relaxed": return 0;
        }
        return 3;
    }
    public int findAppCat (String appName) {
        if(AppCategory.email.contains(appName))
            return 1;
        if(AppCategory.im.contains(appName))
            return 2;
        if(AppCategory.social.contains(appName))
            return 3;
        if(AppCategory.entertainment.contains(appName))
            return 4;
        if(AppCategory.surfing.contains(appName))
            return 5;
        return 0;
    }
    public int isWeekend (String recordTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = null;
        try {
            date = sdf.parse(recordTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int temp = calendar.get(Calendar.DAY_OF_WEEK);
        if(temp == 1 || temp == 7)
            return 1;
        return 0;
    }
    public int findDaySession (String recordTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        int hours = 0;
        try {
            Date date = sdf.parse(recordTime);
            SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
            String sTime=sdf1.format(date);
            hours = Integer.parseInt(sTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int tm = 3,ta = 11,te = 16,tn = 21;

        if(hours>=tm && hours<ta) {
            return 1;
        }
        if(hours>=ta && hours<te) {
            return 2;
        }
        if(hours>=te && hours<tn) {
            return 3;
        }
        else {
            return 4;
        }
    }

    public void move_file(String file_name){
        File sdCardRoot = Environment.getExternalStorageDirectory();

        File dataDir = new File(sdCardRoot, getResources().getString(R.string.tap_file_path)+testDir);;
        File tobeuploadedDir = new File(sdCardRoot, getResources().getString(R.string.archive_file_path) + testDir);

        if(!tobeuploadedDir.exists()) {
            tobeuploadedDir.mkdirs();
        }

        File sourceLocation = new File(dataDir, file_name);
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

        }
    }

    private float calculateSD(List<Float> myITDs, float msi, int no_itds) {
        float sd;
        float diffSum=0;
        for(int i =0 ; i<myITDs.size();i++) {
            diffSum += Math.pow(myITDs.get(i)-msi,2);
        }
        sd = (float) Math.sqrt(diffSum/no_itds);
        return sd;
    }

    public Date convert_to_date(String time){

        Date date=null;

        SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format));
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public float find_time_diff (Date dt1, Date dt2){

        float diff = dt1.getTime() - dt2.getTime();
        return diff;
    }

    public int RetrieveTrainPhaseThreshold(){
        String ctr=getString(R.string.default_trainphase_threshold);
        String ctr1=getString(R.string.default_trainphase_threshold);

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.mood_count_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getString(R.string.trainphase_threshold_ctr), ctr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(ctr == null || ctr.trim().equals("null"))
            return Integer.parseInt(ctr1);
        return Integer.parseInt(ctr);
    }

    public void StoreFeaturesCount(String feature_variable,int value){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_count_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(feature_variable, String.valueOf(value));
        log_editor.apply();
        log_editor.commit();
    }
    public int RetrieveFeaturesCount(String feature_variable){
        String ctr="0";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.mood_count_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(feature_variable, "0");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Integer.parseInt(ctr);
    }

    public void StoreFeatureFileCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.feature_file_ctr) + testCtr, ctr);
        log_editor.apply();
        log_editor.commit();
    }
    public String RetrieveFeatureFileCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.feature_file_ctr) + testCtr, "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void storeSessionId(String sessionId_ctr){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.sessionId_ctr) + testCtr, sessionId_ctr);
        log_editor.apply();
        log_editor.commit();
    }
    public String readSessionId(){
        String sessionId_ctr="000001";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
            sessionId_ctr = pref.getString(getResources().getString(R.string.sessionId_ctr) + testCtr, "000001");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return sessionId_ctr;
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
