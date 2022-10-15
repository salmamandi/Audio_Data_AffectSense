package research.sg.edu.edapp;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MoodRecordPopUp extends AppCompatActivity {


    private RadioGroup radioMoodGroup;
    private RadioButton radioMoodButton;
    private Button btnRecordMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moodrecordpopup);
        this.setFinishOnTouchOutside(false);

        radioMoodGroup = (RadioGroup) findViewById(R.id.radioMoodGroup);
        btnRecordMood = (Button) findViewById(R.id.btnRecordMood);

        btnRecordMood.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int selectedId = radioMoodGroup.getCheckedRadioButtonId();
                try {
                    radioMoodButton = (RadioButton) findViewById(selectedId);
                    String mood = getMood(radioMoodButton);
                    Intent intent=new Intent();
                    intent.putExtra("mood",mood);
                    setResult(2,intent);
                    finish();
                }
                catch(Exception e) {
                    Toast.makeText(MoodRecordPopUp.this,"Please select your emotion", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String getMood(RadioButton radioMoodButton) {
        String mood="";
        String mood_string = (String)radioMoodButton.getText();

        switch(mood_string.toUpperCase()) {
            case "SAD / DEPRESSED": mood="Sad";
                break;
            case "HAPPY / EXCITED": mood="Happy";
                break;
            case "STRESSED": mood="Stressed";
                break;
            case "RELAXED": mood="Relaxed";
                break;
            case "NO RESPONSE": mood="No Response";
                break;
        }
        return mood;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(MoodRecordPopUp.this,"Please select your emotion", Toast.LENGTH_SHORT).show();
    }
}
