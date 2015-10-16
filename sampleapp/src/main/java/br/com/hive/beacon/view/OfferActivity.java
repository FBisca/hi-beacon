package br.com.hive.beacon.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import br.com.hive.beacon.R;
import br.com.hive.hibeacon.core.interfaces.OfferListener;
import br.com.hive.hibeacon.core.model.Device;
import br.com.hive.hibeacon.core.model.Offer;
import br.com.hive.hibeacon.core.model.Place;

/**
 * Created by FBisca on 18/09/2015.
 */
public class OfferActivity extends AppCompatActivity implements OfferListener {

    public static final String ARG_DEVICE = "arg_device";
    public static final String SAMPLE_TOKEN = "95ac2da6a85c8ac3914fe22366380a27";

    private List<Offer> offerList = null;
    private OfferAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mTxtMsg;

    private Device mDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mDevice = getIntent().getParcelableExtra(ARG_DEVICE);
        } else {
            mDevice = savedInstanceState.getParcelable(ARG_DEVICE);
        }

        setContentView(R.layout.activity_offer);
        loadViews();
    }

    private void loadViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.offer_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mTxtMsg = (TextView) findViewById(R.id.txt_msg);

        if (mAdapter == null) {
            mAdapter = new OfferAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (offerList == null && mDevice != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mTxtMsg.setVisibility(View.GONE);
            mDevice.getOffers(SAMPLE_TOKEN, this);
        }
    }

    @Override
    public void onOffersObtained(Place place, List<Offer> offers) {
        mProgressBar.setVisibility(View.GONE);
        if (offers.isEmpty()) {
            mTxtMsg.setVisibility(View.VISIBLE);
            mTxtMsg.setText("Não há ofertas cadastradas");
        } else {
            mAdapter.setItems(offers);
        }
    }

    @Override
    public void onError(ApiException error) {
        if (error instanceof ServiceException) {
            mProgressBar.setVisibility(View.GONE);
            mTxtMsg.setVisibility(View.VISIBLE);
            mTxtMsg.setText(error.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_DEVICE, mDevice);
    }
}
