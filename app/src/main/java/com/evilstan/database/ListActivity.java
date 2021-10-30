package com.evilstan.database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;


public class ListActivity extends AppCompatActivity {


    private Toast mToast; //debugging tool :)
    private DataBaseEditor mDbEditor;

    private ListView mDbItemsListView; //main listView
    private ArrayAdapter<String> mDbAdapter; //adapter for main ListView
    private ExtendedFloatingActionButton mFbAdd;
    private PopupMenu mPopupMenu;

    private List<String> mDbAdapterList; //list for adapter
    private List<Integer> mDbRecordsIdList; //list of autoincrement ID's of waybills from DB

    final static String REGEX_SPLIT_UNITS = ",,"; //regex to split item names and units in DB
    final static String REGEX_SPLIT_ID = ",,,"; //regex to split item names and units in DB

    private boolean directionIn;
    private String tableName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle(R.string.main_activity_title);

        init();
        readDataBase();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readDataBase();
    }

    @SuppressLint("ResourceType")
    private void init() {

        Intent intent = getIntent();
        directionIn = intent.getBooleanExtra("direction_in", true);

        if (directionIn) {
            tableName = "waybill_In";
            setTitle(R.string.list_waybill_in);
        } else {
            tableName = "waybill_Out";
            setTitle(R.string.list_waybill_out);
        }

        mDbItemsListView = findViewById(R.id.main_list_view);
        mFbAdd = findViewById(R.id.fb_add);

        mPopupMenu = new PopupMenu(this, this.mFbAdd);
        mPopupMenu.inflate(R.layout.popup_menu_layout);
    }

    //TODO make filter
    /*
      Показати накладні, які містять:
      Тринога
      Балка 2,65
     */

    //TODO make categories

    //Показати накладні, які містять: Горизонт опалубка Вертикал опалубка


    private void readDataBase() {
        mDbEditor = new DataBaseEditor(ListActivity.this);
        mDbRecordsIdList = mDbEditor.getRecordIdList(tableName);

        mDbAdapterList = mDbEditor.getData(tableName);

        mDbAdapter = new ArrayAdapter<>(ListActivity.this,
            android.R.layout.simple_list_item_1, mDbAdapterList);
        mDbItemsListView.setAdapter(mDbAdapter);
    }


    private String[] readSharedPreferences() {
        SharedPreferences sPref = this.getSharedPreferences("elements", 0);
        int size = sPref.getInt("elements_size", 0);

        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = sPref.getString("elements" + "_" + i, null);
        }

        return array;
    }

    private int parseId(String text) {
        // split string by spaces and search for first number
        String[] splitText = text.split("\\s+");
        for (String s : splitText) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
            }
        }
        return -1;
    }

    private void initListeners() {

        mFbAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });

        mDbItemsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, ViewWaybillActivity.class);
                int idNumber = mDbRecordsIdList.get(position);
                intent.putExtra("id", idNumber);
                intent.putExtra("tableName", tableName);
                intent.putExtra("direction_in", directionIn);
                startActivity(intent);
            }
        });

        mDbItemsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                long id) {
                int idNumber = mDbRecordsIdList.get(position);
                showAlertDeleteItem(idNumber, position);
                return true;
            }

        });

        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuItem_in:
                        Intent intent = new Intent(ListActivity.this, AddWaybillActivity.class);
                        intent.putExtra("InOrOut", "in");
                        startActivity(intent);
                        break;
                    case R.id.menuItem_out:
                        Intent intent2 = new Intent(ListActivity.this, AddWaybillActivity.class);
                        intent2.putExtra("InOrOut", "out");
                        startActivity(intent2);
                        break;
                    case R.id.menu_item_edit:
                        Intent intent3 = new Intent(ListActivity.this, SettingsActivity.class);
                        intent3.putExtra("Toast", "HelloWorld");
                        startActivity(intent3);
                        break;
                    case R.id.menu_item_balance:
                        //TODO add activity to view all items quantity, something like EditView
                        Intent intent4 = new Intent(ListActivity.this, SummaryActivity.class);
                        intent4.putExtra("Toast", "HelloWorld");
                        startActivity(intent4);
                        break;
                }
                return false;
            }
        });
    }


    private void deleteItemFromDB(int id, int position) {
        mDbEditor.deleteRecord(id,tableName);
        mDbAdapterList.remove(position);
        mDbAdapter.notifyDataSetChanged();
        readDataBase();
    }


    public void showAlertDeleteItem(int idNumber, int position) {
        AlertDialog.Builder firstAlertDialog = new AlertDialog.Builder(ListActivity.this);
        AlertDialog.Builder secondAlertDialog = new AlertDialog.Builder(ListActivity.this);

        //double ask before deleting
        secondAlertDialog.setMessage(R.string.irreversible_action);
        secondAlertDialog.setTitle(R.string.are_you_sure);
        secondAlertDialog.setPositiveButton(R.string.no_not_delete, (dialog, which) -> {
            return;
        });
        secondAlertDialog.setNegativeButton(R.string.yes_delete,
            (dialog, id) -> deleteItemFromDB(idNumber, position));
        AlertDialog secondDialog = secondAlertDialog.create();

        firstAlertDialog.setMessage(R.string.delete_waybill_from_database);
        firstAlertDialog.setTitle(R.string.waybill_will_be_deleted);
        firstAlertDialog.setPositiveButton(R.string.yes_delete, (dialog, id) -> secondDialog.show());
        firstAlertDialog.setNegativeButton(R.string.no_not_delete, (dialog, id) -> {
            return;
        });

        AlertDialog firstDialog = firstAlertDialog.create();
        firstDialog.show();
    }


    private void showMessage(String message) {

        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }


}