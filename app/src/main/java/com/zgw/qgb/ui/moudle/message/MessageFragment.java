package com.zgw.qgb.ui.moudle.message;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.RecycleViewCursorAdapter;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.message.contract.MessageContract;
import com.zgw.qgb.ui.moudle.message.presenter.MessagePresenter;

import butterknife.BindView;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;

public class MessageFragment
        extends BaseMainFragment<MessagePresenter>
        implements MessageContract.IMessageView, LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private String title;

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
  /*      RxPermissions rxPermissions = new RxPermissions(getmActivity());
        rxPermissions.request( Manifest.permission.READ_CONTACTS)
                .subscribe(new BaseObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean){
                            initLoader();
                        }else{
                            ToastUtils.showNormal("读取联系人的权限申请被拒绝");
                        }
                    }
                });
*/

    }


    private void initLoader() {
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    private void setAdapter() {


        mAdapter = new MessageCursorAdapter(null);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //mAdapter.changeCursor(null);
        LoaderManager.getInstance(this).destroyLoader(0);
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
        if (loader!=null && loader.getId() == 0) {
            mAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}



