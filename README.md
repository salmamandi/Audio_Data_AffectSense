# swype
Swipe keyboard for BTP

* In features, I planned on storing velocityx, and vy separately. Not doing that.

* Will sessiondur be same as duration? Perhaps not

* PredictionService -> //Store additional features
                       //mean_itd, #backspace_key,#splsymbol_key,#touch_count,#erased_text_length,typ_dur,time_of_day
                       line 138
                       
* BuildModelService.Java, `setAttributes()` line 374. Perhaps my features not be added since model is yet to be defined ?
* MyTestPrediction, `setAttributes()`, line 157. and 98 Exact same concern.

* SeeDatabase lacks R.id.pressure. Need to build now -> Added id in features_view_template.xml


CODE BASE:

* `ChartLibs/` need not be tampered with, since that is for showing charts
* `FinalClasses/` only new features added in featureDetails
* `inputmethodcommon/` maybe no changes required, since about fragmentation and related.
* `Modelclasses` - To add functions for get, set
 * BaseDataFileClass -> Added setSwipe, getSwipe Time, Key
 * FeatureDataFileClass -> Add pressure, velocity, duration

* MainActivity -> Features added. Corresponding function parameter length changed.
* CalculateKLD, MoodConfirmation -> mention features

* Features stored in same 9-tuple. Either it has msi, rmsi features, or it has pressure, velocity features


- [ ] http://www.cnergres.iitkgp.ac.in/projects/tapsense/ Check for login

Changes done, now committing
TO DO:
kb/ to detect swipe -< Only in kbSoftKeyBoard perhaps
ExtractFeatureService to extract features
Add isSwipe element in features to distinguish ?

FeatureDataClass initiate p, v, sD to 0.0

Add getSwipeWord, SHAIME (SwipeDetection) and words2 files in R.raw
Save_feature replacesd with FeatureDataFileClass, DB_helper with FeaturesProvider
DBHelper used SQLite to save, need to see how saving done here to proceed further

* Managed to get basic swype working. Sep 10 7:46 pm
Things TODO:
* Backspace
* Port SoftKeyBoard to SwipeDetection

- [x] Velocity, pressure, duration, is A-okay.
- [x] Did not add sendKey, onKey from kbSoftkeyboard. Rest all ported
- [ ] X check utility of printSamples in SwipeDetection
- [ ] X check if playclick needed -> doesn't work
- [x] See how logs are saved. 
- [x] Switching to emojis should work, 
- [x] space, caps, del
- [x] Selecting suggested words. Suggestions work
- [x] Selecting suggestions crashes
- [x] Rename SwipeDetection to kbSwipeKeyboard
- [x] suggest even on typing
- [x] emojis should print

x_y_Tap_label.txt => id, app, tap time, tap key, mood state, record time
every move

Features/Feature_label => sid, msi, rmsi, sessionlen, backspace, splchar, sessiondur, appname, record time, moodstate
difference of 2-3 hours

- [x] Easiest solution to store pressure, velocity, duration: store in tap_files. Every onKey activity is recorded.
- [x] PVD not computed in ExtractFeaturesService

Extensions:

- [x] X Store v_x, v_y separately ?
- [ ] Read new words, names and such
- [ ] use ngram for suggestion
- [ ] Frequency implies usability, ranked suggestions perhaps ?
- [x] X suggest only top 5 words to increase response - Incorrect. It's slow because it has to read every time the raw file
- [x] X Typing 'g', suggestions made. Deleted 'g', must remove suggestions perhaps ? not really important
- [ ] continuous backspace could be faster
- [x] Capitalise after '.'. 
- [x] First letter capital if it was capital.
- [x] Add manual logging ?
- [ ] No suggestions for number/ smileys
- [ ] "Fill data" crashes app
- [ ] Fancy stuff for ESM notification. Grid of emojis basically.
- [ ] Find faster way to use dictionary than read file, or perhaps suggest asynchronously - trie?
- [ ] no need to suggest if n-1 did not have suggestion

- [ ] New features to be suggested:
    * % type
    * velocity only above certain threshold, which would mean only proper swipes are counted. If velocity is good, record the pressure, else not.
    * Pick a 90 %ile value.
    * More features to be added - swipetime, taptime, modpressure,std deviation for pressure, velocity
	* separate velocity and pressure features with isSwipe. Tap separate, swipe separate
	
- [ ] better dictionary
	
- [ ] see how much our suggestions were used - autosuggest
	
- [ ] modPressure - upto 0.01 neighbourhood
    * Why are we using only modPressure, and not modVelocity?
    * Velocity, unlike pressure, has a simple correlation with the typing / swiping speed of the user.
    * However, the same cannot be said about pressure. Even in slow typing instances, the pressure values can be very high, which might 
        lead to inconclusive results. We can remove those discrepencies by taking the mod value, which could give us a better idea of 
        centrality rather than meanPressure.
    * mPressure, mVelocity are mean values.

Resume from ExtractFeatureService.java, line 276