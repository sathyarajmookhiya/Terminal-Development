package uk.co.transaxiom.android.txandroidlib.card;

import java.util.Arrays;

public class nCounterKey {
    public byte[] keyID;
    public byte[] key;

    public nCounterKey(byte[] pKeyID, byte[] pKey) {
        keyID = pKeyID;
        key = pKey;
    }

    public boolean MatchesWith(byte[] compare) {
        if ((keyID == null) || (compare == null)) {
            return false;
        }
        return Arrays.equals(compare, keyID);
    }

    public int Serialise(byte[] buffer, int pOffset) {
        System.arraycopy(keyID, 0, buffer, pOffset, keyID.length);
        pOffset += keyID.length;
        System.arraycopy(key, 0, buffer, pOffset, key.length);
        return pOffset + key.length;
    }
}
