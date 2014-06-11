package pl.edu.agh.sm.whereisthatbus.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.CountCallback;
import com.parse.ParseQueryAdapter;
import com.parse.*;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchActivity extends Activity {
    AutoCompleteTextView busStopsNameSearch;
    Spinner lineNameSpinnerSearch;
    Spinner directionSpinnerSearch;
    Button searchButton;
    DataBaseRepository db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = new DataBaseRepository(this);

        busStopsNameSearch = (AutoCompleteTextView) findViewById(R.id.busStopsNameSearch);
        lineNameSpinnerSearch = (Spinner) findViewById(R.id.lineNameSpinnerSearch);
        directionSpinnerSearch = (Spinner) findViewById(R.id.directionSpinnerSearch);
        searchButton = (Button) findViewById(R.id.searchButton);

        setBusStopsNameSearchAdapter();
        setLineNameSpinnerSearchAdapter(-1);
        setDirectionSpinnerSearchAdapter();

        setBusStopsNameSearchListeners();
        setlineNameSpinnerSearchListeners();
        setsearchButtonListeners();

        Parse.initialize(this, "V6fkKxIRViQ7S7Ftje0VlFca7y64iBoHBKi3yhBP", "mXh0C4i7FdILIiqzErEb15FcOOMguHou7LzpmpG9");
    }

    private void setBusStopsNameSearchAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllBusStopsNames());
        busStopsNameSearch.setAdapter(adapter);
        busStopsNameSearch.setThreshold(1);
    }

    private void setLineNameSpinnerSearchAdapter(int busStopId) {
        ArrayAdapter<String> adapter;
        if (busStopId == -1) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLineNames());
        } else {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLinesForBusStop(Integer.toString(busStopId)));
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineNameSpinnerSearch.setAdapter(adapter);
    }

    private void setDirectionSpinnerSearchAdapter() {
        String line = lineNameSpinnerSearch.getSelectedItem().toString();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getEndStopsForLine(line));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpinnerSearch.setAdapter(adapter);
    }

    private void setBusStopsNameSearchListeners() {
        busStopsNameSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String busStopName = busStopsNameSearch.getText().toString();
                int busStopId = db.getBusStopId(busStopName);
                setLineNameSpinnerSearchAdapter(busStopId);
                setDirectionSpinnerSearchAdapter();
            }
        });
    }

    private void setlineNameSpinnerSearchListeners() {
        lineNameSpinnerSearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setDirectionSpinnerSearchAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setsearchButtonListeners() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ReportObject");
                query.whereEqualTo("line_number", lineNameSpinnerSearch.getSelectedItem().toString());
                query.whereEqualTo("bus_stop_id", db.getBusStopId(busStopsNameSearch.getText().toString()));
                query.whereEqualTo("line_direction_id", db.getBusStopId(directionSpinnerSearch.getSelectedItem().toString()));
                query.setLimit(2);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        Intent intent = new Intent(SearchActivity.this, DisplayActivity.class);

                        if (object == null) {
                            Log.d("score", "The getFirst request failed.");//nie udało się pobrać lub nie ma danych w bazie
                        } else {
                            intent.putExtra("lastSeen", new SimpleDateFormat("HH:mm").format(object.getCreatedAt()));
                            Log.d("score", "Retrieved the object.");
                        }
                        //String newstring = new SimpleDateFormat("HH:mm").format(object.getCreatedAt());
                        intent.putExtra("lineNumber", lineNameSpinnerSearch.getSelectedItem().toString());
                        intent.putExtra("busStop", busStopsNameSearch.getText().toString());
                        intent.putExtra("lineDirection", directionSpinnerSearch.getSelectedItem().toString());
                        //intent.putExtra("lastSeen", object.getDate("createdAt"));
                        //startActivity(intent);
                        Log.i("tak", lineNameSpinnerSearch.getSelectedItem().toString());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        builder.setMessage("Autobus lini " + lineNameSpinnerSearch.getSelectedItem().toString() + " z przystanku "+ busStopsNameSearch.getText().toString()+" był ostatnio widziany o "+new SimpleDateFormat("HH:mm").format(object.getCreatedAt())).setTitle("Informacja");
                        AlertDialog dialog = builder.create();
                    }
                });
                //Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
            }
        });
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
