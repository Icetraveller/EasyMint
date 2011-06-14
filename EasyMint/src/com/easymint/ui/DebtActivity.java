package com.easymint.ui;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;

public class DebtActivity extends BaseMultiPaneActivity  {
	private static final String TAG = "Debt Activity";

	private MintDBHelper mDbHelper;
	private DebtAdapter debtAdapter;
	private long longSelectedItemID = 0;
	
	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;
	private static final int ACTIVITY_DETAIL=2;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debt);
		getActivityHelper().setupActionBar(getTitle(), 0);
		mDbHelper = new MintDBHelper(this);
		mDbHelper.open();
//		fillDB();
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
		Cursor cursor = mDbHelper.fetchAllDebt();
		startManagingCursor(cursor);
		
		
		ListView listView = (ListView) findViewById(R.id.list_debt);

		debtAdapter = new DebtAdapter(this, cursor);

		
		listView.setAdapter(debtAdapter);
	}
	
	private boolean deleteDebt(long id){
		Boolean success = false;
		success = mDbHelper.deleteDebt(longSelectedItemID);
		fillData();
		return success;
	}
	
	 /**
	  * 
	  * @author Elonix
	  *	custom adapter to modify data filled in item row
	  */
	 private class DebtAdapter extends CursorAdapter {
		 
		 private final LayoutInflater mInflater;
		 
		public DebtAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = LayoutInflater.from(context);
			
			
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			TextView indicator = (TextView) view.findViewById(R.id.indicator);
			
			ImageView statusImageView = (ImageView) view.findViewById(R.id.right_top);
			
			ImageView indicatorImageView = (ImageView) view.findViewById(R.id.star_button);
			
			TextView debtorTextView = (TextView) view.findViewById(R.id.center);
			
			TextView dateTextView = (TextView) view.findViewById(R.id.bottom);
			
			TextView moneyTextView = (TextView) view.findViewById(R.id.right_center);
			
			final long id = cursor.getLong(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_ROWID));
			Log.d(TAG, "id "+id);
			
//			CheckBox cb = (CheckBox) view.findViewById(R.id.right_checkbox);
			
			
			
			String prefixString = "";
			//setup status image
			
			final int status = cursor.getInt(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_STATUS));
			switch (status) {
			case 0:
				statusImageView.setImageResource(R.drawable.ic_blotter_income);
				prefixString=getResources().getString(R.string.description_owe);
				break;
			case 1:
				statusImageView.setImageResource(R.drawable.ic_blotter_expense);
				prefixString=getResources().getString(R.string.description_owed);
				break;
			default:
				break;
			}
			
			Currency currency = Currency.getInstance("CNY");
			currency.getSymbol();
			
			//setup debt price
			final float price = cursor.getFloat(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_PRICE));
			moneyTextView.setText(price+" "+currency.getSymbol(Locale.CHINA));
			
			int clear = cursor.getInt(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_CLEAR));
			
			if (clear == 0) {
				indicatorImageView.setVisibility(View.INVISIBLE);
				indicator.setBackgroundColor(getResources().getColor(R.color.uncommon));
			}
			if(clear ==1){
				indicatorImageView.setVisibility(View.VISIBLE);
				indicator.setBackgroundColor(getResources().getColor(R.color.common));
				view.setBackgroundResource(R.color.press);
			}
			
			Log.d(TAG, "id "+id+"clear "+clear);
			
			//setup date
			final String dateString = cursor.getString(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_DATE));
			dateTextView.setText(dateString);
			
			final String noteString = cursor.getString(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_NOTES));
			
			//check has debtor
			final String debtorString = cursor.getString(cursor.getColumnIndexOrThrow(MintDBHelper.KEY_DEBTOR));
			debtorTextView.setText(prefixString+" "+debtorString);
			final ImageView iView = indicatorImageView;
			view.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					int Oclear = 0;
					String preString = "";
					
					if(iView.getVisibility()==View.INVISIBLE){
						Oclear = 1;
						preString=getString(R.string.description_clear);
						Toast.makeText(DebtActivity.this, getString(R.string.and)+debtorString+preString, Toast.LENGTH_SHORT).show();
						iView.setVisibility(View.VISIBLE);
						
						}
					else if(iView.getVisibility()==View.VISIBLE){
						Oclear = 0;
						preString=getString(R.string.description_unclear);
						Toast.makeText(DebtActivity.this, getString(R.string.and)+debtorString+preString, Toast.LENGTH_SHORT).show();
						iView.setVisibility(View.INVISIBLE);
					}
					boolean flag = mDbHelper.updateDebt(id, debtorString, price, dateString, noteString, status, Oclear);
					Log.d(TAG, "id "+id+"flat "+flag+"   clear   "+Oclear);
				}});
			
			view.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					longSelectedItemID = id;
					Dialog itemLongClickDialog = new AlertDialog.Builder(DebtActivity.this)
					.setTitle(R.string.dialog_edit_or_delete)
					.setItems(R.array.entries_dialog_edit_or_delete, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch(which){
							case 0:
								Intent peopleEditIntent = new Intent(DebtActivity.this, AddDebtActivity.class);
								peopleEditIntent.putExtra(MintDBHelper.KEY_ROWID, longSelectedItemID);
								startActivityForResult(peopleEditIntent, ACTIVITY_EDIT);
								break;
							case 1:
								Dialog deleteDebtDialog = new AlertDialog.Builder(DebtActivity.this)
								.setTitle(R.string.dialog_delete_title)
								.setMessage(R.string.dialog_delete_text)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if(deleteDebt(longSelectedItemID)){
											Toast.makeText(DebtActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
										}else Toast.makeText(DebtActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
									}
								})
								.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}}).create();
								deleteDebtDialog.show();
								break;
							}
						}
						
					}).create();
					itemLongClickDialog.show();
					return false;
				}});
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.list_item_debt, parent, false);
		}
}
}
