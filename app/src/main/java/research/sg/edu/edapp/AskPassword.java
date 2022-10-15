package research.sg.edu.edapp;

import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AskPassword extends AppCompatActivity {

    EditText editPassword;
    Button enter;
    TextView tv1;
    Boolean password = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_password);
        this.setFinishOnTouchOutside(false);

        editPassword = (EditText) findViewById(R.id.editPassword);
        enter = (Button) findViewById(R.id.enter);
        tv1 = (TextView) findViewById(R.id.errPwdMsg);

        editPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkPassword();
                    return true;
                }
                return false;
            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.enter) {
                    checkPassword();
                }
            }
        });
    }

    public void checkPassword() {
        String enteredPassword = editPassword.getText().toString().trim();
        if(enteredPassword.equals(getString(R.string.developer_password))) {
            password = true;
            Intent intent=new Intent();
            intent.putExtra("password",password);
            setResult(1,intent);
            finish();//finishing activity
        } else if(editPassword.getText().equals("")) {
            tv1.setText("Enter Password!!");
            editPassword.setText("");
            if(editPassword.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        } else {
            tv1.setText("Wrong Password!!");
            editPassword.setText("");
            if(editPassword.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("password",password);
        setResult(1,intent);
        finish();
    }
}
