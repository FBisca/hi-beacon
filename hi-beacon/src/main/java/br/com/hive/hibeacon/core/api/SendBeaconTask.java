package br.com.hive.hibeacon.core.api;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.hive.hibeacon.core.model.Device;
import br.com.hive.hibeacon.core.model.Offer;
import br.com.hive.hibeacon.core.model.Place;
import br.com.hive.hibeacon.network.Network;

/**
 * Created by FBisca on 17/09/2015.
 */
public class SendBeaconTask extends AsyncTask<Device, Void, SendBeaconTask.Result> {

    protected String errorMessage;
    private volatile String mAppAccessToken;

    public SendBeaconTask(String appAccessToken) {
        this.mAppAccessToken = appAccessToken;
    }

    @Override
    protected SendBeaconTask.Result doInBackground(Device... params) {
        if (params.length <= 0) {
            errorMessage = "SendBeaconTask params is empty";
            return null;
        }

        Network.Response response = Network.create("http://ambev.beacons.hive.com.br/services/ws")
                .setMethod(Network.Method.POST)
                .addParameter("action", "getOffers")
                .addParameter("app_access_token", mAppAccessToken)
                .addParameter("beacon_uuid", params[0].getUUID().toString())
                .addParameter("beacon_bt_major", params[0].getMajor())
                .addParameter("beacon_bt_minor", params[0].getMinor())
                .addParameter("age_gate", "Y").request();

        if (response.getError() != null) {
            try {
                JSONObject obj = new JSONObject(response.getResponse());
                if (obj.getInt("error") == 1) {
                    errorMessage = obj.getString("msg");
                } else {
                    return parseResult(obj);
                }
            } catch (JSONException e) {
                errorMessage = "JSONException: " + e.getMessage();
                return null;
            }
        }

        errorMessage = "Communication error";
        return null;
    }

    private SendBeaconTask.Result parseResult(JSONObject obj) throws JSONException {
        Place place = new Place();
        List<Offer> offerList = new ArrayList<>();

        JSONObject pos = obj.optJSONObject("point_of_sale");
        if (pos != null) {
            place.setBeaconUUID(pos.getString("beacon_uuid"));
            place.setBeaconMajor(pos.getInt("beacon_bt_major"));
            place.setBeaconMinor(pos.getInt("beacon_bt_minor"));
            place.setName(pos.getString("name"));
            place.setStreet(pos.getString("street"));
            place.setDistrict(pos.getString("district"));
            place.setCity(pos.getString("city"));
            place.setState(pos.getString("state"));
        }

        JSONArray offers = obj.optJSONArray("offers");
        if (offers != null) {
            for (int i = 0; i < offers.length(); i++) {
                JSONObject offer = offers.getJSONObject(i);
                Offer offerObj = new Offer();
                offerObj.setMessage(offer.getString("offer_text"));
                offerList.add(offerObj);
            }
        }

        return new Result(place, offerList);
    }

    public class Result {
        private Place place;
        private List<Offer> offerList;

        public Result(Place place, List<Offer> offerList) {
            this.place = place;
            this.offerList = offerList;
        }

        public List<Offer> getOfferList() {
            return offerList;
        }

        public Place getPlace() {
            return place;
        }
    }
}
