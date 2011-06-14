package com.easymint.provider;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MintDBHelper {
	
	private static final String TAG="MintDBHelper";
	
	public static final String DATABASE_TABLE_CONSUMPTION = "consumption";
    public static final String DATABASE_TABLE_RANK = "rank";
    public static final String DATABASE_TABLE_DEBT = "debt";
    public static final String DATABASE_TABLE_BUDGET = "budget";
    
    /*
     * common
     */
    public static final String KEY_ROWID = "_id";	//主键
    public static final String KEY_DATE = "date";	//日期
    public static final String KEY_NOTES= "notes";	//备注
    public static final String KEY_STATUS= "status";	//status(0 income/ )
    
    /*
     * consumption
     */
    public static final String KEY_CONSUMPTION_TITLE = "consumption_title";	//消费项目名称
    public static final String KEY_PRICE = "price";	//消费金额
    public static final String KEY_CONSUMPTION_TYPE = "consumption_type";	//消费类型
    public static final String KEY_QUANTITY= "quantity";	//此次购买的数量
    
    
    /*
     * rank
     */
    public static final String KEY_COUNTS = "counts";	//计数，记录该物品购买次数
    public static final String KEY_LASTPRICE = "last_price";	//上次购买金额
    
    /*
     * debt
     */
    public static final String KEY_DEBTOR = "debtor";	//债务人 
    public static final String KEY_CLEAR = "clear";	//是否清算
    
    /*
     * budget
     */
    public static final String KEY_BUDGET = "budget";	//预算金额
    public static final String KEY_OUT = "out";	//实际支出
    public static final String KEY_CYCLE = "cycle";	//预算周期
   
    private static final String DATABASE_NAME = "Mint";
    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;
    
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    
    /*
     * SQL
     */
	private static final String DATABASE_CREATE_CONSUMPTION = "create table if not exists consumption(_id integer primary key autoincrement, consumption_title text not null, price numeric not null, consumption_type integer not null, quantity integer not null, notes text, date text not null, status integer not null);";
	private static final String DATABASE_CREATE_RANK = "create table if not exists rank(_id integer primary key autoincrement, consumption_title text not null, last_price numeric not null, counts integer not null);";
	private static final String DATABASE_CREATE_DEBT= "create table if not exists debt(_id integer primary key autoincrement, debtor text not null, price numeric not null, date text not null, notes text, status integer not null, clear integer not null);";
	private static final String DATABASE_CREATE_BUDGET = "create table if not exists budget(_id integer primary key autoincrement, consumption_type integer not null, budget numeric not null, out numeric not null, cycle text not null);";
	
	/*
	 * Helper
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper{
    	DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	try {
        		db.execSQL(DATABASE_CREATE_CONSUMPTION);
        		db.execSQL(DATABASE_CREATE_RANK);
        		db.execSQL(DATABASE_CREATE_DEBT);
        		db.execSQL(DATABASE_CREATE_BUDGET);
			} catch (Exception e) {
				Log.e(TAG," DatabaseHelper", e);
			}
        }
        
        @Override
        public void onOpen(SQLiteDatabase db) {
        	super.onOpen(db);
        };

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
	
	
	public MintDBHelper(Context ctx) {
        this.mCtx = ctx;
    }
	
	public MintDBHelper open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    
    /*
     * custom methods
     */
    /*
     * about consumption
     */
    public long createConsumption(String consumptionTitle, float price, int consumptionType, String date, int quantity, String notes, int status) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONSUMPTION_TITLE, consumptionTitle);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_CONSUMPTION_TYPE, consumptionType);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_QUANTITY, quantity);
        initialValues.put(KEY_NOTES, notes);
        initialValues.put(KEY_STATUS, status);
        return mDb.insert(DATABASE_TABLE_CONSUMPTION, null, initialValues);
    }
    
    public boolean deleteConsumption(long rowId) {
        return mDb.delete(DATABASE_TABLE_CONSUMPTION, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateConsumption(long rowId,String consumptionTitle, float price, int consumptionType, String date, int quantity, String notes, int status) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONSUMPTION_TITLE, consumptionTitle);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_CONSUMPTION_TYPE, consumptionType);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_QUANTITY, quantity);
        initialValues.put(KEY_NOTES, notes);
        initialValues.put(KEY_STATUS, status);
        return mDb.update(DATABASE_TABLE_CONSUMPTION, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllConsumption() {
        return mDb.query(DATABASE_TABLE_CONSUMPTION, new String[] {KEY_ROWID,
        		KEY_CONSUMPTION_TITLE,KEY_PRICE,KEY_CONSUMPTION_TYPE,KEY_DATE,KEY_QUANTITY,KEY_NOTES,KEY_STATUS}, null, null, null, null, KEY_DATE+" DESC");
    }
    
    public Cursor fetchConsumptionById(long id) {
        return mDb.query(DATABASE_TABLE_CONSUMPTION, new String[] {KEY_ROWID,
        		KEY_CONSUMPTION_TITLE,KEY_PRICE,KEY_CONSUMPTION_TYPE,KEY_DATE,KEY_QUANTITY,KEY_NOTES,KEY_STATUS}, KEY_ROWID+"="+id, null, null, null, KEY_DATE+" DESC");
    }
    public Cursor fetchConsumptionByThings(String sqlString) {
    	return mDb.query(DATABASE_TABLE_CONSUMPTION, new String[] {KEY_ROWID,
    			KEY_CONSUMPTION_TITLE,KEY_PRICE,KEY_CONSUMPTION_TYPE,KEY_DATE,KEY_QUANTITY,KEY_NOTES,KEY_STATUS}, sqlString, null, null, null, KEY_DATE+" DESC");
    }
    
    /*
     * about rank
     */
    public long createRank(String consumptionTitle, float lastPrice, int counts) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONSUMPTION_TITLE, consumptionTitle);
        initialValues.put(KEY_LASTPRICE, lastPrice);
        initialValues.put(KEY_COUNTS, counts);
        return mDb.insert(DATABASE_TABLE_RANK, null, initialValues);
    }
    
    public boolean deleteRank(long rowId) {
        return mDb.delete(DATABASE_TABLE_RANK, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateRank(long rowId,String consumptionTitle, float lastPrice, int counts) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONSUMPTION_TITLE, consumptionTitle);
        initialValues.put(KEY_LASTPRICE, lastPrice);
        initialValues.put(KEY_COUNTS, counts);
        return mDb.update(DATABASE_TABLE_RANK, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllRank() {
        return mDb.query(DATABASE_TABLE_RANK, new String[] {KEY_ROWID,
        		KEY_CONSUMPTION_TITLE,KEY_LASTPRICE,KEY_COUNTS}, null, null, null, null, KEY_COUNTS+" DESC");
    }
    
    /*
     * about debt
     */
    public long createDebt(String debtor, float price, String date, String notes, int status, int clear) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DEBTOR, debtor);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_NOTES, notes);
        initialValues.put(KEY_STATUS, status);
        initialValues.put(KEY_CLEAR, clear);
        return mDb.insert(DATABASE_TABLE_DEBT, null, initialValues);
    }
    
    public boolean deleteDebt(long rowId) {
        return mDb.delete(DATABASE_TABLE_DEBT, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateDebt(long rowId,String debtor, float price, String date, String notes, int status, int clear) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_DEBTOR, debtor);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_NOTES, notes);
        initialValues.put(KEY_STATUS, status);
        initialValues.put(KEY_CLEAR, clear);
        return mDb.update(DATABASE_TABLE_DEBT, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
    }
    public boolean simpleUpdateDebt(long rowId,int clear) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_CLEAR, clear);
    	return mDb.update(DATABASE_TABLE_DEBT, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllDebt() {
        return mDb.query(DATABASE_TABLE_DEBT, new String[] {KEY_ROWID,
        		KEY_DEBTOR,KEY_PRICE,KEY_DATE,KEY_NOTES,KEY_STATUS,KEY_CLEAR}, null, null, null, null, KEY_CLEAR+", "+KEY_DATE+" DESC");
    }
    
    public Cursor fetchDebt(long mRowId){
    	return mDb.query(DATABASE_TABLE_DEBT, new String[] {KEY_ROWID,
        		KEY_DEBTOR,KEY_PRICE,KEY_DATE,KEY_NOTES,KEY_STATUS,KEY_CLEAR}, KEY_ROWID+" = "+mRowId, null, null, null, null);
    }
    
    /*
     * about budget
     */
    public long createBudget(int consumptionType, float budget, float out, String cycle) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONSUMPTION_TYPE, consumptionType);
        initialValues.put(KEY_BUDGET, budget);
        initialValues.put(KEY_OUT, out);
        initialValues.put(KEY_CYCLE, cycle);
        return mDb.insert(DATABASE_TABLE_BUDGET, null, initialValues);
    }
    
    public boolean deleteBudget(long rowId) {
        return mDb.delete(DATABASE_TABLE_BUDGET, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateBudget(long rowId,int consumptionType, float budget, float out, String cycle) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_CONSUMPTION_TYPE, consumptionType);
        initialValues.put(KEY_BUDGET, budget);
        initialValues.put(KEY_OUT, out);
        initialValues.put(KEY_CYCLE, cycle);
        return mDb.update(DATABASE_TABLE_BUDGET, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllBudget() {
        return mDb.query(DATABASE_TABLE_BUDGET, new String[] {KEY_ROWID,
        		KEY_CONSUMPTION_TYPE,KEY_BUDGET,KEY_OUT,KEY_CYCLE}, null, null, null, null, null);
    }
    public Cursor fetchBudgetById(long id) {
    	return mDb.query(DATABASE_TABLE_BUDGET, new String[] {KEY_ROWID,
    			KEY_CONSUMPTION_TYPE,KEY_BUDGET,KEY_OUT,KEY_CYCLE}, KEY_ROWID+"="+id, null, null, null, null);
    }
    
    
    
    public Cursor fetchBudgetByTypeandDate(int consumptionType, String dateString){
    	
    	return mDb.query(DATABASE_TABLE_BUDGET, new String[] {KEY_ROWID,
        		KEY_CONSUMPTION_TYPE,KEY_BUDGET,KEY_OUT,KEY_CYCLE}, KEY_CONSUMPTION_TYPE +"="+consumptionType+" and "+KEY_CYCLE + "='"+dateString+"'", null, null, null, null);
    }
}
