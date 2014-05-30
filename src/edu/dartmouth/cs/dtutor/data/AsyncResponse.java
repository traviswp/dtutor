package edu.dartmouth.cs.dtutor.data;

import org.json.JSONArray;
import org.json.JSONException;

public interface AsyncResponse {

	void processFinish(JSONArray result) throws JSONException;

	void processFinish(Integer result);

}
