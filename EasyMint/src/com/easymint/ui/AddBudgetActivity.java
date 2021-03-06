package com.easymint.ui;

import java.util.Calendar;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;
import com.easymint.ui.widget.DateSlider;
import com.easymint.ui.widget.MonthYearDateSlider;



import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AddBudgetActivity extends BaseMultiPaneActivity {
	private static final String TAG = "Add_Budget Activity";

	private MintDBHelper mDbHelper;
	private Long mRowId;
	
	private int mYear;  
	private int mMonth; 
	private int mDay; 
	static final int DATE_DIALOG_ID = 0;
	String string_time;
	
	private float out;
	
	private Button dateButton;
	private Button confirmButton;
	private Button cancelButton;
	private Spinner consumptionTypeSpinner;
	private EditText primaryEditText,secondaryEditText;
	
	
	private EditText budgetEditText;
	private EditText outEditText;
	
	private static final char[] commaChars = new char[]{'.', ','};
	private static final char[] acceptedChars = new char[]{'0','1','2','3','4','5','6','7','8','9'};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addbudget);
		getActivityHelper().setupActionBar(getTitle(), getResources().getColor(R.color.epic));

		mDbHelper = new MintDBHelper(this);
		mDbHelper.open();
		
		mRowId = savedInstanceState != null ? savedInstanceState.getLong(MintDBHelper.KEY_ROWID) 
				: null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(MintDBHelper.KEY_ROWID) 
					: null;
		}
		
		findView();
		
		final Calendar c = Calendar.getInstance();     
		mYear = c.get(Calendar.YEAR);      
		mMonth = c.get(Calendar.MONTH);      
		mDay = c.get(Calendar.DAY_OF_MONTH); 
		c.add(Calendar.MONTH, 1);
		string_time=String.valueOf(mMonth)+"-"+String.valueOf(mYear)+" "+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR);

		
		populateFields();
		
		
	}
	
	private void findView() {
		
		ImageView gongImageView = (ImageView) findViewById(R.id.status);
		gongImageView.setVisibility(View.GONE);
		
		Button gongButton = (Button) findViewById(R.id.time);
		gongButton.setVisibility(View.GONE);
		
		ToggleButton statusToggleButton =(ToggleButton) findViewById(R.id.toggle);
		statusToggleButton.setChecked(true);
		statusToggleButton.setEnabled(false);
		
		dateButton = (Button)findViewById(R.id.date);
		
		dateButton = (Button) findViewById(R.id.date);
		dateButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
				
			}});
		
		primaryEditText = (EditText) findViewById(R.id.primary);
		primaryEditText.setKeyListener(keyListener);
		primaryEditText.setOnFocusChangeListener(selectAllOnFocusListener);
		
		
		secondaryEditText = (EditText) findViewById(R.id.secondary);
		secondaryEditText.setOnFocusChangeListener(selectAllOnFocusListener);

		
		consumptionTypeSpinner = (Spinner)findViewById(R.id.spinner);
	   
		

		confirmButton = (Button)findViewById(R.id.confirm);
		cancelButton = (Button)findViewById(R.id.cancel);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				boolean flag=saveState();
				if(flag)
				{
					finish();
					setResult(RESULT_OK);
				}
			} 
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				finish();
			} 
		});
		
		dateButton.setOnClickListener(new View.OnClickListener() {       
			public void onClick(View v) {      
				showDialog(DATE_DIALOG_ID);          
			}      
		});
		
	}
	
	protected void onDotOrComma() {
		secondaryEditText.requestFocus();
	}
	

	private final View.OnFocusChangeListener selectAllOnFocusListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText t = (EditText) v;
			if (hasFocus) {
				t.selectAll();
			}
		}
	};
	
private final NumberKeyListener keyListener = new NumberKeyListener() {
		
		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			if (end - start == 1) {
				char c = source.charAt(0);
				if (c == '.' || c == ',') {
					onDotOrComma();
					return "";
				}
			}
			return super.filter(source, start, end, dest, dstart, dend);
		}

		@Override
		public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
			char c = event.getMatch(commaChars);
			if (c == '.' || c == ',') {
				onDotOrComma();
				return true;
			}
			return super.onKeyDown(view, content, keyCode, event);
		}
		
		@Override
		protected char[] getAcceptedChars() {
			return acceptedChars;
		}

		@Override
		public int getInputType() {
			return InputType.TYPE_CLASS_PHONE;
		}
	};

	protected Dialog onCreateDialog(int id) {   
		 final Calendar c = Calendar.getInstance();
		 c.set(mYear, mMonth, mDay);
		switch (id) {  
		case DATE_DIALOG_ID:    
			return new MonthYearDateSlider(this,mMonthYearSetListener,c);
//			return new DatePickerDialog(this,mDateSetListener,mYear,mMonth,mDay);   
		}
		return null;
	}
	
	private DateSlider.OnDateSetListener mMonthYearSetListener =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
            	// update the dateText view with the corresponding date
            	mYear = selectedDate.get(Calendar.YEAR);
            	mMonth = selectedDate.get(Calendar.MONTH);
            	updateDisplay();
            }
    };
//	private DatePickerDialog.OnDateSetListener mDateSetListener =new DatePickerDialog.OnDateSetListener() {    
//		public void onDateSet(DatePicker view, int year,                  
//				int monthOfYear, int dayOfMonth) {        
//			mYear = year;        
//			mMonth = monthOfYear;          
//			mDay = dayOfMonth; 
//			string_time=String.valueOf(pad(mMonth))+"-"+String.valueOf(pad(mDay))+"-"+String.valueOf(mYear);
//			updateDisplay();
//		}       
//	};
	
	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
	private void updateDisplay() {  
		string_time = new StringBuilder().append(pad(mMonth + 1)).append("-").append(mYear).toString();
		Calendar calendar = Calendar.getInstance();
		calendar.set(mYear, mMonth, 1);
		String display = String.format("  %tB %tY - ", calendar,calendar);
		calendar.add(Calendar.MONTH, 1);
		display+=String.format("%tB %tY ", calendar,calendar);
		dateButton.setText(display);
	}
	private void populateFields() {
		if (mRowId != null) {
			Cursor budgetCursor = mDbHelper.fetchBudgetById(mRowId);
			startManagingCursor(budgetCursor);
			budgetCursor.moveToFirst();
			
			float price = budgetCursor.getFloat(budgetCursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_BUDGET));
			String priceString = Float.toString(price);
			String[] priceStrings = priceString.split("\\.");
			
			primaryEditText.setText(priceStrings[0]);
			if (priceStrings.length == 2) {
				secondaryEditText.setText(priceStrings[1]);
			}

			int type=budgetCursor.getInt(budgetCursor.getColumnIndexOrThrow(MintDBHelper.KEY_CONSUMPTION_TYPE));
			consumptionTypeSpinner.setSelection(type);
			
			String dateString = budgetCursor.getString(budgetCursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_CYCLE));
			
			String[] datepieces = dateString.split(" ");
			String[] datepiece = datepieces[0].split("-");
			mMonth=Integer.parseInt(datepiece[0])-1;
			mYear=Integer.parseInt(datepiece[1]);
			
			out = budgetCursor.getFloat(budgetCursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_OUT));
		}
		updateDisplay();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}
	
	protected void onDestory(){
		 if(mDbHelper != null){
			 mDbHelper.close();
		 }
		 super.onDestroy();
	 }
	
	private boolean saveState() {
		
		boolean check_flag=true;
		String primary = primaryEditText.getText().toString();
		String secondary = secondaryEditText.getText().toString();
		String money=primary+"."+secondary;
		

		int type=0;
		type=consumptionTypeSpinner.getSelectedItemPosition();
		
		

		if((check_flag&&TextUtils.isEmpty(primary))&&(check_flag&&TextUtils.isEmpty(secondary))){
			check_flag=false;
			Toast.makeText(AddBudgetActivity.this, "金钱不能为空！", Toast.LENGTH_SHORT).show();
		}
		
		if(check_flag&&!TextUtils.isDigitsOnly(primary)){
			check_flag=false;
			Toast.makeText(AddBudgetActivity.this, "金钱必须为数字！", Toast.LENGTH_SHORT).show();
		}

		if(check_flag&&!TextUtils.isDigitsOnly(secondary)){
			check_flag=false;
			Toast.makeText(AddBudgetActivity.this, "金钱必须为数字！", Toast.LENGTH_SHORT).show();
		}
		
		if(check_flag)
		{
			float budget=Float.parseFloat(money);
			float finalbudget;
			Cursor existbudgetCursor;
			Long existRowId;
			if (mRowId == null) 
			{
				existbudgetCursor=mDbHelper.fetchBudgetByTypeandDate(type,string_time);
				if(existbudgetCursor.getCount()>0)
				{
					existbudgetCursor.moveToFirst();
					existRowId=existbudgetCursor.getLong(existbudgetCursor
							.getColumnIndexOrThrow(MintDBHelper.KEY_ROWID));
					finalbudget=budget+existbudgetCursor.getFloat(existbudgetCursor
							.getColumnIndexOrThrow(MintDBHelper.KEY_BUDGET));
					out=existbudgetCursor.getFloat(existbudgetCursor
							.getColumnIndexOrThrow(MintDBHelper.KEY_OUT));
					mDbHelper.updateBudget(existRowId,type,finalbudget,out,string_time);
				}

				else
				{
					//consider has already had some expense
					
					Calendar calendar = Calendar.getInstance();
					calendar.set(mYear, mMonth, 1);
					String from = mYear+"-"+pad(mMonth+1)+"-01 00:00";
					calendar.add(Calendar.MONTH, 1);
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					String to = mYear+"-"+pad(mMonth+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59";
					
					float outSum = 0;
					
					Cursor expenseCursor = mDbHelper.fetchConsumptionSumByDateAndTypeAndStatus(from, to,type,1);
					startManagingCursor(expenseCursor);
					if(expenseCursor.getCount()>0){
						expenseCursor.moveToFirst();
						outSum += expenseCursor.getFloat(2);
					}
					
					Cursor incomeCursor = mDbHelper.fetchConsumptionSumByDateAndTypeAndStatus(from, to,type,0);
					startManagingCursor(incomeCursor);
					if(incomeCursor.getCount()>0){
						incomeCursor.moveToFirst();
						outSum =outSum - incomeCursor.getFloat(2);
					}
					
					out = out+outSum;
					
					long id = mDbHelper.createBudget(type,budget,out,string_time);
					if (id > 0) 
					{
						mRowId = id;
					}
				}
			}   
			else
			{
				existbudgetCursor=mDbHelper.fetchBudgetByTypeandDate(type,string_time);
				if(existbudgetCursor.getCount()>0)
				{
					existbudgetCursor.moveToFirst();
					existRowId=existbudgetCursor.getLong(existbudgetCursor
							.getColumnIndexOrThrow(MintDBHelper.KEY_ROWID));
					finalbudget=budget+existbudgetCursor.getFloat(existbudgetCursor
							.getColumnIndexOrThrow(MintDBHelper.KEY_BUDGET));
					out=out+existbudgetCursor.getFloat(existbudgetCursor
							.getColumnIndexOrThrow(MintDBHelper.KEY_OUT));
					mDbHelper.updateBudget(existRowId,type,finalbudget,out,string_time);
					mDbHelper.deleteBudget(mRowId);
				}
				else 
				{
					mDbHelper.updateBudget(mRowId,type,budget,out,string_time);
				}
			}
		}
		return check_flag;
	}
}