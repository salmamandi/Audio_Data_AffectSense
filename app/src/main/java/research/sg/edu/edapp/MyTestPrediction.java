package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class MyTestPrediction extends IntentService {


    public MyTestPrediction() {
        super("MyTestPrediction");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            predictData();
        }
    }

    private void predictData() {

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File featureDir = new File(sdCardRoot, getString(R.string.test_features_file_path));
        File modelDir = new File(sdCardRoot, getResources().getString(R.string.model_file_path));

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));

        final File[] modelFiles = modelDir.listFiles();
        Arrays.sort(modelFiles);
        Instances data = null;
        for (File modelFile : modelFiles) {
            //Load Model Object
            RandomForest rf = null;
            String modelPath = modelFile.getAbsolutePath();
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelPath));
                rf = (RandomForest) in.readObject();
                in.close();
            } catch (Exception e) {

            }

            data = setAttributes();
            String lastMood = RetrieveLastMood();

            File filefolder = new File(String.valueOf(featureDir));
            if (filefolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".txt");
                }
            }) != null) {
                final File[] files = filefolder.listFiles();
                Arrays.sort(files);
                for (File file : files) {
                    if (file.isDirectory())
                        continue;
                    else if (file != null) {
                        System.out.println(file);

                        InputStream instream;
                        try {
                            instream = new FileInputStream(file);
                            if (instream != null) {
                                InputStreamReader inputreader = new InputStreamReader(instream);
                                BufferedReader buffreader = new BufferedReader(inputreader);
                                String line[];
                                String temp;
                                temp = buffreader.readLine();
                                while ((temp = buffreader.readLine()) != null) {
                                    line = temp.split(",");
                                    // TODO: Maybe don't add features here, but check what line prints
                                    System.out.print(">= Line bits : " + line);
                                    double[] values = new double[]{Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3]),
                                            Double.parseDouble(line[4]), Double.parseDouble(line[5]), Double.parseDouble(line[6]), getMoodIndex(lastMood), getMoodIndex(line[9])};
                                    Instance instance = new DenseInstance(1, values);
                                    System.out.println(instance);
                                    data.add(instance);
                                    lastMood = line[9];
                                }
                                instream.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(data);
                    }
                }
            }

            data.setClassIndex(data.numAttributes() - 1);
            //Start Prediction for Each Feature Row
            for (int i = 0; i < data.numInstances(); i++) {
                //Classify Instance Using Model
                double pred = 0;
                try {
                    pred = rf.classifyInstance(data.instance(i));
                } catch (Exception e) {
                }
                System.out.print("== Actual : " + data.classAttribute().value((int) data.instance(i).classValue()));
                System.out.println(" == Predict : " + data.classAttribute().value((int) pred));
            }
        }
    }

    public String RetrieveLastMood(){
        String ctr="Mood";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.last_mood), "Mood");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ctr;
    }

    private double getMoodIndex(String str) {
        switch(str) {
            case "Happy": return 0;
            case "Sad": return 1;
            case "Stressed": return 2;
            case "Relaxed": return 3;
        }
        return -1;
    }

    private Instances setAttributes(){
        Instances data;
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        // - numeric
        // TODO MINE Attributes NOT YET MAYBE?
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

        // 2. create Instances object
        data = new Instances("TapFeaturesRelation", atts, 0);

        //System.out.println(data);

        return data;
    }

}
