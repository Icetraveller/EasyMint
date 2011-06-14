package com.easymint.ui;

import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BlotterActivity extends BaseMultiPaneActivity{
	private static final String TAG="Blotter Activity";
	
	private MintDBHelper mDbHelper;
	
	public static String START_SQL = "start_sql";
	private String sqlString = null;
	
	private static int ACTIVITY_EDIT=0;
	
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_blotter);
	        
	        sqlString = savedInstanceState != null ? savedInstanceState.getString(START_SQL) 
					: null;
	        if (sqlString == null) {
				Bundle extras = getIntent().getExtras();            
				sqlString = extras != null ? extras.getString(START_SQL) 
						: null;
			}
	        
	        mDbHelper = new MintDBHelper(this);
	        mDbHelper.open();
	        fillData();
	    }
	 
	 protected void onDestory(){
		 if(mDbHelper != null){
			 mDbHelper.close();
		 }
		 super.onDestroy();
	 }
	 
	 /**
	  * fill db data in each item in the list
	  */
	 private void fillData() {
		// get cursor
		Cursor cursor = mDbHelper.fetchConsumptionByThings(sqlString);
		startManagingCursor(cursor);
		
		
		getActivityHelper().setupActionBar(getTitle(), 0);
		ListView listView = (ListView) findViewById(R.id.list_blotter);

		BlotterAdapter blotterAdapter = new BlotterAdapter(this, cursor);
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int postion,
					long id) {
				
				final long selectedItemID = id;
				Dialog itemLongClickDialog = new AlertDialog.Builder(BlotterActivity.this)
				.setTitle(R.string.dialog_edit_or_delete)
				.setItems(R.array.entries_dialog_edit_or_delete, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							Intent editIntent = new Intent(BlotterActivity.this, AddConsumptionActivity.class);
							editIntent.putExtra(MintDBHelper.KEY_ROWID, selectedItemID);
							startActivityForResult(editIntent, ACTIVITY_EDIT);
							break;
						case 1:
							Dialog deleteCOnsumptionDialog = new AlertDialog.Builder(BlotterActivity.this)
							.setTitle(R.string.dialog_delete_title)
							.setMessage(R.string.dialog_delete_text)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if(deleteConsumption(selectedItemID)){
										Toast.makeText(BlotterActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
									}else Toast.makeText(BlotterActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
								}

							})
							.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}}).create();
							deleteCOnsumptionDialog.show();
							break;
						}
					}
					
				}).create();
				itemLongClickDialog.show();
				
			}
		});
		
		listView.setAdapter(blotterAdapter);
	}
	 
	 private boolean deleteConsumption(
				long selectedItemID) {
		 	Boolean success = false;
			success = mDbHelper.deleteConsumption(selectedItemID);
			fillData();
			return success;
		}
	 
	 /**
	  * 
	  * @author Elonix
	  *	custom adapter to modify data filled in item row
	  */
	 private class BlotterAdapter extends CursorAdapter {
		 
		 private final LayoutInflater mInflater;
		 
		public BlotterAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = LayoutInflater.from(context);
			
			
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			/**
			 * @indicator : weight in budget
			 * @statusImageView : status of income or expense
			 * @title : name of the expense
			 * @category : consumption type
			 * @date : date
			 * @price : price of the expense
			 * @budgetRemain : budget - price;
			 */
			TextView indicator = (TextView) view.findViewById(R.id.indicator);
			
			ImageView statusImageView = (ImageView) view.findViewById(R.id.right_top);
			
			TextView titleTextView = (TextView) view.findViewById(R.id.top);
			
			TextView categoryTextView = (TextView) view.findViewById(R.id.center);
			
			TextView dateTextView = (TextView) view.findViewById(R.id.bottom);
			
			TextView priceTextView = (TextView) view.findViewById(R.id.right);
			
			TextView budgetRemain = (TextView) view.findViewById(R.id.right_center);
			
			
			//setup status image
			int status = cursor.getInt(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_STATUS));
			switch (status) {
			case 0:
				statusImageView.setImageResource(R.drawable.ic_blotter_income);
				break;
			case 1:
				statusImageView.setImageResource(R.drawable.ic_blotter_expense);
				break;
			default:
				break;
			}
			
			//setup title
			String titleString = cursor.getString(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_CONSUMPTION_TITLE));
			
			//if no title
			if(TextUtils.isEmpty(titleString) || titleString == null){
				titleTextView.setVisibility(View.INVISIBLE);
			}
			else {
				titleTextView.setText(titleString);
			}
			
			//setup date
			String dateString = cursor.getString(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_DATE));
			dateTextView.setText(dateString);
			
			//setup price
			float price = cursor.getFloat(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_PRICE));
			Currency currency = Currency.getInstance("CNY");
			currency.getSymbol();
			priceTextView.setText(price+" "+currency.getSymbol(Locale.CHINA));
			
			//setup category
			int consumptionType = cursor.getInt(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_CONSUMPTION_TYPE));
			String[] typeStrings = getResources().getStringArray(R.array.account_type_array);
			categoryTextView.setText(typeStrings[consumptionType]);
			
			//check has Budget
			Cursor budgetCursor = mDbHelper.fetchBudgetByTypeandDate(consumptionType,dateString);
			startManagingCursor(budgetCursor);
			
//			budgetCursor = findMaxBudgetInTime(budgetCursor);
			
			float budgetSum = 0;
			float budgetOut = 0;
			budgetCursor.moveToPrevious();
			if(budgetCursor.moveToNext()){
				
				budgetSum = budgetCursor.getFloat(budgetCursor.getColumnIndexOrThrow(MintDBHelper.KEY_BUDGET));
				Log.d(TAG, "budgetSum: "+budgetSum);
				budgetOut = budgetCursor.getFloat(budgetCursor.getColumnIndexOrThrow(MintDBHelper.KEY_OUT));
				budgetRemain.setText(""+budgetOut+ " "+ currency.getSymbol(Locale.CHINA));
				//expense
				if(status == 1){
					/*
					 * setup indicator
					 * grey : 0~0.01
					 * green : 0.01 ~ 0.05
					 * blue : 0.05 ~ 0.25
					 * purple : 0.25 ~ 0.5
					 * orange : >0.5
					 */
					float rate = price/budgetSum;
					Log.d(TAG, "rate: "+budgetSum);
					if(rate <0.01){
						indicator.setBackgroundColor(getResources().getColor(R.color.common));
					}
					if(rate >=0.01 && rate < 0.05){
						indicator.setBackgroundColor(getResources().getColor(R.color.uncommon));
					}
					if(rate >=0.05 && rate < 0.25){
						indicator.setBackgroundColor(getResources().getColor(R.color.rare));
					}
					if(rate >=0.25 && rate < 0.5){
						indicator.setBackgroundColor(getResources().getColor(R.color.epic));
					}
					if(rate >=0.5){
						indicator.setBackgroundColor(getResources().getColor(R.color.legendary));
					}
//					
				}
			}
			else {
				indicator.setBackgroundColor(getResources().getColor(R.color.common));
			}
			budgetRemain.setVisibility(View.INVISIBLE);//setup budget remain
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.list_item_blotter, parent, false);
		}
		
//		private Cursor findMaxBudgetInTime(Cursor c){
//			if(c == null){
//				return null;
//			}
//			startManagingCursor(c);
//			c.moveToPrevious();
//			float budgetSum = 0;
//			String budgetCycle = "";
//			Calendar start =Calendar.getInstance();
//			Calendar end =Calendar.getInstance();
//			Cursor tmpCursor=null;
//			startManagingCursor(tmpCursor);
//			
//			while(c.moveToNext()){
//				float budgetSum2 = c.getFloat(c.getColumnIndexOrThrow(MintDBHelper.KEY_BUDGET));
//				budgetCycle = c.getString(c.getColumnIndexOrThrow(MintDBHelper.KEY_CYCLE));
//				
//				String[] period = budgetCycle.split("~");
//				String[] startTime = period[0].split(",");
//				start.set(Integer.parseInt(startTime[0]),Integer.parseInt(startTime[1]), Integer.parseInt(startTime[2]));
//				String[] endTime = period[1].split(",");
//				end.set(Integer.parseInt(endTime[0]),Integer.parseInt(endTime[1]), Integer.parseInt(endTime[2]));
//				
//				if(budgetSum2 >= budgetSum && Calendar.getInstance().after(start) && Calendar.getInstance().before(end)){
//					budgetSum = budgetSum2;
//					tmpCursor = c;
//				}
//			}
//			return tmpCursor;
//		}
		
	 }
}
