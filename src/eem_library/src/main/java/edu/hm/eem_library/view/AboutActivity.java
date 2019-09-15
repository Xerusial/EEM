package edu.hm.eem_library.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.hm.eem_library.R;

/**
 * Activity printing all used libraries and contact information
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
