package research.sg.edu.edapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dropbox.core.android.Auth;

public class LoginDropboxxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_dropboxx);
        Button SignInButton = (Button) findViewById(R.id.login);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume");
        getAccessToken();
    }

    public void getAccessToken() {
        System.out.println("i am in getaccesstoken");
        // uncomment the below line if you asks for token to user for dropbox authentication that is needed to store data in their dropbox
        //String accessToken = Auth.getOAuth2Token();//generate Access Token
        // hard coded token that authenticates anyone who use my app
        //String accessToken="i_k31xsLm8cAAAAAAAAAAU6GcpCCjFUhTYuLz5bzK_cCdRUuaJj0vumGJNfBq43b";
        String accessToken="WX_Qmc5vie4AAAAAAAAAATbAoZ8Q6oQ0E16Gi9w3KjA0VImMsHZsEjJcQ-hxaofm";
        if (accessToken != null && !IsAlreadyRegistered()) {
            //Store accessToken in SharedPreferences
            System.out.println("I got access token and registered");
            SharedPreferences prefs = getSharedPreferences("research.sg.edu.valdio.dropboxintegration", Context.MODE_PRIVATE);
            prefs.edit().putString("access-token", accessToken).apply();

            //Proceed to Registration
            Intent intent = new Intent(LoginDropboxxActivity.this, PerformRegistration.class);
            startActivity(intent);
            finish();
        }
        else {
            System.out.println("I didn't get access token and not registered");
        }
    }
    public boolean IsAlreadyRegistered(){

        boolean registration_flag= false;
        SharedPreferences mood_pref=null;

        //Sharedpreference based Registration Status

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.moodrecorder_pkg), Context.CONTEXT_IGNORE_SECURITY);
            mood_pref = con.getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_PRIVATE);

            registration_flag = mood_pref.getBoolean(getResources().getString(R.string.sharedpref_registration_flag), false);
            System.out.println("Registration Status:" + registration_flag);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return registration_flag;
    }
}