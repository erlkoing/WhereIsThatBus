package pl.edu.agh.sm.whereisthatbus.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class SearchActivity extends Activity {
    AutoCompleteTextView busStopsNameSearch;
    Spinner lineNameSpinnerSearch;
    Spinner directionSpinnerSearch;
    Button searchButton;
    ImageButton refreshButton;
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
        refreshButton = (ImageButton) findViewById(R.id.refreshButtonSearch);

        setBusStopsNameSearchAdapter();
        setLineNameSpinnerSearchAdapter(-1);
        setDirectionSpinnerSearchAdapter();

        setBusStopsNameSearchListeners();
        setLineNameSpinnerSearchListeners();
        setSearchButtonListeners();
        setRefreshButtonListeners();

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
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLinesForBusStop(busStopId));
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineNameSpinnerSearch.setAdapter(adapter);
    }

    private void setDirectionSpinnerSearchAdapter() {
        String line = lineNameSpinnerSearch.getSelectedItem().toString();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getEndStopIdsForLine(line));
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

    private void setLineNameSpinnerSearchListeners() {
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

    private int getActualDayTimeInMinutes() {
        Calendar calendar = Calendar.getInstance();
        int currentDayTime = 0;
        currentDayTime += 60 * calendar.get(Calendar.HOUR_OF_DAY);
        currentDayTime += calendar.get(Calendar.MINUTE);
        return currentDayTime;
    }

    private String getCurrentDayLabel() {
        Calendar calendar = Calendar.getInstance();
        return getDayOfTheWeekLabel(calendar.get(Calendar.DAY_OF_WEEK));
    }

    private String getDayOfTheWeekLabel(int dayOfTheWeek) {
        if (dayOfTheWeek == 1) return "N";
        else if(dayOfTheWeek == 7) return "S";
        else return "T";
    }

    private int convertDateToMinutest(Date date) {
        int dayTimeMinutes = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        dayTimeMinutes = 60 * hours;
        dayTimeMinutes += minutes;

        return dayTimeMinutes;
    }

    private void setRefreshButtonListeners() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNearestBusStopAsCurrentBusStop();
            }
        });
    }

    private void setSearchButtonListeners() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isInternetConnection(getApplicationContext())) {
                    final int currentTime = getActualDayTimeInMinutes();
                    final String dayLabel = getCurrentDayLabel();
                    final int stopId = db.getBusStopId(busStopsNameSearch.getText().toString());

                    String lineName = lineNameSpinnerSearch.getSelectedItem().toString();
                    int lineDirectionId = db.getBusStopId(directionSpinnerSearch.getSelectedItem().toString());

                    final String lineId = db.getLineId(lineName, lineDirectionId, stopId);
                    final String connectionId = db.getNearestConnectionId(currentTime, lineId, stopId, dayLabel);

                    int timeDifferenceBetweenFirstAndCurrentBusStop = db.getTimeBeenFirstAndCurrentBusStopForConnection(connectionId, stopId);
                    int stopPlacement = db.getStopPlacement(lineId, stopId);

                    Toast.makeText(getApplicationContext(),"Proszę czekać, pobierane są dane", Toast.LENGTH_SHORT).show();

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ReportObject");
                    query.whereEqualTo("line_number", lineName);
                    query.whereEqualTo("line_id", lineId);
                    query.whereEqualTo("line_direction_id", lineDirectionId);
                    query.whereLessThan("stop_placement", stopPlacement); // zeby nie brac pod uwage polaczen rejestrowanych na kolejnych przystankach na trasie
                    query.whereGreaterThan("report_time", (System.currentTimeMillis() - 1000 * 60 * timeDifferenceBetweenFirstAndCurrentBusStop) / 1000); // zeby zawedzic czas wyszukiwania

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            List<Integer> minutesToArrive = new ArrayList<Integer>();
                            List<ReportData> reportDataList = new ArrayList<ReportData>();
                            for (ParseObject parseObject: parseObjects) {
                                int reportStopId = parseObject.getInt("bus_stop_id");
                                int timeBetweenBusStops = db.getTimeBeetweenBusStops(connectionId, reportStopId, stopId);

                                long reportTimeInMiliseconds = parseObject.getLong("report_time") * 1000;
                                long estimatedTimeOfArrival = reportTimeInMiliseconds + timeBetweenBusStops * 60*1000;
                                Date estimatedDateOfArrival = new Date(estimatedTimeOfArrival);

                                int expectedArrivalTime = convertDateToMinutest(estimatedDateOfArrival) - convertDateToMinutest(new Date(System.currentTimeMillis()));

                                if (expectedArrivalTime >=0)
                                    reportDataList.add(new ReportData(reportTimeInMiliseconds, expectedArrivalTime));
                            }

                            String message = "";

                            if (reportDataList.size() > 1) {
                                Collections.sort(reportDataList, new ReportDataComparator());
                                Collections.reverse(reportDataList);

                                int deltaT = db.getDeltaTimeBetweenNearestConnections(currentTime, lineId, stopId, dayLabel);
                                for (ReportData reportData: reportDataList) {
                                    if (minutesToArrive.size() == 0) {
                                        minutesToArrive.add(reportData.minutestToArrive);
                                    } else {
                                        boolean add = true;
                                        for (Integer i: minutesToArrive) {
                                            if (Math.abs(i - reportData.minutestToArrive) - deltaT < 0) {
                                                add = false;
                                            }
                                        }

                                        if (add) {
                                            minutesToArrive.add(reportData.minutestToArrive);
                                        }
                                    }
                                }
                                Collections.sort(minutesToArrive);
                                message = "Najbliższe połączenie za: " + minutesToArrive.get(0).toString() + " minut";
                                if(minutesToArrive.size() > 1) {
                                    message += "\nKolejne połączenia za: ";
                                    for (int i = 1; i < minutesToArrive.size() - 1; i++) {
                                        message += minutesToArrive.get(i) + ", ";
                                    }
                                    message += minutesToArrive.get(minutesToArrive.size() - 1);
                                }
                                message += "\nNajblizsze planowane połączenie za: " + db.getNearestConnectionTimeArrival(currentTime, lineId, stopId, dayLabel) + " minut";

                            } else if (reportDataList.size() == 1) {
                                minutesToArrive.add(reportDataList.get(0).minutestToArrive);
                                message = "Najbliższe połączenie za: " + minutesToArrive.get(0).toString() + " minut";
                                message += "\nNajblizsze planowane połączenie za: " + db.getNearestConnectionTimeArrival(currentTime, lineId, stopId, dayLabel) + " minut";
                            } else if (reportDataList.size() == 0) {
                                message = "Brak informacji o połączeniach podanej linii.";
                                message += "\nNajblizsze planowane połączenie za: " + db.getNearestConnectionTimeArrival(currentTime, lineId, stopId, dayLabel) + " minut";
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                            builder.setTitle("Informacja");
                            builder.setMessage(message);

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SearchActivity.this.finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Brak połączenia z internetem.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private boolean isInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
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

class ReportData {
    long reportTime;
    int minutestToArrive;

    public ReportData( long reportTime, int minutestToArrive) {
        this.reportTime = reportTime;
        this.minutestToArrive = minutestToArrive;
    }
}

class ReportDataComparator implements Comparator<ReportData> {

    @Override
    public int compare(ReportData o1, ReportData o2) {
        if (o1.reportTime >= o2.reportTime) {
            return 1;
        } else {
            return -1;
        }
    }
}
