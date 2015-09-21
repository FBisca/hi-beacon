package br.com.hive.hibeacon.core.parsers;

import java.util.ArrayList;

import br.com.hive.hibeacon.core.model.AD;
import br.com.hive.hibeacon.utils.Utils;

/**
 * Created by FBisca on 21/09/2015.
 */
public abstract class BeaconParser {

    public abstract ArrayList<AD> parseScanRecord(byte[] scanRecord);

    public static class AltBeaconParser extends BeaconParser {

        @Override
        public ArrayList<AD> parseScanRecord(byte[] scanRecord) {
            ArrayList<AD> adList = new ArrayList<>();
            int pos = 0;
            int order = 1;
            while (pos < scanRecord.length
                    && scanRecord[pos] > 0) {
                byte[] adArray = Utils.subArray(scanRecord, pos, scanRecord[pos] + 1);
                adList.add(parseAD(adArray, order++));
                pos += scanRecord[pos] + 1;
            }
            return adList;
        }


        private AD parseAD(byte[] adArray, int order) {
            AD ad = new AD();
            ad.setData(Utils.subArray(adArray, 1, adArray[0]));
            if (order == 1) {
                ad.setType(AD.ADType.FLAG);
            } else if (order == 2){
                ad.setType(AD.ADType.DATA);

                if (adArray[0] >= 26) {
                    ad.setManufacterId( ((adArray[3] & 0xFF) << 8) | (adArray[2] & 0xFF) );
                    ad.setBeaconPrefix( ((adArray[4] & 0xFF) << 8) | (adArray[5] & 0xFF) );
                    ad.setUuid(Utils.getUUID(Utils.subArray(adArray, 6, 16)));
                    ad.setMajor( ((adArray[22] & 0xFF) << 8) | (adArray[23] & 0xFF) );
                    ad.setMinor( ((adArray[24] & 0xFF) << 8) | (adArray[25] & 0xFF) );
                    ad.setTxPower(adArray[26] & 0xFF);
                }

            } else {
                ad.setType(AD.ADType.UNKNOWN);
            }

            return ad;
        }

    }

}
