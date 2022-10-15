package research.sg.edu.edapp;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    String[] timeStampList;
    String[] emotionList;
    LayoutInflater inflter;

    SparseIntArray radioChecked;
    public static ArrayList<String> selectedAnswers;

    static class ViewHolder {
        protected Button button;
        protected RadioButton happy;
        protected RadioButton sad;
        protected RadioButton stressed;
        protected RadioButton relaxed;
        protected RadioGroup emotions;
    }

    public CustomAdapter(Context applicationContext, String[] timeStampList,String[] emotionList) {
        this.context = applicationContext;
        this.timeStampList = timeStampList;
        this.emotionList = emotionList;
        radioChecked = new SparseIntArray(timeStampList.length);
        selectedAnswers = new ArrayList<>();
        for (int i = 0; i < timeStampList.length; i++) {
            selectedAnswers.add("No Response");
        }
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return timeStampList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        final int index = i;
        ViewHolder viewHolder = null;
        if (view == null) {
            view = inflter.inflate(R.layout.validate_predictions_template, null);
            viewHolder = new ViewHolder();
            viewHolder.emotions = (RadioGroup) view.findViewById(R.id.emotionRadioGroup);
            viewHolder.button = (Button) view.findViewById(R.id.button8);
            viewHolder.happy = (RadioButton) view.findViewById(R.id.happyRadioButton);
            viewHolder.sad = (RadioButton) view.findViewById(R.id.sadRadioButton);
            viewHolder.stressed = (RadioButton) view.findViewById(R.id.stressedRadioButton);
            viewHolder.relaxed = (RadioButton) view.findViewById(R.id.relaxedRadioButton);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if(!selectedAnswers.get(i).equals("No Response")) {
            if(selectedAnswers.get(i).equals("Happy"))
                viewHolder.emotions.getChildAt(0).setEnabled(true);
            else if(selectedAnswers.get(i).equals("Sad"))
                viewHolder.emotions.getChildAt(1).setEnabled(true);
            else if(selectedAnswers.get(i).equals("Stressed"))
                viewHolder.emotions.getChildAt(2).setEnabled(true);
            else if(selectedAnswers.get(i).equals("Relaxed"))
                viewHolder.emotions.getChildAt(3).setEnabled(true);
        }

        viewHolder.emotions.setOnCheckedChangeListener(null);
        viewHolder.emotions.clearCheck();


        if(radioChecked.indexOfKey(i)>-1){
            viewHolder.emotions.check(radioChecked.get(i));
        }else{
            viewHolder.emotions.clearCheck();
        }

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAnswers.set(i, "No Response");
                radioChecked.put(i,-1);
                finalViewHolder.emotions.clearCheck();
            }
        });

        viewHolder.emotions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId>-1){
                    radioChecked.put(i, checkedId);
                } else {
                    if(radioChecked.indexOfKey(i)>-1)
                        radioChecked.removeAt(radioChecked.indexOfKey(i));
                }
            }
        });

        viewHolder.happy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectedAnswers.set(i, "Happy");
            }
        });
        viewHolder.sad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectedAnswers.set(i, "Sad");
            }
        });
        viewHolder.stressed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectedAnswers.set(i, "Stressed");
            }
        });
        viewHolder.relaxed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedAnswers.set(i, "Relaxed");
                }
            }
        });

        TextView timeStamp = (TextView) view.findViewById(R.id.timeStamp);
        TextView emotion = (TextView) view.findViewById(R.id.emotion);

        timeStamp.setText(timeStampList[i]);
        emotion.setText(emotionList[i]);
        return view;
    }
}
