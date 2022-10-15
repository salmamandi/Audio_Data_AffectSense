package research.sg.edu.edapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import research.sg.edu.edapp.ChartLibs.animation.Easing;
import research.sg.edu.edapp.ChartLibs.charts.BarChart;
import research.sg.edu.edapp.ChartLibs.charts.PieChart;
import research.sg.edu.edapp.ChartLibs.components.AxisBase;
import research.sg.edu.edapp.ChartLibs.components.Legend;
import research.sg.edu.edapp.ChartLibs.components.XAxis;
import research.sg.edu.edapp.ChartLibs.components.YAxis;
import research.sg.edu.edapp.ChartLibs.data.BarData;
import research.sg.edu.edapp.ChartLibs.data.BarDataSet;
import research.sg.edu.edapp.ChartLibs.data.BarEntry;
import research.sg.edu.edapp.ChartLibs.data.Entry;
import research.sg.edu.edapp.ChartLibs.data.PieData;
import research.sg.edu.edapp.ChartLibs.data.PieDataSet;
import research.sg.edu.edapp.ChartLibs.data.PieEntry;
import research.sg.edu.edapp.ChartLibs.formatter.IAxisValueFormatter;
import research.sg.edu.edapp.ChartLibs.formatter.PercentFormatter;
import research.sg.edu.edapp.ChartLibs.highlight.Highlight;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IBarDataSet;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IDataSet;
import research.sg.edu.edapp.ChartLibs.listener.OnChartValueSelectedListener;
import research.sg.edu.edapp.ChartLibs.utils.ColorTemplate;

public class StatsMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Menu mOptionsMenu;

    int happy=0,sad=0,stressed=0,relaxed=0;
    int hcat1=0,hcat2=0,hcat3=0,hcat4=0,hcat5=0,hcat6=0,hweekEnd=0,hweekDay=0;
    int scat1=0,scat2=0,scat3=0,scat4=0,scat5=0,scat6=0,sweekEnd=0,sweekDay=0;
    int stcat1=0,stcat2=0,stcat3=0,stcat4=0,stcat5=0,stcat6=0,stweekEnd=0,stweekDay=0;
    int rcat1=0,rcat2=0,rcat3=0,rcat4=0,rcat5=0,rcat6=0,rweekEnd=0,rweekDay=0;
    int hMorn=0,hAfter=0,hEven=0,hNight=0;
    int sMorn=0,sAfter=0,sEven=0,sNight=0;
    int stMorn=0,stAfter=0,stEven=0,stNight=0;
    int rMorn=0,rAfter=0,rEven=0,rNight=0;
    String mostUsedApp;
    float sumCat1 = (float) 0.0,sumCat2= (float) 0.0,sumCat3= (float) 0.0,sumCat4= (float) 0.0,sumCat5= (float) 0.0,sumCat6= (float) 0.0;
    float sumMorn = (float) 0.0,sumAfter= (float) 0.0,sumEven= (float) 0.0,sumNight= (float) 0.0;
    TextView tv1,tv2;
    TextView basicInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);

        tv1 = (TextView) header.findViewById(R.id.nametext);
        tv2 = (TextView) header.findViewById(R.id.emailtext);

        tv1.setText(RetrieveUserName());
        tv2.setText(RetrieveUserEmail());

        Bundle extras = getIntent().getExtras();
        happy = extras.getInt("happy");
        sad = extras.getInt("sad");
        stressed = extras.getInt("stressed");
        relaxed = extras.getInt("relaxed");
        hcat1 = extras.getInt("hcat1");
        hcat2 = extras.getInt("hcat2");
        hcat3 = extras.getInt("hcat3");
        hcat4 = extras.getInt("hcat4");
        hcat5 = extras.getInt("hcat5");
        hcat6 = extras.getInt("hcat6");
        hweekEnd = extras.getInt("hweekEnd");
        hweekDay = extras.getInt("hweekEnd");
        scat1 = extras.getInt("scat1");
        scat2 = extras.getInt("scat2");
        scat3 = extras.getInt("scat3");
        scat4 = extras.getInt("scat4");
        scat5 = extras.getInt("scat5");
        scat6 = extras.getInt("scat6");
        sweekEnd = extras.getInt("sweekEnd");
        sweekDay = extras.getInt("sweekEnd");
        stcat1 = extras.getInt("stcat1");
        stcat2 = extras.getInt("stcat2");
        stcat3 = extras.getInt("stcat3");
        stcat4 = extras.getInt("stcat4");
        stcat5 = extras.getInt("stcat5");
        stcat6 = extras.getInt("stcat6");
        stweekEnd = extras.getInt("stweekEnd");
        stweekDay = extras.getInt("stweekEnd");
        rcat1 = extras.getInt("rcat1");
        rcat2 = extras.getInt("rcat2");
        rcat3 = extras.getInt("rcat3");
        rcat4 = extras.getInt("rcat4");
        rcat5 = extras.getInt("rcat5");
        rcat6 = extras.getInt("rcat6");
        rweekEnd = extras.getInt("rweekEnd");
        rweekEnd = extras.getInt("rweekEnd");
        rweekDay= extras.getInt("startdate");
        rweekDay = extras.getInt("rweekEnd");

        hMorn = extras.getInt("hMorn");
        hAfter = extras.getInt("hAfter");
        hEven = extras.getInt("hEven");
        hNight = extras.getInt("hNight");
        sMorn = extras.getInt("sMorn");
        sAfter = extras.getInt("sAfter");
        sEven = extras.getInt("sEven");
        sNight = extras.getInt("sNight");
        stMorn = extras.getInt("stMorn");
        stAfter = extras.getInt("stAfter");
        stEven = extras.getInt("stEven");
        stNight = extras.getInt("stNight");
        rMorn = extras.getInt("rMorn");
        rAfter = extras.getInt("rAfter");
        rEven = extras.getInt("rEven");
        rNight = extras.getInt("rNight");
        mostUsedApp = extras.getString("mostUsedApp");

        basicInfo = (TextView) findViewById(R.id.basic_info);
        basicInfo.setText("\n\nMost Frequent App : " + mostUsedApp + "\n\n\n\t\t\tHappy Counts : " + happy + "\n\n\t\t\tSad Counts : " + sad +
                                                            "\n\n\t\t\tStressed Counts : " + stressed + "\n\n\t\t\tRelaxed Counts : " + relaxed);
    }

    public String RetrieveUserName(){
        String ctr="000000";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.user_details_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.user_name), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public String RetrieveUserEmail(){
        String ctr="000000";
        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.user_details_file), Context.MODE_MULTI_PROCESS);
            ctr = pref.getString(getResources().getString(R.string.user_email_id), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    int backpress=0;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            backpress = 0;
        } else {
            if (backpress==1) {
                super.onBackPressed();
                return;
            }

            backpress = (backpress + 1);
            final Toast toast = Toast.makeText(getApplicationContext(), "Press Back again to Exit",Toast.LENGTH_SHORT);
            toast.show();

            new CountDownTimer(1000, 500) {
                public void onTick(long millisUntilFinished) {toast.show();}
                public void onFinish() {toast.cancel();}
            }.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.stats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        backpress = 0;
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Hello Settings Works",Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.actionPieToggleValues: {
                for (IDataSet<?> set : mChart.getData().getDataSets())
                    set.setDrawValues(!set.isDrawValuesEnabled());
                mChart.invalidate();
                return true;
            }
            case R.id.actionPieToggleXVals: {

                mChart.setDrawEntryLabels(!mChart.isDrawEntryLabelsEnabled());
                mChart.invalidate();
                return true;
            }
            case R.id.actionPieSave: {
                if (mChart.saveToGallery("title" + System.currentTimeMillis(), 50)) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionPieToggleSpin: {
                mChart.spin(1000, mChart.getRotationAngle(), mChart.getRotationAngle() + 360, Easing.EasingOption
                        .EaseInCubic);
                return true;
            }
            case R.id.actionBarCToggleValues: {
                List<IBarDataSet> sets = barChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    BarDataSet set = (BarDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }

                barChart.invalidate();
                break;
            }
            case R.id.actionBarCTogglePinch: {
                if (barChart.isPinchZoomEnabled())
                    barChart.setPinchZoom(false);
                else
                    barChart.setPinchZoom(true);

                barChart.invalidate();
                break;
            }
            case R.id.actionBarCSave: {
                if (barChart.saveToGallery("title" + System.currentTimeMillis(), 50)) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionBarCHelp: {
                Intent intent = new Intent(StatsMenu.this, StatsDescription.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private RelativeLayout mainLayout;
    private PieChart mChart;
    private BarChart barChart;

    private float[] yData;
    private String[] xData = {"Sad","Happy","Relaxed","Stressed"};

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        backpress = 0;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*View header = (View) getLayoutInflater().inflate(R.layout.content_stats_menu, null);
        basicInfo = (TextView) header.findViewById(R.id.basic_info);
        basicInfo.setText("");
        */
        if (id == R.id.overview) {
            mainLayout = (RelativeLayout) findViewById(R.id.content_stats_menu);
            //getMenuInflater().inflate(R.menu.pie, mOptionsMenu);
            setMenuOptions(1);
            mainLayout.removeAllViewsInLayout();

            mChart = new PieChart(this);

            mainLayout.addView(mChart);
            mainLayout.setBackgroundColor(Color.LTGRAY);

            mChart.setUsePercentValues(true);
            mChart.getDescription().setEnabled(false);
            //mChart.setExtraOffsets(5, 10, 5, 5);

            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColor(Color.WHITE);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setHoleRadius(3);
            mChart.setTransparentCircleRadius(5);

            mChart.setDrawCenterText(true);

            mChart.setRotationAngle(270);

            mChart.setRotationEnabled(true);
            mChart.setHighlightPerTapEnabled(true);

            ViewGroup.LayoutParams params = mChart.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;

            mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
            // mChart.spin(2000, 0, 360);

            setOverviewData();

            mChart.setDrawEntryLabels(true);
            for (IDataSet<?> set : mChart.getData().getDataSets())
                set.setDrawValues(true);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(5f);

            // entry label styling
            mChart.setEntryLabelColor(Color.WHITE);
            //mChart.setEntryLabelTypeface(mTfRegular);
            mChart.setEntryLabelTextSize(12f);
            mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    PieEntry pe = (PieEntry) e;
                    if (e == null)
                        return;

                    final Toast toast = Toast.makeText(getApplicationContext(), pe.getLabel() + " " + (int)e.getY() + " Times",Toast.LENGTH_SHORT);
                    toast.show();
                    //Log.i("VAL SELECTED", pe.getLabel() + " " + e.getY());

                    new CountDownTimer(1000, 500) {
                        public void onTick(long millisUntilFinished) {toast.show();}
                        public void onFinish() {toast.cancel();}
                    }.start();
                }

                @Override
                public void onNothingSelected() {
                    //Log.i("PieChart", "nothing selected");
                }

            });

            // Handle the camera action
        } else if (id == R.id.detailed) {
            mainLayout = (RelativeLayout) findViewById(R.id.content_stats_menu);
            setMenuOptions(2);

            mainLayout.removeAllViewsInLayout();

            barChart = new BarChart(this);

            mainLayout.addView(barChart);
            mainLayout.setBackgroundColor(Color.LTGRAY);
            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                @Override
                public void onValueSelected(Entry e, Highlight h) {

                    BarEntry entry = (BarEntry) e;
                    final Toast toast;
                    String session = null;
                    int sum = 0;
                    String emotion = null;
                    switch (h.getStackIndex()) {
                        case 0:
                            emotion = "Happy";
                            switch((int) entry.getX()) {
                                case 0:
                                    session = "Morning";
                                    sum = hMorn;
                                    break;
                                case 1:
                                    session = "Afternoon";
                                    sum = hAfter;
                                    break;
                                case 2:
                                    session = "Evening";
                                    sum = hEven;
                                    break;
                                case 3:
                                    session = "Night";
                                    sum = hNight;
                                    break;
                            }
                            break;
                        case 1:
                            emotion = "Sad";
                            switch((int) entry.getX()) {
                                case 0:
                                    session = "Morning";
                                    sum = sMorn;
                                    break;
                                case 1:
                                    session = "Afternoon";
                                    sum = sAfter;
                                    break;
                                case 2:
                                    session = "Evening";
                                    sum = sEven;
                                    break;
                                case 3:
                                    session = "Night";
                                    sum = sNight;
                                    break;
                            }
                            break;
                        case 2:
                            emotion = "Stressed";
                            switch((int) entry.getX()) {
                                case 0:
                                    session = "Morning";
                                    sum = stMorn;
                                    break;
                                case 1:
                                    session = "Afternoon";
                                    sum = stAfter;
                                    break;
                                case 2:
                                    session = "Evening";
                                    sum = stEven;
                                    break;
                                case 3:
                                    session = "Night";
                                    sum = stNight;
                                    break;
                            }
                            break;
                        case 3:
                            emotion = "Relaxed";
                            switch((int) entry.getX()) {
                                case 0:
                                    session = "Morning";
                                    sum = rMorn;
                                    break;
                                case 1:
                                    session = "Afternoon";
                                    sum = rAfter;
                                    break;
                                case 2:
                                    session = "Evening";
                                    sum = rEven;
                                    break;
                                case 3:
                                    session = "Night";
                                    sum = rNight;
                                    break;
                            }
                            break;
                    }
                    toast = Toast.makeText(getApplicationContext(), "In " + session + ", " + emotion + " " + sum + " Times", Toast.LENGTH_SHORT);
                    toast.show();
                    //Log.i("VAL SELECTED", "Value: " + entry.getYVals()[h.getStackIndex()]);

                    new CountDownTimer(1000, 500) {
                        public void onTick(long millisUntilFinished) {toast.show();}
                        public void onFinish() {toast.cancel();}
                    }.start();
                }

                @Override
                public void onNothingSelected() {
                    //Log.i("BarChart", "nothing selected");
                }
            });

            barChart.getDescription().setEnabled(false);

            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            barChart.setMaxVisibleValueCount(100);
            // scaling can now only be done on x- and y-axis separately
            barChart.setPinchZoom(false);

            barChart.setDrawGridBackground(false);
            barChart.setDrawBarShadow(false);

            barChart.setDrawValueAboveBar(false);
            barChart.setHighlightFullBarEnabled(false);

            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getXAxis().setDrawGridLines(false);
            String[] labels = {"Morning","Afternoon","Evening","Night"};
            barChart.getXAxis().setValueFormatter(new LabelFormatter(labels));
            // change the position of the y-labels

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            barChart.getAxisRight().setEnabled(false);
            barChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);

            XAxis xLabels = barChart.getXAxis();
            xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
            //xLabels.setEnabled(false);
            xLabels.setXOffset((float) 1.0);
            xLabels.setYOffset((float) 5.0);

            xLabels.setGranularity(1f);
            xLabels.setGranularityEnabled(true);

            ViewGroup.LayoutParams params = barChart.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;

            setDetailedData();
            setDrawValuesDisabled(barChart);

            Legend l = barChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setFormSize(8f);
            l.setFormToTextSpace(4f);
            l.setXEntrySpace(6f);

        } else if (id == R.id.app_wise) {
            mainLayout = (RelativeLayout) findViewById(R.id.content_stats_menu);
            setMenuOptions(2);

            mainLayout.removeAllViewsInLayout();

            barChart = new BarChart(this);

            mainLayout.addView(barChart);
            mainLayout.setBackgroundColor(Color.LTGRAY);


            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                @Override
                public void onValueSelected(Entry e, Highlight h) {

                    BarEntry entry = (BarEntry) e;
                    final Toast toast;
                    String appType = null;
                    int sum = 0;
                    String emotion = null;

                    switch (h.getStackIndex()) {
                        case 0:
                            emotion = "Happy";
                            switch((int) entry.getX()) {
                                case 0:
                                    appType = "Email";
                                    sum = hcat1;
                                    break;
                                case 1:
                                    appType = "IM";
                                    sum = hcat2;
                                    break;
                                case 2:
                                    appType = "Social";
                                    sum = hcat3;
                                    break;
                                case 3:
                                    appType = "Entertainment";
                                    sum = hcat4;
                                    break;
                                case 4:
                                    appType = "Surfing";
                                    sum = hcat5;
                                    break;
                                case 5:
                                    appType = "Misc";
                                    sum = hcat6;
                                    break;
                            }
                            break;
                        case 1:
                            emotion = "Sad";
                            switch((int) entry.getX()) {
                                case 0:
                                    appType = "Email";
                                    sum = scat1;
                                    break;
                                case 1:
                                    appType = "IM";
                                    sum = scat2;
                                    break;
                                case 2:
                                    appType = "Social";
                                    sum = scat3;
                                    break;
                                case 3:
                                    appType = "Entertainment";
                                    sum = scat4;
                                    break;
                                case 4:
                                    appType = "Surfing";
                                    sum = scat5;
                                    break;
                                case 5:
                                    appType = "Misc";
                                    sum = scat6;
                                    break;
                            }
                            break;
                        case 2:
                            emotion = "Stressed";
                            switch((int) entry.getX()) {
                                case 0:
                                    appType = "Email";
                                    sum = stcat1;
                                    break;
                                case 1:
                                    appType = "IM";
                                    sum = stcat2;
                                    break;
                                case 2:
                                    appType = "Social";
                                    sum = stcat3;
                                    break;
                                case 3:
                                    appType = "Entertainment";
                                    sum = stcat4;
                                    break;
                                case 4:
                                    appType = "Surfing";
                                    sum = stcat5;
                                    break;
                                case 5:
                                    appType = "Misc";
                                    sum = stcat6;
                                    break;
                            }
                            break;
                        case 3:
                            emotion = "Relaxed";
                            switch((int) entry.getX()) {
                                case 0:
                                    appType = "Email";
                                    sum = rcat1;
                                    break;
                                case 1:
                                    appType = "IM";
                                    sum = rcat2;
                                    break;
                                case 2:
                                    appType = "Social";
                                    sum = rcat3;
                                    break;
                                case 3:
                                    appType = "Entertainment";
                                    sum = rcat4;
                                    break;
                                case 4:
                                    appType = "Surfing";
                                    sum = rcat5;
                                    break;
                                case 5:
                                    appType = "Misc";
                                    sum = rcat6;
                                    break;
                            }
                            break;
                    }
                    toast = Toast.makeText(getApplicationContext(), appType + ": " + emotion + " " + sum + " Times", Toast.LENGTH_SHORT);
                    toast.show();
                    //Log.i("VAL SELECTED", "Value: " + entry.getYVals()[h.getStackIndex()]);

                    new CountDownTimer(1000, 500) {
                        public void onTick(long millisUntilFinished) {toast.show();}
                        public void onFinish() {toast.cancel();}
                    }.start();
                }

                @Override
                public void onNothingSelected() {
                    //Log.i("BarChart", "nothing selected");
                }
            });

            barChart.getDescription().setEnabled(false);

            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            barChart.setMaxVisibleValueCount(100);

            // scaling can now only be done on x- and y-axis separately
            barChart.setPinchZoom(false);

            barChart.setDrawGridBackground(false);
            barChart.setDrawBarShadow(false);

            barChart.setDrawValueAboveBar(false);
            barChart.setHighlightFullBarEnabled(false);

            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getXAxis().setDrawGridLines(false);
            String[] labels = {"Email","IM","Social","Entertainment","Surfing","Misc."};
            barChart.getXAxis().setValueFormatter(new LabelFormatter(labels));
            // change the position of the y-labels

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            barChart.getAxisRight().setEnabled(false);
            barChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);

            XAxis xLabels = barChart.getXAxis();
            xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
            //xLabels.setEnabled(false);
            //xLabels.setXOffset((float) 1.0);
            xLabels.setTextSize(8);
            xLabels.setYOffset((float) 3.0);

            xLabels.setGranularity(1f);
            xLabels.setGranularityEnabled(true);

            ViewGroup.LayoutParams params = barChart.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;

            setAppWiseData();

            setDrawValuesDisabled(barChart);

            Legend l = barChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setFormSize(8f);
            l.setFormToTextSpace(4f);
            l.setXEntrySpace(6f);

        } else if (id == R.id.app_usage) {

            mainLayout = (RelativeLayout) findViewById(R.id.content_stats_menu);
            //getMenuInflater().inflate(R.menu.pie, mOptionsMenu);
            setMenuOptions(1);
            mainLayout.removeAllViewsInLayout();

            mChart = new PieChart(this);

            mainLayout.addView(mChart);
            mainLayout.setBackgroundColor(Color.LTGRAY);

            mChart.setUsePercentValues(true);
            mChart.getDescription().setEnabled(false);
            //mChart.setExtraOffsets(5, 10, 5, 5);

            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColor(Color.WHITE);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setHoleRadius(3);
            mChart.setTransparentCircleRadius(5);

            mChart.setDrawCenterText(true);

            mChart.setRotationAngle(270);

            mChart.setRotationEnabled(true);
            mChart.setHighlightPerTapEnabled(true);

            ViewGroup.LayoutParams params = mChart.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;

            mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
            // mChart.spin(2000, 0, 360);

            setAppUsageData();

            mChart.setDrawEntryLabels(true);
            for (IDataSet<?> set : mChart.getData().getDataSets())
                set.setDrawValues(true);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(5f);

            // entry label styling
            mChart.setEntryLabelColor(Color.WHITE);
            //mChart.setEntryLabelTypeface(mTfRegular);
            mChart.setEntryLabelTextSize(12f);
            mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    PieEntry pe = (PieEntry) e;
                    if (e == null)
                        return;

                    Intent intent = new Intent(StatsMenu.this,AppUsageDist.class);

                    if(pe.getLabel().toString().equals("Email")) {
                        intent.putExtra("cat","Email");
                        intent.putExtra("happy",hcat1);
                        intent.putExtra("sad",scat1);
                        intent.putExtra("stressed",stcat1);
                        intent.putExtra("relaxed",rcat1);
                    }
                    else if(pe.getLabel().toString().equals("IM")) {
                        intent.putExtra("cat","Social");
                        intent.putExtra("happy",hcat2);
                        intent.putExtra("sad",scat2);
                        intent.putExtra("stressed",stcat2);
                        intent.putExtra("relaxed",rcat2);
                    }
                    else if(pe.getLabel().toString().equals("Social")) {
                        intent.putExtra("cat","Entertainment");
                        intent.putExtra("happy",hcat3);
                        intent.putExtra("sad",scat3);
                        intent.putExtra("stressed",stcat3);
                        intent.putExtra("relaxed",rcat3);
                    }
                    else if(pe.getLabel().toString().equals("Entertainment")) {
                        intent.putExtra("cat","Surfing");
                        intent.putExtra("happy",hcat4);
                        intent.putExtra("sad",scat4);
                        intent.putExtra("stressed",stcat4);
                        intent.putExtra("relaxed",rcat4);
                    }
                    else if(pe.getLabel().toString().equals("Surfing")) {
                        intent.putExtra("cat","Misc.");
                        intent.putExtra("happy",hcat5);
                        intent.putExtra("sad",scat5);
                        intent.putExtra("stressed",stcat5);
                        intent.putExtra("relaxed",rcat5);
                    }
                    else if(pe.getLabel().toString().equals("Misc.")) {
                        intent.putExtra("cat","Social");
                        intent.putExtra("happy",hcat6);
                        intent.putExtra("sad",scat6);
                        intent.putExtra("stressed",stcat6);
                        intent.putExtra("relaxed",rcat6);
                    }
                    startActivity(intent);


                    /*final Toast toast = Toast.makeText(getApplicationContext(), pe.getLabel() + " " + (int)e.getY() + " Times",Toast.LENGTH_SHORT);
                    toast.show();
                    Log.i("VAL SELECTED", pe.getLabel() + " " + e.getY());

                    new CountDownTimer(1000, 500) {
                        public void onTick(long millisUntilFinished) {toast.show();}
                        public void onFinish() {toast.cancel();}
                    }.start();*/

                }

                @Override
                public void onNothingSelected() {
                    //Log.i("PieChart", "nothing selected");
                }

            });

        } else if (id == R.id.nav_share) {
            try {
                if (mChart.saveToGallery("title" + System.currentTimeMillis(), 50)) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {

            }
            try {
                if (barChart.saveToGallery("title" + System.currentTimeMillis(), 50)) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT).show();            }
            catch (Exception e) {

            }
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class LabelFormatter implements IAxisValueFormatter {
        private final String[] mLabels;

        public LabelFormatter(String[] labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mLabels[(int) value];
        }
    }

    public void setDrawValuesDisabled(BarChart bChart) {
        List<IBarDataSet> sets = bChart.getData()
                .getDataSets();

        for (IBarDataSet iSet : sets) {
            BarDataSet set = (BarDataSet) iSet;
            set.setDrawValues(false);
        }
        bChart.invalidate();
    }

    public void setOverviewData() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        if(happy!=0) {
            colors.add(getResources().getColor(R.color.blue));
            entries.add(new PieEntry(happy, "Happy"));
        }
        if(sad!=0) {
            colors.add(getResources().getColor(R.color.red));
            entries.add(new PieEntry(sad, "Sad"));
        }
        if(stressed!=0) {
            colors.add(getResources().getColor(R.color.yellow));
            entries.add(new PieEntry(stressed, "Stressed"));
        }
        if(relaxed!=0) {
            colors.add(getResources().getColor(R.color.green));
            entries.add(new PieEntry(relaxed, "Relaxed"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        //data.setValueTypeface(mTfLight);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    public void setAppUsageData() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);


        if(hcat1 + scat1 + stcat1 + rcat1 != 0) {
            //colors.add(getResources().getColor(R.color.blue));
            entries.add(new PieEntry(hcat1 + scat1 + stcat1 + rcat1, "Email"));
        }
        if(hcat2 + scat2 + stcat2 + rcat2 != 0) {
            //colors.add(getResources().getColor(R.color.red));
            entries.add(new PieEntry(hcat2 + scat2 + stcat2 + rcat2, "IM"));
        }
        if(hcat3 + scat3 + stcat3 + rcat3 != 0) {
            //colors.add(getResources().getColor(R.color.yellow));
            entries.add(new PieEntry(hcat3 + scat3 + stcat3 + rcat3, "Social"));
        }
        if(hcat4 + scat4 + stcat4 + rcat4 != 0) {
            //colors.add(getResources().getColor(R.color.green));
            entries.add(new PieEntry(hcat4 + scat4 + stcat4 + rcat4, "Entertainment"));
        }
        if(hcat5 + scat5 + stcat5 + rcat5 != 0) {
            //colors.add(getResources().getColor(R.color.green));
            entries.add(new PieEntry(hcat5 + scat5 + stcat5 + rcat5, "Surfing"));
        }
        if(hcat6 + scat6 + stcat6 + rcat6 != 0) {
            //colors.add(getResources().getColor(R.color.green));
            entries.add(new PieEntry(hcat5 + scat5 + stcat5 + rcat5, "Misc."));
        }

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        //data.setValueTypeface(mTfLight);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    public void setDetailedData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        sumMorn = hMorn + sMorn + stMorn + rMorn ;
        yVals1.add(new BarEntry(0, new float[]{(float)hMorn*100/sumMorn, (float)sMorn*100/sumMorn, (float)stMorn*100/sumMorn, (float)rMorn*100/sumMorn}));
        sumAfter = hAfter + sAfter + stAfter + rAfter;
        yVals1.add(new BarEntry(1, new float[]{(float)hAfter*100/sumAfter, (float)sAfter*100/sumAfter, (float)stAfter*100/sumAfter, (float)rAfter*100/sumAfter}));
        sumEven = hEven + sEven + stEven + rEven;
        yVals1.add(new BarEntry(2, new float[]{(float)hEven*100/sumEven, (float)sEven*100/sumEven, (float)stEven*100/sumEven, (float)rEven*100/sumEven}));
        sumNight = hNight + sNight + stNight + rNight;
        yVals1.add(new BarEntry(3, new float[]{(float)hNight*100/sumNight, (float)sNight*100/sumNight, (float)stNight*100/sumNight, (float)rNight*100/sumNight}));

        BarDataSet set1;

        if (barChart.getData() != null &&
                barChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {

            set1 = new BarDataSet(yVals1, " ");

            ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(getResources().getColor(R.color.blue));
            colors.add(getResources().getColor(R.color.red));
            colors.add(getResources().getColor(R.color.yellow));
            colors.add(getResources().getColor(R.color.green));

            set1.setColors(colors);
            set1.setStackLabels(new String[]{"Happy", "Sad", "Stressed","Relaxed"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextColor(Color.WHITE);
            barChart.setData(data);

        }

        barChart.setFitBars(true);
        barChart.invalidate();
    }

    public void setAppWiseData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        System.out.println("Set App Wise Data");

        sumCat1 = hcat1 + scat1 + stcat1 + rcat1;
        System.out.println(hcat1 + " " + scat1 + " " +stcat1 + " " + rcat1 + " " + sumCat1);
        yVals1.add(new BarEntry(0, new float[]{(float)hcat1*100/sumCat1, (float)scat1*100/sumCat1, (float)stcat1*100/sumCat1, (float)rcat1*100/sumCat1}));

        sumCat2 = hcat2 + scat2 + stcat2 + rcat2;
        System.out.println(hcat2 + " " + scat2 + " " +stcat2 + " " + rcat2 + " " + sumCat2);
        yVals1.add(new BarEntry(1, new float[]{(float)hcat2*100/sumCat2, (float)scat2*100/sumCat2, (float)stcat2*100/sumCat2, (float)rcat2*100/sumCat2}));

        sumCat3 = hcat3 + scat3 + stcat3 + rcat3;
        System.out.println(hcat3 + " " + scat3 + " " +stcat3 + " " + rcat3 + " " + sumCat3);
        yVals1.add(new BarEntry(2, new float[]{(float)hcat3*100/sumCat3, (float)scat3*100/sumCat3, (float)stcat3*100/sumCat3, (float)rcat3*100/sumCat3}));

        sumCat4 = hcat4 + scat4 + stcat4 + rcat4;
        System.out.println(hcat4 + " " + scat4 + " " +stcat4 + " " + rcat4 + " " + sumCat4);
        yVals1.add(new BarEntry(3, new float[]{(float)hcat4*100/sumCat4, (float)scat4*100/sumCat4, (float)stcat4*100/sumCat4, (float)rcat4*100/sumCat4}));

        sumCat5 = hcat5 + scat5 + stcat5 + rcat5;
        System.out.println(hcat5 + " " + scat5 + " " +stcat5 + " " + rcat5 + " " + sumCat5);
        yVals1.add(new BarEntry(4, new float[]{(float)hcat5*100/sumCat5, (float)scat5*100/sumCat5, (float)stcat5*100/sumCat5, (float)rcat5*100/sumCat5}));

        sumCat6 = hcat6 + scat6 + stcat6 + rcat6;
        System.out.println(hcat6 + " " + scat6 + " " +stcat6 + " " + rcat6 + " " + sumCat6);
        yVals1.add(new BarEntry(5, new float[]{(float)hcat6*100/sumCat6, (float)scat6*100/sumCat6, (float)stcat6*100/sumCat6, (float)rcat6*100/sumCat6}));

        BarDataSet set1;
            set1 = new BarDataSet(yVals1, " ");

            ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(getResources().getColor(R.color.blue));
            colors.add(getResources().getColor(R.color.red));
            colors.add(getResources().getColor(R.color.yellow));
            colors.add(getResources().getColor(R.color.green));

            set1.setColors(colors);
            set1.setStackLabels(new String[]{"Happy", "Sad", "Stressed","Relaxed"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextColor(Color.WHITE);
            barChart.setData(data);
            barChart.setFitBars(true);
            barChart.invalidate();
    }

    public void setMenuOptions (int section) {
        MenuItem menuSettings = mOptionsMenu.findItem(R.id.action_settings);
        MenuItem actionPieToggleValues = mOptionsMenu.findItem(R.id.actionPieToggleValues);
        MenuItem actionPieToggleXVals = mOptionsMenu.findItem(R.id.actionPieToggleXVals);
        MenuItem actionPieSave = mOptionsMenu.findItem(R.id.actionPieSave);
        //MenuItem actionPieToggleSpin = mOptionsMenu.findItem(R.id.actionPieToggleSpin);
        MenuItem actionBarCToggleValues = mOptionsMenu.findItem(R.id.actionBarCToggleValues);
        MenuItem actionBarCSave = mOptionsMenu.findItem(R.id.actionBarCSave);
        MenuItem actionBarCTogglePinch = mOptionsMenu.findItem(R.id.actionBarCTogglePinch);


        if(section == 1) {
            //menuSettings.setVisible(false);
            actionPieToggleValues.setVisible(true);
            actionPieToggleXVals.setVisible(true);
            actionPieSave.setVisible(true);
            actionBarCToggleValues.setVisible(false);
            actionBarCSave.setVisible(false);
            actionBarCTogglePinch.setVisible(false);
        }
        if(section == 2) {
            //menuSettings.setVisible(false);
            actionPieToggleValues.setVisible(false);
            actionPieToggleXVals.setVisible(false);
            actionPieSave.setVisible(false);
            actionBarCToggleValues.setVisible(true);
            actionBarCSave.setVisible(true);
            actionBarCTogglePinch.setVisible(true);
        }
    }
}
