package com.easymint.ui;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;

public class AddConsumptionActivity extends BaseMultiPaneActivity {

	private Long mRowId;
	private MintDBHelper mDbHelper;
	private EditText titleEditText;
	private EditText money_entry;
	private EditText primaryEditText; //整数位
	private EditText secondaryEditText;	//小数位
	private ToggleButton statusToggleButton;
	private EditText quantityEditText; //数量
	private EditText noteEditText;   //编辑框
	private Spinner typeSpinner;    

	private Button dateButton; 
	private Button timeButton;  //日期时间
	private RadioButton payout;
	private RadioButton income;
	private Button confirmButton;
	private Button cancelButton;     //提交取消

	private int mYear;  
	private int mMonth; 
	private int mDay;   
	
	String string_time;  //时间定义

	private int mHour;   
	private int mMinute; 
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	
	private String string_date; //日期定义
	private String date_time;
	private int status = 0;
	
	private static final char[] commaChars = new char[]{'.', ','};
	private static final char[] acceptedChars = new char[]{'0','1','2','3','4','5','6','7','8','9'};
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		mDbHelper = new MintDBHelper(this);	
		mDbHelper.open();
		setContentView(R.layout.activity_addconsumption);
		
		getActivityHelper().setupActionBar(getTitle(), getResources().getColor(R.color.uncommon));
		
		
		mRowId = savedInstanceState != null ? savedInstanceState.getLong(MintDBHelper.KEY_ROWID) 
				: null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(MintDBHelper.KEY_ROWID) 
					: null;
		}
		findView();
		
		

		// get the current time     
		final Calendar c = Calendar.getInstance();   //获取日期时间    
		mYear = c.get(Calendar.YEAR);      
		mMonth = c.get(Calendar.MONTH);      
		mDay = c.get(Calendar.DAY_OF_MONTH);        
		string_date=String.valueOf(mYear)+"-"+String.valueOf(mMonth)+"-"+String.valueOf(mDay);
		mHour = c.get(Calendar.HOUR_OF_DAY);   
		mMinute = c.get(Calendar.MINUTE);
		string_time=String.valueOf(mHour)+":"+String.valueOf(mMinute);

		populateFields();
	}

	private void findView() {
		ImageView gongImageView = (ImageView) findViewById(R.id.status);
		gongImageView.setVisibility(View.GONE);
		timeButton = (Button)findViewById(R.id.time);
		dateButton = (Button)findViewById(R.id.date);

		titleEditText = (EditText)findViewById(R.id.account_content_entry);
		quantityEditText = (EditText)findViewById(R.id.account_number_entry);
		noteEditText = (EditText)findViewById(R.id.account_remark_entry);
		

		typeSpinner = (Spinner)findViewById(R.id.account_type_spinner);
		
		statusToggleButton = (ToggleButton) findViewById(R.id.toggle);
		statusToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					status = 0;
					Toast.makeText(AddConsumptionActivity.this, R.string.description_alter_expense, Toast.LENGTH_SHORT).show();
				}else {
					status = 1;
					Toast.makeText(AddConsumptionActivity.this, R.string.description_alter_income, Toast.LENGTH_SHORT).show();
				}
				
			}});
		
		primaryEditText = (EditText) findViewById(R.id.primary);
		primaryEditText.setKeyListener(keyListener);
		primaryEditText.setOnFocusChangeListener(selectAllOnFocusListener);
		
		
		secondaryEditText = (EditText) findViewById(R.id.secondary);
		secondaryEditText.setOnFocusChangeListener(selectAllOnFocusListener);
		

		confirmButton = (Button)findViewById(R.id.account_submit);
		cancelButton = (Button)findViewById(R.id.account_cancel);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				boolean flag=saveState();
				if(flag)
				{
					setResult(RESULT_OK);
					finish();
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

		timeButton.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View v) {        
				showDialog(TIME_DIALOG_ID);         
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
		switch (id) {   
		case DATE_DIALOG_ID:    
			return new DatePickerDialog(this,mDateSetListener,mYear,mMonth,mDay);   

		case TIME_DIALOG_ID:     
			return new TimePickerDialog(this,mTimeSetListener,mHour,mMinute,false);  
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =new DatePickerDialog.OnDateSetListener() {    
		public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {        
			mYear = year;
			mMonth = monthOfYear;        
			mDay = dayOfMonth;
			string_date=String.valueOf(pad(mMonth+1))+"-"+String.valueOf(pad(mDay))+"-"+String.valueOf(mYear);
			updateDisplayDate();
		}       
	};

	private TimePickerDialog.OnTimeSetListener mTimeSetListener =new TimePickerDialog.OnTimeSetListener() {     
		public void onTimeSet(TimePicker view, int hourOfDay, int minute){   
			mHour = hourOfDay;         
			mMinute = minute; 
			string_time=String.valueOf(mHour)+":"+String.valueOf(mMinute);
			updateDisplayTime();
		}  
	};

	private void updateDisplayTime() {  
		timeButton.setText(new StringBuilder().append(pad(mHour)).append(":").append(pad(mMinute)));
	}
	private void updateDisplayDate() {  
		dateButton.setText(new StringBuilder().append(mMonth + 1).append("-").append(mDay).append("-").append(mYear).append(" "));
	}

	private static String pad(int c) {  
		if (c >= 10)    
			return String.valueOf(c);  
		else      
			return "0" + String.valueOf(c);
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor consumptionCursor = mDbHelper.fetchConsumptionById(mRowId);
			startManagingCursor(consumptionCursor);
			consumptionCursor.moveToFirst();
			
			titleEditText.setText(consumptionCursor.getString(
					consumptionCursor.getColumnIndexOrThrow(MintDBHelper.KEY_CONSUMPTION_TITLE)));
//			money_entry.setText(consumptionCursor.getString(
//					consumptionCursor.getColumnIndexOrThrow(MintDBHelper.KEY_PRICE)));
			
			float price = consumptionCursor.getFloat(consumptionCursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_PRICE));
			String priceString = Float.toString(price);
			String[] priceStrings = priceString.split("\\.");
			
			primaryEditText.setText(priceStrings[0]);
			if (priceStrings.length == 2) {
				secondaryEditText.setText(priceStrings[1]);
			}
			
			status= consumptionCursor.getInt(consumptionCursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_STATUS));
			switch (status) {
			case 0:
				statusToggleButton.setChecked(false);
				break;
			case 1:
				statusToggleButton.setChecked(true);
				break;
			default:
				break;
			}
			
			quantityEditText.setText(consumptionCursor.getString(
					consumptionCursor.getColumnIndexOrThrow(MintDBHelper.KEY_QUANTITY)));
			noteEditText.setText(consumptionCursor.getString(
					consumptionCursor.getColumnIndexOrThrow(MintDBHelper.KEY_NOTES)));

			int type=consumptionCursor.getInt(consumptionCursor.getColumnIndexOrThrow(MintDBHelper.KEY_CONSUMPTION_TYPE));
			typeSpinner.setSelection(type);
			
			String dateString = consumptionCursor.getString(consumptionCursor
					.getColumnIndexOrThrow(MintDBHelper.KEY_DATE));
			String[] dateStrings = dateString.split(" ");
			
			String[] datepiece = dateStrings[0].split("-");
			mMonth=Integer.parseInt(datepiece[0])-1;
			mDay=Integer.parseInt(datepiece[1]);
			mYear=Integer.parseInt(datepiece[2]);
			
			String[] timepiece = dateStrings[1].split(":");
			mHour = Integer.parseInt(timepiece[0]);
			mMinute = Integer.parseInt(timepiece[1]);
		}
		updateDisplayTime();
		updateDisplayDate();
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

	private boolean saveState() {
		boolean check_flag=true;
		String content =titleEditText.getText().toString();
		String primary =primaryEditText.getText().toString();
		String secondary =secondaryEditText.getText().toString();
		
		String number =quantityEditText.getText().toString();
		String remark =noteEditText.getText().toString();

		int type=typeSpinner.getSelectedItemPosition();
		float float_money=0;
		int int_number=0;
		date_time=string_date+" "+string_time;

		if(check_flag&&TextUtils.isEmpty(primary)){
			check_flag=false;
			Toast.makeText(AddConsumptionActivity.this, "金钱不能为空！", Toast.LENGTH_SHORT).show();
		}
		
		if(check_flag&&!TextUtils.isDigitsOnly(primary)){
			check_flag=false;
			Toast.makeText(AddConsumptionActivity.this, "金钱必须为数字！", Toast.LENGTH_SHORT).show();
		}

		if(check_flag&&TextUtils.isEmpty(number)&&TextUtils.isDigitsOnly(number)){
			check_flag=false;
			Toast.makeText(AddConsumptionActivity.this, "数量必须为整数！", Toast.LENGTH_SHORT).show();
		}


		if(check_flag)
		{
//			float_money=Float.parseFloat(money);
			int_number=Integer.parseInt(number);	
			
			
			if (mRowId == null) {
				long id = mDbHelper.createConsumption(
						content,float_money,type,date_time,int_number,remark,status);
				if (id > 0) {
					mRowId = id;
				}
			}
			else {
				mDbHelper.updateConsumption(mRowId,content,float_money,type,date_time,int_number,remark,status);

			}
		}
		return check_flag;
	}
}
