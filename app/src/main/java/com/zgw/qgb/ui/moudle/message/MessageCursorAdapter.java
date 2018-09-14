package com.zgw.qgb.ui.moudle.message;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgw.qgb.base.adapter.RecycleViewCursorAdapter;

/**
 * Name:MessageCursorAdapter
 * Created by Tsinling on 2018/2/26 9:56.
 * description:
 */

public class MessageCursorAdapter extends RecycleViewCursorAdapter<MessageCursorAdapter.ViewHolder> {
    //BulkCursorToCursorAdapter

    public MessageCursorAdapter(Cursor c) {
        super(c);
    }
    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        //Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        if (cursor == null) {
            return;
        }
        int contactsId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));

        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        // query the corresponding phone number via contact_id, noting the relation between the
        // table {@link ContactsContract.Contacts} and the table {@link ContactsContract.CommonDataKind}
        Cursor c = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " LIKE ?",
                new String[]{String.valueOf(contactsId)},
                null
        );

        String phoneNumber = "";
        try {
            if (c.moveToFirst()) {
                int numberColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                // a contact may have may phone number, so we need to fetch all. otherwise, you
                // can use{@link ContactsContract.CommonDataKinds.phone.TYPE.*} to limit the query
                //condition.
                do {
                    phoneNumber += c.getString(numberColumn) + ",";
                } while (c.moveToNext());

            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        holder.text1.setText(phoneNumber);
        holder.text2.setText(name);
    }

    @Override
    protected void onContentChanged() {

    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;

        ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}



