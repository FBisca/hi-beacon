package br.com.hive.beacon.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.hive.beacon.R;
import br.com.hive.hibeacon.core.model.Device;

/**
 * Created by FBisca on 17/09/2015.
 */
public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {

    public static final int VT_BEACON = 0, VT_LOADING = 1, VT_EMPTY = 2;

    private List<Object> mObjects = new ArrayList<>();
    private AdapterListener mListener;

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            if (mObjects.isEmpty()) {
                showEmpty();
            }
        }
    };

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        registerAdapterDataObserver(mDataObserver);
        if (mObjects.isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterAdapterDataObserver(mDataObserver);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == VT_BEACON) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beacon, parent, false);
        } else if (viewType == VT_LOADING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
        } else if (viewType == VT_EMPTY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false);
        }

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder.getItemViewType() == VT_BEACON) {
            Device device = (Device) mObjects.get(position);

            holder.mTxtBeaconName.setText("Nome: " + device.getName());
            holder.mTxtMajor.setText("Major: " + String.valueOf(device.getMajor()));
            holder.mTxtMinor.setText("Minor: " + String.valueOf(device.getMinor()));
            holder.mTxtRssi.setText("RSSI: " + String.valueOf(device.getRssi()));
            holder.mTxtUUID.setText("UUID: " + device.getUUID().toString());
            holder.mTxtDistance.setText("Distância: " + device.getDistance());
        }

    }

    public void showEmpty() {
        mObjects.add(new Gap(VT_EMPTY));
        notifyItemInserted(0);
    }

    public void showLoading(boolean show) {
        Iterator<Object> iterator = mObjects.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof Gap
                    && ((Gap) next).viewType == VT_LOADING) {
                iterator.remove();
                notifyItemRemoved(i);
            }
            i++;
        }

        if (show) {
            mObjects.add(0, new Gap(VT_LOADING));
            notifyItemInserted(0);
        } else {
            if (mObjects.isEmpty()) {
                showEmpty();
            }
        }
    }

    public void addDevice(Device device) {
        removeEmpty();

        mObjects.add(device);
        notifyItemInserted(mObjects.size() - 1);
    }

    private void removeEmpty() {
        Iterator<Object> iterator = mObjects.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof Gap
                    && ((Gap) next).viewType == VT_EMPTY) {
                iterator.remove();
                notifyItemRemoved(i);
            }
            i++;
        }
    }

    public void updateDevice(Device device) {
        removeEmpty();

        Iterator<Object> iterator = mObjects.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof Device) {
                Device listDevice = (Device) next;
                if (listDevice.getAddress().equals(device.getAddress())) {
                    iterator.remove();
                    mObjects.add(i, device);
                    notifyItemChanged(i);
                    break;
                }
            }
            i++;
        }
    }

    public void removeDevice(Device device) {
        Iterator<Object> iterator = mObjects.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof Device) {
                Device listDevice = (Device) next;
                if (listDevice.getAddress().equals(device.getAddress())) {
                    iterator.remove();
                    notifyItemRemoved(i);
                    break;
                }
            }
            i++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = mObjects.get(position);
        if (obj instanceof Gap) {
            return ((Gap) obj).viewType;
        } else if (obj instanceof Device) {
            return VT_BEACON;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    public void setListener(AdapterListener mListener) {
        this.mListener = mListener;
    }

    public boolean isDeviceAdded(Device device) {
        for (Object obj : mObjects) {
            if (obj instanceof Device) {
                Device onList = (Device) obj;
                if (onList.getAddress().equals(device.getAddress())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class Gap {
        private int viewType;

        public Gap(int viewType) {
            this.viewType = viewType;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTxtBeaconName, mTxtMajor, mTxtMinor, mTxtUUID, mTxtRssi, mTxtDistance;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == VT_BEACON) {
                mTxtBeaconName = (TextView) itemView.findViewById(R.id.txt_beacon_name);
                mTxtMajor = (TextView) itemView.findViewById(R.id.txt_major);
                mTxtMinor = (TextView) itemView.findViewById(R.id.txt_minor);
                mTxtUUID = (TextView) itemView.findViewById(R.id.txt_uuid);
                mTxtRssi = (TextView) itemView.findViewById(R.id.txt_rssi);
                mTxtDistance = (TextView) itemView.findViewById(R.id.txt_distance);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onDeviceClick((Device) mObjects.get(getAdapterPosition()));
                        }
                    }
                });
            } else if (viewType == VT_EMPTY) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onEmptyClick();
                        }
                    }
                });
            }
        }
    }

    public interface AdapterListener {
        void onEmptyClick();
        void onDeviceClick(Device device);
    }
}
