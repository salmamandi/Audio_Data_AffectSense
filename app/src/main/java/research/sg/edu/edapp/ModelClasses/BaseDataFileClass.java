package research.sg.edu.edapp.ModelClasses;

/**
 * Created by weirdmyth on 24/10/16.
 */

public class BaseDataFileClass {

    public String sessionId,appName,tapTime,tapKey,moodState,recordTime,pressure, velocity, swipeDuratiion;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTapTime() {
        return tapTime;
    }

    public void setTapTime(String tapTime) {
        this.tapTime = tapTime;
    }

    public String getTapKey() {
        return tapKey;
    }

    public void setTapKey(String tapKey) {
        this.tapKey = tapKey;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getVelocity() {
        return velocity;
    }

    public void setVelocity(String velocity) {
        this.velocity = velocity;
    }

    public String getSwipeDuration() {
        return swipeDuratiion;
    }

    public void setSwipeDuration(String swipeDuratiion) {
        this.swipeDuratiion = swipeDuratiion;
    }

    public String getMoodState() {
        return moodState;
    }

    public void setMoodState(String moodState) {
        this.moodState = moodState;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }
}
