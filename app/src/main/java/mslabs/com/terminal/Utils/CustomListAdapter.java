package mslabs.com.terminal.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mslabs.com.terminal.R;
import uk.co.transaxiom.android.txandroidlib.terminal.RedeemedValue;

public class CustomListAdapter extends BaseAdapter {

    private List<RedeemedValue> mData = new ArrayList<RedeemedValue>();
    private List<String> mDataString = new ArrayList<String>();
    private LayoutInflater mInflater;
    Context context;

    public CustomListAdapter(Context context, List<RedeemedValue> data) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;


    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RedeemedValue getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.listviewdata, null);
            holder.textView = (TextView) convertView.findViewById(R.id.transction_data_date);
            holder.textView1 = (TextView) convertView.findViewById(R.id.transction_data_amount);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

      /*  Collections.sort(mData, new Comparator<RedeemedValue>() {
            public int compare(RedeemedValue first, RedeemedValue second)  {
                return first.getTimestamp().compareTo(second.getTimestamp());
            }
        });
*/

        holder.textView.setText(mData.get(position).getTimestamp());
        holder.textView1.setText(""+mData.get(position).getAmount());


        return convertView;
    }

    private static class ViewHolder {
        public TextView textView;
        public TextView textView1;

    }

}
