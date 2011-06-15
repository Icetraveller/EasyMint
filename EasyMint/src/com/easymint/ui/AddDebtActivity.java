package com.easymint.ui;

import java.util.Calendar;

import android.R.integer;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;
import com.easymint.ui.widget.AlternativeDateSlider;
import com.easymint.ui.widget.DateSlider;
import com.easymint.ui.widget.TimeSlider;

public class AddDebtActivity extends BaseMultiPaneActivity{
	private static final String TAG = "Add Debt Activity";
	private MintDBHelper mDbHelper;
	private Button dateButton;
	private Button timeButton;
	private EditText titleEditText;
	private EditText contentEditText;
	private ImageView clearImageView;
	private ToggleButton statusToggleButton;
	private EditText primaryEditText;
	private EditText secondaryEditText;
	private Button confirmButton;
	private Button cancelButton;
	private Long mRowId;
	
	private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
	
	private int clear = -1;
	private String debtorToSave = "";
	private float priceToSave = 0;
	private int status = 0;
	
	
	private static final int DIALOG_CLEAR = 0;
	private static final int DIALOG_DATE = 1;
	private static final int DIALOG_TIME = 2;
	private static final int DIALOG_YES_OR_NO = 3;
	private static final int DIALOG_EXIT = 4;
	
	private static final char[] commaChars = new char[]{'.', ','};
	private static final char[] acceptedChars = new char[]{'0','1','2','3','4','5','6','7','8','9'};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adddebt);

		mDbHelper = new MintDBHelper(this);
		mDbHelper.open();
		
		getActivityHelper().setupActionBar(getTitle(), getResources().getColor(R.color.legendary));
		
		findview();
		
		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(MintDBHelper.KEY_ROWID) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras
					.getLong(MintDBHelper.KEY_ROWID) : null;
		}
		
		// get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute=c.get(Calendar.MINUTE);
		
		populateFields();
	}
	
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	public void onDestroy() {
		if(mDbHelper != null)
			mDbHelper.close();
		super.onDestroy();
	}
	
	private void populateFields() {
		if (mRowId != null && mRowId != 0) {
				Cursor addDebtCursor = mDbHelper.fetchDebt(mRowId);
				startManagingCursor(addDebtCursor);
				addDebtCursor.moveToFirst();
				String titleString = addDebtCursor.getString(addDebtCursor
						.getColumnIndexOrThrow(MintDBHelper.KEY_DEBTOR));
				titleEditText.setText(titleString);

				String contentString = addDebtCursor.getString(addDebtCursor
						.getColumnIndexOrThrow(MintDBHelper.KEY_NOTES));
				contentEditText.setText(contentString);

				clear = addDebtCursor.getInt(addDebtCursor
						.getColumnIndexOrThrow(MintDBHelper.KEY_CLEAR));

				status = addDebtCursor.getInt(addDebtCursor
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

				float price = addDebtCursor.getFloat(addDebtCursor
						.getColumnIndexOrThrow(MintDBHelper.KEY_PRICE));
				String priceString = Float.toString(price);
				String[] priceStrings = priceString.split("\\.");
				
				primaryEditText.setText(priceStrings[0]);
				if (priceStrings.length == 2) {
					secondaryEditText.setText(priceStrings[1]);
				}

				String dateString = addDebtCursor.getString(addDebtCursor
						.getColumnIndexOrThrow(MintDBHelper.KEY_DATE));
				String[] dateStrings = dateString.split(" ");
				
				String[] datepiece = dateStrings[0].split("-");
				mDay = Integer.parseInt(datepiece[2]);
				mMonth=Integer.parseInt(datepiece[1])-1;
				mYear=Integer.parseInt(datepiece[0]);
				
				String[] timepiece = dateStrings[1].split(":");
				mHour = Integer.parseInt(timepiece[0]);
				mMinute = Integer.parseInt(timepiece[1]);
				
				
		}else {
			clear=0;
		}
		updateDisplayclear();
		updateDisplayTime();
		updateDisplayDate();
	}
	
	private Boolean checkSaveable(){
		boolean flag = true;
		
		String primaryString = primaryEditText.getText().toString();
		String secondaryString = secondaryEditText.getText().toString();
		if (!TextUtils.isEmpty(primaryString)&&TextUtils.isDigitsOnly(primaryString) && TextUtils.isDigitsOnly(secondaryString)) {
			
			String priceString = primaryString+"."+secondaryString;
			Log.d(TAG, ""+priceString);
			priceToSave = Float.parseFloat(priceString);
		}
		else{
			Toast.makeText(AddDebtActivity.this, R.string.error_price, Toast.LENGTH_SHORT).show();
			flag=false;
		}
		
		debtorToSave = titleEditText.getText().toString();
		if(TextUtils.isEmpty(debtorToSave)){
			Toast.makeText(AddDebtActivity.this, R.string.error_empty_debtor, Toast.LENGTH_SHORT).show();
			flag=false;
		}
		
		
		
		return flag;
	}
	
	private void save(){
		String date = new StringBuilder()
		.append(mYear).append("-").append(pad(mMonth + 1)).append("-").append(pad(mDay)).append(" ")
		.append(mHour).append(":").append(mMinute).toString();
		String notes = contentEditText.getText().toString();
		 if (mRowId == null ) {
	            long id = mDbHelper.createDebt(debtorToSave, priceToSave, date, notes, status, clear);
	            if (id > 0) {
	                mRowId = id;
	            }
	        } else {
	            mDbHelper.updateDebt(mRowId, debtorToSave, priceToSave, date, notes, status, clear);
	        }
	}
	
	protected void onResume() {
        super.onResume();
        populateFields();
    }

	private void findview(){
		//time bar
		clearImageView = (ImageView) findViewById(R.id.status);
		clearImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CLEAR);
				
			}});
		dateButton = (Button) findViewById(R.id.date);
		dateButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DATE);
				
			}});
		timeButton = (Button) findViewById(R.id.time);
		timeButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_TIME);
				
			}});
		//general
		titleEditText = ( EditText) findViewById(R.id.title);
		contentEditText = (EditText) findViewById(R.id.content);
		
		//amount
		statusToggleButton = (ToggleButton) findViewById(R.id.toggle);
		statusToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					status=1;
					Toast.makeText(AddDebtActivity.this, R.string.description_owe_debtor, Toast.LENGTH_SHORT).show();
				}else {
					status=0;
					Toast.makeText(AddDebtActivity.this, R.string.description_owed_debtor, Toast.LENGTH_SHORT).show();
				}
				
			}});
		
		primaryEditText = (EditText) findViewById(R.id.primary);
		primaryEditText.setKeyListener(keyListener);
		primaryEditText.setOnFocusChangeListener(selectAllOnFocusListener);
		
		
		secondaryEditText = (EditText) findViewById(R.id.secondary);
		secondaryEditText.setOnFocusChangeListener(selectAllOnFocusListener);
		
		
		//button bar
		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}});
		confirmButton =(Button) findViewById(R.id.save);
		confirmButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				boolean flag = checkSaveable();
				if(flag) {
					save();
					finish();
				}
				
			}});
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
	
//	private DatePickerDialog.OnDateSetListener mDateSetListener =
//        new DatePickerDialog.OnDateSetListener() {
//
//            public void onDateSet(DatePicker view, int year, 
//                                  int monthOfYear, int dayOfMonth) {
//                mYear = year;
//                mMonth = monthOfYear;
//                mDay = dayOfMonth;
//                updateDisplayDate();
//            }
//        };
	
	// define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateSetListener =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
            	// update the dateText view with the corresponding date
            	mYear = selectedDate.get(Calendar.YEAR);
            	mMonth=selectedDate.get(Calendar.MONTH);
            	mDay = selectedDate.get(Calendar.DAY_OF_MONTH);
            	updateDisplayDate();
//                dateText.setText(String.format("The chosen date:%n%te. %tB %tY", selectedDate, selectedDate, selectedDate));
            }
    };
    
    private DateSlider.OnDateSetListener mTimeSetListener =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
            	// update the dateText view with the corresponding date
            	mHour = selectedDate.get(Calendar.HOUR_OF_DAY);
              mMinute = selectedDate.get(Calendar.MINUTE);
              updateDisplayTime();
//                dateText.setText(String.format("The chosen time:%n%tR", selectedDate));
            }
    };
        
     // the callback received when the user "sets" the time in the dialog
//        private TimePickerDialog.OnTimeSetListener mTimeSetListener =
//            new TimePickerDialog.OnTimeSetListener() {
//                public void onTimeSet(TimePicker view, int hourOfDay, int minute ) {
//                    mHour = hourOfDay;
//                    mMinute = minute;
//                    updateDisplayTime();
//                }
//            };
        
     // updates the date in the TextView
	private void updateDisplayDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(mYear, mMonth, mDay,mHour,mMinute);
		dateButton.setText(String.format("  %tB %te %tY ", calendar,calendar,calendar));
	}

	// updates the time in the TextView
	private void updateDisplayTime() {
		timeButton.setText(new StringBuilder().append(pad(mHour)).append(":")
				.append(pad(mMinute)));
	}
	
	// updates the clear in the imageview
	private void updateDisplayclear() {
		switch (clear) {
		case 0:
			clearImageView
					.setImageResource(R.drawable.transaction_status_unreconciled_2);
			break;
		case 1:
			clearImageView
					.setImageResource(R.drawable.transaction_status_reconciled_2);
			break;
		}
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
		
	}
	
	
	protected Dialog onCreateDialog(int id) {
		final Calendar c = Calendar.getInstance();
		c.set(mYear, mMonth, mDay, mHour, mMinute);
		switch (id) {
		case DIALOG_CLEAR:
			return new AlertDialog.Builder(AddDebtActivity.this)
            .setIcon(R.drawable.transaction_status_unreconciled_2)
            .setTitle(R.string.dialog_clear_title)
            .setSingleChoiceItems(R.array.entries_dialog_clear, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	clear = whichButton;
                }
            })
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	updateDisplayclear();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            })
           .create();
		case DIALOG_DATE:
//			return new DatePickerDialog(this,
//                    mDateSetListener,
//                    mYear, mMonth, mDay);
			 return new AlternativeDateSlider(this,mDateSetListener,c);
		case DIALOG_TIME:
//			return new TimePickerDialog(this,
//	                mTimeSetListener, mHour, mMinute, false);
			return new TimeSlider(this,mTimeSetListener,c);
		case DIALOG_YES_OR_NO:
			return null;
		case DIALOG_EXIT:
			return null;
		}
		return null;
	}
	
	

}
