package com.andranym.skyblockbazaarstatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class FavoriteRecViewAdapter extends RecyclerView.Adapter<FavoriteRecViewAdapter.ViewHolder> {
    final static String TAG = "FavoriteRecViewAdapter";

    private ArrayList<Favorite> favorites = new ArrayList<>();
    private ArrayList<String> favoriteJustTitle = new ArrayList<>();
    Context context;


    public FavoriteRecViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favorites,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();
        holder.txtItemName.setText(favorites.get(position).getItemTitle());
        holder.txtItemInfo.setText(favorites.get(position).getItemDesc());
        //Remove the ability to delete the final element/
        if (favorites.size() == 1) {
            holder.btnDeleteItem.setVisibility(View.GONE);
        }
        //Remove ability to move it up if its already at the top
        if (position ==  0) {
            holder.btnMoveUp.setEnabled(false);
            holder.btnMoveUp.setVisibility(View.INVISIBLE);
        }
        //Likewise for the ability to move it down
        if (position == favorites.size()-1) {
            holder.btnMoveDown.setEnabled(false);
            holder.btnMoveDown.setVisibility(View.INVISIBLE);
        }

        holder.btnMoveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.swap(favorites,position,position-1);
                Collections.swap(favoriteJustTitle,position,position-1);
                setFavorites(favorites);
                setSaveData(favoriteJustTitle);
                String favoritesList = new Gson().toJson(favoriteJustTitle);
                editor.putString("favoritesList",favoritesList);
                editor.commit();
                Log.d(TAG,favoritesList);
                notifyDataSetChanged();
            }
        });
        holder.btnMoveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.swap(favorites,position,position+1);
                Collections.swap(favoriteJustTitle,position,position+1);
                setFavorites(favorites);
                setSaveData(favoriteJustTitle);
                String favoritesList = new Gson().toJson(favoriteJustTitle);
                editor.putString("favoritesList",favoritesList);
                editor.commit();
                Log.d(TAG,favoritesList);
                notifyDataSetChanged();
            }
        });
        holder.btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favorites.remove(position);
                favoriteJustTitle.remove(position);
                setFavorites(favorites);
                setSaveData(favoriteJustTitle);
                String favoritesList = new Gson().toJson(favoriteJustTitle);
                editor.putString("favoritesList",favoritesList);
                editor.commit();
                Log.d(TAG,favoritesList);
                notifyDataSetChanged();
            }
        });
        //Opens new activity with vastly more detail
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent viewDetails = new Intent(context, ExtraDetailViewPricesActivity.class);
                viewDetails.putExtra("itemToExpand",favoriteJustTitle.get(position));
                context.startActivity(viewDetails);
            }
        });
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void setFavorites(ArrayList<Favorite> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    public void setSaveData(ArrayList<String> favorites) {
        this.favoriteJustTitle = favorites;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        CardView parent;
        TextView txtItemInfo;
        TextView txtItemName;
        Button btnMoveUp;
        Button btnMoveDown;
        Button btnDeleteItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtItemInfo = itemView.findViewById(R.id.txtItemInfo);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
            btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
