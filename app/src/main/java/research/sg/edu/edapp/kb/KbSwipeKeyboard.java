package research.sg.edu.edapp.kb;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import androidx.annotation.NonNull;
import androidx.core.view.VelocityTrackerCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import research.sg.edu.edapp.R;

import static android.content.ContentValues.TAG;
import static java.lang.Math.abs;

/**
 * Created by kaustubh on 1/9/17.
 */
/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.
 */
public class KbSwipeKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener{

    static final boolean DEBUG = false;

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;

    private InputMethodManager mInputMethodManager;

    private KbLatinKeyboardView mInputView;
    private KbCandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock, mBigCaps = false;
    private long mLastShiftTime;
    private long mMetaState;

    private KbLatinKeyboard mSymbolsKeyboard, mSymbolsShiftedKeyboard;
    private KbLatinKeyboard mQwertyKeyboard;
    private KbLatinKeyboard mSmileyKeyboard,mSmileyShiftedKeyboard;
    private KbLatinKeyboard mPhoneKeyboard,mPhoneShiftedKeyboard;
    private KbLatinKeyboard mNumKeyboard,mNumPassKeyboard;
    private KbLatinKeyboard mDateKeyboard,mTimeKeyboard;
    private KbLatinKeyboard mCurKeyboard;
    ArrayList<String> mCandidateList;
    private String mWordSeparators;

    private String imei_no;
    private File dataDir,sdCardRoot;
    private int LongestWordLength = 20;

    private static String old_pkg="Dummy_Pkg";

    private String swipe = "", test = "", swipe2 = "";
    private List<Keyboard.Key> keyList;
    private double pressure,duration,velocity,start,end;
    private VelocityTracker mvel=null;
    private float old_x=0,old_y=0,olddir_x=0,olddir_y=0;
    private GetSwipeWord swipeWord;
    double x_vel=0,y_vel=0;
    int n_event=1,np_event=1;
    char frequent_char=0;
    private List<String> mSuggestions; // store top suggestions in string
    private String currentDateandTime;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class RecentUseComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        Log.d("HI THERE","HI THERE");
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);

        //Clear the SharedPreference
        //SharedPreferences prefs =
        //getApplicationContext().getSharedPreferences("TapSenseSharedPref", Context.MODE_WORLD_READABLE).edit().clear().apply();
        //SharedPreferences.Editor editor = prefs.edit();
        //editor.putString("TimeStamp", currentDateandTime);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mQwertyKeyboard = new KbLatinKeyboard(this, R.xml.qwerty);
            mSymbolsKeyboard = new KbLatinKeyboard(this, R.xml.symbols);
            mSymbolsShiftedKeyboard = new KbLatinKeyboard(this, R.xml.symbols_shift);
        }
        else {
            mQwertyKeyboard = new KbLatinKeyboard(this, R.xml.qwerty_v4);
            mSymbolsKeyboard = new KbLatinKeyboard(this, R.xml.symbols_v4);
            mSymbolsShiftedKeyboard = new KbLatinKeyboard(this, R.xml.symbols_shift_v4);
        }
        mSmileyKeyboard = new KbLatinKeyboard(this, R.xml.smileys);
        mSmileyShiftedKeyboard = new KbLatinKeyboard(this, R.xml.smileys_shift);

        mPhoneKeyboard = new KbLatinKeyboard(this, R.xml.phone_keys);
        mPhoneShiftedKeyboard = new KbLatinKeyboard(this, R.xml.phone_shiftkeys);

        mNumKeyboard = new KbLatinKeyboard(this, R.xml.number_keys);
        mNumPassKeyboard = new KbLatinKeyboard(this, R.xml.numpassword_keys);
        mDateKeyboard = new KbLatinKeyboard(this, R.xml.date_keys);
        mTimeKeyboard = new KbLatinKeyboard(this, R.xml.time_keys);

        sdCardRoot = Environment.getExternalStorageDirectory();
        dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = telephonyManager.getDeviceId();
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        int variation;
        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
                mCurKeyboard = mNumKeyboard;
                variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                    mCurKeyboard = mNumPassKeyboard;

                break;
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;

                if (variation == InputType.TYPE_DATETIME_VARIATION_DATE)
                    mCurKeyboard = mDateKeyboard;
                else if (variation == InputType.TYPE_DATETIME_VARIATION_TIME)
                    mCurKeyboard = mTimeKeyboard;
                else
                    mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mPhoneKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mQwertyKeyboard;
                mPredictionOn = true;
                //mPredictionOn = false;

                // We now look for a few special variations of text that will
                // modify our behavior.
                variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();

        // TODO: Do something to get all the text, when finished
        // or sent enter, read all entries, add each to dict

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        mCurKeyboard = mQwertyKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        // Apply the selected keyboard to the input view.
        mInputView.setKeyboard(mCurKeyboard);
        retrieveKeys();
        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        mInputView.setSubtypeOnSpaceKey(subtype);
        if(mCurKeyboard == mQwertyKeyboard) {
            mCapsLock = true;
            mInputView.setShifted(mCapsLock);
        }
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                            int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    public void retrieveKeys() {
        keyList = mInputView.getKeyboard().getKeys();
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, 1);
            System.out.println("Commit typed: "+mComposing+ " "+mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        return Character.isLetter(code);
    }


    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    public String getAppName() {
        String packagename;
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                //it is locked
                System.out.println("[AppLogger]Screen is locked");
                packagename = "LockScreen";
            }
            else {
                //it is not locked

                ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                packagename = componentInfo.getPackageName();
                System.out.println("[AppLogger]Build Version:"+Build.VERSION.SDK_INT+",Package Name:"+packagename );
            }
        }
        else {
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                //it is locked
                System.out.println("[AppLogger]Screen is locked");
                packagename = "LockScreen";
            }
            else {
                packagename=getTopPackage();
            }
            System.out.println("[AlarmReceiver]Build Version:"+Build.VERSION.SDK_INT + "Package Name:" +packagename);
        }
        return packagename;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String getTopPackage() {

        RecentUseComparator mRecentComp =new RecentUseComparator();

        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts-1000*10, ts);
        if (usageStats == null || usageStats.size() == 0) {
            //return NONE_PKG;
            return old_pkg;
        }
        Collections.sort(usageStats, mRecentComp);
        old_pkg=usageStats.get(0).getPackageName();
        return usageStats.get(0).getPackageName();
    }

    @Override public View onCreateCandidatesView() {
        mCandidateView = new KbCandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }

    /**
     * given motion event find which key character is at that particular coordinate
     */
    public char getkeylabel(MotionEvent event) {
        char c=0;
        String s="";

        // For each key in the key list
        for (Keyboard.Key k : keyList) {
            // If the coordinates from the Motion event are inside of the key
            if (k.isInside((int) event.getX(), (int) event.getY())) {
                // k is the key pressed
                Log.d("Debugging",
                        "Key pressed: key=" + k.label+ " X=" + k.x + " - Y=" + k.y);
                int centreX, centreY;
                centreX = (k.width/2) + k.x;
                centreY = (k.width/2) + k.x;
                s= String.valueOf(k.label);
                if(s.equals("null") || s.equals("SPACE")||s.equals("CAPS")||s.equals("DEL")) {
                    Log.d("Debugging","special char: space/caps/del");
                    //swipe="";
                    c = 0;
                }
                else
                    c=k.label.charAt(0);
                // These values are relative to the Keyboard View
                // Log.d("Debugging",
                //       "Centre of the key pressed: X="+centreX+" - Y="+centreY);
            }
        }

        return c;
    }

    /**
     * function to retrieve key if direction has changed
     */
    public void check_change(MotionEvent event) {

        float new_x=event.getX(),new_y=event.getY();
        // if(((new_x-old_x)>0 && olddir_x<0)||((new_x-old_x)<0 && olddir_x>0)){
        swipe+=getkeylabel(event);
        //}

        //   if(((new_y-old_y)>0 && olddir_y<0)||((new_y-old_y)<0 && olddir_y>0)){
        //  swipe+=getkeylabel(event);
        //}
    }

     /*
      * get unique characters in string
      */
    public String get_final_string(String s) {

        String ans = "", s2 = "";
        int freq[] = new int[256];
        int count = 1, count1 = 1, count2 = 1, count3 = 1;
        int len = s.length(), avg = 0, iter = 0;
        char c1 = 0,c2 = 0 ,c3 = 0;

        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) >= 97 && s.charAt(i) <= 122)
                s2+=s.charAt(i);
        }
        s = s2;
        len = s.length();
        if(len == 0)
        {
            frequent_char = 0;
            return null;
        }
        if(len == 1) {
            frequent_char = 0;
            return s;
        }
        ans += s.charAt(0);
        /*
        if(s.charAt(0)>=65 && s.charAt(0)<=91)
        {   if(len==1)
            {
            return null;
            }
            s=s.substring(1);
            len--;
        }
        */
        //not working
        /*
        c1=s.charAt(0);
        int iter=1;
        while(s.charAt(iter)==s.charAt(iter-1))
        {iter+=1;
        count1+=1;
        }
        c2=s.charAt(iter);
        for(iter+=1;iter<len;iter++){
            if(s.charAt(iter)==s.charAt(iter-1)){
                count2+=1;
            }
        }
        c3=s.charAt(iter);
        for(iter+=1;iter<len;iter++){
            if(s.charAt(iter)==s.charAt(iter-1)){
                count3+=1;
            }
        }
        if(count1>=count2){
            if(count2>=count3){

            }else{
                int temp=count2;
                char t=c2;
                count2=count3;
                c2=c3;
                count3=temp;
                c3=t;

            }
        }else{
            int temp=count2;
            char t=c2;
            count2=count1;
            c2=c1;
            count1=temp;
            c1=t;
            if(count2>=count3){

            }else{
                if()
            }
        }
        */
        //working method
        count1 = 1;count2 = 1;
        c1 = s.charAt(0);
        for(int i = 1; i < len; i++) {
            if(s.charAt(i-1) == s.charAt(i)) {
                count1++;
                continue;
            }
            else{
                if(count1 > count2) {
                    count2 = count1;
                    c1 = s.charAt(i-1);

                }
                count1 = 1;
                count+= 1;
            }
        }
        avg = len/count;
        frequent_char = c1;
        int current = 1;

        for(int i = 1; i < len; i++) {
            if(s.charAt(i-1) == s.charAt(i)) {
                current += 1;
                continue;
            }
            else{
                if(current > avg & ans.charAt(ans.length()-1) != s.charAt(i-1))
                    ans += s.charAt(i-1);
                current = 1;
            }
        }

        return ans;
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override
    public View onCreateInputView() {
        mInputView = (KbLatinKeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setKeyboard(mQwertyKeyboard);
        if(mInputView.isPreviewEnabled())
            mInputView.setPreviewEnabled(false);

        KbSwipeKeyboard gd = new KbSwipeKeyboard();
        swipeWord = new GetSwipeWord(this);

        mInputView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                int index = event.getActionIndex();
                int action = event.getActionMasked();
                int pointerId = event.getPointerId(index);

                //check for actions of motion event
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    //retrieve key at current
                    char ch = getkeylabel(event);
                    if(ch > 0)
                        swipe += ch;
                    // test+=swipe.charAt(0);
                    //set up start timer for measuring duration
                    //  start=System.currentTimeMillis();
                    //setup velocity tracker
                    if(mvel == null) {
                        // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                        mvel = VelocityTracker.obtain();
                    }
                    else {
                        // Reset the velocity tracker back to its initial state.
                        mvel.clear();
                    }
                }

                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    mvel.addMovement(event);
                    mvel.computeCurrentVelocity(1000);
                    // Log velocity of pixels per second
                    // Best practice to use VelocityTrackerCompat where possible.
                    x_vel += abs(VelocityTrackerCompat.getXVelocity(mvel,pointerId));
                    y_vel += abs(VelocityTrackerCompat.getYVelocity(mvel,pointerId));
                    n_event += 1;
                    //  Log.d("", "X velocity: " +  x_vel);
                    //  Log.d("", "Y velocity: " +  y_vel);

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //record time when finger lifted up
                    //           end=System.currentTimeMillis();
                    //calculate duration
                    //         duration=(end-start)/100;
                    //calculate velocity pixels per sec
                    //  Log.d("", "X velocity: " +  x_vel);
                    // Log.d("", "Y velocity: " +  y_vel);

                    velocity = Math.sqrt(x_vel*x_vel + y_vel*y_vel);
                    //obtain pressure
                    pressure += event.getPressure();
                    np_event += 1;
                    // initially thought to store P,V,D at this point, but incorrect
                    // get final string
                    swipe2 = get_final_string(swipe);
                    swipe = "";
                    if(swipe2 == null)
                        swipe2 = "";
                    // print generated string
                    System.out.println(swipe+"\n 2nd "+ swipe2);
                }

                if(((int)old_x) == 0 & ((int) old_y) == 0) {
                    old_x = event.getX();
                    old_y = event.getY();
                    swipe += getkeylabel(event);
                }
                else if(((int)olddir_x) == 0 &((int)olddir_y) == 0) { 
                    olddir_x = event.getX()-old_x;
                    olddir_y = event.getY()-old_y;
                    old_x = event.getX();
                    old_y = event.getY();
                }
                else{
                    check_change(event);
                }

                // Return false to avoid consuming the touch event
                return false;
            }
        });
        // swipe="";
        return mInputView;
    }

    // printSamples might not be required here
    void printSamples(MotionEvent ev) {

        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        // setContentView(R.layout.activity_main);
        //TextView display = (TextView) findViewById(R.id.showMotion);
        double time=0,xcord=0,ycord=0,pathlength=0,xs=0,xe=0,ys=0,ye=0,pressure=0;
        String show = "";
        pressure=ev.getPressure();
        for (int h = 0; h < historySize; h++) {

            // System.out.printf("At time %d:", ev.getHistoricalEventTime(h));
            if(h!=0) {
                time += (ev.getHistoricalEventTime(h)-ev.getHistoricalEventTime(h-1));
            }

            for (int p = 0; p < pointerCount; p++) {
                xcord+=ev.getHistoricalX(p, h);
                ycord+=ev.getHistoricalY(p, h);
                //   show += String.format("  pointer %d: (%f,%f)",
                //         ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
            xcord/=pointerCount;
            ycord/=pointerCount;
            if (h==0)
            {
                xs=xcord;
                ys=ycord;
            }else{
                xe=xcord;
                ye=ycord;
            }

        }
        pathlength=Math.sqrt((xs-xe)*(xs-xe)+(ys-ye)*(ys-ye));
        time/=historySize;
        //show+=String.format("At time %d:", ev.getEventTime());
        show += String.format("Show %f,%f",time,pathlength);
        for (int p = 0; p < pointerCount; p++) {
            //show += String.format("  pointer %d: (%f,%f)",ev.getPointerId(p); ev.getX(p); ev.getY(p);
        }

        System.out.println(show);
    }

    void printSamples(MotionEvent ev, MotionEvent ev2) {

        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        // setContentView(R.layout.activity_main);
        //     TextView display = (TextView) findViewById(R.id.showMotion);
        String show = "";
        for (int h = 0; h < historySize; h++) {
            //System.out.printf("At time %d:", ev.getHistoricalEventTime(h));

            for (int p = 0; p < pointerCount; p++) {
                show += String.format("  pointer %d: (%f,%f)", ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
        }
        show += String.format("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            show += String.format("  pointer %d: (%f,%f)",
                    ev.getPointerId(p), ev.getX(p), ev.getY(p));
        }
        show += "\n";
        for (int h = 0; h < historySize; h++) {
            System.out.printf("At time %d:", ev.getHistoricalEventTime(h));

            for (int p = 0; p < pointerCount; p++) {
                show += String.format("  pointer %d: (%f,%f)",
                        ev2.getPointerId(p), ev2.getHistoricalX(p, h), ev2.getHistoricalY(p, h));
            }
        }
        show += String.format("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            show += String.format("  pointer %d: (%f,%f)",
                    ev2.getPointerId(p), ev2.getX(p), ev2.getY(p));
        }
        System.out.println(show);
//        display.setText(show);
    }

    @Override
    public void onRelease(int primaryCode) {
        // if(!(primaryCode==Keyboard.KEYCODE_DELETE || primaryCode==Keyboard.KEYCODE_SHIFT || (char)primaryCode==' ')) {
            //record time when finger lifted up
            end = System.currentTimeMillis();
            //calculate duration
            duration = (end - start) / 1000;
            //pressure is pressure/nt
            // TODO: Not dividing pressure here
            pressure = pressure ;/// np_event;
            Log.d("ans", "X velocity: " + x_vel / n_event);
            Log.d("ans", "Y velocity: " + y_vel / n_event);
            Log.d("ans", "pressure: " + pressure);
            Log.d("ans", "duration: " + duration);

            //save into database
            ContentValues values = new ContentValues();
            values.put(KbTouchEvent.TouchEntry.TE_APP_NAME, getAppName());
            values.put(KbTouchEvent.TouchEntry.TE_TIMESTAMP, currentDateandTime);
            values.put(KbTouchEvent.TouchEntry.TE_KEY, primaryCode);
            values.put(KbTouchEvent.TouchEntry.TE_PRESSURE, pressure);
            // TODO: Need to store v_x and v_y separately ?
            values.put(KbTouchEvent.TouchEntry.TE_VELOCITY, velocity);
            values.put(KbTouchEvent.TouchEntry.TE_SWIPE_DURATION, duration);
            Uri uri = getContentResolver().insert(KbContentProvider.CONTENT_URI, values);
        // }
        //end saving
        start = 0; end = 0;
        pressure = 0; np_event = 1;
        x_vel = 0; y_vel = 0; n_event = 1;
        swipe = ""; test = "";
    }

    @Override
    public void onPress(int primaryCode) {
        String first = "" + (char)primaryCode;
        test += (char)primaryCode;
        Log.d("Like",first);
        if(first == ".") {
            mCapsLock = true;
            mInputView.setShifted(mCapsLock);
        }
        //swipe=(char)primaryCode+swipe;
        //set up start timer for measuring duration
        start = System.currentTimeMillis();
    }

    public String strings_matched(String regex){
        ArrayList<String> suggest = swipeWord.get_suggestion(regex);
        String result = "";
        int count = 0;
        System.out.println("Dictionary swipe: " + suggest);
        for (String s : suggest) {
            if (s.length() >= swipe2.length() - 2 && s.length() <= swipe2.length() + 2) {
                // System.out.println("Dictionary are " + s);
                if(count==0)
                    result=s;
                count++;
                if (count == 5) // prints top 5 matches
                    break;
            }
        }
        setSuggestions(suggest, true, true);
        return result;

    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        // TODO: Read all words, add to dict
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        currentDateandTime = sdf.format(new Date());
        // originally storing features here, but can't use PVD features.
        // since every interaction will have a computed pressure value with it, data will be same

        InputConnection ic = getCurrentInputConnection();
        // playClick(primaryCode); // doesn't work anyway
        String s = "";
        //s+=c;
        //Log.d("coming",s);

        if (isWordSeparator(primaryCode) || primaryCode == 32) {
            // System.out.println("Inside OnKey : "+mComposing);
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());

        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            swipe = "";
            handleBackspace();
            // System.out.println("backspace done");
            typedWordSuggestions();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == Keyboard.KEYCODE_DONE) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));

        } else if (primaryCode == KbLatinKeyboardView.KEYCODE_OPTIONS) {
        // Show a menu or somethin'

        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                current = mQwertyKeyboard;
            } else {
                current = mSymbolsKeyboard;
            }
            mInputView.setKeyboard(current);
            if (current == mSymbolsKeyboard) {
                current.setShifted(false);
            }

        } else if (primaryCode == -9 && mInputView != null) {
            // emoji key
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard || current == mQwertyKeyboard) {
                current = mSmileyKeyboard;
            } else {
                current = mQwertyKeyboard;
            }
            mInputView.setKeyboard(current);
            if (current == mSmileyKeyboard) {
                current.setShifted(false);
            }

        } else if (primaryCode == -11 && mInputView != null) {
            // shift key
            Keyboard current = mInputView.getKeyboard();
            if (current == mPhoneKeyboard) {
                current = mPhoneShiftedKeyboard;
            } else {
                current = mPhoneKeyboard;
            }
            mInputView.setKeyboard(current);
            if (current == mSmileyKeyboard) {
                current.setShifted(false);
            }
        } else {
            Keyboard current = mInputView.getKeyboard();
            if(current == mSmileyKeyboard || current == mSmileyShiftedKeyboard) {
                mComposing.append((char) 55357);
                commitTyped(getCurrentInputConnection());
            }
            char code = (char) primaryCode;
            if (swipe2.length() != 0 && code != swipe2.charAt(swipe2.length() - 1)) {
                swipe2 += "+" + code;
            }
            if (frequent_char == 0 || frequent_char == code || frequent_char == test.charAt(0))
                test += ".*" + code;
            else
                test += ".*" + frequent_char + ".*" + code;
            Log.d("Pattern", test);
            if (Character.isLetter(code) && mInputView.isShifted()) {
                code = Character.toUpperCase(code);
            }
            if (swipe2.length() < 3) {
                ic.commitText(String.valueOf(code), 1);
                // updateCandidates();
                if(mInputView.isShifted() && !mBigCaps) {
                    if(mCapsLock) {
                        mCapsLock = false;
                    }
                    mInputView.setShifted(!mInputView.isShifted());
                }
                typedWordSuggestions();
                swipe = "";
                swipe2 = "";
            } else {
                s = strings_matched(test);
                s += " ";
                if (s.length() <= 1) {
                    ic.commitText(String.valueOf(code), 1);
                    // updateCandidates();
                    if(mInputView.isShifted() && !mBigCaps) {
                        if(mCapsLock) {
                            mCapsLock = false;
                        }
                        mInputView.setShifted(!mInputView.isShifted());
                    }
                    typedWordSuggestions();
                } else {
                    if(mInputView.isShifted()) {
                        s = s.substring(0, 1).toUpperCase() + s.substring(1);
                    }
                    if(mInputView.isShifted() && !mBigCaps) {
                        if(mCapsLock) {
                            mCapsLock = false;
                        }
                        mInputView.setShifted(!mInputView.isShifted());
                    }

                    ic.commitText(s, s.length());
                }
            }
        }
    }

    /*
     * Whenever no swipe motion is done, just consider substring matching problem
     */
    private void typedWordSuggestions() {
        String lastWord = getLastWord().toLowerCase();
        String lastChar = (String) getCurrentInputConnection().getTextBeforeCursor(1, 0);
        if(lastChar == " " || lastChar == "") {
            setSuggestions(null, false, false);
        }
        ArrayList<String> suggest = swipeWord.get_suggestion(lastWord + ".*");
        int count = 0;
        System.out.println("Dictionary: " + suggest);
        /*
        for (String s: suggest) {
            System.out.println("Dictionary are " + s);
            count++;
            if (count == 5)
                break;
        }
        */
        setSuggestions(suggest, true, true);

    }
    // for audio. Doesn't work
    private void playClick(int keyCode){
       /*
         AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
        */
    }

    public ArrayList<String> readFile() {
        ArrayList<String> list = new ArrayList<String>();

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.words2);

            InputStreamReader dataInputStream = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(dataInputStream);
            String each = null;
            while ((each = reader.readLine()) != null) {
                list.add(each);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;

    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        ArrayList<String> listData = readFile();
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                //list.add(mComposing.toString());
                for (int j = 0; j < listData.size(); j++) {
                    String str = mComposing.toString().toLowerCase();
                    if (listData.get(j).startsWith(str)) {
                        list.add(listData.get(j));
                    }
                }
                mCandidateList = list;
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
            mSuggestions = suggestions;
        }
    }

    /**
     * Return the last proper word, which can be replaced by the suggestive text
     */
    @NonNull
    private String getLastWord() {
        String typed = (String) getCurrentInputConnection().getTextBeforeCursor(LongestWordLength, 0);
        int split = typed.lastIndexOf(' ');
        // in the event last character is space
        if(typed.length() < 1) {
            return "";
        }
        if(typed.charAt(typed.length() - 1) == ' ') {
            String type2 = typed.substring(0, typed.length() - 1);
            split = type2.lastIndexOf(' ');
        }
        // split = -1 => no match
        split += 1;
        String lastWord = typed.substring(split, typed.length());
        // System.out.println("Split "+ split + " "+ typed.substring(0, split) + "<" + lastWord + ">");
        return lastWord;
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            if (mCapsLock && !mBigCaps) {
                mBigCaps = true;
                mCapsLock = !mCapsLock;
            } else if (mCapsLock && mBigCaps) {
                mBigCaps = false;
            }
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }
        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            mInputView.setKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        } else if (currentKeyboard == mSmileyKeyboard) {
            mSmileyKeyboard.setShifted(true);
            mInputView.setKeyboard(mSmileyShiftedKeyboard);
            mSmileyShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSmileyShiftedKeyboard) {
            mSmileyShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mSmileyKeyboard);
            mSmileyKeyboard.setShifted(false);
        }
    }

    // redundant function, kept for reference
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        // System.out.println("Handle Character");
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } else {
            getCurrentInputConnection().commitText(mComposing, 1);
            getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if(index >= 0 && index < mSuggestions.size()) {
            String selected = mSuggestions.get(index);

            String rmed = getLastWord();
            int rm = rmed.length();
            getCurrentInputConnection().deleteSurroundingText(rm, 0);
            boolean isCapped = (rmed.substring(0, 1).equals(rmed.substring(0, 1).toUpperCase()));
            if(isCapped) {
                selected = selected.substring(0, 1).toUpperCase() + selected.substring(1);
            }
            getCurrentInputConnection().commitText(selected, selected.length());
        }
        /*
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            mComposing.setLength(index);
            mComposing = new StringBuilder(mCandidateList.get(index) + " ");
            commitTyped(getCurrentInputConnection());
        }
        */
    }
    //on keyboard listener
    @Override
    public void swipeUp() {
        Log.d(TAG,"Up");
    }

    @Override
    public void swipeRight() {
        Log.d(TAG,"Right");
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    @Override
    public void swipeLeft() {
        Log.d(TAG,"Left");
        handleBackspace();
    }

    @Override
    public void swipeDown() {
        Log.d(TAG,"Down");
        handleClose();
    }
}