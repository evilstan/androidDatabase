package com.evilstan.database;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initComponents();
    }


    private void initComponents() {
        Button btnAdd = findViewById(R.id.btn_add_waybill);
        Button btnList = findViewById(R.id.btn_list);
        Button btnSummary = findViewById(R.id.btn_summary_table);
        Button btnSettings = findViewById(R.id.btn_settings);
        Button btnAbout = findViewById(R.id.btn_about);

        btnAdd.setOnClickListener(this::selectActivity);
        btnList.setOnClickListener(this::selectActivity);
        btnSummary.setOnClickListener(this::selectActivity);
        btnSettings.setOnClickListener(this::selectActivity);
        btnAbout.setOnClickListener(this::selectActivity);
    }

    private void selectActivity(View view){
        String id = getResources().getResourceEntryName(view.getId()); //checking button id
        Intent intent = new Intent();

        switch (id){
            case "btn_add_waybill":
                askDirection(AddWaybillActivity.class);
                break;
            case "btn_list":
                askDirection(ListActivity.class);
                break;
            case "btn_summary_table":
                intent.setClass(StartActivity.this, SummaryActivity.class);
                startActivity(intent);
                break;
            case "btn_settings":
                intent.setClass(StartActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case "btn_about":
                Toast.makeText(StartActivity.this,"Спасибо что нажали",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void askDirection(Class<?> c){
        Intent intent = new Intent();
        intent.setClass(StartActivity.this, c);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StartActivity.this);

        dialogBuilder.setTitle(R.string.waybill_direction);
        dialogBuilder.setPositiveButton(R.string.material_in, (dialog, id) -> {
            intent.putExtra("direction_in", true); //true - in, false - out
            startActivity(intent);
        });

        dialogBuilder.setNegativeButton(R.string.material_out, (dialog, id) -> {
            intent.putExtra("direction_in", false); //true - in, false - out
            startActivity(intent);
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }
}
