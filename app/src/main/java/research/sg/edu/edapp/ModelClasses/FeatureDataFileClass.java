package research.sg.edu.edapp.ModelClasses;

import java.io.Serializable;

/**
 * Created by weirdmyth on 25/10/16.
 */

public class FeatureDataFileClass implements Serializable {

    public String sessionId,msi,rmsi,splchar_percentage,backspace_percentage,sessionLength,sessionDuration,appName,recordTime,moodState;
    // TODO: FEATURE ADDITION MINE ADDED
    public String mpressure, mvelocity, mswipeDuration;
    public String typeTime, swipeTime, modPressure, sdPressure, sdVelocity, isSwipe;

    public String getRecordTime() {
        return recordTime;
    }

    public String getSessionLength() {
        return sessionLength;
    }

    public void setSessionLength(String sessionLength) {
        this.sessionLength = sessionLength;
    }

    public String getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(String sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public String getSessionId() {

        return sessionId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMsi() {
        return msi;
    }

    public void setMsi(String msi) {
        this.msi = msi;
    }

    public String getRmsi() {
        return rmsi;
    }

    public void setRmsi(String rmsi) {
        this.rmsi = rmsi;
    }

    public String getSplchar_percentage() {
        return splchar_percentage;
    }

    public void setSplchar_percentage(String splchar_percentage) {
        this.splchar_percentage = splchar_percentage;
    }

    public String getBackspace_percentage() {
        return backspace_percentage;
    }

    public void setBackspace_percentage(String backspace_percentage) {
        this.backspace_percentage = backspace_percentage;
    }

    public String getMoodState() {
        return moodState;
    }

    public void setMoodState(String moodState) {
        this.moodState = moodState;
    }
    
    public String getmpressure() {
        return mpressure;
    }

    public void setmpressure(String mpressure) {
        this.mpressure = mpressure;
    }

    public String getmvelocity() {
        return mvelocity;
    }

    public void setmvelocity(String mvelocity) {
        this.mvelocity = mvelocity;
    }

    public String getmswipeDuration() {
        return mswipeDuration;
    }

    public void setmswipeDuration(String mswipeDuration) {
        this.mswipeDuration = mswipeDuration;
    }

    public String getTypeTime() {
        return typeTime;
    }

    public void setTypeTime(String typeTime) {
        this.typeTime = typeTime;
    }

    public String getSwipeTime() {
        return swipeTime;
    }

    public void setSwipeTime(String swipeTime) {
        this.swipeTime = swipeTime;
    }

    public String getModPressure() {
        return modPressure;
    }

    public void setModPressure(String modPressure) {
        this.modPressure = modPressure;
    }

    public String getSdPressure() {
        return sdPressure;
    }

    public void setSdPressure(String sdPressure) {
        this.sdPressure = sdPressure;
    }

    public String getSdVelocity() {
        return sdVelocity;
    }

    public void setSdVelocity(String sdVelocity) {
        this.sdVelocity = sdVelocity;
    }

    public String getIsSwipe() {
        return isSwipe;
    }

    public void setIsSwipe(String isSwipe) {
        this.isSwipe = isSwipe;
    }
}
