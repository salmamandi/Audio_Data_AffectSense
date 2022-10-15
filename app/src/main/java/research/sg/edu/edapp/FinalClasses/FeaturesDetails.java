package research.sg.edu.edapp.FinalClasses;

import android.provider.BaseColumns;

/**
 * Created by weirdmyth on 25/11/16.
 */

public final class FeaturesDetails {
    public FeaturesDetails() {
    }

    /* Inner class that defines the table contents */
    public static abstract class FeaturesEntry implements BaseColumns {
        // TODO: FEATURE ADDITION done
        public static final String TABLE_NAME = "EmoFeatures";
        public static final String SESSIONID = "SessionId";
        public static final String MSI = "Msi";
        public static final String RMSI = "Rmsi";
        public static final String SESSIONLEN = "SessionLen";
        public static final String BACKSPACEPER = "BackSpacePer";
        public static final String SPLCHARPER = "SplCharPer";
        public static final String SESSIONDUR = "SessionDur";
        public static final String EMOTION = "Emotion";
        public static final String APP_NAME = "AppName";
        public static final String TIMESTAMP = "TimeStamp";

        public static final String PRESSURE = "mPressure";
        public static final String VELOCITY = "mVelocity";
        public static final String SWIPEDURATION = "mSwipeDuration";

        // add more features after controlled experiment for a week
        public static final String TYPETIME = "TypeTime";
        public static final String SWIPETIME = "SwipeTime";
        public static final String MODPRESSURE = "ModPressure";
        public static final String SDPRESSURE = "stdDevPressure";
        public static final String SDVELOCITY= "stdDevVeloctiy";
        public static final String ISSWIPE = "isSwipe";

        //public static final String ESM_KEY = "Key";
    }
}
