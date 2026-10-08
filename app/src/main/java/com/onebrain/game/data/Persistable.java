package com.onebrain.game.data;

import org.json.JSONException;
import org.json.JSONObject;

/** Objects that can be written to and restored from the local save file. */
public interface Persistable {

    JSONObject toJson() throws JSONException;

    /** Must tolerate missing keys so old save files keep loading after updates. */
    void fromJson(JSONObject json);
}
