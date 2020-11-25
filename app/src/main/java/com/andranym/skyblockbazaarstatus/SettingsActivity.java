package com.andranym.skyblockbazaarstatus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static java.lang.Math.abs;

public class SettingsActivity extends AppCompatActivity {

    RadioButton radioByPopularity;
    RadioButton radioAlphabetical;
    RadioGroup radioGroupViewPricesSort;
    RadioButton radioNone;
    RadioButton radioRing;
    RadioButton radioArtifact;
    RadioButton radioSeal;
    RadioGroup radioGroupDiscountAmount;
    RadioButton radioRegFuel;
    RadioButton radioCatalyst;
    RadioGroup radioGroupFuelType;
    Button btnSaveInvestment;
    Button btnSaveBazaarTax;
    EditText editNumBazaarInvestment;
    EditText editDecimalBazaarTax;
    Switch switchHideOrderWarning;
    TextView txtFuelType;
    EditText editTextFuel;
    Button btnMinionHelp;
    Spinner spinnerUpgrade1;
    Spinner spinnerUpgrade2;
    EditText editNumCustomBoost;
    EditText editNumCustomFly;
    CheckBox checkBoxFarmCrystal;
    CheckBox checkBoxWoodCrystal;
    Button btnSaveMinionSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Settings");

        //regionLoad all settings data
        final SharedPreferences settingsData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final int viewPricesOrder = settingsData.getInt("viewPricesOrder",0);
        final long investmentAmount = settingsData.getLong("BazaarInvestmentAmount",100000);
        final int hideBad = settingsData.getInt("hideBadOrders",0);
        final int discountAmount = settingsData.getInt("shadyDiscount",0);
        final int bazaarTax = settingsData.getInt("personalBazaarTaxAmount",1250);
        final int fuelType = settingsData.getInt("fuelType",1);
        final int normalFuelNumber = settingsData.getInt("normalFuelNumber",0);
        final int catalystFuelNumber = settingsData.getInt("catalystFuelNumber",3);
        final int customBoostNormal = settingsData.getInt("customBoostNormal",0);
        final int customBoostFly = settingsData.getInt("customBoostFly",0);
        final String defaultUpgrade1 = settingsData.getString("defaultUpgrade1","Super Compactor");
        final String defaultUpgrade2 = settingsData.getString("defaultUpgrade2","Diamond Spreading");
        final int woodChecked = settingsData.getInt("woodChecked",0);
        final int farmChecked = settingsData.getInt("farmChecked",0);
        //endregion

        //regionInitialize UI elements
        btnSaveInvestment = findViewById(R.id.btnSaveInvestment);
        btnSaveBazaarTax = findViewById(R.id.btnSaveBazaarTax);
        editNumBazaarInvestment = findViewById(R.id.editNumBazaarInvestment);
        editDecimalBazaarTax = findViewById(R.id.editDecimalBazaarTax);
        switchHideOrderWarning = findViewById(R.id.switchHideOrderWarning);
        radioGroupViewPricesSort = findViewById(R.id.radioGroupViewPricesSort);
        radioAlphabetical = findViewById(R.id.radioAlphabetical);
        radioByPopularity = findViewById(R.id.radioByPopularity);
        radioGroupDiscountAmount = findViewById(R.id.radioGroupDiscountAmount);
        radioNone = findViewById(R.id.radioNone);
        radioRing = findViewById(R.id.radioRing);
        radioArtifact = findViewById(R.id.radioArtifact);
        radioSeal = findViewById(R.id.radioSeal);
        radioGroupFuelType = findViewById(R.id.radioGroupFuelType);
        radioRegFuel = findViewById(R.id.radioRegFuel);
        radioCatalyst = findViewById(R.id.radioCatalyst);
        txtFuelType = findViewById(R.id.txtFuelType);
        spinnerUpgrade1 = findViewById(R.id.spinnerUpgrade1);
        spinnerUpgrade2 = findViewById(R.id.spinnerUpgrade2);
        editNumCustomBoost = findViewById(R.id.editNumCustomBoost);
        editNumCustomFly = findViewById(R.id.editNumCustomFly);
        btnMinionHelp = findViewById(R.id.btnMinionHelp);
        checkBoxFarmCrystal = findViewById(R.id.checkBoxFarmCrystal);
        checkBoxWoodCrystal = findViewById(R.id.checkBoxWoodCrystal);
        btnSaveMinionSettings = findViewById(R.id.btnSaveMinionSettings);
        editTextFuel = findViewById(R.id.editTextFuel);
        //endregion

        //region Check the radio buttons according to saved user preferences
            //regionFor the order of view prices
                //Sort by popularity
                if (viewPricesOrder == 0){
                    radioByPopularity.setChecked(true);
                }
                //Sort Alphabetically
                else{
                    radioAlphabetical.setChecked(true);
                }
            //endregion

            //regionFor shady discount
                if (discountAmount == 0) {
                    radioNone.setChecked(true);
                }
                if (discountAmount == 1) {
                    radioRing.setChecked(true);
                }
                if (discountAmount == 2) {
                    radioArtifact.setChecked(true);
                }
                if (discountAmount == 3) {
                    radioSeal.setChecked(true);
                }
            //endregion

            //regionFor the fuel type
                if(fuelType == 1) {
                    radioRegFuel.setChecked(true);
                } else {
                    radioCatalyst.setChecked(true);
                }
            //endregion
        //endregion

        //regionPrice Order Settings
            radioGroupViewPricesSort.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    SharedPreferences.Editor editor = settingsData.edit();
                    switch (checkedId) {
                        case R.id.radioByPopularity:
                            editor.putInt("viewPricesOrder", 0);
                            editor.commit();
                            break;
                        case R.id.radioAlphabetical:
                            editor.putInt("viewPricesOrder", 1);
                            editor.commit();
                            break;
                    }
                }
            });
        //endregion

        //region Arbitrage settings
            //region Investment Amount
                editNumBazaarInvestment.addTextChangedListener(new NumberTextWatcherForThousand(editNumBazaarInvestment));
                editNumBazaarInvestment.setText(Long.toString(investmentAmount));
                btnSaveInvestment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editNumBazaarInvestment.getText().toString().equals("")) {
                            String commasRemoved = NumberTextWatcherForThousand.trimCommaOfString(editNumBazaarInvestment.getText().toString());
                            long newInvestment = Long.parseLong(commasRemoved);
                            SharedPreferences.Editor editor = settingsData.edit();
                            editor.putLong("BazaarInvestmentAmount", newInvestment);
                            editor.commit();
                        }
                    }
                });
                if (hideBad != 0) {
                    switchHideOrderWarning.setChecked(true);
                } else {
                    switchHideOrderWarning.setChecked(false);
                }

                switchHideOrderWarning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences.Editor editor = settingsData.edit();
                        if(switchHideOrderWarning.isChecked()) {
                            editor.putInt("hideBadOrders",1);
                        }else{
                            editor.putInt("hideBadOrders",0);
                        }
                        editor.commit();
                    }
                });
            //endregion

            //region Shady options
                radioGroupDiscountAmount.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        SharedPreferences.Editor editor = settingsData.edit();
                        switch (checkedId) {
                            case R.id.radioNone:
                                editor.putInt("shadyDiscount", 0);
                                editor.commit();
                                break;
                            case R.id.radioRing:
                                editor.putInt("shadyDiscount", 1);
                                editor.commit();
                                break;
                            case R.id.radioArtifact:
                                editor.putInt("shadyDiscount", 2);
                                editor.commit();
                                break;
                            case R.id.radioSeal:
                                editor.putInt("shadyDiscount", 3);
                                editor.commit();
                                break;
                        }
                    }
                });
            //endregion

            //region Bazaar Tax amount
                //SharedPreferences doesn't let you store decimals, so I just store as int, then divide to get it back
                final double bazaarTaxFixed = Round3(((double)bazaarTax) / 1000);
                editDecimalBazaarTax.setText(String.valueOf(bazaarTaxFixed));

                //Save any new bazaar tax in the proper way
                btnSaveBazaarTax.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editDecimalBazaarTax.getText().toString().equals("")) {
                            double bazaarTaxOG = Round3(Double.parseDouble(editDecimalBazaarTax.getText().toString()));
                            if (bazaarTaxOG > 9.999 || (abs(bazaarTaxOG)<0.001) || bazaarTaxOG < -9.999) {
                                Toast toast = Toast.makeText(getApplicationContext(),"STOP TRYING TO BREAK MY APP\nEnter a proper percentage like 1.25",Toast.LENGTH_SHORT);
                                toast.show();
                                editDecimalBazaarTax.setText(String.valueOf(bazaarTaxFixed));
                            } else {
                                int bazaarTax = (int) (bazaarTaxOG * 1000);
                                SharedPreferences.Editor editor = settingsData.edit();
                                editor.putInt("personalBazaarTaxAmount", bazaarTax);
                                editor.commit();
                            }
                        }
                    }
                });

            //endregion
        //endregion

        //regionMinion Optimizer Settings

            //region Display Help
                btnMinionHelp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayHelp();
                    }
                });
            //endregion

            //regionSelect Fuel type
                radioGroupFuelType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        SharedPreferences.Editor editor = settingsData.edit();
                        int catalystFuelNumber2 = settingsData.getInt("catalystFuelNumber",3);
                        int normalFuelNumber2 = settingsData.getInt("normalFuelNumber",0);
                        switch (checkedId) {
                            case R.id.radioCatalyst:
                                editor.putInt("fuelType", 0);
                                editor.commit();
                                txtFuelType.setText("Default Fuel Multiplier:");
                                editTextFuel.setText(Integer.toString(catalystFuelNumber2));
                                break;
                            case R.id.radioRegFuel:
                                editor.putInt("fuelType", 1);
                                editor.commit();
                                txtFuelType.setText("Default Fuel Boost Percentage:");
                                editTextFuel.setText(Integer.toString(normalFuelNumber2));
                                break;
                        }
                    }
                });
            //endregion

            //region Load fuel and custom boost choices
            if (fuelType == 1) {
                editTextFuel.setText(Integer.toString(normalFuelNumber));
            } else {
                editTextFuel.setText(Integer.toString(catalystFuelNumber));
            }
            editNumCustomBoost.setText(Integer.toString(customBoostNormal));
            editNumCustomFly.setText(Integer.toString(customBoostFly));
            //endregion

            //region Fill Spinners with choices
                ArrayList<String> upgrades = new ArrayList<>();
                upgrades.add("Compactor");
                upgrades.add("Super Compactor");
                upgrades.add("Auto Smelter");
                upgrades.add("Diamond Spreading");
                upgrades.add("Enchanted Egg");
                upgrades.add("Minion Expander 5%");
                upgrades.add("Flycatcher 20%");
                upgrades.add("Custom Speed Boost " + customBoostNormal + "%");
                upgrades.add("Custom Fuel Boost " + customBoostFly + "%");
                upgrades.add("Flint Shovel");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, upgrades);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUpgrade1.setAdapter(adapter);
                spinnerUpgrade2.setAdapter(adapter);
                spinnerUpgrade1.setSelection(adapter.getPosition(defaultUpgrade1));
                spinnerUpgrade2.setSelection(adapter.getPosition(defaultUpgrade2));
            //endregion

            //region Select which booster crystals are used
                //Prefill if selected in the past
                if (woodChecked == 1) {
                    checkBoxWoodCrystal.setChecked(true);
                }
                if (farmChecked == 1) {
                    checkBoxFarmCrystal.setChecked(true);
                }
                //Save if changes are made
                checkBoxFarmCrystal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = settingsData.edit();
                        if (isChecked) {
                            editor.putInt("farmChecked",1);
                        } else {
                            editor.putInt("farmChecked",0);
                        }
                        editor.commit();
                    }
                });
                checkBoxWoodCrystal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = settingsData.edit();
                        if (isChecked) {
                            editor.putInt("woodChecked",1);
                        } else {
                            editor.putInt("woodChecked",0);
                        }
                        editor.commit();
                    }
                });
            //endregion

            //regionSave settings
            btnSaveMinionSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = settingsData.edit();
                    String upgrade1 = spinnerUpgrade1.getSelectedItem().toString();
                    String upgrade2 = spinnerUpgrade2.getSelectedItem().toString();
                    editor.putString("defaultUpgrade1",upgrade1);
                    editor.putString("defaultUpgrade2",upgrade2);
                    int fuelNumber;
                    if (!editTextFuel.getText().toString().equals("")) {
                        fuelNumber = Integer.parseInt(editTextFuel.getText().toString());
                        if (fuelNumber < 1000) {
                            if (fuelType == 1) {
                                editor.putInt("normalFuelNumber", fuelNumber);
                            } else {
                                editor.putInt("catalystFuelNumber", fuelNumber);
                            }
                        } else {
                            Toast tooHigh = Toast.makeText(getApplicationContext(),"Number too big, pick a smaller one",Toast.LENGTH_SHORT);
                            tooHigh.show();

                        }
                    }
                    int customBoostNormal2 = customBoostNormal;
                    if (!editNumCustomBoost.getText().toString().equals("")) {
                        customBoostNormal2 = Integer.parseInt(editNumCustomBoost.getText().toString());
                        if (customBoostNormal2 < 1000) {
                            editor.putInt("customBoostNormal", customBoostNormal2);
                        } else {
                            Toast tooHigh = Toast.makeText(getApplicationContext(),"Number too big, pick a smaller one",Toast.LENGTH_SHORT);
                            tooHigh.show();
                        }
                    }
                    int customBoostFly2 = customBoostFly;
                    if(!editNumCustomFly.getText().toString().equals("")) {
                        customBoostFly2 = Integer.parseInt(editNumCustomFly.getText().toString());
                        if (customBoostFly2 < 1000) {
                        editor.putInt("customBoostFly", customBoostFly2);
                        } else {
                            Toast tooHigh = Toast.makeText(getApplicationContext(),"Number too big, pick a smaller one",Toast.LENGTH_SHORT);
                            tooHigh.show();
                        }
                    }
                    editor.commit();
                    //Update spinners
                    ArrayList<String> upgrades = new ArrayList<>();
                    upgrades.add("Compactor");
                    upgrades.add("Super Compactor");
                    upgrades.add("Auto Smelter");
                    upgrades.add("Diamond Spreading");
                    upgrades.add("Enchanted Egg");
                    upgrades.add("Minion Expander 5%");
                    upgrades.add("Flycatcher 20%");
                    upgrades.add("Custom Speed Boost " + customBoostNormal2 + "%");
                    upgrades.add("Custom Fuel Boost " + customBoostFly2 + "%");
                    upgrades.add("Flint Shovel");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, upgrades);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerUpgrade1.setAdapter(adapter);
                    spinnerUpgrade2.setAdapter(adapter);
                    spinnerUpgrade1.setSelection(adapter.getPosition(upgrade1));
                    spinnerUpgrade2.setSelection(adapter.getPosition(upgrade2));
                }
            });
            //endregion

        //endregion
    }

    //Display help message
    public void displayHelp() {
        MinionHelpMessage message = new MinionHelpMessage();
        message.show(getSupportFragmentManager(),"example dialog");
    }

    //Quick method for removing pesky floating point imprecision decimals
    public double Round3(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Used code from internet to add commas to the investment amount automatically
    /**
     * Created by Shreekrishna on 12/14/2014.
     */
    public static class NumberTextWatcherForThousand implements TextWatcher {

        EditText editText;


        public NumberTextWatcherForThousand(EditText editText) {
            this.editText = editText;


        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try
            {
                editText.removeTextChangedListener(this);
                String value = editText.getText().toString();


                if (value != null && !value.equals(""))
                {

                    if(value.startsWith(".")){
                        editText.setText("0.");
                    }
                    if(value.startsWith("0") && !value.startsWith("0.")){
                        editText.setText("");

                    }


                    String str = editText.getText().toString().replaceAll(",", "");
                    if (!value.equals(""))
                        editText.setText(getDecimalFormattedString(str));
                    editText.setSelection(editText.getText().toString().length());
                }
                editText.addTextChangedListener(this);
                return;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                editText.addTextChangedListener(this);
            }

        }

        public static String getDecimalFormattedString(String value)
        {
            StringTokenizer lst = new StringTokenizer(value, ".");
            String str1 = value;
            String str2 = "";
            if (lst.countTokens() > 1)
            {
                str1 = lst.nextToken();
                str2 = lst.nextToken();
            }
            String str3 = "";
            int i = 0;
            int j = -1 + str1.length();
            if (str1.charAt( -1 + str1.length()) == '.')
            {
                j--;
                str3 = ".";
            }
            for (int k = j;; k--)
            {
                if (k < 0)
                {
                    if (str2.length() > 0)
                        str3 = str3 + "." + str2;
                    return str3;
                }
                if (i == 3)
                {
                    str3 = "," + str3;
                    i = 0;
                }
                str3 = str1.charAt(k) + str3;
                i++;
            }

        }

        public static String trimCommaOfString(String string) {
//        String returnString;
            if(string.contains(",")){
                return string.replace(",","");}
            else {
                return string;
            }

        }
    }
}