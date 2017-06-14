package com.example.oliver.planet;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

/**
 * Created by Oliver on 5/21/2017.
 */

public class LevelSelectorActivity extends ListActivity {

    String[] test = {"Level 1", "Level 2", "Level 3"};

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_selector_layout);

        listView = (ListView) getListView(); // findViewById(R.id.mobile_list);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.level_list_item, test);


        listView.setAdapter(adapter);

        //TODO
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO

        Intent intent = new Intent(this, GameActivity.class);

        //TODO - Add level!
        startActivity(intent);
    }

    public void newLevel(View view) {
        Log.i("click", "New level!");
    }

}
