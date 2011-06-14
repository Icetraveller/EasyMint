package com.easymint.ui;

import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;
import com.easymint.ui.BudgetActivity.BudgetAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BudgetActivity extends BaseMultiPaneActivity {
	private static final String TAG = "Budget Activity";

	private MintDBHelper mDbHelper;
	private static int ACTIVITY_EDIT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budget);

		mDbHelper = new MintDBHelper(this);
		mDbHelper.open();
		
//		mDbHelper.createBudget(1, 2000, 100, "2010-1989");
		
		fillData();
	}
	
	protected void onDestory(){
		 if(mDbHelper != null){
			 mDbHelper.close();
		 }
		 super.onDestroy();
	 }

	private void fillData() {
		// get cursor
		Cursor cursor = mDbHelper.fetchAllBudget();
		startManagingCursor(cursor);

		getActivityHelper().setupActionBar(getTitle(), 0);
		ListView listView = (ListView) findViewById(R.id.list_budget);

		BudgetAdapter budgetrAdapter = new BudgetAdapter(this, cursor);
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int postion,
					long id) {
				final long selectedItemID = id;
				Dialog itemLongClickDialog = new AlertDialog.Builder(BudgetActivity.this)
				.setTitle(R.string.dialog_edit_or_delete)
				.setItems(R.array.entries_dialog_edit_or_delete, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							Intent peopleEditIntent = new Intent(BudgetActivity.this, AddBudgetActivity.class);
							peopleEditIntent.putExtra(MintDBHelper.KEY_ROWID, selectedItemID);
							startActivityForResult(peopleEditIntent, ACTIVITY_EDIT);
							break;
						case 1:
							Dialog deleteBudgetDialog = new AlertDialog.Builder(BudgetActivity.this)
							.setTitle(R.string.dialog_delete_title)
							.setMessage(R.string.dialog_delete_text)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if(deleteBudget(selectedItemID)){
										Toast.makeText(BudgetActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
									}else Toast.makeText(BudgetActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
								}

							})
							.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}}).create();
							deleteBudgetDialog.show();
							break;
						}
					}
					
				}).create();
				itemLongClickDialog.show();
				
			}
		});
		listView.setAdapter(budgetrAdapter);
	}
	
	private boolean deleteBudget(long selectedItemID) {
		Boolean success = false;
		success = mDbHelper.deleteBudget(selectedItemID);
		fillData();
		return success;
	}

	public class BudgetAdapter extends CursorAdapter {

		private final LayoutInflater mInflater;

		public BudgetAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = LayoutInflater.from(context);

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			int mYear;
		    int mMonth;
		    int mDay;
		    int mHour;
		    int mMinute;

			TextView dateTextView = (TextView) view.findViewById(R.id.date);

			TextView budget = (TextView) view.findViewById(R.id.budget);

			TextView consumption_type = (TextView) view
					.findViewById(R.id.consumption_type);

			TextView consumption = (TextView) view.findViewById(R.id.consumption);

			TextView amount = (TextView) view.findViewById(R.id.amount);
			
			ProgressBar progressbar=(ProgressBar) view.findViewById(R.id.progress);


			String dateString = cursor.getString(cursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_CYCLE));
			
			String[] dateStrings = dateString.split(" ");
			
			
			String[] datepiece = dateStrings[0].split("-");
			mMonth=Integer.parseInt(datepiece[0])-1;
			mDay=Integer.parseInt(datepiece[1]);
			mYear=Integer.parseInt(datepiece[2]);
			
			String[] timepiece = dateStrings[1].split(":");
			mHour = Integer.parseInt(timepiece[0]);
			mMinute = Integer.parseInt(timepiece[1]);
			final Calendar c = Calendar.getInstance();
			c.set(mYear, mMonth, mDay, mHour, mMinute);
			dateTextView.setText(String.format("  %tB %te %tY %tH:%02d", c,c,c,c,mMinute));
			
			dateTextView.setText(dateString);
 
			float budgetString = cursor.getFloat(cursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_BUDGET));
			budget.setText(""+budgetString);
			
			String consumption_typeString = cursor.getString(cursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_CONSUMPTION_TYPE));
			consumption_type.setText(consumption_typeString);
			
			float consumptionString = cursor.getFloat(cursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_OUT));
			consumption.setText("-"+consumptionString);
			
			float amountString=budgetString-consumptionString;
			if(amountString>0)
			{
			    amount.setText("+"+amountString);
			}
			else
			{
				amount.setText("-"+amountString);	
			}
			
			progressbar.setMax((int)budgetString);
			progressbar.setProgress((int)amountString);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(
					R.layout.activity_budget_list_item, parent, false);
			return view;
		}
	}
}