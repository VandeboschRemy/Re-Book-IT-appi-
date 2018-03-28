package be.rebookit.vandeboschremy.re_book_it;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private TextView title,subtitle,authors,edition,courses,isbn,institutions,price,count,quality;
    private Button buyButton;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if(intent.hasExtra(this.getString(R.string.row_id_key))){
            pos = intent.getIntExtra(this.getString(R.string.row_id_key), 0);
        }
        if(intent.hasExtra(this.getString(R.string.query_key))){
            String query = intent.getStringExtra(this.getString(R.string.query_key));
            String searchBy = intent.getStringExtra(this.getString(R.string.searhedBy_key));
            if(query != null){
                cursor = DatabaseUtils.getCursorFromDBySearch(query, searchBy);
            }
            else{
                cursor = DatabaseUtils.getCursorFromDB(this);
            }
        }

        bookImage = (ImageView) findViewById(R.id.bookImage);
        bookImage.setImageResource(R.drawable.default_image);

        title = (TextView) findViewById(R.id.tv_TitleDetail);
        subtitle = (TextView) findViewById(R.id.tv_subtitle);
        authors = (TextView) findViewById(R.id.tv_authors);
        edition = (TextView) findViewById(R.id.tv_edition);
        courses = (TextView) findViewById(R.id.tv_courses);
        isbn = (TextView) findViewById(R.id.tv_ISBN);
        institutions = (TextView) findViewById(R.id.tv_institutions);
        price = (TextView) findViewById(R.id.tv_price);
        count = (TextView) findViewById(R.id.tv_count);
        quality = (TextView) findViewById(R.id.tv_quality);
        buyButton = (Button) findViewById(R.id.buy_button);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse("https://rebookit.be/login");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

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

        String authorString = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_AUTHORS));
        authorString = authorString.replace("[","").replace("]","");
        authors.setText("Authors: "+ authorString);

        edition.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_EDITION)));
        if(!cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSES)).equals("[]")){
            String coursesString = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSES));
            coursesString = coursesString.replace("]", "").replace("[", "");
            courses.setText("Courses: "+ coursesString);

            String institutionsString = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_INSTITUTIONS));
            institutionsString = institutionsString.replace("]","").replace("[","");
            institutions.setText("Universities: "+ institutionsString);
        }
        else{
            courses.setText("No courses found");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    public void share(MenuItem item){
        String mimeType = "text/plain";
        String title = "Share the book with all your friends";
        String text = "Just found this awesome book:\n"
                        +this.title.getText() + "\n"
                        +this.authors.getText() + "\n"
                        +this.isbn.getText() + "\n"
                        +this.quality.getText() + "\n"
                        +"Shared with the Re-Book IT app.";
        Intent intent = ShareCompat.IntentBuilder.from(this)
                                                    .setChooserTitle(title)
                                                    .setType(mimeType)
                                                    .setText(text)
                                                    .getIntent();
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
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
            if(bitmap == null) return;
            else{
                Log.i("DetailActivity", "image loaded");
                bookImage.setImageBitmap(bitmap);
            }

        }
    }
}
