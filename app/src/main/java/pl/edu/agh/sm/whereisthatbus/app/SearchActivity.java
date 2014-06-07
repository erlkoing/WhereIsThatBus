package pl.edu.agh.sm.whereisthatbus.app;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


public class SearchActivity extends Activity {

    Button goSearch;
    String busStopValue, lineValue, directionValue;

    public void startDisplayActivity() {
        Intent i = new Intent(this, DisplayActivity.class);
        i.putExtra("busStop", busStopValue);
        i.putExtra("line", lineValue);
        i.putExtra("direction", directionValue);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getBusStopsName());
        final AutoCompleteTextView busStopsName = (AutoCompleteTextView) findViewById(R.id.busStopsName);
        busStopsName.setAdapter(adapter);
        busStopsName.setThreshold(1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getLinesNumber());
        AutoCompleteTextView linesName = (AutoCompleteTextView) findViewById(R.id.linesName);
        linesName.setAdapter(adapter2);
        linesName.setThreshold(1);

        ArrayAdapter<String> directionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getDirectionName());
        AutoCompleteTextView directionName = (AutoCompleteTextView) findViewById(R.id.directionName);
        directionName.setAdapter(directionAdapter);
        directionName.setThreshold(1);

        busStopsName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                busStopValue = (String) parent.getItemAtPosition(position);
                //Log.i("Zaznaczenie: ", selection);
            }
        });

        linesName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lineValue = (String) adapterView.getItemAtPosition(i);
            }
        });

        directionName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                directionValue = (String) adapterView.getItemAtPosition(i);
            }
        });

        goSearch = (Button) findViewById(R.id.goSearchButton);

        goSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (busStopsName != null && lineValue != null && directionValue != null) {
                    //Vector<HashMap<String, String>> queryResult = fetchDataFromDatabase("Select ");
                    startDisplayActivity();
                }else{
                    Toast.makeText(getApplicationContext(), "Wprowadzono niepoprawne dane", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<String> getBusStopsName() {
        Vector<HashMap<String, String>> queryResult = fetchDataFromDatabase(SQLQueries.GET_ALL_BUS_STOPS);

        List<String> busStopsName = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            busStopsName.add(element.get("Name"));
        }

        return busStopsName;
    }

    private List<String> getLinesNumber() {
        Vector<HashMap<String, String>> queryResult = fetchDataFromDatabase(SQLQueries.GET_ALL_LINES);

        List<String> linesNumber = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            linesNumber.add(element.get("Name"));
        }

        return linesNumber;
    }

    private List<String> getDirectionName() {
        Vector<HashMap<String, String>> queryResult = fetchDataFromDatabase(SQLQueries.GET_ALL_DIRECTIONS);

        List<String> directionName = new ArrayList<String>();

        for(HashMap<String, String> element: queryResult) {
            directionName.add(element.get("LastStopName"));
        }
        return directionName;
    }

    private Vector<HashMap<String, String>> fetchDataFromDatabase(String query) {
        BaseDatabaseRepository dataBase = new BaseDatabaseRepository(this);
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> data = dataBase.executeQuery(query, null);
        dataBase.closeDatabase();

        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}