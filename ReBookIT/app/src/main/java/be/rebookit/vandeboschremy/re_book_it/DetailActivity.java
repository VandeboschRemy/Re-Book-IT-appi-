package be.rebookit.vandeboschremy.re_book_it;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Vandebosch Remy on 21/02/2018.
 */

public class DetailActivity extends AppCompatActivity{

    private int pos;
    private ImageView bookImage;
    private Cursor cursor;
    private TextView title,subtitle,authors,edition,courses,coursesFull,isbn,institutions,price,count,quality;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if(intent.hasExtra("ROW_ID")){
            pos = intent.getIntExtra("ROW_ID", 0);
        }
        if(intent.hasExtra("QUERY")){
            String query = intent.getStringExtra("QUERY");
            if(query != null){
                cursor = DatabaseUtils.getCursorFromDBySearch(query);
            }
            else{
                cursor = DatabaseUtils.getCursorFromDB(this);
            }
        }

        bookImage = (ImageView) findViewById(R.id.bookImage);
        title = (TextView) findViewById(R.id.tv_TitleDetail);
        subtitle = (TextView) findViewById(R.id.tv_subtitle);
        authors = (TextView) findViewById(R.id.tv_authors);
        edition = (TextView) findViewById(R.id.tv_edition);
        courses = (TextView) findViewById(R.id.tv_courses);
        coursesFull = (TextView) findViewById(R.id.tv_courses_full);
        isbn = (TextView) findViewById(R.id.tv_ISBN);
        institutions = (TextView) findViewById(R.id.tv_institutions);
        price = (TextView) findViewById(R.id.tv_price);
        count = (TextView) findViewById(R.id.tv_count);
        quality = (TextView) findViewById(R.id.tv_quality);

        showData(pos);
    }

    public void showData(int pos){
        cursor.moveToPosition(pos);

        title.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_TITLE)));
        if(!cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_SUBTITLE)).equals("")){
            subtitle.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_SUBTITLE)));
        }
        else{
            subtitle.setText("No subtitle found");
        }
        authors.setText("Authors: "+ cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_AUTHORS)));
        edition.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_EDITION)));
        if(!cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSES)).equals("[]")){
            courses.setText("Courses: "+ cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSES)));
            coursesFull.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSESFULLNAME)));
            institutions.setText("Universities: "+ cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_INSTITUTIONS)));
        }
        else{
            courses.setText("No courses found");
            coursesFull.setHeight(0);
            institutions.setText("No universities found");
        }
        isbn.setText("ISBN: "+ cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_ISBN)));
        price.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_PRICE))+"€");
        count.setText("Count: "+ cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COUNT)));
        quality.setText("Quality: "+ cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_QUALITY)) +"%");

        String url = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_IMAGEURL));
        Log.i("DetailAcitivy", url);

        new GetImage().execute("https://rebookit.be/" + url);
    }

    public class GetImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                Uri uri = Uri.parse(urls[0]);
                URL url = new URL(uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            Log.i("DetailActivity", "image loaded");
            bookImage.setImageBitmap(bitmap);
        }
    }
}
