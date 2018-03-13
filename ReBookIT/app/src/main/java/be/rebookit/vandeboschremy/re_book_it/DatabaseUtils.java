package be.rebookit.vandeboschremy.re_book_it;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Vandebosch Remy on 5/03/2018.
 */

public class DatabaseUtils {

    private static SQLiteDatabase db;
    private static BookDataSheetDBHelper helper;

    public static void saveToDB(Context context, Cursor cursor){
        helper = new BookDataSheetDBHelper(context);
        db = helper.getWritableDatabase();
        if(getCursorFromDB(context) != null) helper.onUpgrade(db, 1, 1);
        Log.i("DatabaseUtils", String.valueOf(cursor.getCount()));

        ContentValues values = new ContentValues();
        while(cursor.moveToNext()){
            values.put(BookDataSheet.DataTable.COLUMN_NAME_TITLE, cursor.getString(cursor.getColumnIndex("title")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_AUTHORS, cursor.getString(cursor.getColumnIndex("authors")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_COUNT, cursor.getString(cursor.getColumnIndex("count")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_COURSES, cursor.getString(cursor.getColumnIndex("courses")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_COURSESFULLNAME, cursor.getString(cursor.getColumnIndex("courses_full_name")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_CREATEDAT, cursor.getString(cursor.getColumnIndex("created_at")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_EDITION, cursor.getString(cursor.getColumnIndex("edition")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_IMAGEURL, cursor.getString(cursor.getColumnIndex("image_url")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_INSTITUTIONS, cursor.getString(cursor.getColumnIndex("institutions")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_SUBTITLE, cursor.getString(cursor.getColumnIndex("subtitle")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_PRICE, cursor.getString(cursor.getColumnIndex("price")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_QUALITY, cursor.getString(cursor.getColumnIndex("quality")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_ISBN, cursor.getString(cursor.getColumnIndex("isbn")));

            long newRowId = db.insert(BookDataSheet.DataTable.TABLE_NAME, null, values);
            if(newRowId == -1) Log.e("DataBaseUtils", "error writing to database");
        }
        Log.i("DatabaseUtils", "database created ");
    }

    public static Cursor getCursorFromDB(Context context){
        helper = new BookDataSheetDBHelper(context);
        db = helper.getReadableDatabase();
        Log.i("DataBaseUtils", "read data from database");
        return db.query(BookDataSheet.DataTable.TABLE_NAME, null, null, null, null, null, null);
    }

    public static Cursor getCursorFromDBySearch(String searchterm){
        searchterm = "%" + searchterm + "%";
        String where = BookDataSheet.DataTable.COLUMN_NAME_TITLE + " LIKE ?";
        String[]whereArgs = new String[]{searchterm};

        return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, whereArgs, null, null, null);
    }


    public String sortByDate(){
        return null;
    }

    public String sortByPrice(){
        return null;
    }

    public String sortByQuality(){
        return null;
    }

    public String sortByTitle(){
        return null;
    }
}
