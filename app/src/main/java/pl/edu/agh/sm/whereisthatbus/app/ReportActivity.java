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

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;


public class ReportActivity extends Activity {
    AutoCompleteTextView busStopsNameReport;
    Spinner lineNameSpinnerReport;
    Spinner directionSpinnerReport;
    Button reportButton;
    DataBaseRepository db;

    List<BusStopCoords> busStopCoordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = new DataBaseRepository(this);
        busStopCoordsList = db.getBusStopsCoords();

        busStopsNameReport = (AutoCompleteTextView) findViewById(R.id.busStopsNameReport);
        lineNameSpinnerReport = (Spinner) findViewById(R.id.lineNameSpinnerReport);
        directionSpinnerReport = (Spinner) findViewById(R.id.directionSpinnerReport);
        reportButton = (Button) findViewById(R.id.reportButton);

        setBusStopsNameReportAdapter();
        setLineNameSpinnerReportAdapter(-1);
        setDirectionSpinnerReportAdapter();

        setBusStopsNameReportListeners();
        setLineNameSpinnerReportListeners();
        setReportButtonListeners();

        Parse.initialize(this, "V6fkKxIRViQ7S7Ftje0VlFca7y64iBoHBKi3yhBP", "mXh0C4i7FdILIiqzErEb15FcOOMguHou7LzpmpG9");
    }

    private void setNearestBusStopAsCurrentBusStop() {
        String nearestBusStop = getNearestBusStopName();
        busStopsNameReport.setText(nearestBusStop);
        busStopsNameReport.performCompletion();
        setLineNameSpinnerReportAdapter(db.getBusStopId(nearestBusStop));
        setDirectionSpinnerReportAdapter();
    }

    private void setBusStopsNameReportAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllBusStopsNames());
        busStopsNameReport.setAdapter(adapter);
        busStopsNameReport.setThreshold(1);
    }

    private void setLineNameSpinnerReportAdapter(int busStopId) {
        ArrayAdapter<String> adapter;
        if (busStopId == -1) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLineNames());
        } else {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLinesForBusStop(Integer.toString(busStopId)));
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineNameSpinnerReport.setAdapter(adapter);
    }

    private void setDirectionSpinnerReportAdapter() {
        String line = lineNameSpinnerReport.getSelectedItem().toString();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getEndStopsForLine(line));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpinnerReport.setAdapter(adapter);
    }

    private void setBusStopsNameReportListeners() {
        busStopsNameReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String busStopName = busStopsNameReport.getText().toString();
                int busStopId = db.getBusStopId(busStopName);
                setLineNameSpinnerReportAdapter(busStopId);
                setDirectionSpinnerReportAdapter();
            }
        });
    }

    private void setLineNameSpinnerReportListeners() {
        lineNameSpinnerReport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setDirectionSpinnerReportAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setReportButtonListeners() {
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseObject reportData = new ParseObject("ReportObject");
                reportData.put("bus_stop_id", db.getBusStopId(busStopsNameReport.getText().toString()));
                reportData.put("line_number", lineNameSpinnerReport.getSelectedItem().toString());
                reportData.put("line_direction_id", db.getBusStopId(directionSpinnerReport.getSelectedItem().toString()));
                reportData.put("report_time", System.currentTimeMillis());
                reportData.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        afterReport();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNearestBusStopAsCurrentBusStop();
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

    private void afterReport() {
        Toast.makeText(getApplicationContext(), "Report Send", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report, menu);
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
