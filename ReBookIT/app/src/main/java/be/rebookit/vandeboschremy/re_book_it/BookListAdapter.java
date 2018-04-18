package be.rebookit.vandeboschremy.re_book_it;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The adapter for the recyclerview.
 * Created by Vandebosch Remy on 28/02/2018.
 */

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookListAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    /**
     * The contructor for the adapter.
     * @param context The context.
     * @param cursor The cursor for which to create the adapter.
     */
    public BookListAdapter(Context context, Cursor cursor){
        this.mContext = context;
        this.mCursor = cursor;
        Log.i("BookListAdapter", String.valueOf(mCursor.getCount()));
    }

    /**
     * Create the viewholder for the recyclerview.
     * @param parent The parent.
     * @param viewType The viewType of the viewholder.
     * @return A BookListAdapterViewHolder for the recyclerview.
     */
    @Override
    public BookListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.book_recycler_view_list_item, parent, false);
        return new BookListAdapterViewHolder(view);
    }

    /**
     * Bind the content to the viewholder.
     * @param holder The viewholder to bind the content to.
     * @param position The position of the adapter in the cursor.
     */
    @Override
    public void onBindViewHolder(BookListAdapterViewHolder holder, int position) {
        if(!mCursor.moveToPosition(position)) return;

        String title, price;
        int quality;
        title = mCursor.getString(mCursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_TITLE));
        price = mCursor.getString(mCursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_PRICE));
        quality = mCursor.getInt(mCursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_QUALITY));

        holder.tv.setText(title);
        holder.tv_priceTag.setText(price+" €");

        // Depending on the quality load a different color smiley
        if(quality <= 30){
            holder.qualitytag.setImageResource(R.drawable.yellow_smiley);
        }
        else if(quality > 30 && quality <= 50){
            holder.qualitytag.setImageResource(R.drawable.grey_smiley);
        }
        else if(quality > 50 && quality <= 70){
            holder.qualitytag.setImageResource(R.drawable.red_smiley);
        }
        else if(quality >= 80){
            holder.qualitytag.setImageResource(R.drawable.green_smiley);
        }

        holder.itemView.setTag(mCursor.getString(mCursor.getColumnIndex("title")));
    }

    /**
     * Return the numbre items in the cursor.
     * @return The number of items in the cursor.
     */
    @Override
    public int getItemCount() {
        if(mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * The viewholder for the recyclerview that is going to hold the content.
     */
    class BookListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tv;
        private TextView tv_priceTag;
        private ImageView qualitytag;

        /**
         * The contructor for the viewholder.
         * @param itemView The view.
         */
        public BookListAdapterViewHolder(View itemView){
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_bookTitle);
            tv_priceTag = (TextView) itemView.findViewById(R.id.tv_priceTag);
            qualitytag = (ImageView) itemView.findViewById(R.id.qualitytag);

            itemView.setOnClickListener(this);
        }

        /**
         * Triggers if there is clicked on a viewholder.
         * @param v The view that is clicked on.
         */
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition(); // get the position of the adapter.
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(mContext.getString(R.string.row_id_key), pos); // provide the adapterposition.
            intent.putExtra(mContext.getString(R.string.query_key), MainActivity.getQuery()); // provide the query that was searched by.
            intent.putExtra(mContext.getString(R.string.searhedBy_key), MainActivity.getSearchBy()); // provide the state of the spinner.
            mContext.startActivity(intent);
        }
    }
}
