package com.example.fetchrewardscodingexercise;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<Integer> expandableListTitle;
    Map<Integer, List<ListItem>> expandableListDetail;
    RelativeLayout noItemsText;
    RelativeLayout loadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noItemsText = (RelativeLayout) findViewById(R.id.noItemsInList);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://fetch-hiring.s3.amazonaws.com/hiring.json";


        List<ListItem> listOfItems = new ArrayList<ListItem>();
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (Objects.equals(response, "")) {
                            noItemsText.setVisibility(View.VISIBLE);
                        } else {
                            try {
                                JSONArray responseAsArray = new JSONArray(response);
                                for (int i = 0; i < responseAsArray.length(); i++) {
                                    JSONObject listItemAsObject = responseAsArray.getJSONObject(i);

                                    Integer id = listItemAsObject.getInt("id");
                                    Integer listId = listItemAsObject.getInt("listId");

                                    //Checks null condition for name
                                    String name = listItemAsObject.optString("name", null);

                                    // checks for blank names
                                    if (name != "null" && !name.equals("")) {
                                        listOfItems.add(new ListItem(id, listId, name));
                                    }
                                }

                                // List is first sorted by list id
                                Collections.sort(listOfItems, new Comparator<ListItem>() {
                                    @Override
                                    public int compare(ListItem l1, ListItem l2) {
                                        return l1.getListId().compareTo(l2.getListId());
                                    }
                                });


                                // Now we group listitems by list id in a hashmap
                                Map<Integer, List<ListItem>> mapOrderedByListId = listOfItems.stream().collect(Collectors.groupingBy(ListItem::getListId));

                                // Now we sort the internal list items by name
                                for (Integer key : mapOrderedByListId.keySet()) {
                                    Collections.sort(mapOrderedByListId.get(key), new Comparator<ListItem>() {
                                        @Override
                                        public int compare(ListItem l1, ListItem l2) {
                                            return l1.getName().compareTo(l2.getName());
                                        }
                                    });
                                }

                                expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
                                expandableListDetail = mapOrderedByListId;
                                expandableListTitle = new ArrayList<Integer>(mapOrderedByListId.keySet());

                                loadingPanel.setVisibility(View.INVISIBLE);

                                expandableListAdapter = new CustomExpandableListAdapter(MainActivity.this, expandableListTitle, expandableListDetail);
                                expandableListView.setAdapter(expandableListAdapter);
                                expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                                    @Override
                                    public void onGroupExpand(int groupPosition) {
                                    }
                                });

                                expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                                    @Override
                                    public void onGroupCollapse(int groupPosition) {
                                    }
                                });

                                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                    @Override
                                    public boolean onChildClick(ExpandableListView parent, View v,
                                                                int groupPosition, int childPosition, long id) {
                                        //TODO: Open Dialog and show list item values
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "List Id: " + expandableListTitle.get(groupPosition)
                                                        + ", id: " +
                                                        expandableListDetail.get(
                                                        expandableListTitle.get(groupPosition)).get(
                                                        childPosition).getId()
                                                + ", name: " + expandableListDetail.get(
                                                        expandableListTitle.get(groupPosition)).get(
                                                        childPosition).getName()
                                                , Toast.LENGTH_LONG
                                        ).show();
                                        return false;
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Volley Error", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                this.recreate();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }
}
