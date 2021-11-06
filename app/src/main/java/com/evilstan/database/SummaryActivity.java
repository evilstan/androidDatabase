package com.evilstan.database;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Shows a sum of all items in database
 */

public class SummaryActivity extends AppCompatActivity {

    private Toast mToast;
    private final String TABLE_NAME_IN = "waybill_In";
    private final String TABLE_NAME_OUT = "waybill_Out";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        setTitle(R.string.balance_activity_title);

        init();
    }
    //TODO on list item press show message with quantity and list of waybills, which contains item

    /**
     * Балка 2,65, 3 накладних: №№ 25(25.10.2021), 27(25.10.2021), 155(31.10.2021
     **/

    private void init() {
        ListView mListViewItems = findViewById(R.id.lv_balance);
        List<String> mItemsList = readDataBase();
        ArrayAdapter<String> mItemsAdapter =
            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mItemsList);
        mListViewItems.setAdapter(mItemsAdapter);
    }


    private List<String> readDataBase() {
        DataBaseEditor mDBEditor = new DataBaseEditor(SummaryActivity.this);
        Map<String, Double> mapIn = mDBEditor.showBalance(TABLE_NAME_IN);
        Map<String, Double> mapOut = mDBEditor.showBalance(TABLE_NAME_OUT);
        Map<String, Double> resultMap = compareMaps(mapIn, mapOut);

        List<String> mItemsList = new ArrayList<>();

        for (Entry<String, Double> es : resultMap.entrySet()) {
            String item = es.getKey().split(ListActivity.REGEX_SPLIT_UNITS)[0];
            String unit = es.getKey().split(ListActivity.REGEX_SPLIT_UNITS)[1];

            //if value negative - say about it
            String issue = "";
            double v = es.getValue();

            if (v < 0) {
                issue = SummaryActivity.this.getResources().getString(R.string.check);
            }

            //if value integer - trim decimal part
            String value = es.getValue().toString();

            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            //add to list item name, trimmed value and unit
            if (v != 0) {
                mItemsList.add(item + "  " + value + " " + unit + "      " + issue);
            }
        }

        Collections.sort(mItemsList);
        return mItemsList;
    }

    private Map<String, Double> compareMaps(Map<String, Double> mapIn, Map<String, Double> mapOut) {

        for (Entry<String, Double> e : mapOut.entrySet()) {
            String key = e.getKey();
            double value = -e.getValue();
            mapIn.merge(key, value, Double::sum);
        }
        return mapIn;
    }

    private void showMessage(String message, boolean isShort) {

        if (mToast != null) {
            mToast.cancel();
        }
        int i;

        if (isShort) {
            i = Toast.LENGTH_SHORT;
        } else {
            i = Toast.LENGTH_LONG;
        }

        mToast = Toast.makeText(this, message, i);
        mToast.show();
    }
}
