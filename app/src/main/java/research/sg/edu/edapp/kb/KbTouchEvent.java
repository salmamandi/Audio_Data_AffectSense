package research.sg.edu.edapp.kb;

import android.provider.BaseColumns;

public final class KbTouchEvent {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public KbTouchEvent() {
    }

    /* Inner class that defines the table contents */
    public static abstract class TouchEntry implements BaseColumns {

        // TODO: FEATURE ADDITION
        public static final String TE_TABLE_NAME = "TouchEvent";
        public static final String TE_APP_NAME = "AppName";
        public static final String TE_TIMESTAMP = "TimeStamp";
        public static final String TE_KEY = "Key";
        public static final String TE_PRESSURE = "Pressure";
        public static final String TE_VELOCITY = "Velocity";
        public static final String TE_SWIPE_DURATION = "SwipeDuration";
        public static final String TE_TYPETIME = "TapTime";
        public static final String TE_SWIPETIME = "SwipeTime";
        public static final String TE_MODPRESSURE = "ModPressure";
        public static final String TE_SDPRESSURE = "stdDevPressure";
        public static final String TE_SDVELOCITY= "stdDevVeloctiy";
        public static final String TE_ISSWIPE = "isSwipe";
    }
}