package com.zgw.qgb.ui.moudle.message;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.RecycleViewCursorAdapter;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.message.contract.MessageContract;
import com.zgw.qgb.ui.moudle.message.presenter.MessagePresenter;

import butterknife.BindView;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;

public class MessageFragment extends BaseMainFragment<MessagePresenter>
        implements MessageContract.IMessageView,LoaderManager.LoaderCallbacks<Cursor>
{

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private String title;

    public MessageFragment() {
    }

    public static MessageFragment newInstance(String title) {
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(Bundler.start()
                .put(EXTRA, title).end());
        return fragment;
    }


    @Override
    public void initData() {
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA);
        }
    }



    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_message;
    }

    @Override
    protected MessagePresenter getPresenter() {
        return new MessagePresenter(this);
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);

        setAdapter();

        getActivity().getSupportLoaderManager().initLoader(0, null, this);

    }

    private void setAdapter() {


        mAdapter = new RecycleViewCursorAdapter(getContext(),null) {
            /*@RestrictTo(LIBRARY_GROUP)
            protected int[] mFrom;*/
            Context mContext;
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                mContext = parent.getContext();
                View v = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {

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

                ((ViewHolder) holder).text1.setText(phoneNumber);
                ((ViewHolder) holder).text2.setText(name);
            }

            @Override
            protected void onContentChanged() {

            }

          /*  private void findColumns(Cursor c, String[] from) {
                if (c != null) {
                    int i;
                    int count = from.length;
                    if (mFrom == null || mFrom.length != count) {
                        mFrom = new int[count];
                    }
                    for (i = 0; i < count; i++) {
                        mFrom[i] = c.getColumnIndexOrThrow(from[i]);
                    }
                } else {
                    mFrom = null;
                }
            }*/
        };


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        //mAdapter.changeCursor(null);
    }

    // These are the Contacts rows that we will retrieve.
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.CONTACT_STATUS,
            ContactsContract.Contacts.CONTACT_PRESENCE,
            ContactsContract.Contacts.PHOTO_ID,
            ContactsContract.Contacts.LOOKUP_KEY,
    };

    private RecycleViewCursorAdapter mAdapter;
    private String mCurFilter;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
        if (mCurFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mCurFilter));
        } else {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        String selection = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";

        return new CursorLoader(getContext(),
                baseUri,
                CONTACTS_SUMMARY_PROJECTION,
                selection,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            mAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}


class ViewHolder extends RecyclerView.ViewHolder {
    TextView text1;
    TextView text2;
    public ViewHolder(View itemView) {
        super(itemView);
        text1 = itemView.findViewById(android.R.id.text1);
        text2 = itemView.findViewById(android.R.id.text2);
    }
}

