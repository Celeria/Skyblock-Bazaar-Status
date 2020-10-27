package com.andranym.skyblockbazaarstatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        double bazaarTax = 1-((double)(settings.getInt("personalBazaarTaxAmount",1250))/1000/100);

        //region Get product information
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
        //endregion

        //region Code for buy button
        final double finalBuyPrice = buyPrice;
        holder.buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountWanted = holder.editAmount.getText().toString();
                int numberWanted = 0;
                if (!amountWanted.equals("")){
                    numberWanted = Integer.parseInt(amountWanted);
                }
                boolean possible = true;
                if (StonkActivity.stonkBalance < (Round2(finalBuyPrice) * (double)numberWanted)) {
                    possible = false;
                }
                if (possible) {
                    StonkActivity.stonkBalance -= (Round2(finalBuyPrice) * (double)numberWanted);
                    String money = Double.toString(StonkActivity.stonkBalance);
                    int currentlyOwned = settings.getInt("stonksOwned" + currentProduct,0);
                    int nowOwned = currentlyOwned + numberWanted;
                    final SharedPreferences.Editor editor = settings.edit();
                    editor.putString("stonkBalance",money);
                    editor.putInt("stonksOwned" + currentProduct,nowOwned);
                    editor.apply();
                    String itemDesc = data.get(position).getProductName() + "\nYou own: " + addCommas(Integer.toString(nowOwned));
                    holder.txtItemNameStonk.setText(itemDesc);
                }
            }
        });
        //endregion

        //region Code for sell button

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
        TextView orderHistory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtItemNameStonk = itemView.findViewById(R.id.txtItemNameStonk);
            txtStonkPriceInfo = itemView.findViewById(R.id.txtItemInfoStonk);
            editAmount = itemView.findViewById(R.id.editAmount);
            buy = itemView.findViewById(R.id.btnBuyStonk);
            sell = itemView.findViewById(R.id.btnSellStonk);
            orderHistory = itemView.findViewById(R.id.txtOrderHistory);
        }
    }

    //Add commas method, adjusted to work with decimal places at the end
    public String addCommasAdjusted(String digits) {
        //Store the part with the decimal
        String afterDecimal = digits.substring(digits.length()-2);
        //Run original code on the raw string with the decimal part cut off
        String beforeDecimal = digits.substring(0,digits.length()-2);

        String result = "";
        for (int i=1; i <= beforeDecimal.length(); ++i) {
            char ch = beforeDecimal.charAt(beforeDecimal.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        //Put the decimals back on before returning
        result = result + afterDecimal;
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
