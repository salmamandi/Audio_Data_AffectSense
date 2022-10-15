package research.sg.edu.edapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import research.sg.edu.edapp.ChartLibs.animation.Easing;
import research.sg.edu.edapp.ChartLibs.charts.PieChart;
import research.sg.edu.edapp.ChartLibs.components.Legend;
import research.sg.edu.edapp.ChartLibs.data.Entry;
import research.sg.edu.edapp.ChartLibs.data.PieData;
import research.sg.edu.edapp.ChartLibs.data.PieDataSet;
import research.sg.edu.edapp.ChartLibs.data.PieEntry;
import research.sg.edu.edapp.ChartLibs.formatter.PercentFormatter;
import research.sg.edu.edapp.ChartLibs.highlight.Highlight;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IDataSet;
import research.sg.edu.edapp.ChartLibs.listener.OnChartValueSelectedListener;
import research.sg.edu.edapp.ChartLibs.utils.ColorTemplate;

public class AppUsageDist extends AppCompatActivity implements OnChartValueSelectedListener {

    private RelativeLayout mainLayout;
    private PieChart mChart;
    String cat = "";
    int happy=0,sad=0,stressed=0,relaxed=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_app_usage_dist);

        Bundle extras = getIntent().getExtras();
        cat = extras.getString("cat");
        happy = extras.getInt("happy");
        sad = extras.getInt("sad");
        stressed = extras.getInt("stressed");
        relaxed = extras.getInt("relaxed");
//        getSupportActionBar().setTitle("Distribution for" + cat);

        mainLayout = (RelativeLayout) findViewById(R.id.activity_app_usage_dist);
        mChart = new PieChart(this);

        mainLayout.addView(mChart);
        mainLayout.setBackgroundColor(Color.GRAY);

        // mChart = (PieChart) findViewById(R.id.chart2);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        //mChart.setExtraOffsets(5, 10, 5, 5);

        //mChart.setDragDecelerationFrictionCoef(0.95f);

        //mChart.setCenterTextTypeface(mTf.Light);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(3);
        mChart.setTransparentCircleRadius(5);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(270);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        ViewGroup.LayoutParams params = mChart.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        setData();

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        mChart.setDrawEntryLabels(true);
        for (IDataSet<?> set : mChart.getData().getDataSets())
            set.setDrawValues(true);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(5f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        //mChart.setEntryLabelTypeface(mTfRegular);
        mChart.setEntryLabelTextSize(12f);
    }

    public void setData() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

        entries.add(new PieEntry(happy, "Happy"));
        entries.add(new PieEntry(sad, "Sad"));
        entries.add(new PieEntry(stressed, "Stressed"));
        entries.add(new PieEntry(relaxed, "Relaxed"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        colors.add(getResources().getColor(R.color.blue));
        colors.add(getResources().getColor(R.color.red));
        colors.add(getResources().getColor(R.color.yellow));
        colors.add(getResources().getColor(R.color.green));

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

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        PieEntry pe = (PieEntry) e;
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
}
