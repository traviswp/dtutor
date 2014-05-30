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

public class QueryDbForCourse extends AsyncTask<String, String, JSONArray>
{
	public AsyncResponse asyncResponse = null;
	private Context context;
	private String courseDept = null;
	private int courseNum = -1;
	private int mType = -1; //member type of the user navigating the app
	private JSONArray jsonArray = null; //results of query will be put into a jsonStr array

	public QueryDbForCourse(Context context, int mType, String courseDept, int courseNum) {
		this.context = context;
		this.courseDept = courseDept;
		this.courseNum = courseNum;
		this.mType = mType;
	}

	@Override
	protected JSONArray doInBackground(String... params) {
		String url_select = null;
		String result = null;
		String course = courseDept; // full course name (ex. COSC 50)

		if((courseDept!=null)) { // either query on the course dept only or on department plus number

			if(courseNum != -1) // if the course number was specified, append it
				course +=  "%20" + String.valueOf(courseNum); 

		}
		else if(courseDept==null) { //the course hasn't been specified (we don't support search only based on course number
			if(courseNum != -1) // if the course number was specified, append it
				course = String.valueOf(courseNum); 
		}

		url_select = "http://tutord.comlu.com/query_courses.php?course=" + course + "&mType=" + String.valueOf(mType);
		
		Log.d("arvi", "uri_select "+url_select);
		
		if(url_select == null || course==null || !(mType==0 || mType==1)) {
			Log.d("NO", "THE QUERY DID NOT RESULT IN ANYTHING");
			return null;
		}

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
