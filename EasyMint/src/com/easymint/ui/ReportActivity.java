package com.easymint.ui;

/**
 * author:wangyujie
 * version:1.0
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.easymint.R;
import com.easymint.provider.MintDBHelper;

public class ReportActivity extends BaseMultiPaneActivity {

	/* 
	 * sum ,avg ,detail ,
	 * 饮食，交通，娱乐，日常，学习，其他
	 * 分类统计
	 * */
	private MintDBHelper mDbHelper;
	private final int consumptionOut = 1;
	private final int consumptionIn = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		
		mDbHelper =  new MintDBHelper(this);
		mDbHelper.open();
		
		getActivityHelper().setupActionBar(getTitle(), 0);
		
//		fillDB();
		selectRadioButton();
	}
	
	 protected void onDestory(){
		 if(mDbHelper != null){
			 mDbHelper.close();
		 }
		 super.onDestroy();
	 }
	
	private Object [] readDataFromCursor(Cursor in ,Cursor out ,String date){
		in.moveToFirst();
		Object [] data = new Object[4];
		data[0] = in.getInt(0);
		data[1] = date;
		data[2] = in.getFloat(2);
		out.moveToFirst();
		data[3]	= out.getFloat(2);
		return data;
	}
	private void fillSumByDate(){
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		
		String startOfDay = " 00:00";
		String endOfDay = " 23:59";
		cal.add(Calendar.DATE, 0);
		String today  = dFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		String yesterday = dFormat.format(cal.getTime());
		cal.add(Calendar.DATE, 1);
		cal.add(Calendar.WEEK_OF_MONTH, -1);
		String week = dFormat.format(cal.getTime());
		cal.add(Calendar.WEEK_OF_MONTH, 1);
		cal.add(Calendar.MONTH, -1);
		String month = dFormat.format(cal.getTime());
		cal.add(Calendar.MONTH,1);
		cal.add(Calendar.MONTH, -3);
		String threeMonth = dFormat.format(cal.getTime());
		cal.add(Calendar.MONTH, 3);
		cal.add(Calendar.MONTH, -6);
		String sixMonth = dFormat.format(cal.getTime());
		cal.add(Calendar.MONTH, 6);
		
		
//		Log.d("report","yesterday "+yesterday);
//		Log.d("report","today "+today);
//		Log.d("report","week " + week);
//		Log.d("report","month " + month);
//		Log.d("report","three month " + threeMonth);
//		Log.d("report","six month " + sixMonth);
	
		
		Cursor sumCursor = mDbHelper.fetchConsumptionSum();
		this.startManagingCursor(sumCursor);
		sumCursor.moveToFirst();
		float sum = sumCursor.getFloat(1);

		String cols[] = new String [] {MintDBHelper.KEY_ROWID,MintDBHelper.KEY_DATE,"IN" , "OUT"};
		MatrixCursor cursor = new MatrixCursor(cols);
		
		//today
		Cursor TodayIn = mDbHelper.fetchConsumptionSumByDate(today + startOfDay, today + endOfDay, consumptionIn);
		Cursor TodayOut = mDbHelper.fetchConsumptionSumByDate(today + startOfDay, today + endOfDay, consumptionOut);
		this.startManagingCursor(TodayIn);
		this.startManagingCursor(TodayOut);
		cursor.addRow(readDataFromCursor(TodayIn, TodayOut, "今天"));
		
		//yesterday
		Cursor YesterdayIn = mDbHelper.fetchConsumptionSumByDate(yesterday + startOfDay, yesterday + endOfDay, consumptionIn);
		Cursor YesterdayOut = mDbHelper.fetchConsumptionSumByDate(yesterday + startOfDay, yesterday + endOfDay, consumptionOut);
		this.startManagingCursor(YesterdayIn);
		this.startManagingCursor(YesterdayOut);
		cursor.addRow(readDataFromCursor(YesterdayIn, YesterdayOut, "昨天"));
		
		//week
		Cursor weekIn = mDbHelper.fetchConsumptionSumByDate(week + startOfDay, today + endOfDay, consumptionIn);
		Cursor weekOut = mDbHelper.fetchConsumptionSumByDate(week + startOfDay, today + endOfDay, consumptionOut);
		this.startManagingCursor(weekIn);
		this.startManagingCursor(weekOut);
		cursor.addRow(readDataFromCursor(weekIn, weekOut, "上周"));
		
		//month
		Cursor monthIn = mDbHelper.fetchConsumptionSumByDate(month + startOfDay, today + endOfDay, consumptionIn);
		Cursor monthOut = mDbHelper.fetchConsumptionSumByDate(month + startOfDay, today + endOfDay, consumptionOut);
		this.startManagingCursor(monthIn);
		this.startManagingCursor(monthOut);
		cursor.addRow(readDataFromCursor(monthIn, monthOut, "上个月内"));

		//threeMonth
		Cursor threeMonthIn = mDbHelper.fetchConsumptionSumByDate(threeMonth+ startOfDay, today + endOfDay, consumptionIn);
		Cursor threeMonthOut = mDbHelper.fetchConsumptionSumByDate(threeMonth + startOfDay, today + endOfDay, consumptionOut);
		this.startManagingCursor(threeMonthIn);
		this.startManagingCursor(threeMonthOut);
		cursor.addRow(readDataFromCursor(threeMonthIn, threeMonthOut, "三个月内"));

		//sixMonth
		Cursor sixMonthIn = mDbHelper.fetchConsumptionSumByDate(sixMonth + startOfDay, today + endOfDay, consumptionIn);
		Cursor sixMonthOut = mDbHelper.fetchConsumptionSumByDate(sixMonth + startOfDay, today + endOfDay, consumptionOut);
		this.startManagingCursor(sixMonthIn);
		this.startManagingCursor(sixMonthOut);
		cursor.addRow(readDataFromCursor(sixMonthIn, sixMonthOut, "半年内"));
		
		
		ReportAdapter rAdapter = new ReportAdapter(this, cursor);
		ListView sumView = (ListView) findViewById(R.id.list_report);
		
		rAdapter.setRectangle(sum);
		
		sumView.setAdapter(rAdapter);
		
		sumView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				
				SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				
				String startOfDay = " 00:00";
				String endOfDay = " 23:59";
				String from = new String();
				String to = new String();
				
				to = dFormat.format(cal.getTime()) + endOfDay;
				
				switch (arg2){
				case 0:
					break;
				case 1:
					cal.add(Calendar.DATE, -1);
					to = dFormat.format(cal.getTime()) + endOfDay;
					break;
				case 2:
					cal.add(Calendar.WEEK_OF_MONTH, -1);
					break;
				case 3:
					cal.add(Calendar.MONTH, -1);
					break;
				case 4:
					cal.add(Calendar.MONTH, -3);
					break;
				case 5:
					cal.add(Calendar.MONTH, -6);
					break;
				}
				from = dFormat.format(cal.getTime()) + startOfDay;
				
				String selection = new String();
				selection = MintDBHelper.KEY_DATE + " between " + "'" + from + "'" + " and " + "'" + to + "'";
				bundle.putString(BlotterActivity.START_SQL, selection);
				
				intent.putExtras(bundle);
				intent.setClass(ReportActivity.this,BlotterActivity.class);
				
				startActivity(intent);
			}
		});
	}
	
	//传递函数参数
//	private void fillSumBy()
	
	private void fillSum(String groupBy){
		
		Cursor cursorIn = mDbHelper.fetchConsumptionSumByType(groupBy,consumptionIn);
		this.startManagingCursor(cursorIn);
		Cursor cursorOut = mDbHelper.fetchConsumptionSumByType(groupBy,consumptionOut);
		this.startManagingCursor(cursorOut);
		
		Cursor sumCursor = mDbHelper.fetchConsumptionSum();
		this.startManagingCursor(sumCursor);
		sumCursor.moveToFirst();
		float sum = sumCursor.getFloat(1);
		
		String cols[] = new String [] {MintDBHelper.KEY_ROWID,MintDBHelper.KEY_CONSUMPTION_TYPE,"IN" , "OUT"};
		MatrixCursor cursor = new MatrixCursor(cols);
		
		boolean outFlag [] = new boolean [cursorOut.getCount()] ;
		for(int i = 0; i < outFlag.length ; i++){
			outFlag [i] = false;
		}
		cursorIn.moveToFirst();
		while(!cursorIn.isAfterLast() ){
			Object data [] = new Object [4];
			data[0] = cursorIn.getInt(0);
			data[1] = cursorIn.getInt(1);
			data[2] = cursorIn.getFloat(2);
			data[3] = 0.0 ;
			
			cursorOut.moveToFirst();
			for( int i = 0; i < cursorOut.getCount(); i ++){
				if(cursorIn.getInt(1) == cursorOut.getInt(1)){
					data[3] = cursorOut.getFloat(2);
					outFlag[cursorOut.getPosition()] = true;
				}
				cursorOut.moveToNext();
			}
			
			cursor.addRow(data);
			cursorIn.moveToNext();
		}
		for( int i = 0; i < outFlag.length ; i ++){
			if(outFlag[i] == false){
				cursorOut.moveToPosition(i);
				Object outData [] = new Object [4];
				outData[0] = cursorOut.getInt(0);
				outData[1] = cursorOut.getInt(1);
				outData[2] = 0.0;
				outData[3] = cursorOut.getFloat(2);
				
				cursor.addRow(outData);
			}
		}
		
		ReportAdapter rAdapter = new ReportAdapter(this, cursor);
		ListView sumView = (ListView) findViewById(R.id.list_report);
		
		rAdapter.setRectangle(sum);
		
		sumView.setAdapter(rAdapter);
		
		sumView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				
				TextView typeView = (TextView) arg1.findViewById(R.id.report_item_groupby);
				//type为int型，此处返回string
				String [] keys = getResources().getStringArray(R.array.account_type_array);
				String typeString = null;
				for(int i = 0; i < keys.length; i ++){
					if(keys[i].equals(typeView.getText())){
						typeString = "" + i;
						break;
					}
				}
				
				
				bundle.putString(BlotterActivity.START_SQL,MintDBHelper.KEY_CONSUMPTION_TYPE + "=" + typeString);
				intent.putExtras(bundle);
				
				intent.setClass(ReportActivity.this,BlotterActivity.class);
				
				startActivity(intent);
			}
		});
	}
	//for test used
	public void fillDB(){
		 mDbHelper.createConsumption("Title", 12, 1, "2011-06-08 13:13", 1, "", 1);
		 mDbHelper.createConsumption("Title2", 12, 1, "2011-06-09 13:13", 1, "", 0);
		 mDbHelper.createConsumption("Title3", 12, 2, "2011-06-15 13:13", 1, "", 0);
		 mDbHelper.createConsumption("Title4", 12, 2, "2011-06-14 13:13", 1, "", 1);
		 mDbHelper.createConsumption("Title5", 12, 3, "2011-06-13 13:13", 1, "", 0);
		 mDbHelper.createConsumption("Title5", 12, 3, "2011-06-11 13:13", 1, "", 0);
		 mDbHelper.createConsumption("Title5", 12, 4, "2011-06-13 13:13", 1, "", 1);
		 mDbHelper.createConsumption("Title5", 12, 4, "2011-06-14 13:13", 1, "", 1);
		 
	 }
	
	private void selectRadioButton(){
		RadioGroup rg = (RadioGroup) findViewById(R.id.report_radio_group);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(R.id.report_button_type == checkedId){
					fillSum(MintDBHelper.KEY_CONSUMPTION_TYPE);
				}
				else if(R.id.report_button_date == checkedId){
					fillSumByDate();
				}
			}
		});
		
	}
	
	
	private class ReportAdapter extends CursorAdapter {

		private LayoutInflater mInflater;
		
		private float consumptionSum = 0;
		
		
		public ReportAdapter(Context context, Cursor c ) {
			super(context, c);
			// TODO Auto-generated constructor stub
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView groupBy = (TextView) view.findViewById(R.id.report_item_groupby);
			TextView rectangleIn = (TextView) view.findViewById(R.id.report_rectangle_in);
			TextView sumIn = (TextView) view.findViewById(R.id.report_item_in_sum);
			TextView rectangleOut = (TextView) view.findViewById(R.id.report_rectangle_out);
			TextView sumOut = (TextView) view.findViewById(R.id.report_item_out_sum);
			if(cursor.getColumnName(1).equals(MintDBHelper.KEY_CONSUMPTION_TYPE)){
				int i = cursor.getInt(1);
				String [] array = getResources().getStringArray(R.array.account_type_array);
				groupBy.setText(array[i]);
			}
			else if(cursor.getColumnName(1).equals(MintDBHelper.KEY_DATE)){
				String date = cursor.getString(1);
				groupBy.setText(date);

			}
			float priceIn = cursor.getFloat(2);
			float priceOut = cursor.getFloat(3);
			int parentWidth = getWindowManager().getDefaultDisplay().getWidth();
			int widthIn =(int) (( priceIn / getRectangle() ) * 0.7 * parentWidth);
			int widthOut = (int ) ( ( priceOut / getRectangle() ) * 0.7 * parentWidth);
			
			if (priceIn == 0) {
				rectangleIn.setVisibility(View.GONE);
				sumIn.setVisibility(View.GONE);
			}
			if (priceOut == 0) {
				rectangleOut.setVisibility(View.GONE);
				sumOut.setVisibility(View.GONE);
			}
			rectangleIn.setWidth(widthIn);
			rectangleOut.setWidth(widthOut);
			
			sumIn.setText(""+priceIn + Currency.getInstance("CNY").getSymbol(Locale.CHINA));
			sumOut.setText(""+priceOut + Currency.getInstance("CNY").getSymbol(Locale.CHINA));
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return mInflater.inflate(R.layout.list_item_report, parent ,false);
		}
		
		private void setRectangle(float width){
			this.consumptionSum = width;
		}
		private int getRectangle(){
			return (int)consumptionSum;
		}
		
	}
	
}

