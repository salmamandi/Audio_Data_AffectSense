package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import research.sg.edu.edapp.FinalClasses.FeaturesDetails;
import research.sg.edu.edapp.ModelClasses.FeatureDataFileClass;

public class CalculateKLD extends IntentService {

    private int totalCount=0;
    List<FeatureDataFileClass> featureMood1,featureMood2,featureMood3,featureMood4;
    double[][] KLD = new double[4][6];

    public CalculateKLD() {
        super("CalculateKLD");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            System.out.println("KLD Service Starts");
            FetchLists(getApplicationContext());
            if(featureMood1.size()!=0)
                CalculateKLD(featureMood1,featureMood2,featureMood3,featureMood4,0);
            else
                for(int i=0;i<Integer.parseInt(getString(R.string.no_of_features));i++)
                    KLD[0][i]=0;
            if(featureMood2.size()!=0)
                CalculateKLD(featureMood2,featureMood1,featureMood3,featureMood4,1);
            else
                for(int i=0;i<Integer.parseInt(getString(R.string.no_of_features));i++)
                    KLD[1][i]=0;
            if(featureMood3.size()!=0)
                CalculateKLD(featureMood3,featureMood1,featureMood2,featureMood4,2);
            else
                for(int i=0;i<Integer.parseInt(getString(R.string.no_of_features));i++)
                    KLD[2][i]=0;
            if(featureMood4.size()!=0)
                CalculateKLD(featureMood4,featureMood1,featureMood2,featureMood3,3);
            else
                for(int i=0;i<Integer.parseInt(getString(R.string.no_of_features));i++)
                    KLD[3][i]=0;
            StoreKldMatrix(KLD);
        }
    }

    public void FetchLists(Context context) {

        ContentProviderClient CR = context.getContentResolver().acquireContentProviderClient(FeaturesProvider.CONTENT_URI);
        try {

            Cursor tCursor = CR.query(FeaturesProvider.CONTENT_URI, new String[]{FeaturesDetails.FeaturesEntry.MSI,FeaturesDetails.FeaturesEntry.RMSI,
                    FeaturesDetails.FeaturesEntry.SESSIONLEN,FeaturesDetails.FeaturesEntry.BACKSPACEPER,FeaturesDetails.FeaturesEntry.SPLCHARPER,
                    FeaturesDetails.FeaturesEntry.SESSIONDUR,FeaturesDetails.FeaturesEntry.EMOTION/*,
                    FeaturesDetails.FeaturesEntry.PRESSURE, FeaturesDetails.FeaturesEntry.VELOCITY,
                    FeaturesDetails.FeaturesEntry.SWIPEDURATION*/}, null, null, null);

            tCursor.moveToFirst();

            totalCount = tCursor.getCount();

            featureMood1 = new ArrayList<FeatureDataFileClass>();
            featureMood2 = new ArrayList<FeatureDataFileClass>();
            featureMood3 = new ArrayList<FeatureDataFileClass>();
            featureMood4 = new ArrayList<FeatureDataFileClass>();

            FeatureDataFileClass featureEntry = null;

            while (!tCursor.isAfterLast()) {

                featureEntry = new FeatureDataFileClass();

                featureEntry.setMsi(tCursor.getString(0));
                featureEntry.setRmsi(tCursor.getString(1));
                featureEntry.setSessionLength(tCursor.getString(2));
                featureEntry.setBackspace_percentage(tCursor.getString(3));
                featureEntry.setSplchar_percentage(tCursor.getString(4));
                featureEntry.setSessionDuration(tCursor.getString(5));

                if(tCursor.getString(6).equals("2")) {
                    featureEntry.setMoodState("Happy");
                    featureMood1.add(featureEntry);
                }
                else if(tCursor.getString(6).equals("-2")) {
                    featureEntry.setMoodState("Sad");
                    featureMood2.add(featureEntry);
                }
                else if(tCursor.getString(6).equals("0")) {
                    featureEntry.setMoodState("Relaxed");
                    featureMood3.add(featureEntry);
                }
                else if(tCursor.getString(6).equals("1")) {
                    featureEntry.setMoodState("Stressed");
                    featureMood4.add(featureEntry);
                }

                // TODO: MINE deubgging add data ADDED
                // TODO: FEATURE ADDITION Not urgent
                /*System.out.println(tCursor.getString(0)     //msi
                        + "," + tCursor.getString(1)        //rmsi
                        + "," + tCursor.getString(2)        //sessionlen
                        + "," + tCursor.getString(3)        //backspaceper
                        + "," + tCursor.getString(4)        //splcharper
                        + "," + tCursor.getString(5)        //sessiondur
                        + "," + tCursor.getString(6));      //emotion
                        + "," + tCursor.getString(7)        //pressure
                        + "," + tCursor.getString(8)        //velocity
                        + "," + tCursor.getString(9));      //swipeduration
*/
                tCursor.moveToNext();
            }
            tCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void CalculateKLD(List<FeatureDataFileClass> featureMood1, List<FeatureDataFileClass> featureMood2,
                             List<FeatureDataFileClass> featureMood3, List<FeatureDataFileClass> featureMood4, int moodId) {

        int currTotal = totalCount - featureMood1.size();
        float mood2Per = featureMood2.size()*100/currTotal,mood3Per = featureMood3.size()*100/currTotal,mood4Per=featureMood4.size()*100/currTotal;

        int reqFeatures2= (int) (mood2Per*featureMood1.size()/100),reqFeatures3= (int) (mood3Per*featureMood1.size()/100);
        int reqFeatures4= (int) (mood4Per*featureMood1.size()/100);

        if(reqFeatures2+reqFeatures3+reqFeatures4>featureMood1.size()) {
            int diff = (reqFeatures2+reqFeatures3+reqFeatures4)  - featureMood1.size();
            if(featureMood2.size() > featureMood3.size() && featureMood2.size() > featureMood4.size())
                reqFeatures2 -= diff;
            else if(featureMood3.size() > featureMood2.size() && featureMood3.size() > featureMood4.size())
                reqFeatures3 -= diff;
            else
                reqFeatures4 -= diff;
        }

        else if(reqFeatures2+reqFeatures3+reqFeatures4<featureMood1.size()) {
            int diff = featureMood1.size() - (reqFeatures2 +reqFeatures3+reqFeatures4);
            if(featureMood2.size() > featureMood3.size() && featureMood2.size() > featureMood4.size())
                reqFeatures2 += diff;
            else if(featureMood3.size() > featureMood2.size() && featureMood3.size() > featureMood4.size())
                reqFeatures3 += diff;
            else
                reqFeatures4 += diff;
        }

        FeatureDataFileClass e1;

        double[] f1 = new double[featureMood1.size()];
        double[] f2 = new double[featureMood1.size()];
        double[] f3 = new double[featureMood1.size()];
        double[] f4 = new double[featureMood1.size()];
        double[] f5 = new double[featureMood1.size()];
        double[] f6 = new double[featureMood1.size()];
        double[] f7 = new double[featureMood1.size()];
        double[] f8 = new double[featureMood1.size()];
        double[] f9 = new double[featureMood1.size()];

        double[] f11 = new double[featureMood1.size()];
        double[] f12 = new double[featureMood1.size()];
        double[] f13 = new double[featureMood1.size()];
        double[] f14 = new double[featureMood1.size()];
        double[] f15 = new double[featureMood1.size()];
        double[] f16 = new double[featureMood1.size()];
        double[] f17 = new double[featureMood1.size()];
        double[] f18 = new double[featureMood1.size()];
        double[] f19 = new double[featureMood1.size()];

       // System.out.println(featureMood1.size() + " " + featureMood2.size() + " " + featureMood3.size() + " " + featureMood4.size() + " " +
       //         mood2Per + " " + mood3Per + " " + mood4Per + " " + reqFeatures2 + " " + reqFeatures3 + " " + reqFeatures4 + " " + currTotal);

        int loopCount = featureMood1.size();
        int diff=0;
    //    System.out.println("1-->>"+reqFeatures2+" "+featureMood2.size());
        if(reqFeatures2>featureMood2.size()) {
            diff += reqFeatures2 - featureMood2.size();
            reqFeatures2 = reqFeatures2 - (reqFeatures2 - featureMood2.size());
        }
    //    System.out.println("2-->>"+reqFeatures3+" "+featureMood3.size());
        if(reqFeatures3>featureMood3.size()) {
            diff += reqFeatures3 - featureMood3.size();
            reqFeatures3 = reqFeatures3 - (reqFeatures3 - featureMood3.size());
        }
     //   System.out.println("3-->>"+reqFeatures4+" "+featureMood4.size());
        if(reqFeatures4>featureMood4.size()) {
            diff += reqFeatures4 - featureMood4.size();
            reqFeatures4 = reqFeatures4 - (reqFeatures4 - featureMood4.size());
        }
        loopCount -= diff;
    //    System.out.println("Diff-->>"+diff+" "+loopCount);

        Collections.shuffle(featureMood1);
    //    System.out.println(diff+" "+loopCount+ " " + featureMood1.size());

    //    System.out.println("Check 1");
        for(int i=0;i<loopCount;i++) {
            e1 = featureMood1.get(i);
            f1[i] = Double.parseDouble(e1.getMsi());
            f2[i] = Double.parseDouble(e1.getRmsi());
            f3[i] = Double.parseDouble(e1.getSessionLength());
            f4[i] = Double.parseDouble(e1.getBackspace_percentage());
            f5[i] = Double.parseDouble(e1.getSplchar_percentage());
            f6[i] = Double.parseDouble(e1.getSessionDuration());
            /*
            f7[i] = Double.parseDouble(e1.getPressure());
            f8[i] = Double.parseDouble(e1.getVelocity());
            f9[i] = Double.parseDouble(e1.getSwipeDuration());
            */
        }

        int i=0;

        Collections.shuffle(featureMood2);
        Collections.shuffle(featureMood3);
        Collections.shuffle(featureMood4);

     //   System.out.println("Check 2");

        while(reqFeatures2!=0) {
            if(featureMood2.size()<reqFeatures2) {
                reqFeatures2=featureMood2.size();
            }
            /*if(featureMood2.size()==0)
                break;*/

    //        System.out.println("Fea 2");
    //        System.out.print(reqFeatures2 + " 1--->> ");
    //        System.out.println(featureMood2.size()-reqFeatures2);
            e1 = featureMood2.get(featureMood2.size()-reqFeatures2);
            f11[i] = Double.parseDouble(e1.getMsi());
            f12[i] = Double.parseDouble(e1.getRmsi());
            f13[i] = Double.parseDouble(e1.getSessionLength());
            f14[i] = Double.parseDouble(e1.getBackspace_percentage());
            f15[i] = Double.parseDouble(e1.getSplchar_percentage());
            f16[i] = Double.parseDouble(e1.getSessionDuration());
            /*
            f17[i] = Double.parseDouble(e1.getPressure());
            f18[i] = Double.parseDouble(e1.getVelocity());
            f19[i] = Double.parseDouble(e1.getSwipeDuration());
            */
            reqFeatures2--;
            i++;
        }
     //   System.out.println("Check 3");

        while(reqFeatures3!=0) {
            if(featureMood3.size()<reqFeatures3) {
                reqFeatures3=featureMood3.size();
            }
            /*if(featureMood3.size()==0)
                break;*/

   //         System.out.println("Fea 3");
     //       System.out.print(reqFeatures3 + " 2--->> ");
     //       System.out.println(featureMood3.size()-reqFeatures3);
            e1 = featureMood3.get(featureMood3.size()-reqFeatures3);
            f11[i] = Double.parseDouble(e1.getMsi());
            f12[i] = Double.parseDouble(e1.getRmsi());
            f13[i] = Double.parseDouble(e1.getSessionLength());
            f14[i] = Double.parseDouble(e1.getBackspace_percentage());
            f15[i] = Double.parseDouble(e1.getSplchar_percentage());
            f16[i] = Double.parseDouble(e1.getSessionDuration());
            /*
            f17[i] = Double.parseDouble(e1.getPressure());
            f18[i] = Double.parseDouble(e1.getVelocity());
            f19[i] = Double.parseDouble(e1.getSwipeDuration());
            */
            reqFeatures3--;
            i++;
        }
     //   System.out.println("Check 4");

        while(reqFeatures4!=0) {
            if(featureMood4.size()<reqFeatures4) {
                reqFeatures4=featureMood4.size();
            }
            /*if(featureMood4.size()==0)
                break;*/


     //       System.out.println("Fea 4");
    //        System.out.print(reqFeatures4 + " 3--->> ");
     //       System.out.println(featureMood4.size()-reqFeatures4);
            e1 = featureMood4.get(featureMood4.size()-reqFeatures4);
            f11[i] = Double.parseDouble(e1.getMsi());
            f12[i] = Double.parseDouble(e1.getRmsi());
            f13[i] = Double.parseDouble(e1.getSessionLength());
            f14[i] = Double.parseDouble(e1.getBackspace_percentage());
            f15[i] = Double.parseDouble(e1.getSplchar_percentage());
            f16[i] = Double.parseDouble(e1.getSessionDuration());
            /*
            f17[i] = Double.parseDouble(e1.getPressure());
            f18[i] = Double.parseDouble(e1.getVelocity());
            f19[i] = Double.parseDouble(e1.getSwipeDuration());
            */
            reqFeatures4--;
            i++;
        }

        KLD[moodId][0] = klDivergence(f1,f11);
        KLD[moodId][1] = klDivergence(f2,f12);
        KLD[moodId][2] = klDivergence(f3,f13);
        KLD[moodId][3] = klDivergence(f4,f14);
        KLD[moodId][4] = klDivergence(f5,f15);
        KLD[moodId][5] = klDivergence(f6,f16);
        /*
        KLD[moodId][6] = klDivergence(f7,f17);
        KLD[moodId][7] = klDivergence(f8,f18);
        KLD[moodId][8] = klDivergence(f9,f19);
        */

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

    public void StoreKldMatrix(double[][] kld){

        String kld_str= "";

        for(int i=0;i<4;i++) {
            for(int j=0;j<Integer.parseInt(getString(R.string.no_of_features));j++) {
                kld_str += String.valueOf(kld[i][j]) + ",";
            }
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.kld_vector_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.kld_vector), kld_str);
        log_editor.apply();
        log_editor.commit();

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File logDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        String kld_log = getString(R.string.log_file_path) + getString(R.string.KLD_log_file_name);
        //String errout = currentDateandTime+" Build Model Failed (No Train Data File Exists)\n";
        String out = currentDateandTime+","+kld_str+"\n";

        byte[] file_data = out.toString().getBytes();

        File kld_file = new File(logDir, kld_log);
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(kld_file,true);
            fos.write(file_data);
            fos.close();
        }
        catch (IOException e1) {
            //Log.e("Exception", "File write failed: " + e1.toString());
        }
        System.out.println("KLD Storage Done");

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
                for(int j=0;j<Integer.parseInt(getString(R.string.no_of_features));j++) {
                    kld[i][j] = Double.parseDouble(kldStrArr[k++]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return kld;

    }

}
