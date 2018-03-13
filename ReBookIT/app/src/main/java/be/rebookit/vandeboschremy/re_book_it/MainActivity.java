package be.rebookit.vandeboschremy.re_book_it;


import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String json;
    private RecyclerView mBookList;
    private BookListAdapter mAdapter;
    private Cursor mCursor;
    private static String query;
    private static boolean startedFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookList = (RecyclerView) findViewById(R.id.rv);
        mBookList.setLayoutManager(new LinearLayoutManager(this));
        query = null;

        if(DatabaseUtils.getCursorFromDB(MainActivity.this) != null){
            showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
        }
        if(!startedFlag) new Downloader().execute("https://rebookit.be/search");
        startedFlag = true;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

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

    public String convertToRightFormat(String text){
        String jsonFormat;
        String correctJsonFormat = "";

        int start = text.indexOf("[{");
        int end = text.indexOf("}]")+("}]").length();
        if(start == -1 || end == 1) return null;
        jsonFormat = text.substring(start, end);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showData(DatabaseUtils.getCursorFromDBySearch(query));
                MainActivity.this.query = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        menu.findItem(R.id.action_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                showData(DatabaseUtils.getCursorFromDB(MainActivity.this));
                MainActivity.this.query = null;
                return true;
            }
        });
        return true;
    }

    public static String getQuery(){
        return query;
    }

    public void showData(Cursor cursor){
        mBookList.setVisibility(View.VISIBLE);
        mCursor = cursor;
        mAdapter = new BookListAdapter(MainActivity.this, mCursor);
        mBookList.setAdapter(mAdapter);
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
