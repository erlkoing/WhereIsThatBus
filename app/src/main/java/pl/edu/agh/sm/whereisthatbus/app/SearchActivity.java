package pl.edu.agh.sm.whereisthatbus.app;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;


public class SearchActivity extends Activity {
    AutoCompleteTextView busStopsNameSearch;
    Spinner lineNameSpinnerSearch;
    Spinner directionSpinnerSearch;
    Button searchButton;
    DataBaseRepository db;

    List<BusStopCoords> busStopCoordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = new DataBaseRepository(this);
        busStopCoordsList = db.getBusStopsCoords();

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

    @Override
    protected void onResume() {
        super.onResume();
        setNearestBusStopAsCurrentBusStop();
    }

    private void setNearestBusStopAsCurrentBusStop() {
        String nearestBusStop = getNearestBusStopName();
        busStopsNameSearch.setText(nearestBusStop);
        busStopsNameSearch.performCompletion();
        setLineNameSpinnerSearchAdapter(db.getBusStopId(nearestBusStop));
        setDirectionSpinnerSearchAdapter();
    }

    private String getNearestBusStopName() {
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String bestLocationProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestLocationProvider);
        BusStopCoords nearestBusStop = null;

        double currentLat = location.getLatitude();
        double currentLon = location.getLongitude();
        for (BusStopCoords busStopCoords: busStopCoordsList) {
            if (nearestBusStop == null) {
                nearestBusStop = busStopCoords;
            } else {
                if (distanceBetweenPoints(busStopCoords.lat, busStopCoords.lon, currentLat, currentLon) < distanceBetweenPoints(nearestBusStop.lat, nearestBusStop.lon, currentLat, currentLon)) {
                    nearestBusStop = busStopCoords;
                }
            }
        }

        return nearestBusStop.busStopName;
    }

    private double distanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
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
                Toast.makeText(getApplicationContext(),"Prosze czekac, pobierane sa dane", Toast.LENGTH_LONG).show();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ReportObject");
                query.whereEqualTo("line_number", lineNameSpinnerSearch.getSelectedItem().toString());
                query.whereEqualTo("bus_stop_id", db.getBusStopId(busStopsNameSearch.getText().toString()));
                query.whereEqualTo("line_direction_id", db.getBusStopId(directionSpinnerSearch.getSelectedItem().toString()));
                query.whereGreaterThan("report_time", System.currentTimeMillis() - 1000 * 60 * 60);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        Toast.makeText(getApplicationContext(),"Dane zostaly pobrane", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),Integer.toString(parseObjects.size()), Toast.LENGTH_LONG).show();
                        String lineId = db.getLineId(Integer.parseInt(lineNameSpinnerSearch.getSelectedItem().toString()), db.getBusStopId(directionSpinnerSearch.getSelectedItem().toString()));
                        Toast.makeText(getApplicationContext(),"Line id = " + lineId    , Toast.LENGTH_LONG).show();

                        for (ParseObject parseObject: parseObjects) {

                            Date date = new Date(parseObject.getLong("report_time"));
                            String messsage = "Autobus linii " + lineNameSpinnerSearch.getSelectedItem().toString() + " byl ostatnio widziany na przystanku " + db.getBusStopName(parseObject.getInt("bus_stop_id")) + "o godzinie " + date.toString();

                            Toast.makeText(getApplicationContext(),messsage, Toast.LENGTH_LONG).show();

                        }

                    }
                });
                /*query.setLimit(1);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                        builder.setTitle("Informacja");
                        if (object != null) {
                            builder.setMessage("Autobus lini " + lineNameSpinnerSearch.getSelectedItem().toString() + " z przystanku " + busStopsNameSearch.getText().toString() + " był ostatnio widziany o " + new SimpleDateFormat("HH:mm").format(object.getCreatedAt()));
                        }else{
                            builder.setMessage("Brak informacji o autobusie lini " + lineNameSpinnerSearch.getSelectedItem().toString() + " z przystanku " + busStopsNameSearch.getText().toString());
                        }
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SearchActivity.this.finish();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });*/
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
