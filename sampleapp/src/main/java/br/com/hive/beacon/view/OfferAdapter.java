package br.com.hive.beacon.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.hive.beacon.R;
import br.com.hive.hibeacon.core.model.Offer;

/**
 * Created by FBisca on 17/09/2015.
 */
public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    public static final int VT_OFFER = 0;

    private List<Object> mObjects = new ArrayList<>();


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == VT_OFFER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        }

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder.getItemViewType() == VT_OFFER) {
            Offer offer = (Offer) mObjects.get(position);

            holder.mTxtMessage.setText(offer.getMessage());
        }

    }

    public void setItems(List<Offer> offers) {

        int count = mObjects.size();
        mObjects.clear();
        notifyItemRangeRemoved(0, count);

        if (offers == null) {
            return;
        }

        mObjects.addAll(offers);
        notifyItemRangeInserted(0, offers.size());
    }

    @Override
    public int getItemViewType(int position) {
        return VT_OFFER;
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTxtMessage;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == VT_OFFER) {
                mTxtMessage = (TextView) itemView.findViewById(R.id.txt_message);
            }

        }
    }

}
