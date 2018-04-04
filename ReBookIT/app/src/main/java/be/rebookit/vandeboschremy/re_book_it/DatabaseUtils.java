package be.rebookit.vandeboschremy.re_book_it;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by Vandebosch Remy on 5/03/2018.
 */

public class DatabaseUtils {

    private static SQLiteDatabase db;
    private static BookDataSheetDBHelper helper;
    private static Context mContext;
    private static String sortBy, maxPrice;

    public static void saveToDB(Context context, Cursor cursor){
        mContext = context;
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
            values.put(BookDataSheet.DataTable.COLUMN_NAME_PRICE, Float.valueOf(cursor.getString(cursor.getColumnIndex("price"))));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_QUALITY, cursor.getString(cursor.getColumnIndex("quality")));
            values.put(BookDataSheet.DataTable.COLUMN_NAME_ISBN, cursor.getString(cursor.getColumnIndex("isbn")));

            long newRowId = db.insert(BookDataSheet.DataTable.TABLE_NAME, null, values);
            if(newRowId == -1) Log.e("DataBaseUtils", "error writing to database");
        }
        Log.i("DatabaseUtils", "database created ");
    }

    public static Cursor getCursorFromDB(Context context){
        mContext = context;
        helper = new BookDataSheetDBHelper(context);
        db = helper.getReadableDatabase();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        sortBy = prefs.getString(context.getString(R.string.sort_by_key), "0");
        maxPrice = String.valueOf(prefs.getInt(context.getString(R.string.max_price_key), 150));

        Log.i("Database", sortBy);

        if(sortBy.equals("0")){
            return sortByDate();
        }
        else if(sortBy.equals("1")){
            return sortByPrice(Order.ASC);
        }
        else if(sortBy.equals("2")){
            return sortByPrice(Order.DESC);
        }
        else if(sortBy.equals("3")){
            return sortByTitle();
        }
        else if(sortBy.equals("4")){
            return sortByQuality(Order.ASC);
        }
        else if(sortBy.equals("5")){
            return sortByQuality(Order.DESC);
        }
        else return sortByDate();
    }

    public static Cursor getCursorFromDBySearch(String searchterm, String searchBy){
        searchterm = "%" + searchterm + "%";
        String where;
        String[] searchByArray = mContext.getResources().getStringArray(R.array.searchChoice);
        if(searchBy.equals(searchByArray[0])){
            where = BookDataSheet.DataTable.COLUMN_NAME_TITLE + " LIKE ? AND " + BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        }
        else if(searchBy.equals(searchByArray[1])){
            where = BookDataSheet.DataTable.COLUMN_NAME_AUTHORS + " LIKE ? AND " + BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        }
        else if(searchBy.equals(searchByArray[2])){
            where = BookDataSheet.DataTable.COLUMN_NAME_COURSES + " LIKE ? AND " + BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        }
        else if(searchBy.equals(searchByArray[3])){
            where = BookDataSheet.DataTable.COLUMN_NAME_ISBN + " LIKE ? AND " + BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        }
        else where = null;
        String[]whereArgs = new String[]{searchterm};

        return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, whereArgs, null, null, null);
    }


    public static Cursor sortByDate(){
        String where  = BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, null, null, null, BookDataSheet.DataTable.COLUMN_NAME_CREATEDAT+" DESC");
    }

    public static Cursor sortByPrice(Order order){
        String where  = BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        if(order.equals(Order.ASC)){
            return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, null, null, null, BookDataSheet.DataTable.COLUMN_NAME_PRICE+" ASC");
        }
        else if(order.equals(Order.DESC)){
            return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, null, null, null, BookDataSheet.DataTable.COLUMN_NAME_PRICE+" DESC");
        }
        return null;
    }

    public static Cursor sortByQuality(Order order){
        String where  = BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        if(order.equals(Order.ASC)){
            return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, null, null, null, BookDataSheet.DataTable.COLUMN_NAME_QUALITY+" ASC");
        }
        else if(order.equals(Order.DESC)){
            return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, null, null, null, BookDataSheet.DataTable.COLUMN_NAME_QUALITY+" DESC");
        }
        return null;
    }

    public static Cursor sortByTitle(){
        String where  = BookDataSheet.DataTable.COLUMN_NAME_PRICE + " <= " + maxPrice;
        return db.query(BookDataSheet.DataTable.TABLE_NAME, null, where, null, null, null, BookDataSheet.DataTable.COLUMN_NAME_TITLE+" ASC");
    }
}
