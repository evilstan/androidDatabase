package com.evilstan.database;

import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class Row {

    private LinearLayout baseLayout;
    private final TextView txNumerator;
    private final AutoCompleteTextView autocompleteElements;
    private final Spinner spinnerUnits;
    private final EditText editTextQuantity;
    private final int ID;

    public Row(Context context, int counter, ArrayAdapter<String> itemsAdapter,
        ArrayAdapter<String> unitsAdapter) {
        ID = counter - 1;

        baseLayout = new LinearLayout(context);
        baseLayout = (LinearLayout) View.inflate(context, R.layout.inflate_layout, null);
        baseLayout.setId(View.generateViewId());

        editTextQuantity = addEditText(context);
        spinnerUnits = addSpinner(context, unitsAdapter);
        autocompleteElements = addAutoComplete(context, editTextQuantity.getId(), itemsAdapter);
        txNumerator = addTextView(context, counter);

        editTextQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(0.0, 1000.0)});

        baseLayout.addView(txNumerator);
        baseLayout.addView(autocompleteElements);
        baseLayout.addView(spinnerUnits);
        baseLayout.addView(editTextQuantity);
    }

    public void setUnit(int position) {
        spinnerUnits.setSelection(position);
        editTextQuantity.requestFocus();
        editTextQuantity.selectAll();
    }

    public EditText getEditText() {
        return editTextQuantity;
    }

    public AutoCompleteTextView getAutocompleteTextView() {
        return autocompleteElements;
    }

    public String getName() {
        return autocompleteElements.getText().toString();
    }

    public Double getValue() {
        try {
            return Double.parseDouble(editTextQuantity.getText().toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public LinearLayout getBaseLayout() {
        return baseLayout;
    }

    public void setName(String textName) {
        autocompleteElements.setText(textName);
    }

    public int getSpinnerPos() {
        return spinnerUnits.getSelectedItemPosition();
    }

    public void setValue(Double value) {
        String textValue = value.toString();
        editTextQuantity.setText(textValue);
    }


    public int getRowId() {
        return ID;
    }

    private Spinner addSpinner(Context context, ArrayAdapter<String> adapter) {
        Spinner spinner = (Spinner) View.inflate(context, R.layout.inflate_spinner, null);
        spinner.setId(View.generateViewId());
        spinner.setLayoutParams(
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.8f));
        spinner.setAdapter(adapter);
        spinner.setSelection(4);
        return spinner;
    }

    private EditText addEditText(Context context) {
        EditText editText = (EditText) View.inflate(context, R.layout.inflate_edit_text, null);
        editText.setLayoutParams(
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        editText.setId(View.generateViewId());
        return editText;
    }

    private AutoCompleteTextView addAutoComplete(Context context, int id,
        ArrayAdapter<String> adapter) {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) View
            .inflate(context, R.layout.inflate_autocomplete, null);
        autoCompleteTextView.setLayoutParams(
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.6f));
        autoCompleteTextView.setNextFocusForwardId(id);
        autoCompleteTextView.setId(View.generateViewId());
        autoCompleteTextView.setAdapter(adapter);
        return autoCompleteTextView;
    }

    private TextView addTextView(Context context, int counter) {
        String s = counter + ".";
        TextView textView = (TextView) View.inflate(context, R.layout.inflate_text_view, null);
        textView.setLayoutParams(
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));
        textView.setText(s);
        return textView;
    }
}

