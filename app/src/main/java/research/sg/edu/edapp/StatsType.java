package research.sg.edu.edapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import research.sg.edu.edapp.FinalClasses.StatsDetails;

public class StatsType extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_type);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Button btn1,btn2;
        private TextView text1,text2;


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);

                text1 = (TextView) rootView.findViewById(R.id.date);
                btn1 = (Button) rootView.findViewById(R.id.selectDate);
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Auto-generated method stub
                        showDatePicker(1);
                    }
                });

                View.OnClickListener listnr = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        Date d1 = null;
                        String str1 = text1.getText().toString();
                        try {
                            d1 = df.parse(str1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(text1.getText().toString().isEmpty()) {
                            Snackbar.make(getView(), "Select A Date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }
                        Date now = new Date();
                        if(d1.compareTo(now)>0) {
                            Snackbar.make(getView(), "Select Valid Date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }

                        ShowRetreivedEmoStats(str1,str1);
                    }
                };
                Button btn = (Button) rootView.findViewById(R.id.dailyBtn);
                btn.setOnClickListener(listnr);
            }

            else {
                rootView = inflater.inflate(R.layout.other_layout, container, false);

                text1 = (TextView) rootView.findViewById(R.id.date1);
                btn1 = (Button) rootView.findViewById(R.id.fromDate);
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        showDatePicker(1);
                    }
                });

                text2 = (TextView) rootView.findViewById(R.id.date2);
                btn2 = (Button) rootView.findViewById(R.id.toDate);
                btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        showDatePicker(2);
                    }
                });

                View.OnClickListener listnr = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        Date d1 = null,d2 = null;
                        String str1 = text1.getText().toString();
                        String str2 = text2.getText().toString();
                        try {
                            d1 = df.parse(str1);
                            d2 = df.parse(str2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(text1.getText().toString().isEmpty()) {
                            Snackbar.make(getView(), "Select Start Date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }
                        if(text2.getText().toString().isEmpty()) {
                            Snackbar.make(getView(), "Select End Date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }
                        if(d1.compareTo(d2)>0) {
                            Snackbar.make(getView(), "Select Valid Date Range", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }
                        Date now = new Date();
                        if(d1.compareTo(now)>0) {
                            Snackbar.make(getView(), "Select Valid Start Date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }
                        if(d2.compareTo(now)>0) {
                            Snackbar.make(getView(), "Select Valid End Date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            return;
                        }

                        ShowRetreivedEmoStats(str1,str2);

                    }
                };

                Button btn = (Button) rootView.findViewById(R.id.weekBtn);
                btn.setOnClickListener(listnr);
            }

            return rootView;
        }

        void ShowRetreivedEmoStats(String str1,String str2) {
            Map<String, Integer> stringsCount = new HashMap<String, Integer>();
            Map.Entry<String,Integer> mostRepeated = null;
            int happy=0,sad=0,stressed=0,relaxed=0;
            int hcat1=0,hcat2=0,hcat3=0,hcat4=0,hcat5=0,hcat6=0,hweekEnd=0,hweekDay=0;
            int scat1=0,scat2=0,scat3=0,scat4=0,scat5=0,scat6=0,sweekEnd=0,sweekDay=0;
            int stcat1=0,stcat2=0,stcat3=0,stcat4=0,stcat5=0,stcat6=0,stweekEnd=0,stweekDay=0;
            int rcat1=0,rcat2=0,rcat3=0,rcat4=0,rcat5=0,rcat6=0,rweekEnd=0,rweekDay=0;
            int hMorn=0,hAfter=0,hEven=0,hNight=0;
            int sMorn=0,sAfter=0,sEven=0,sNight=0;
            int stMorn=0,stAfter=0,stEven=0,stNight=0;
            int rMorn=0,rAfter=0,rEven=0,rNight=0;
            String mostUsedApp = null;
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date d1 = null,d2 = null;
            try {
                d1 = sdf1.parse(str1);
                d2 = sdf1.parse(str2);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String PROVIDER_NAME = "research.sg.edu.edapp.StatsProvider";
            String URL = "content://" + PROVIDER_NAME + "/EmoStats";
            Uri CONTENT_URI = Uri.parse(URL);
            ContentProviderClient CR = getContext().getContentResolver().acquireContentProviderClient(CONTENT_URI);
            try {
                Cursor tCursor = CR.query(CONTENT_URI, null, "date(" + StatsDetails.StatsEntry.TIMESTAMP + ") >= date(?) and date(" +
                        StatsDetails.StatsEntry.TIMESTAMP + ") <= date(?)", new String[]{sdf2.format(d1), sdf2.format(d2)}, null);
                tCursor.moveToFirst();

                if(tCursor.getCount() == 0) {
                    Toast.makeText(getContext(), "Zero records", Toast.LENGTH_SHORT).show();
                    return;
                }

                while (!tCursor.isAfterLast()) {
                    System.out.println( tCursor.getString(0)                //id
                                        + "," + tCursor.getString(1)        //appname
                                        + "," + tCursor.getString(2)        //timestamp
                                        + "," + tCursor.getString(3)        //appcategory
                                        + "," + tCursor.getString(4)        //weekend
                                        + "," + tCursor.getString(5)        //daysession
                                        + "," + tCursor.getString(6));      //emotion

                    //Process AppName
                    String string = tCursor.getString(1);
                    if (string.length() > 0) {
                        string = string.toLowerCase();
                        Integer count = stringsCount.get(string);
                        if(count == null) count = new Integer(0);
                        count++;
                        stringsCount.put(string,count);
                    }

                    //Count Emotions
                    if(tCursor.getInt(6)==2) {
                        happy++;
                        //Count App Category
                        if(tCursor.getInt(3)==1)
                            hcat1++;
                        else if(tCursor.getInt(3)==2)
                            hcat2++;
                        else if(tCursor.getInt(3)==3)
                            hcat3++;
                        else if(tCursor.getInt(3)==4)
                            hcat4++;
                        else if(tCursor.getInt(3)==5)
                            hcat5++;
                        else
                            hcat6++;

                        //Count WeekEnds
                        if(tCursor.getInt(4)==1)
                            hweekEnd++;
                        else
                            hweekDay++;

                        //Count Daysessions
                        if(tCursor.getInt(5)==1)
                            hMorn++;
                        else if(tCursor.getInt(5)==2)
                            hAfter++;
                        else if(tCursor.getInt(5)==3)
                            hEven++;
                        else
                            hNight++;

                    }
                    else if(tCursor.getInt(6)==-2) {
                        sad++;
                        //Count App Category
                        if(tCursor.getInt(3)==1)
                            scat1++;
                        else if(tCursor.getInt(3)==2)
                            scat2++;
                        else if(tCursor.getInt(3)==3)
                            scat3++;
                        else if(tCursor.getInt(3)==4)
                            scat4++;
                        else if(tCursor.getInt(3)==5)
                            scat5++;
                        else
                            scat6++;

                        //Count WeekEnds
                        if(tCursor.getInt(4)==1)
                            sweekEnd++;
                        else
                            sweekDay++;

                        //Count Daysessions
                        if(tCursor.getInt(5)==1)
                            sMorn++;
                        else if(tCursor.getInt(5)==2)
                            sAfter++;
                        else if(tCursor.getInt(5)==3)
                            sEven++;
                        else
                            sNight++;
                    }
                    else if(tCursor.getInt(6)==1) {
                        stressed++;
                        //Count App Category
                        if(tCursor.getInt(3)==1)
                            stcat1++;
                        else if(tCursor.getInt(3)==2)
                            stcat2++;
                        else if(tCursor.getInt(3)==3)
                            stcat3++;
                        else if(tCursor.getInt(3)==4)
                            stcat4++;
                        else if(tCursor.getInt(3)==5)
                            stcat5++;
                        else
                            stcat6++;

                        //Count WeekEnds
                        if(tCursor.getInt(4)==1)
                            stweekEnd++;
                        else
                            stweekDay++;

                        //Count Daysessions
                        if(tCursor.getInt(5)==1)
                            stMorn++;
                        else if(tCursor.getInt(5)==2)
                            stAfter++;
                        else if(tCursor.getInt(5)==3)
                            stEven++;
                        else
                            stNight++;
                            stNight++;
                    }
                    else if(tCursor.getInt(6)==0) {
                        relaxed++;
                        //Count App Category
                        if(tCursor.getInt(3)==1)
                            rcat1++;
                        else if(tCursor.getInt(3)==2)
                            rcat2++;
                        else if(tCursor.getInt(3)==3)
                            rcat3++;
                        else if(tCursor.getInt(3)==4)
                            rcat4++;
                        else if(tCursor.getInt(3)==5)
                            rcat5++;
                        else
                            rcat6++;

                        //Count WeekEnds
                        if(tCursor.getInt(4)==1)
                            rweekEnd++;
                        else
                            rweekDay++;

                        //Count Daysessions
                        if(tCursor.getInt(5)==1)
                            rMorn++;
                        else if(tCursor.getInt(5)==2)
                            rAfter++;
                        else if(tCursor.getInt(5)==3)
                            rEven++;
                        else
                            rNight++;
                    }

                    tCursor.moveToNext();
                }
                tCursor.close();

                for(Map.Entry<String, Integer> e: stringsCount.entrySet()) {
                    if(mostRepeated == null || mostRepeated.getValue()<e.getValue())
                        mostRepeated = e;
                }
                mostUsedApp = mostRepeated.getKey();
                System.out.println("Most Used APP ===>> " + mostUsedApp + " " + happy + " " + sad + " " + stressed + " " + relaxed);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getActivity(), StatsMenu.class);
            intent.putExtra("happy", happy);
            intent.putExtra("sad", sad);
            intent.putExtra("stressed", stressed);
            intent.putExtra("relaxed", relaxed);
            intent.putExtra("hcat1", hcat1);
            intent.putExtra("hcat2", hcat2);
            intent.putExtra("hcat3", hcat3);
            intent.putExtra("hcat4", hcat4);
            intent.putExtra("hcat5", hcat5);
            intent.putExtra("hcat6", hcat6);
            intent.putExtra("hweekEnd", hweekEnd);
            intent.putExtra("hweekEnd", hweekDay);
            intent.putExtra("hMorn", hMorn);
            intent.putExtra("hAfter", hAfter);
            intent.putExtra("hEven", hEven);
            intent.putExtra("hNight", hNight);
            intent.putExtra("scat1", scat1);
            intent.putExtra("scat2", scat2);
            intent.putExtra("scat3", scat3);
            intent.putExtra("scat4", scat4);
            intent.putExtra("scat5", scat5);
            intent.putExtra("scat6", scat6);
            intent.putExtra("sweekEnd", sweekEnd);
            intent.putExtra("sweekEnd", sweekDay);
            intent.putExtra("sMorn", sMorn);
            intent.putExtra("sAfter", sAfter);
            intent.putExtra("sEven", sEven);
            intent.putExtra("sNight", sNight);
            intent.putExtra("stcat1", stcat1);
            intent.putExtra("stcat2", stcat2);
            intent.putExtra("stcat3", stcat3);
            intent.putExtra("stcat4", stcat4);
            intent.putExtra("stcat5", stcat5);
            intent.putExtra("stcat6", stcat6);
            intent.putExtra("stweekEnd", stweekEnd);
            intent.putExtra("stweekEnd", stweekDay);
            intent.putExtra("stMorn", stMorn);
            intent.putExtra("stAfter", stAfter);
            intent.putExtra("stEven", stEven);
            intent.putExtra("stNight", stNight);
            intent.putExtra("rcat1", rcat1);
            intent.putExtra("rcat2", rcat2);
            intent.putExtra("rcat3", rcat3);
            intent.putExtra("rcat4", rcat4);
            intent.putExtra("rcat5", rcat5);
            intent.putExtra("rcat6", rcat6);
            intent.putExtra("rweekEnd", rweekEnd);
            intent.putExtra("rweekEnd", rweekEnd);
            intent.putExtra("startdate", rweekDay);
            intent.putExtra("rweekEnd", rweekDay);
            intent.putExtra("rMorn", rMorn);
            intent.putExtra("rAfter", rAfter);
            intent.putExtra("rEven", rEven);
            intent.putExtra("rNight", rNight);
            System.out.println("===>> Most Frequent App = " + mostUsedApp);
            intent.putExtra("mostUsedApp",mostUsedApp);
            startActivity(intent);
        }

        private void showDatePicker(int i) {
            DatePickerFragment date = new DatePickerFragment();

            Calendar calender = Calendar.getInstance();
            Bundle args = new Bundle();
            args.putInt("year", calender.get(Calendar.YEAR));
            args.putInt("month", calender.get(Calendar.MONTH));
            args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
            date.setArguments(args);

            if(i == 1)
                date.setCallBack(fordate1);
            else
                date.setCallBack(fordate2);
            date.show(getFragmentManager(), "Date Picker");
        }

        DatePickerDialog.OnDateSetListener fordate1 = new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                text1.setText(String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear + 1)
                        + "-" + String.valueOf(year));
            }
        };

        DatePickerDialog.OnDateSetListener fordate2 = new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                text2.setText(String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear + 1)
                        + "-" + String.valueOf(year));
            }
        };

    }

    public static class DatePickerFragment extends DialogFragment {
        DatePickerDialog.OnDateSetListener ondateSet;
        private int year, month, day;

        public DatePickerFragment() {}

        public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
            ondateSet = ondate;
        }

        @SuppressLint("NewApi")
        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            year = args.getInt("year");
            month = args.getInt("month");
            day = args.getInt("day");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), ondateSet, year, month, day);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Per Day Report";
                case 1:
                    return "Combined Report";
            }
            return null;
        }
    }

}
