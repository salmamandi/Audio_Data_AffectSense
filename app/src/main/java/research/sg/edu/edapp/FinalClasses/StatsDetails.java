package research.sg.edu.edapp.FinalClasses;

import android.provider.BaseColumns;

/**
 * Created by weirdmyth on 25/11/16.
 */

public final class StatsDetails {
    public StatsDetails() {
    }

    /* Inner class that defines the table contents */
    public static abstract class StatsEntry implements BaseColumns {

        public static final String TABLE_NAME = "EmoStats";
        public static final String EMOTION = "Emotion";
        public static final String APP_NAME = "AppName";
        public static final String TIMESTAMP = "TimeStamp";
        public static final String APP_CAT = "AppCategory";
        public static final String WEEKEND = "WeekEnd";
        public static final String DAYSESSION = "DaySession";
        //public static final String ESM_KEY = "Key";
    }
}
