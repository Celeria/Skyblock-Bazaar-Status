package com.andranym.skyblockbazaarstatus;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyMarkerView extends MarkerView {

    private TextView txtContent;
    private TextView txtContent2;

    public MyMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        txtContent = (TextView) findViewById(R.id.txtMarkerView);
        txtContent2 = findViewById(R.id.txtMarkerView2);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        long timeMilliseconds = (long)(e.getX());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
        Date resultdate = new Date(timeMilliseconds);
        String date = sdf.format(resultdate);
        String date2 = date.substring(6);
        txtContent.setText(date); // set the entry-value as the display text
        txtContent2.setText(date2);
    }

//    @Override
//    public int getXOffset(float xpos) {
//        // this will center the marker-view horizontally
//        return -(getWidth() / 2);
//    }
//
//    @Override
//    public int getYOffset(float ypos) {
//        // this will cause the marker-view to be above the selected value
//        return -getHeight();
//    }
}
