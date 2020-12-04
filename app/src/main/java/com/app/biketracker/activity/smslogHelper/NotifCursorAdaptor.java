package com.app.biketracker.activity.smslogHelper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.biketracker.R;

public class NotifCursorAdaptor extends CursorAdapter {
    Context context;

    public NotifCursorAdaptor(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        try{
            TextView time = view.findViewById(R.id.time);
            TextView date = view.findViewById(R.id.date);
            ImageView imgDelete = view.findViewById(R.id.imgDelete);
            TextView AppText = view.findViewById(R.id.app_text);

            int appTimeColumnIndex = cursor.getColumnIndex(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TIME);
            int appDateColumnIndex = cursor.getColumnIndex(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_DATE);
            int appTextColumnIndex = cursor.getColumnIndex(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TEXT);


            time.setText(String.format("Time: %s", cursor.getString(appTimeColumnIndex)));
            date.setText(String.format("Date: %s", cursor.getString(appDateColumnIndex)));
            String message = cursor.getString(appTextColumnIndex);

            if(message.contains("messages |")){
                String[] separated = message.split("\\|");
                AppText.setText(separated[1].trim());
            }else {
                AppText.setText(message);
            }

            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long currNotifId = cursor.getInt(cursor.getColumnIndex(NotificationsContract.NotifEntry._ID));
                    Uri currentNotifUri = ContentUris.withAppendedId(NotificationsContract.NotifEntry.CONTENT_URI, currNotifId);
                    int rowDeleted = context.getContentResolver().delete(currentNotifUri, null, null);

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
