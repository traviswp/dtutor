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
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;

public class InsertMemberDb extends AsyncTask<String, String, Integer>
{
	public AsyncResponse asyncResponse = null;
	private Context context;
	private String email= null;
	private String name = null;
	private String courses = null;
	private String about = null;
	private int mType = -1; //member type of the information we want to insert

	public InsertMemberDb(Context context, String email, String name, ArrayList<String> courses, String about, int mType) {
		this.context = context;
		this.email = email;
		this.name = name;
		this.courses = courses.toString();
		this.about = about;
		this.mType = mType;
	}

	@Override
	protected Integer doInBackground(String... params) {
		String url_select = null;
		int code = 0;
		String result = null;
		
		Log.d("arvi", "Tutor COURSES INSERT: " + courses);
		Log.d("arvi", "Tutor name INSERT: " + name);
		
//		courses = courses.substring(1, courses.length()-1);
//		courses = courses.trim().replaceAll(" ", "%20"); // replace all white space with %20
		String regex = "\\[|\\]";
		courses = courses.trim().replaceAll(" ", "%20"); // replace all white space with %20
		courses = courses.replaceAll(regex, "");
		
		about = about.trim().replaceAll(" ", "%20"); // replace all white space with %20
		about = about.replaceAll(regex, "");
		
		name = name.trim().replaceAll(" ", "%20");
		
		Log.d("arvi", "Tutor COURSES INSERT: " + courses);
		Log.d("arvi", "Tutor name INSERT: " + name);
		
		if (mType == -1) // User type has not been specified
			url_select = Globals.SERVER_URL + "/insert_member.php?email=" + email + "&name=" + name + "&mType=" + String.valueOf(mType);
		if (mType == 0) // User is a tutor
			url_select = Globals.SERVER_URL + "/insert_member.php?email=" + email + "&name=" + name + "&courses=\'" + courses + "\'&about=" + about + "&mType=" + String.valueOf(mType);
		if (mType == 1) // User is a tutor
			url_select = Globals.SERVER_URL + "/insert_member.php?email=" + email + "&name=" + name + "&courses=\'" + courses + "\'&about=" + about + "&mType=" + String.valueOf(mType);
		
		Log.d("arvi", "adding member to server db: "+url_select);

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
			return 0;
		
		try
		{
			JSONObject json_data = new JSONObject(result);
			code=(json_data.getInt("code"));
		}
		catch(Exception e)
		{
			Log.e("Failed: ", e.toString());
		}

		// return the a flag which indicates whether or not we inserted successfully
		return code;

	}
	protected void onPostExecute(Integer result) {
		asyncResponse.processFinish(result);
	}
}
