package pwittchen.com.icsl.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.pwittchen.icsl.library.InternetConnectionStateListener;
import com.pwittchen.icsl.library.event.ConnectivityStatusChangedEvent;
import com.pwittchen.icsl.library.event.WifiAccessPointsRefreshedEvent;
import com.pwittchen.icsl.library.helper.NetworkHelper;
import com.pwittchen.icsl.library.receiver.ConnectivityStatus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pwittchen.com.icsl.R;
import pwittchen.com.icsl.adapter.ScanResultAdapter;
import pwittchen.com.icsl.eventbus.BusProvider;
import pwittchen.com.icsl.room.RoomLocator;

/**
 * Sample activity
 * It's still under development and contains too many things for the quick start
 * @TODO: In the future I'll create a few examples. Both simple and more advanced
 */
public class MainActivity extends Activity {

    private InternetConnectionStateListener internetConnectionStateListener;
    private TextView tvConnectivityStatus;
    private TextView tvWifiInfo;
    private TextView tvLastUpdate;
    private TextView tvRoomLocation;
    private ListView lvAccessPointScanResults;

    private List<ScanResult> accessPoints = new ArrayList<ScanResult>();
    private ScanResultAdapter scanResultAdapter;
    private RoomLocator roomLocator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setScanResultAdapter();
        roomLocator = new RoomLocator(this);

        tvRoomLocation.setText(roomLocator.getNearestRoom());

        // passing Context and instance of Otto Event Bus
        internetConnectionStateListener = new InternetConnectionStateListener(this, BusProvider.getInstance());

        // register InternetConnectionStateListener
        internetConnectionStateListener.register();
    }

    private void initializeViews() {
        tvConnectivityStatus = (TextView) findViewById(R.id.tv_connectivity_status);
        tvWifiInfo = (TextView) findViewById(R.id.tv_wifi_info);
        tvLastUpdate = (TextView) findViewById(R.id.tv_last_update);
        lvAccessPointScanResults = (ListView) findViewById(R.id.lv_access_point_scan_results);
        tvRoomLocation = (TextView) findViewById(R.id.tv_room_location);
    }

    /**
     * This method is used only to display list of available access points.
     * It's an additional feature and we don't have to use it.
     */
    private void setScanResultAdapter() {
        scanResultAdapter = new ScanResultAdapter(this, R.layout.list_row, accessPoints);
        lvAccessPointScanResults.setAdapter(scanResultAdapter);
        scanResultAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register event bus
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister event bus
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister InternetConnectionStateListener
        internetConnectionStateListener.unregister();
    }

    @Subscribe
    public void connectivityStatusChanged(ConnectivityStatusChangedEvent event) {
        ConnectivityStatus status = event.getConnectivityStatus();
        tvConnectivityStatus.setText(String.format("connectivity status: %s", status.toString()));

        // display WiFi info in TextView, when device connects to the WiFi access point
        if (status == ConnectivityStatus.WIFI_CONNECTED) {
            tvWifiInfo.setText(String.format("WiFi Info:\n%s", event.getWifiInfo().toString()));
        } else if (status == ConnectivityStatus.OFFLINE || status == ConnectivityStatus.MOBILE_CONNECTED) {
            tvWifiInfo.setText("WiFi Info is not available.");
        }
        setLastUpdate();
    }

    @Subscribe
    public void wifiAccessPointsRefreshed(WifiAccessPointsRefreshedEvent event) {
        refreshAccessPointsListAndWifiInfo();
        setScanResultAdapter();
        tvRoomLocation.setText(roomLocator.getNearestRoom());
        setLastUpdate();
    }

    private void setLastUpdate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:m:s");
        tvLastUpdate.setText(String.format("last update: %s", dateTimeFormatter.print(new DateTime())));
    }

    private void refreshAccessPointsListAndWifiInfo() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        tvWifiInfo.setText(wifiManager.getConnectionInfo().toString());
        accessPoints = wifiManager.getScanResults();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshAccessPointsListAndWifiInfo();
            setScanResultAdapter();
            setLastUpdate();
            return true;
        } else if(id == R.id.action_room_locator) {
            Intent intent = new Intent(this, RoomLocatorActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
