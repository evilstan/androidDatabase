package com.evilstan.database;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

public class ViewWaybillActivity extends AppCompatActivity {

    private Toast toast; //debugging tool :)
    private Map<String, Double> dataMap;
    private int id;
    private ArrayAdapter<String> elementsAdapter, unitsAdapter;
    private DatePickerDialog datePickerDialog;
    private List<String> elementsList, unitsList;
    private Button btnDate;
    private EditText editTextIdNumber;
    private LinearLayout rootLayout;
    private List<Row> rowArrayList = new ArrayList<>();
    private Calendar calendar;
    final String REGEX_SPLIT_UNITS = ListActivity.REGEX_SPLIT_UNITS;
    private static final String TAG = AddWaybillActivity.class.getSimpleName();
    private String tableName;
    private boolean directionIn;
    //TODO ask to push to DB waybill after changing

    //TODO change listView to GridView

    @Override
    protected void onCreate(
        Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_waybill);

        Bundle arguments = getIntent().getExtras();
        id = arguments.getInt("id");   //_id of waybill in database. In future...
        directionIn = arguments.getBoolean("direction_in");

        if (directionIn) {
            tableName = "waybill_In";
            setTitle(R.string.view_waybill_in);
        } else {
            tableName = "waybill_Out";
            setTitle(R.string.view_waybill_out);
        }

        showMessage("id=" + id);
        initComponents();
        readDataBase();
        fillRows();
    }

    private void initComponents() {
        rootLayout = findViewById(R.id.root_layout_edit);
        calendar = Calendar.getInstance();
        editTextIdNumber = findViewById(R.id.et_id);
        btnDate = findViewById(R.id.btn_date);

        unitsList = new ArrayList<>(
            Arrays.asList(getResources().getStringArray(R.array.units))); //new
        unitsAdapter = new ArrayAdapter<>(ViewWaybillActivity.this,
            android.R.layout.simple_list_item_1, unitsList);

        elementsList = new ArrayList<>(
            Arrays.asList(getResources().getStringArray(R.array.elements_name)));
        Collections.sort(elementsList);

        //adapter for autocompleteTextView
        elementsAdapter = new ArrayAdapter<>(ViewWaybillActivity.this,
            android.R.layout.simple_list_item_1,
            elementsList);
    }


    private void readDataBase() {
        DataBaseEditor dbEditor = new DataBaseEditor(ViewWaybillActivity.this);
        dataMap = dbEditor.getRecord(id,tableName);
        Optional<String> firstKey = dataMap.keySet().stream().findFirst();

        //get first pair in map ,parse date and ID and delete it from map
        if (firstKey.isPresent()) {
            String key = firstKey.get();
            double value = dataMap.get(key);
            setDate(key, value);
            dataMap.remove(key);
        }

    }

    private void setDate(String key, double value) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat
            sdf = new SimpleDateFormat("dd.MM.yyyy");

        try {
            calendar.setTime(Objects.requireNonNull(sdf.parse(key)));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date");
        }

        String idNumber = value+"";
        idNumber = idNumber.substring(0, idNumber.length() - 2); //cut decimal part
        editTextIdNumber.setText(idNumber);
        btnDate.setText(sdf.format(calendar.getTime()));
    }


    private void fillRows() {
        for (Entry<String, Double> mapEntry : dataMap.entrySet()) {
            String name = mapEntry.getKey().split(REGEX_SPLIT_UNITS)[0];
            String unitName = mapEntry.getKey().split(REGEX_SPLIT_UNITS)[1];
            double value = mapEntry.getValue();
            int unit = unitsList.indexOf(unitName);

            addRow(name, value, unit);
        }
        rootLayout.clearFocus();
    }

    int counter = 0;

    private void addRow(String name, double value, int unit) {

        counter++;

        Row row = new Row(ViewWaybillActivity.this, counter, elementsAdapter, unitsAdapter);
        rowArrayList.add(row);

        rootLayout.addView(row.getBaseLayout());

        row.setName(name);
        row.setValue(value);
        row.setUnit(unit);

        row.getAutocompleteTextView().setOnItemClickListener((parent, view, position, id) -> {
            String s = (String) parent.getItemAtPosition(position);
            selectUnit(row, s);
        });
    }

    @SuppressLint("SetTextI18n")
    private void selectUnit(Row row, String s) {
        switch (s) { //AI system for intellectual quantity and unit selection :-)
            case "Консоль комплект":             //консоль
                row.setUnit(0);
                break;
            case "Фанера різана":             //Фанера різана
                row.setUnit(1);
                break;
            case "Балка різана":             //Балка різана
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

    private void showMessage(String message) { //debugging tool :)

        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
