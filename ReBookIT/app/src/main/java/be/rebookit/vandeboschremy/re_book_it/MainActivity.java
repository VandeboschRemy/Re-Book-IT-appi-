package be.rebookit.vandeboschremy.re_book_it;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener , SharedPreferences.OnSharedPreferenceChangeListener{

    private String json;
    private Spinner spinner;
    private RecyclerView mBookList;
    private BookListAdapter mAdapter;
    private Cursor mCursor;
    private static String query, searchBy;
    private SharedPreferences prefs;
    private static boolean startedFlag;

    @Override
    /**
     * The method that is called when this activity is created
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check is there is a query saved from last time
        query = null;
        if(savedInstanceState != null && savedInstanceState.containsKey(this.getString(R.string.query_key))){
            query = savedInstanceState.getString(this.getString(R.string.query_key));
        }

        //check if there is a searchBy preference from last time
        searchBy = "Title";
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
        if(DatabaseUtils.getCursorFromDB(MainActivity.this) != null){
            if(query != null){
                spinner.setVisibility(View.VISIBLE);
                showData(DatabaseUtils.getCursorFromDBySearch(query, searchBy));
            }
            else showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
        }
        //perform the call the to website only when the application is starting up
        if(!startedFlag) new Downloader().execute("https://rebookit.be/search");
        startedFlag = true;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     *Build a JSON cursor from a string.
     * @param response The string from which to build a cursor.
     * @return A cursor of type Cursor.
     */
    private Cursor getJSONCursor(String response){
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
     * Because the string returned from the website is not perfect JSON format,
     * convert it to the right format
     * @param text The returned string from the website.
     * @return A String in JSON format.
     */
    public String convertToRightFormat(String text){
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
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
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
        menu.findItem(R.id.action_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
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
                showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
                MainActivity.this.query = null;
                return true;
            }
        });
        return true;
    }

    public void loadSettings(MenuItem item){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(intent);
    }

    public void loadContact(MenuItem item){
        //TO-DO implement contact activity
    }

    /**
     * Return the query that was submitted
     * @return The query, null if query is empty
     */
    public static String getQuery(){
        return query;
    }

    public static String getSearchBy(){
        return searchBy;
    }

    /**
     * display the book list to the user
     * @param cursor The cursor that contains the list to be displayed
     */
    public void showData(Cursor cursor){
        mCursor = cursor;
        mBookList.setVisibility(View.VISIBLE);
        mAdapter = new BookListAdapter(MainActivity.this, mCursor);
        mBookList.setAdapter(mAdapter);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(this.getString(R.string.query_key), query);
        outState.putString(this.getString(R.string.searhedBy_key), searchBy);
    }

    /**
     * Triggers when the user selected an item from the spinner.
     * @param parent
     * @param view
     * @param position The position of the element that was selected.
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Extract the selected item from the spinner
        searchBy = parent.getItemAtPosition(position).toString();
        //apply the filter tot the displayed data
        showData(DatabaseUtils.getCursorFromDBySearch(query, searchBy));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
    }

    private class Downloader extends AsyncTask<String,Void,Void> {
        /**
         * Show a toast to notify the user that the data is loading.
         */
        @Override
        protected void onPreExecute(){
            Toast toast = Toast.makeText(MainActivity.this, "Updating content", Toast.LENGTH_SHORT);
            toast.show();
        }
        /**
         * extract the list of books from the Re-Book-IT website and convert it to a json format.
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
                    DatabaseUtils.saveToDB(MainActivity.this, mCursor);
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
                Toast toast = Toast.makeText(MainActivity.this, "Content has been updated", Toast.LENGTH_SHORT);
                toast.show();
                showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
            }
            else{
                Toast toast = Toast.makeText(MainActivity.this, "Failed updating content", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
