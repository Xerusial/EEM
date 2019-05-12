package edu.hm.eem_host.view;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import edu.hm.eem_host.R;

public class SettingsActivity extends AppCompatActivity {
    final static String PREFS_NAME = "preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        final SharedPreferences.Editor editor = sharedPref.edit();
        final EditText nameEditText = findViewById(R.id.username);
        String profName = sharedPref.getString(getString(R.string.preferences_username), "Username");
        nameEditText.setText(profName);
        findViewById(R.id.bt_save_prefs).setOnClickListener(v -> {
            String username = nameEditText.getText().toString();
            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
            /* Check encodability of username, as it will be used as the service name.
             */
            if(encoder.canEncode(username)){
                editor.putString(getString(R.string.preferences_username), username);
                editor.apply();
                finish();
            } else {
                Toast.makeText(SettingsActivity.this, R.string.toast_username_needs_to_be_UTF_8_compatible, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
