package be.rebookit.vandeboschremy.re_book_it;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * The MainActivity has a recyclerview that displays the list of available books at Re-Book IT.
 * There is also the possibility to search for a specific book based on the title, author, course or ISBN.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener , SharedPreferences.OnSharedPreferenceChangeListener{

    private static String json;
    private Spinner spinner;
    private static RecyclerView mBookList;
    private static BookListAdapter mAdapter;
    private static Cursor mCursor;
    private static String query, searchBy;
    private SharedPreferences prefs;
    private static boolean downloaderStartedFlag, updatedFlag, startedFlag;
    private static Context mContext;
    private static BroadcastReceiver reciever;
    private static TextView tv;

    @Override
    /**
     * The method that is called when this activity is created
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the context
        mContext = MainActivity.this;

        //create the notification to show if the internet is off
        tv = (TextView) findViewById(R.id.no_network_tv);

        //intentfilter for the reciever
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        //register the reciever for connectionupdates
        reciever = new NetworkChangeListener();
        registerReceiver(reciever, filter);

        //check is there is a query saved from last time
        query = null;
        if(savedInstanceState != null && savedInstanceState.containsKey(this.getString(R.string.query_key))){
            query = savedInstanceState.getString(this.getString(R.string.query_key));
        }

        //check if there is a searchBy preference from last time
        searchBy = getResources().getStringArray(R.array.searchChoice)[0];
        if(savedInstanceState != null && savedInstanceState.containsKey(this.getString(R.string.searhedBy_key))){
            searchBy = savedInstanceState.getString(this.getString(R.string.searhedBy_key));
        }

        //create the spinner to provide a choice when searching
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.searchChoice, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        //create the recyclerview
        mBookList = (RecyclerView) findViewById(R.id.rv);
        mBookList.setLayoutManager(new LinearLayoutManager(this));

        //register the preferenceChangeListener
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        //check if there is already a database. If so, load it for the user to see
        if(DatabaseUtils.getCursorFromDB(MainActivity.this).getCount() != 0){
            if(query != null){
                spinner.setVisibility(View.VISIBLE);
                showData(DatabaseUtils.getCursorFromDBySearch(query, searchBy));
            }
            else showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
        }
        else{
            if(!startedFlag) {
                Intent intent = new Intent(this, InfoActivity.class);
                this.startActivity(intent);
            }
        }
        startedFlag = true;
    }

    /**
     * THe method that is called when this activity is destroyed.
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(reciever);
    }

    /**
     *Build a JSON cursor from a string.
     * @param response The string from which to build a cursor.
     * @return A cursor of type Cursor.
     */
    private static Cursor getJSONCursor(String response){
        try{
            JSONArray array = new JSONArray(response);
            return new JSONArrayCursor(array);
        } catch(JSONException exception)
        {
            String ex = exception.getMessage();
            Log.e("MainActivity", ex);
        }
        return null;
    }

    /**
     * Convert the string returned from the website to the right format
     * because it is not perfect JSON format.
     * @param text The returned string from the website.
     * @return A String in JSON format.
     */
    private static String convertToRightFormat(String text){
        String jsonFormat;
        String correctJsonFormat = "";

        //find the start and end of the book list.
        int start = text.indexOf("[{");
        int end = text.indexOf("}]")+("}]").length();
        if(start == -1 || end == -1) return null;
        jsonFormat = text.substring(start, end);

        //check for arrays that contain only one element and remove the brackets.
        for(String subString : jsonFormat.split(":")){
            if(subString.indexOf("[") != -1 &&
                    subString.indexOf("]") != -1 &&
                    !subString.substring(subString.indexOf("["), subString.indexOf("]")).contains(",")){
                subString = subString.replace("[","");
                subString = subString.replace("]","");
            }
            if(!subString.contains("}]")){
                correctJsonFormat = correctJsonFormat + subString + ":";
            }
            else{
                correctJsonFormat = correctJsonFormat + subString;
            }
        }
        return jsonFormat;
    }

    /**
     * Create the options menu.
     * @param menu The menu to create.
     * @return A boolean to notify if the creation was successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //inflate the menu layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        //set an onQueryTextListener to the searchview
        MenuItem searchViewMenuItem = (MenuItem) menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchViewMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showData(DatabaseUtils.getCursorFromDBySearch(query, searchBy));
                //show the spinner when a query is submitted
                spinner.setVisibility(View.VISIBLE);
                //clear the focus so the keyboard disappears
                searchView.clearFocus();
                MainActivity.this.query = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //set an onActionExpandListener to the searchview
        searchViewMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //open the keyboard when the searchview is expanded
                searchView.setIconified(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //make the spinner disappear and load the previous list
                spinner.setVisibility(View.GONE);
                searchView.clearFocus();
                showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
                MainActivity.this.query = null;
                return true;
            }
        });

        //if there is a query expand the SearchView
        //this is used when the screen is rotated
        if(query != null){
            searchViewMenuItem.expandActionView();
            searchView.setQuery(query, false);
            searchView.clearFocus();
        }
        return true;
    }

    /**
     * Load the SettingsActivity.
     * @param item the MenuItem that was clicked.
     */
    public void loadSettings(MenuItem item){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(intent);
    }

    /**
     * Load the ContactAcivity.
     * @param item the MenuItem that was clicked.
     */
    public void loadContact(MenuItem item){
        Intent intent = new Intent(MainActivity.this, ContactAcitivity.class);
        MainActivity.this.startActivity(intent);
    }

    /**
     * Load the InfoActivity.
     * @param item the MenuItem that was clicked.
     */
    public void loadInfo(MenuItem item){
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        MainActivity.this.startActivity(intent);
    }

    /**
     * Redirect to the sell web page of Re-Book IT
     */
    public void loadSell(MenuItem item){
        Uri webpage = Uri.parse("https://rebookit.be/sell");
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Return the query that was submitted
     * @return The query, null if query is empty
     */
    public static String getQuery(){
        return query;
    }

    /**
     * Return the value of the searchBy spinner.
     * @return The value of the searchBy spinnen.
     */
    public static String getSearchBy(){
        return searchBy;
    }

    /**
     * Return the updatedFlag.
     * @return the updateFlag.
     */
    public static Boolean getUpdatedFlag(){
        return updatedFlag;
    }

    /**
     * Return the downloaderStartedFlag.
     * @return the downloaderStartedFlag.
     */
    public static Boolean getDownloaderStartedFlag(){
        return downloaderStartedFlag;
    }

    /**
     * display the book list to the user
     * @param cursor The cursor that contains the list to be displayed
     */
    public static void showData(Cursor cursor){
        mCursor = cursor;
        mBookList.setVisibility(View.VISIBLE);
        mAdapter = new BookListAdapter(mContext, mCursor);
        mBookList.setAdapter(mAdapter);
    }

    /**
     * This method saves the query and state of the spinner when the activity is paused.
     * @param outState The Bundle in which the data is saved.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(this.getString(R.string.query_key), query);
        outState.putString(this.getString(R.string.searhedBy_key), searchBy);
    }

    /**
     * Triggers when the user selected an item from the spinner.
     * @param parent The AdapterView.
     * @param view Te View.
     * @param position The position of the element that was selected.
     * @param id The id.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Extract the selected item from the spinner
        searchBy = parent.getItemAtPosition(position).toString();
        //apply the filter tot the displayed data
        showData(DatabaseUtils.getCursorFromDBySearch(query, searchBy));
    }

    /**
     * Triggers when nothing is selected on the spinner.
     * @param parent The AdapterView.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * This function runs when there has been a change in the settings menu.
     * @param sharedPreferences The preferences.
     * @param key The key.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
    }

    /**
     * This method makes the textview visible that show the user that there is no network connection.
     */
    public static void showNoNetwork(){
        tv.setVisibility(View.VISIBLE);
        tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_down));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setVisibility(View.GONE);
                tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up));
            }
        }, 4000);
    }

    public static class Downloader extends AsyncTask<String,Void,Void> {
        /**
         * Show a toast to notify the user that the data is loading.
         */
        @Override
        protected void onPreExecute(){
            Toast toast = Toast.makeText(mContext, mContext.getString(R.string.toast_start_update), Toast.LENGTH_SHORT);
            toast.show();
            downloaderStartedFlag = true;
        }
        /**
         * Extract the list of books from the Re-Book-IT website and convert it to a json format.
         * @param url the url of the website
         */
        @Override
        protected Void doInBackground(String... url) {
            try {
                Uri uri = Uri.parse(url[0]);
                URL mUrl = new URL(uri.toString());
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                InputStream in = conn.getInputStream();
                String line, text;
                text = "";
                byte[] byteArray = new byte[1024];
                BufferedReader reader = new BufferedReader(new InputStreamReader(in), byteArray.length);
                while((line = reader.readLine()) != null){
                    text = text + line;
                }
                json = convertToRightFormat(text);
                if(json != null){
                    mCursor = getJSONCursor(json);
                    DatabaseUtils.saveToDB(mContext, mCursor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Show a toast to notify the user that the content was updated or that the update failed
         * @param result The result of the download, in this case Void.
         */
        @Override
        protected void onPostExecute(Void result){

            if(json != null){
                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.toast_end_succes), Toast.LENGTH_SHORT);
                toast.show();
                if(getQuery() != null){
                    showData(DatabaseUtils.getCursorFromDBySearch(query, searchBy));
                }
                else{
                    showData(DatabaseUtils.getCursorFromDB(mContext));
                }
                updatedFlag = true;
            }
            else{
                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.toast_end_fail), Toast.LENGTH_SHORT);
                toast.show();
            }
            downloaderStartedFlag = false;
        }
    }
}
