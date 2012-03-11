package de.m19r.cim.ui;

import jibe.sdk.client.apptoapp.AppToAppDatabaseConstants;
import jibe.sdk.client.apptoapp.AppToAppDatabaseConstants.Columns;
import jibe.sdk.client.apptoapp.Config;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.m19r.cim.CimIntents;
import de.m19r.cim.CimSalabimApplication;
import de.m19r.cim.R;

/**
 * This example demonstrates how to use the AppToApp content provider to obtain
 * all friends from my address book who have the same application installed on
 * their phones.
 * 
 * For the sake of keeping it simple, it does not perform the necessary authentication
 * with the Jibe cloud. How to authenticate is demonstrated in the examples for
 * LiveAudioConnection and LiveVideoConnection.
 */
public class FindFriendsActivity extends ListActivity {
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.getInstance().setAppToAppIdentifier(CimSalabimApplication.APP_ID, CimSalabimApplication.APP_SECRET);
        setupUi();
    }

	private void setupUi() {
        setContentView(R.layout.activity_friends_list);
        
        String[] appsProjection = { Columns.JibeUserApps.JIBE_USERID };
        String appsSelection = Columns.JibeUserApps.APP_ID + "=?";
        String[] appsSelectionArgs = { CimSalabimApplication.APP_ID };

        // cursor containing the jibe user ids of all contacts who have this app
        Cursor cursor = getContentResolver().query(AppToAppDatabaseConstants.JIBE_USER_APPS_CONTENT_URI, appsProjection, appsSelection, appsSelectionArgs, null);
        
        // create a string which can be used in an SQL IN clause
        String friendIdSet = createSqlQuerySet(cursor, cursor.getColumnIndex(Columns.JibeUserApps.JIBE_USERID));
        cursor.close();
        
        String[] friendsProjection = {Columns.JibeUsers._ID,
        						Columns.JibeUsers.NICKNAME,
        						Columns.JibeUsers.MSISDN};
        String friendsSelection = Columns.JibeUsers.JIBE_USERID + " IN " + friendIdSet;
        
        // cursor containing all nick names and msidns of users who have this app
        cursor = getContentResolver().query(AppToAppDatabaseConstants.JIBE_USERS_CONTENT_URI,
        					friendsProjection, friendsSelection, null, Columns.JibeUsers.NICKNAME + " ASC");
        startManagingCursor(cursor);
        
        String[] from = {Columns.JibeUsers.NICKNAME,
							Columns.JibeUsers.MSISDN};
        int[] to = {R.id.nick, R.id.msisdn};
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_element_friends_list, cursor, from, to);
        
        setListAdapter(adapter);
	}

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor c = (Cursor) getListAdapter().getItem(position);
		String msisdn = c.getString(2);
		Intent result = new Intent();
		result.putExtra(CimIntents.EXTRA_MSISDN, msisdn);
		setResult(RESULT_OK, result);
		
	}
	
	/**
	 * Creates a list of elements that can be used in an SQL statement, i.e. "(a, b, c, d, ...)".
	 * @param cursor A cursor containing the elements to be added to the set
	 * @param columnIndex Column index of the elements to be added.
	 * @return A string formatted in a form that can be used in an SQL statement
	 */
	private String createSqlQuerySet(Cursor cursor, int columnIndex) {
		if ((cursor == null) || (cursor.isClosed()) || (cursor.getCount() == 0)) {
			return "()";
		}
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		
		cursor.moveToFirst();
		String value = cursor.getString(columnIndex);
		builder.append(value);
		
		while (cursor.moveToNext()) {
			value = cursor.getString(columnIndex);
			if (!TextUtils.isEmpty(value)) {
				builder.append(", ");
				builder.append(value);
			}
		}
		
		builder.append(')');
		return builder.toString();
	}
}