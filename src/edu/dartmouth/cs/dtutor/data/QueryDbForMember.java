package edu.dartmouth.cs.dtutor.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class QueryDbForMember extends AsyncTask<String, String, JSONArray>
{
	public AsyncResponse asyncResponse = null;
	private Context context;
	private String email = null;
	private JSONArray jsonArray = null; //results of query will be put into a jsonStr array

	public QueryDbForMember(Context context, String email) {
		this.context = context;
		this.email = email;
	}

	@Override
	protected JSONArray doInBackground(String... params) {
		String url_select = null;
		String result = null;

		url_select = "http://tutord.comlu.com/query_member.php?email=" + email;
		
		Log.d("arvi", "uri_select "+url_select);

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url_select);

		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

		InputStream is = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(param));

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();

			//read content
			is =  httpEntity.getContent();

		} catch (Exception e) {

			Log.e("log_tag", "Error in http connection "+e.toString());
		}

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while((line=br.readLine())!=null)
			{
				sb.append(line+"\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("log_tag", "Error converting result "+e.toString());
		}


		// parse the results string into a JSON object
		if(result == null)
			return null;
		try {
			jsonArray = new JSONArray(result);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return the query results as a JSON object
		return jsonArray;

	}
	protected void onPostExecute(JSONArray result) {
		try {
			asyncResponse.processFinish(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
