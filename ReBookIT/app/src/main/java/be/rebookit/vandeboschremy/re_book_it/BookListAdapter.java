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
 * Created by Vandebosch Remy on 28/02/2018.
 */

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookListAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private String searchedBy;

    public BookListAdapter(Context context, Cursor cursor, String searchedBy){
        this.mContext = context;
        this.mCursor = cursor;
        this.searchedBy = searchedBy;
        Log.i("BookListAdapter", String.valueOf(mCursor.getCount()));
    }

    @Override
    public BookListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.book_recycler_view_list_item, parent, false);
        return new BookListAdapterViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        if(mCursor == null) return 0;
        return mCursor.getCount();
    }

    class BookListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tv;
        private TextView tv_priceTag;
        private ImageView qualitytag;

        public BookListAdapterViewHolder(View itemView){
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_bookTitle);
            tv_priceTag = (TextView) itemView.findViewById(R.id.tv_priceTag);
            qualitytag = (ImageView) itemView.findViewById(R.id.qualitytag);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(mContext.getString(R.string.row_id_key), pos);
            intent.putExtra(mContext.getString(R.string.query_key), MainActivity.getQuery());
            intent.putExtra(mContext.getString(R.string.searhedBy_key), searchedBy);
            mContext.startActivity(intent);
        }
    }
}
