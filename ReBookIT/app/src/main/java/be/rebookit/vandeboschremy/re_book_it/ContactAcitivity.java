package be.rebookit.vandeboschremy.re_book_it;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Vandebosch Remy on 24/03/2018.
 */

public class ContactAcitivity extends AppCompatActivity implements OnMapReadyCallback {

    private String openingHours;
    private TextView tvOpeningHours;
    private View mapViewOverlay;
    private ScrollView scv;

    /**
     * This method that gets called when the activity is started.
     * @param savedInstanceState The instance state was saved.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        tvOpeningHours = (TextView) findViewById(R.id.tv_opening_hours);

        // Load the map that will display the position of the shop.
        final SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        scv = (ScrollView) findViewById(R.id.scv);

        // Set an overlap to the map so that it is possible to move the map instead of moving the scrollview.
        mapViewOverlay = (View) findViewById(R.id.map_view_overlay);
        mapViewOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scv.requestDisallowInterceptTouchEvent(true);
                return mapViewOverlay.onTouchEvent(event);
            }
        });

        // Start a downloader to load the opening hours.
        new DownloadOpeningHours().execute("https://rebookit.be/contact");
    }

    /**
     * This method extracts the opening hours from the website that is downloaded.
     * @param text The html website as string.
     * @return The opening hours.
     */
    public String extractOpeningHours(String text){
        int start = text.indexOf("Openingsuren</h4>") + ("Openingsuren</h4>                                    <p>").length();
        int end = text.lastIndexOf("Gesloten") + ("Gesloten").length();
        text = text.substring(start, end);
        Log.i("contact", text);
        String output = "";
        for(String subString : text.split("<br>")){
            output = output + subString.trim() + "\n";
        }
        return output;
    }

    /**
     * Set the marker on the map when the map is loaded.
     * @param googleMap The map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(50.9255981,5.3909875); // set the marker.
        googleMap.addMarker(new MarkerOptions().position(location).title(ContactAcitivity.this.getString(R.string.app_name))); // set the title of the marker.
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15)); // zoom the map to street level.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location)); // move the map to the marker.
    }

    /**
     * The downloader that will download the contact page from the website.
     */
    private class DownloadOpeningHours extends AsyncTask<String, Void, Void>{

        /**
         * The method that will download the website in the background.
         * @param url The url of the website to download.
         */
        @Override
        protected Void doInBackground(String... url) {
            try{
                // create the url
                Uri uri = Uri.parse(url[0]);
                URL mUrl = new URL(uri.toString());

                // establish a connection to the website.
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                InputStream in = conn.getInputStream();
                String line, text;
                text = "";
                byte[] byteArray = new byte[1024];
                BufferedReader reader = new BufferedReader(new InputStreamReader(in), byteArray.length);
                while((line = reader.readLine()) != null) {
                    text = text + line;
                }
                openingHours = extractOpeningHours(text);
            }
            catch(Exception e){
                Log.e("ContactActivity", e.toString());
            }
            return null;
        }

        /**
         * Show the opening hours on screen.
         * @param result The result that is always null.
         */
        @Override
        protected void onPostExecute(Void result){
            if(openingHours != null) tvOpeningHours.setText(openingHours);
        }
    }
}
