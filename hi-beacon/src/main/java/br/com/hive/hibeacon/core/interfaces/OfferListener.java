package br.com.hive.hibeacon.core.interfaces;

import java.util.List;

import br.com.hive.hibeacon.core.exceptions.ApiException;
import br.com.hive.hibeacon.core.model.Offer;
import br.com.hive.hibeacon.core.model.Place;

/**
 * Created by FBisca on 21/09/2015.
 */
public interface OfferListener {

    void onOffersObtained(Place place, List<Offer> offers);
    void onError(ApiException error);
}
