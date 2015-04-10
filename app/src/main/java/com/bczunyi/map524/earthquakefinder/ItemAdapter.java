package com.bczunyi.map524.earthquakefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Earthquake> {

    // declaring our ArrayList of items
    private ArrayList<Earthquake> objects;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public ItemAdapter(Context context, int textViewResourceId, ArrayList<Earthquake> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }

		/*
         * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Earthquake i = objects.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView d1 = (TextView) v.findViewById(R.id.timeData);
            TextView d2 = (TextView) v.findViewById(R.id.locData);
            TextView d3 = (TextView) v.findViewById(R.id.coordData);
            TextView d4 = (TextView) v.findViewById(R.id.depthData);
            TextView d5 = (TextView) v.findViewById(R.id.magData);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (d1 != null) {
                d1.setText(i.time);
            }
            if (d2 != null) {
                d2.setText(i.location);
            }
            if (d3 != null) {
                d3.setText("Lat: " + i.lat + " Lon: " + i.lon);
            }
            if (d4 != null) {
                d4.setText(i.depth + "km");
            }
            if (d5 != null) {
                d5.setText(i.magnitude + i.magType);
            }
        }

        // the view must be returned to our activity
        return v;

    }

}