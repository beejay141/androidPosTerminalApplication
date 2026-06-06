package com.iisysgroup.androidlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ConsatVAS extends AppCompatActivity {
    Toolbar toolbar;
    Spinner idMethod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consat_vas);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idMethod = findViewById(R.id.consat_spinner_id_method);

        ArrayAdapter<CharSequence> consatIdAdapter = ArrayAdapter.createFromResource(this,
                R.array.consat_tv_id_method_spinner, android.R.layout.simple_spinner_item);
        consatIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        idMethod.setAdapter(consatIdAdapter);
    }
}
