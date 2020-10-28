package com.andranym.skyblockbazaarstatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class StonkRecViewAdapter extends RecyclerView.Adapter<StonkRecViewAdapter.ViewHolder> {

    private ArrayList<Stonk> data;
    private JSONObject priceData;
    Context context;

    public void setData(ArrayList<Stonk> stonkData,JSONObject priceData2) {
        this.data = stonkData;
        this.priceData = priceData2;
        notifyDataSetChanged();
    }

    public StonkRecViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public StonkRecViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_stonks,parent,false);
        return new StonkRecViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final double bazaarTax = 1-((double)(settings.getInt("personalBazaarTaxAmount",1250))/1000/100);

        //region Get product information and set the initial values
        String possibleCorrection = new FixBadNames().unfix(data.get(position).getProductName());
        final String currentProduct;
        if (possibleCorrection != null) {
            currentProduct = possibleCorrection;
        } else {
            currentProduct = data.get(position).getProductName();
        }
        double buyPrice = 0;
        double sellPrice = 0;
        JSONObject productsList;

        try {
            productsList = priceData.getJSONObject("products");
            int buyOrders = productsList.getJSONObject(currentProduct).getJSONObject("quick_status").getInt("buyOrders");
            int sellOrders = productsList.getJSONObject(currentProduct).getJSONObject("quick_status").getInt("buyOrders");
            if (buyOrders != 0) {
                buyPrice = productsList.getJSONObject(currentProduct).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
            } else {
                buyPrice = 0;
            }
            if (sellOrders != 0) {
                sellPrice = productsList.getJSONObject(currentProduct).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
            } else {
                sellPrice = 0;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String priceInfo = "Buy Price: " + addCommasAdjusted(Double.toString(Round2(buyPrice))) +
                "\nSell Price: " + addCommasAdjusted(Double.toString(Round2(sellPrice*bazaarTax)));
        holder.txtStonkPriceInfo.setText(priceInfo);

        String itemDesc = data.get(position).getProductName() + "\nYou own: " + addCommas(Integer.toString(data.get(position).getItemsOwned()));
        holder.txtItemNameStonk.setText(itemDesc);

        String orderHistory = data.get(position).getOrderHistory();
        holder.orderHistory.setText(orderHistory);
        //endregion

        //region Add explanation of how much it'll cost when you type a number
        final double finalBuyPrice1 = buyPrice;
        final double finalSellPrice = sellPrice;
        new Thread(){
            @Override
            public void run() {
                boolean keepRunning = true;
                while (keepRunning) {
                    String itemsDesiredString = holder.editAmount.getText().toString();
                    if (!itemsDesiredString.equals("") && itemsDesiredString.length() < 9){
                        int numberWanted = Integer.parseInt(itemsDesiredString);
                        double cost = Round2(numberWanted * finalBuyPrice1);
                        double profit = Round2(numberWanted * finalSellPrice * bazaarTax);
                        String updatedPriceInfo = "Buy Price: " + addCommasAdjusted(Double.toString(Round2(finalBuyPrice1))) +
                                "\n  Cost to buy " + numberWanted + ":\n  " + addCommasAdjusted(new BigDecimal(cost).toPlainString()) +
                                "\nSell Price: " + addCommasAdjusted(Double.toString(Round2(finalSellPrice *bazaarTax))) +
                                "\n  Earned by selling " + numberWanted + ":\n  " + addCommasAdjusted(new BigDecimal(profit).toPlainString());
                        new update().execute(updatedPriceInfo);
                    } else {
                        String priceInfo = "Buy Price: " + addCommasAdjusted(Double.toString(Round2(finalBuyPrice1))) +
                                "\nSell Price: " + addCommasAdjusted(Double.toString(Round2(finalSellPrice*bazaarTax)));
                        new update().execute(priceInfo);
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            class update extends AsyncTask<String,Void,String> {

                @Override
                protected String doInBackground(String... strings) {
                    return strings[0];
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    holder.txtStonkPriceInfo.setText(s);
                }
            }
        }.start();

        //endregion

        //regionCode to pin
        final boolean[] isPinned = {settings.getBoolean("isPinned" + currentProduct, false)};
        if (isPinned[0]) {
            holder.pin.setText("UNPIN");
        }
        holder.pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences.Editor editor = settings.edit();
                if (isPinned[0]) {
                    editor.putBoolean("isPinned" + currentProduct,false);
                    editor.commit();
                    holder.pin.setText("PIN");
                    isPinned[0] = false;
                } else {
                    editor.putBoolean("isPinned" + currentProduct,true);
                    editor.commit();
                    holder.pin.setText("UNPIN");
                    isPinned[0] = true;
                    Toast pinned = Toast.makeText(context.getApplicationContext(),"You will see this item even if you own none.",Toast.LENGTH_SHORT);
                    pinned.show();
                }
            }
        });
        //endregion

        //region Code for buy button
        final double finalBuyPrice = buyPrice;
        holder.buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountWanted = holder.editAmount.getText().toString();
                int numberWanted = 0;
                boolean possible = true;
                //Ensure the string isn't empty, or too large to parse an int
                if (!amountWanted.equals("") && amountWanted.length() < 9){
                    numberWanted = Integer.parseInt(amountWanted);
                } else {
                    possible = false;
                }
                if (StonkActivity.stonkBalance < (Round2(finalBuyPrice) * (double)numberWanted)) {
                    possible = false;
                }
                if (possible) {
                    StonkActivity.stonkBalance -= (Round2(finalBuyPrice) * (double)numberWanted);
                    String money = Double.toString(StonkActivity.stonkBalance);
                    int currentlyOwned = settings.getInt("stonksOwned" + currentProduct,0);
                    String previousHistory = settings.getString("orderHistory" + currentProduct,"");
                    int nowOwned = currentlyOwned + numberWanted;
                    String updateHistory = "Bought " + numberWanted + " " + data.get(position).getProductName() + " for " + finalBuyPrice + " each.\n";
                    final SharedPreferences.Editor editor = settings.edit();
                    editor.putString("stonkBalance",(new BigDecimal(money).toPlainString()));
                    editor.putInt("stonksOwned" + currentProduct,nowOwned);
                    editor.putString("orderHistory" + currentProduct,updateHistory + previousHistory);
                    editor.apply();
                    String itemDesc = data.get(position).getProductName() + "\nYou own: " + addCommas(Integer.toString(nowOwned));
                    holder.txtItemNameStonk.setText(itemDesc);
                    holder.orderHistory.setText("Order History:\n" + updateHistory + previousHistory);
                }
            }
        });
        //endregion

        //region Code for sell button
        holder.sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountWanted = holder.editAmount.getText().toString();
                int numberWanted = 0;
                boolean possible = true;
                //Ensure the string isn't empty, or too large to parse an int
                if (!amountWanted.equals("") && amountWanted.length() < 9){
                    numberWanted = Integer.parseInt(amountWanted);
                } else {
                    possible = false;
                }
                int currentlyOwned = settings.getInt("stonksOwned" + currentProduct,0);
                if (currentlyOwned < numberWanted) {
                    possible = false;
                }
                if (possible) {
                    StonkActivity.stonkBalance += (Round2(finalSellPrice * (double)numberWanted * bazaarTax));
                    String money = Double.toString(StonkActivity.stonkBalance);
                    String previousHistory = settings.getString("orderHistory" + currentProduct,"");
                    int nowOwned = currentlyOwned - numberWanted;
                    String updateHistory = "Sold " + numberWanted + " " + data.get(position).getProductName() + " for " + Round2(finalSellPrice * bazaarTax) + " each.\n";
                    final SharedPreferences.Editor editor = settings.edit();
                    editor.putString("stonkBalance",(new BigDecimal(money).toPlainString()));
                    editor.putInt("stonksOwned" + currentProduct,nowOwned);
                    editor.putString("orderHistory" + currentProduct,updateHistory + previousHistory);
                    editor.apply();
                    String itemDesc = data.get(position).getProductName() + "\nYou own: " + addCommas(Integer.toString(nowOwned));
                    holder.txtItemNameStonk.setText(itemDesc);
                    holder.orderHistory.setText("Order History:\n" + updateHistory + previousHistory);
                }
            }
        });
        //endregion

        //region Clear history button
        holder.clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences.Editor editor = settings.edit();
                editor.remove("orderHistory" + currentProduct);
                editor.apply();
                holder.orderHistory.setText("Order History:");
            }
        });

        //endregion
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        CardView parent;
        TextView txtItemNameStonk;
        TextView txtStonkPriceInfo;
        EditText editAmount;
        Button buy;
        Button sell;
        Button pin;
        Button clearHistory;
        TextView orderHistory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtItemNameStonk = itemView.findViewById(R.id.txtItemNameStonk);
            txtStonkPriceInfo = itemView.findViewById(R.id.txtItemInfoStonk);
            editAmount = itemView.findViewById(R.id.editAmount);
            buy = itemView.findViewById(R.id.btnBuyStonk);
            sell = itemView.findViewById(R.id.btnSellStonk);
            clearHistory = itemView.findViewById(R.id.btnClearHistory);
            orderHistory = itemView.findViewById(R.id.txtOrderHistory);
            pin = itemView.findViewById(R.id.btnStonkPin);
        }
    }

    //Add commas method, adjusted to work with decimal places at the end
    public String addCommasAdjusted(String digits) {
        //Store the part with the decimal
        String[] digitsSplit = digits.split("\\.");
        String beforeDecimal = digitsSplit[0];
        String afterDecimal;
        try {
            afterDecimal = digitsSplit[1];
        } catch (Exception e){
            afterDecimal = "0";
        }

        String result = "";
        for (int i=1; i <= beforeDecimal.length(); ++i) {
            char ch = beforeDecimal.charAt(beforeDecimal.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        //Put the decimals back on before returning
        if (afterDecimal.length() < 2) {
            result = result + "." + afterDecimal;
        } else {
            result = result + "." + afterDecimal.substring(0,1);
        }
        return result;
    }

    //Quick method for removing pesky floating point imprecision decimals
    public double Round2(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Add commas method for integers
    public String addCommas(String digits) {
        String result = "";
        for (int i=1; i <= digits.length(); ++i) {
            char ch = digits.charAt(digits.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        return result;
    }
}
