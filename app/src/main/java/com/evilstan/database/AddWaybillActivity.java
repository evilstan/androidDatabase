package com.evilstan.database;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddWaybillActivity extends AppCompatActivity {

    private ExtendedFloatingActionButton fbPush;
    private ExtendedFloatingActionButton fbAddRow;
    private DatePickerDialog datePickerDialog;
    private Button btnDate;
    private EditText editTextIdNumber;
    private LinearLayout rootLayout;
    private Calendar mCalendar;
    private List<String> elementsList;
    private ArrayAdapter<String> elementsAdapter, unitsAdapter;
    private List<Row> rowArrayList = new ArrayList<>();
    private Context context;
    private static final String TAG = AddWaybillActivity.class.getSimpleName();
    private final String REGEX_SPLIT_UNITS = ListActivity.REGEX_SPLIT_UNITS;
    private boolean directionIn = true;
    private String tableName;
    Toast toast;

    int counter = 0;
    public static int WAYBILL_MAX_SIZE = 20;
    boolean wasSaved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_waybill);
//        Objects.requireNonNull(getSupportActionBar()).hide();

        init();
        initComponents();
        initListeners();
        initCalendar();
        setDefaultIdNumber();
        putNewRow();
    }


    private void init() {
        context = AddWaybillActivity.this;
        Intent intent = getIntent();
        directionIn = intent.getBooleanExtra("direction_in", true);

        if (directionIn) {
            tableName = "waybill_In";
            setTitle(R.string.add_waybill_in);
        } else {
            tableName = "waybill_Out";
            setTitle(R.string.add_waybill_out);
        }

    }

    @SuppressLint("SimpleDateFormat")
    private void initCalendar() {
        mCalendar = Calendar.getInstance();
        DateFormat mDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        btnDate.setText(mDateFormat.format(mCalendar.getTime()));

        DatePickerDialog.OnDateSetListener dls = (DatePicker view,
            int year,
            int month,
            int dayOfMonth) -> {
            String mCurrDate = dayOfMonth + "." + (month + 1) + "." + year;
            DateFormat mDateFormatParse = new SimpleDateFormat("d.M.yyyy");

            try {
                Date date = Objects.requireNonNull(mDateFormatParse.parse(mCurrDate));
                mCalendar.setTime(date);
            } catch (NullPointerException | ParseException e) {
                Log.d(TAG, "Wrong date");
            }
        };

        datePickerDialog = new DatePickerDialog(this, dls, mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
    }


    private void setDefaultIdNumber() {
        //set default id number
        String day = Calendar.DAY_OF_MONTH + "";
        String month1 = mCalendar.get(Calendar.MONTH) + 1 + "";
        String defaultIdNumber = day + month1 + 1;
        editTextIdNumber.setText(defaultIdNumber);
    }

    private void putNewRow() {

        counter++;
        if (counter > WAYBILL_MAX_SIZE) {
            showMessage(
                context.getResources().getString(R.string.max_size_reached) + WAYBILL_MAX_SIZE);
            return;
        }

        Row row = new Row(context, counter, elementsAdapter, unitsAdapter);
        rowArrayList.add(row);
        initNewRow(row);

        rootLayout.removeView(fbAddRow); //makes FloatingButton float below rows
        rootLayout.addView(row.getBaseLayout());
        rootLayout.addView(fbAddRow);
        row.getAutocompleteTextView().requestFocus();
    }

    private Row getLastRow() {
        return rowArrayList.get(rowArrayList.size() - 1);
    }

    private void addRow() {
        String name = getLastRow().getName();
        double value = getLastRow().getValue();

        if (name.equals("") && value == 0) {
            getLastRow().getAutocompleteTextView().requestFocus();
        } else if (name.equals("")) {
            getLastRow().getAutocompleteTextView().requestFocus();
            String text = context.getResources().getString(R.string.type_name);
            showMessage(text);
        } else if (value == 0) {
            getLastRow().getEditText().requestFocus();
            String text = context.getResources().getString(R.string.type_item_value);
            showMessage(text);
        } else {
            putNewRow();
        }
    }

    boolean isVisible = false;

    //check if layout is visible
    @Override
    public void onStart() {
        super.onStart();
        isVisible = true;

    }

    private void initNewRow(Row row) {

        row.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    //if next row exist, set focus to it
                    rowArrayList.get(row.getRowId() + 1).getAutocompleteTextView()
                        .requestFocus();
                } catch (IndexOutOfBoundsException e) {
                    //if next row not exist,add new row
                    addRow();
                }
            }
            return false;
        });

        row.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            String s = row.getName();

            //if itemName is empty - notify about this
            if (s.equals("")) {
                row.getAutocompleteTextView().requestFocus();
                showMessage(context.getResources().getString(R.string.type_name));

                // if item is not exist in list - add to it
            } else if (!elementsList.contains(s)) {
                showMessage(context.getResources().getString(R.string.not_found));
                elementsList.add(s.trim());
                elementsAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1, elementsList);
            }
        });

        row.getAutocompleteTextView().setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus && isVisible) {
                //update adapter on betting focus (new elements could be added)
                row.getAutocompleteTextView().setAdapter(elementsAdapter);
            }
        });

        row.getAutocompleteTextView().setOnItemClickListener(
            (AdapterView<?> parent, View view, int position, long id) -> {
                wasSaved = false;
                String s = (String) parent.getItemAtPosition(position);
                selectUnit(row, s);
            });

        row.getAutocompleteTextView().requestFocus();
    }


    @SuppressLint("SetTextI18n")
    private void selectUnit(Row row, String s) {
        switch (s) { //AI system for intellectual quantity and unit selection :-)
            case "Консоль комплект":             //консоль
                row.getEditText().setText("1");
                row.setUnit(0);
                break;
            case "Фанера різана":             //Фанера різана
                row.getEditText().setText("1");
                row.setUnit(1);
                break;
            case "Балка різана":             //Балка різана
                row.getEditText().setText("1");
                row.setUnit(2);
                break;
            case "Балка 2,65":         //Балка 2,65
                row.getEditText().setText("90");
                row.setUnit(4);
                break;
            case "Балка 3,9":            //Балка 3,3, 3,9
            case "Балка 3,3":
                row.getEditText().setText("49");
                row.setUnit(4);
                break;
            case "Фанера 2,5х1,25":            //фанера 2,5х1,25
            case "Фанера 2,3х1,15":
                row.getEditText().setText("30");
                row.setUnit(4);
                break;
            default:
                row.getEditText().setText("1");
                row.setUnit(4);
                break;
        }

        if (s.toLowerCase(Locale.ROOT).contains("щит ")) {
            row.getEditText().setText("5");
            row.setUnit(4);
        }
        if (s.toLowerCase(Locale.ROOT).contains("кут ")) {
            row.getEditText().setText("4");
            row.setUnit(4);
        }
    }

    private void initComponents() {

        rootLayout = findViewById(R.id.root_layout);
        fbPush = findViewById(R.id.floating);
        btnDate = findViewById(R.id.btn_date);
        editTextIdNumber = findViewById(R.id.et_id);
        editTextIdNumber.requestFocus();
        fbAddRow = findViewById(R.id.fb_add_row);

        //TODO make items load from database table "Items" instead of sharedPreferences
        String[] unitsArray = getResources().getStringArray(R.array.units);
        //elementsList = new ArrayList<>(Arrays.asList(readSharedPreferences()));
        elementsList = new ArrayList<>(
            Arrays.asList(getResources().getStringArray(R.array.elements_name)));
        Collections.sort(elementsList);

        //adapter for autocompleteTextView
        elementsAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_list_item_1,
            elementsList);

        //adapter for units spinner
        unitsAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_list_item_1,
            unitsArray);
    }

    private void initListeners() {

        btnDate.setOnClickListener(v -> datePickerDialog.show());

        //on enter set focus on first row
        editTextIdNumber.setOnEditorActionListener((v, actionId, event) -> {
            rowArrayList.get(0).getAutocompleteTextView().requestFocus();
            return false;
        });

        fbPush.setOnClickListener(v -> {
            writeSharedPreferences(elementsList.toArray(new String[0]));
            pushData(rowArrayList);
        });

        fbAddRow.setOnClickListener(v -> addRow());

    }


    private void pushData(List<Row> rowList) {
        DataBaseEditor dbEditor = new DataBaseEditor(context);
        Map<String, Double> dataMap = parseRowList(rowList);
        if (dataMap == null) {
            return;
        }

        int idNumber;

        try {
            idNumber = Integer.parseInt(editTextIdNumber.getText().toString());
        } catch (NumberFormatException e) {
            editTextIdNumber.requestFocus();
            showMessage(context.getString(R.string.type_number));
            return;
        }

        dbEditor.addWaybill(dataMap, idNumber, mCalendar, tableName);
        showMessage(context.getString(R.string.pushed));

        finish();
        //startActivity(getIntent());

    }

    //get data from rows before pushing it to DB
    private Map<String, Double> parseRowList(List<Row> rowList) {
        Map<String, Double> result = new LinkedHashMap<>();

        for (Row row : rowList) {
            String unit = unitsAdapter.getItem(row.getSpinnerPos());
            String name = row.getName();
            double value;

            //if value is empty - set focus on edittext
            try {
                value = row.getValue();
            } catch (NumberFormatException e) {
                showMessage(context.getString(R.string.type_item_value));
                row.getEditText().requestFocus();
                break;
            }

            //if name is empty - set focus on AutocompleteText
            if (name.equals("") && value != 0) {
                showMessage(context.getString(R.string.type_name));
                row.getAutocompleteTextView().requestFocus();
                return null;
            } else if (value == 0) {
                showMessage(context.getString(R.string.type_item_value));
                row.getEditText().requestFocus();
                return null;
            } else {
                name += REGEX_SPLIT_UNITS + unit; // add units to database
                result.put(name, value);
            }
        }
        return result;
    }


    private void writeSharedPreferences(String[] array) {
        SharedPreferences sPref = this.getSharedPreferences("elements", 0);
        SharedPreferences.Editor edPref = sPref.edit();

        edPref.clear();
        edPref.putInt("elements_size", array.length);

        for (int i = 0; i < array.length; i++) {
            edPref.putString("elements" + "_" + i, array[i]);
        }

        edPref.apply();
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


    private void showMessage(String message) { //debugging tool
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }


    @Override
    public void onBackPressed() {
        if (wasSaved) {
            super.onBackPressed();
        } else {
            showAlertExit();
        }
    }

    public void showAlertExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.waybill_will_not_be_saved);
        builder.setTitle(R.string.exit_without_saving);
        builder.setPositiveButton(R.string.dialog_yes_exit, (dialog, id) -> finish());
        builder.setNegativeButton(R.string.dialog_no_stay, (dialog, id) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}