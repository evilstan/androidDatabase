package com.evilstan.database;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    ListView listViewItems;
    ArrayAdapter<String> adapterItems;
    List<String> listItems;
    Toast toast;
    ExtendedFloatingActionButton fbSave;
    private boolean wasChanged = false;

    @Override
    public void onBackPressed() {
        if (wasChanged) showAlertExit();
        else super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.edit_activity_title);

        showMessage("Довге натиснення для видалення", false);

        fbSave = findViewById(R.id.fb_save);
        fbSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putValues(listItems.toArray(new String[0]));
                wasChanged = false;
                finish();
            }
        });

        listViewItems = findViewById(R.id.lv_edit);
        listViewItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //remove item from ListView on Long Click
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showAlertDelete(position);
                wasChanged = true;
                return true;
            }
        });

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialogEdit(position);
                wasChanged = true;
            }
        });
        loadValues();
    }

    private void loadValues() {
        SharedPreferences sPref = this.getSharedPreferences("elements", 0);
        SharedPreferences.Editor edPref = sPref.edit();

        int size = sPref.getInt("elements_size", 0);
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = sPref.getString("elements" + "_" + i, null);
        }

        listItems = new ArrayList<>(Arrays.asList(array));

        adapterItems = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listViewItems.setAdapter(adapterItems);
        adapterItems.notifyDataSetChanged();
    }


    private void putValues(String[] array) {
        SharedPreferences sPref = this.getSharedPreferences("elements", 0);
        SharedPreferences.Editor edPref = sPref.edit();
        edPref.clear();
        edPref.putInt("elements_size", array.length);

        for (int i = 0; i < array.length; i++) {
            edPref.putString("elements" + "_" + i, array[i]);
        }
        edPref.apply();
        finish();
    }

    public void showAlertDelete(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setMessage("Видалити \"" + listItems.get(position) + "\" з бази?")
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listItems.remove(position);
                        adapterItems.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showAlertExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        //builder.setMessage(R.string.dialog_wanna_delete)
        builder.setMessage("Список був змінений. Зміни не збережуться")
                .setTitle("Вийти без збереження?")
                .setPositiveButton(R.string.dialog_yes_exit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_no_stay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogEdit(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        View v = View.inflate(SettingsActivity.this, R.layout.dialog_edit_item, null);
        EditText edit = v.findViewById(R.id.et_dialog);
        edit.setText(listItems.get(position));
        edit.requestFocus();
        edit.selectAll();

        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.edit_dialog_button_edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listItems.set(position, edit.getText().toString());
                    }
                })
                .setNegativeButton(R.string.edit_dialog_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.dialog_edit_title);
        dialog.show();

        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    private void showMessage(String message, boolean isShort) {

        if (toast != null) {
            toast.cancel();
        }
        int i;

        if (isShort) i = Toast.LENGTH_SHORT;
        else i = Toast.LENGTH_LONG;

        toast = Toast.makeText(this, message, i);
        toast.show();
    }
}