package edu.dartmouth.cs.dtutor;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;

/**
 * Container Activity for DTutor fragments.
 */
public class MainActivity extends Activity {

	private static final String TAG = Globals.TAG + ".MainActivity";
	private static final String TAB_KEY_INDEX = "tab_key";

	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = getApplicationContext();

		// ActionBar
		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// create new tabs and and set up the titles of the tabs
		ActionBar.Tab mHomeTab = actionbar.newTab().setText(getString(R.string.ui_tabname_home));
		ActionBar.Tab mSearchTab = actionbar.newTab().setText(getString(R.string.ui_tabname_search));
		ActionBar.Tab mProfileTab = actionbar.newTab().setText(getString(R.string.ui_tabname_profile));

		// create the fragments
		Fragment mHomeFragment = new HomeFragment();
		Fragment mSearchFragment = new SearchFragment();
		Fragment mProfileFragment = new ProfileFragment();

		// bind the fragments to the tabs - set up tabListeners for each tab
		mHomeTab.setTabListener(new DTutorTabsListener(mHomeFragment, getApplicationContext()));
		mSearchTab.setTabListener(new DTutorTabsListener(mSearchFragment, getApplicationContext()));
		mProfileTab.setTabListener(new DTutorTabsListener(mProfileFragment, getApplicationContext()));

		// add the tabs to the action bar
		actionbar.addTab(mHomeTab);
		actionbar.addTab(mSearchTab);
		actionbar.addTab(mProfileTab);

		//set a default for the value of the toggle switch
		//this can be removed later when the app has been fully developed.
		Globals.currentMode="TUTOR";

		// Get the email of the user that is logged in
		if(getIntent().getExtras()!=null) {
			String email = getIntent().getExtras().getString("email");

			Members member = new Members();
			member.setEmail(email);

			MemberDbHelper dbHelper = new MemberDbHelper(getApplicationContext());
			dbHelper.addProfile(member);
		}

		// restore navigation pane
		if(savedInstanceState != null) {
			actionbar.setSelectedNavigationItem(savedInstanceState.getInt(TAB_KEY_INDEX, 0));
		}

	}

	@Override
	protected void onResume() {
		doCheckLogin();
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		MemberDbHelper dbHelper = new MemberDbHelper(mContext);
		dbHelper.deleteAll();
	}
	/**
	 * Check if user is logged in -- if not, redirect to login screen!
	 */
	private void doCheckLogin() {
		//        boolean SKIP_LOGIN_FOR_DEBUG = true;
		//        if(SKIP_LOGIN_FOR_DEBUG)
		//            return;

		MemberDbHelper dbHelper = new MemberDbHelper(mContext);
		if(!dbHelper.doesProfileExist()) { // user is not logged in
			Log.w(TAG, "no user logged in -- redirecting to login.");

			// Redirect to login...
			Intent login = new Intent(mContext, LoginActivity.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);

			// Closing main activity
			finish();
		} else { //the user is already logged in
			Log.d("arvi", "why are we here");
			Members member = dbHelper.getProfile();
			Log.w(TAG, "user is logged in with email: " + member.getEmail());
			//            AuthDatabaseHelper userDb = AuthDatabaseHelper.getInstance(mContext);
			//            Log.w(TAG, "user logged in: " + userDb.getUserDetailsStr());

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_logout:
			Log.d(TAG, getString(R.string.ui_menu_logout));

			MemberDbHelper dbHelper = new MemberDbHelper(mContext);
			if(dbHelper.doesProfileExist()) { // if the user is logged in
				dbHelper.deleteProfile();
				doCheckLogin();
			}

			return true;
		case R.id.menuitem_book_room:
			Log.d(TAG, getString(R.string.ui_menu_book_room));

			Intent intent = new Intent(mContext, MapDisplayActivity.class);
			startActivity(intent);

			return true;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TAB_KEY_INDEX, getActionBar().getSelectedNavigationIndex());
	}
}

/**
 * TabListenr class for managing user interaction with the ActionBar tabs.
 */
class DTutorTabsListener implements ActionBar.TabListener {

	private static final String TAG = "DTutorTabsListener";

	public Fragment fragment;
	public Context context;

	public DTutorTabsListener(Fragment fragment, Context context) {
		this.fragment = fragment;
		this.context = context;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "tab reselected!");
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "tab selected!");
		ft.replace(R.id.fragment_container, fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "tab unselected!");
		ft.remove(fragment);
	}

}