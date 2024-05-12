package com.bezruk.qrcodebarcode.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.bezruk.qrcodebarcode.utility.AppUtils;
import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.utility.ResultOfTypeAndValue;

import java.util.ArrayList;

/**
 * Created by Bezruk on 16/10/18.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mList, mDateList;

    private ClickListener clickListener;

    public HistoryAdapter(Context context, ArrayList<String> list, ArrayList<String> dateList) {
        this.mContext = context;
        this.mList = list;
        this.mDateList = dateList;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view, viewType);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView result, date;
        private ImageButton deleteButton;
        private ImageView actionIcon;
        private LinearLayout item_history_layout;

        public ViewHolder(View v, int viewType) {
            super(v);
            result = (TextView) v.findViewById(R.id.result);
            date = (TextView) v.findViewById(R.id.date);
            deleteButton = (ImageButton) v.findViewById(R.id.deleteButton);
            actionIcon = (ImageView) v.findViewById(R.id.actionIcon);
            item_history_layout = (LinearLayout) v.findViewById(R.id.item_history_layout);

            item_history_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClicked(getLayoutPosition());
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onDeleteClicked(getLayoutPosition());
                    }
                }
            });

            item_history_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemLongClicked(getLayoutPosition());
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d("ARRRR",mList.toString());
        Log.d("ARRRR",mDateList.toString());
        String result = mList.get(position);
        String resultDate = mDateList.get(position);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.result.setText(Html.fromHtml(result, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.result.setText(Html.fromHtml(result));
        }*/


       // int type = AppUtils.getResourceType(result);
        ResultOfTypeAndValue resultValues = AppUtils.getResourceType(result);
        String finalResult = resultValues.getValue();
        String[] titleOfResult = finalResult.split("\n");
        if(titleOfResult[0].length()>26){
            finalResult = titleOfResult[0].substring(0,25)+"...";
        } else {
            finalResult = titleOfResult[0];
        }


        if (resultValues.getType() == Constants.TYPE_TEXT) {
            holder.actionIcon.setImageResource(R.drawable.ic_plain_text);
        } else if (resultValues.getType() == Constants.TYPE_WEB) {
            holder.actionIcon.setImageResource(R.drawable.ic_web);
        } else if (resultValues.getType() == Constants.TYPE_YOUTUBE) {
            holder.actionIcon.setImageResource(R.drawable.ic_video);
        } else if (resultValues.getType() == Constants.TYPE_PHONE) {
            holder.actionIcon.setImageResource(R.drawable.ic_call);
        } else if (resultValues.getType() == Constants.TYPE_EMAIL) {
            holder.actionIcon.setImageResource(R.drawable.ic_email);
        } else if (resultValues.getType() == Constants.TYPE_BARCODE) {
            holder.actionIcon.setImageResource(R.drawable.ic_barcode);
        } else if (resultValues.getType() == Constants.TYPE_WIFI) {
            holder.actionIcon.setImageResource(R.drawable.ic_wifi);
        } else if (resultValues.getType() == Constants.TYPE_SMS) {
            holder.actionIcon.setImageResource(R.drawable.ic_sms);
        } else if (resultValues.getType() == Constants.TYPE_VCARD) {
            holder.actionIcon.setImageResource(R.drawable.ic_contact);
        } else if (resultValues.getType() == Constants.TYPE_GEO) {
            holder.actionIcon.setImageResource(R.drawable.ic_location);
        } else {
            holder.actionIcon.setImageResource(R.drawable.ic_plain_text);
        }

        holder.result.setText(finalResult);
        holder.date.setText(resultDate);

        //holder.result.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface ClickListener {
        public void onDeleteClicked(int position);
        public void onItemClicked(int position);
        public void onItemLongClicked(int position);
    }
}