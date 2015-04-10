package com.bczunyi.map524.earthquakefinder;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class QuakeListActivity extends ActionBarActivity {
    static ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
    static Location from = new Location("");
    float distance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();

        from.setLatitude(intent.getDoubleExtra("lat", 0.0));
        from.setLongitude(intent.getDoubleExtra("lon", 0.0));
        distance = intent.getFloatExtra("dist", 0f);

        quakes.clear();
        for (Earthquake eq : MainActivity.quakes) {
            Location to = new Location("");
            to.setLatitude(eq.lat);
            to.setLongitude(eq.lon);
            String temp = from.getLatitude() + "|" + to.getLatitude() + "|" + from.getLongitude() + "|" + to.getLongitude() + "|" + String.valueOf(from.distanceTo(to));
            if (from.distanceTo(to) < distance)
                quakes.add(eq);
        }
        if(quakes.size() > 0) {
            QuakeListFragment fragment = new QuakeListFragment();
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
            ((TextView) findViewById(R.id.empty)).setText("");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_about: {
                showAbout();
                break;
            }
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.map: {
                goMap();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    public void goMap() {
        startActivity(new Intent(QuakeListActivity.this, MapActivity.class));
    }

    public static class QuakeListFragment extends ListFragment {

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ItemAdapter m_adapter = new ItemAdapter(getActivity(), R.layout.list_item, quakes);

            setListAdapter(m_adapter);
        }
    }
}