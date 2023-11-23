package com.snhu.cs360project;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class ItemsDataBaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "items.db";
    private static final int VERSION = 2;
    private static ItemsDataBaseHandler single_instance = null;
    private final String ActiveUser;
    private ItemsDataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        UserDataBaseHandler userDB = UserDataBaseHandler.getInstance(context);
        ActiveUser = userDB.getActiveUser();
    }
    private static final class ItemTable {
        private static final String TABLE = "items";
        private static final String COL_ID = "_id";
        private static final String COL_ITEMNAME = "itemname";
        private static final String COL_COUNT = "count";
        private static final String COL_DESC = "description";
        private static final String COL_LASTUSER = "lastuser";
        private static final String COL_LASTDATE = "lastdate";
    }
    //https://www.geeksforgeeks.org/singleton-class-java/#
    public static synchronized ItemsDataBaseHandler getInstance(Context context)
    {
        if (single_instance == null)
            single_instance = new ItemsDataBaseHandler(context);

        return single_instance;
    }

    public static class Item {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long get_id() {
            return _id;
        }

        public void set_id(long _id) {
            this._id = _id;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        private String name;
        private long _id;
        private int count;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getLastUser() {
            return lastUser;
        }

        public void setLastUser(String lastUser) {
            this.lastUser = lastUser;
        }

        public String getLastDate() {
            return lastDate;
        }

        public void setLastDate(String lastDate) {
            this.lastDate = lastDate;
        }

        private String desc;
        private String lastUser;
        private String lastDate;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // https://www.tutorialspoint.com/example-to-create-a-table-with-all-datatypes-in-mysql-using-jdbc
        db.execSQL("CREATE TABLE " + ItemTable.TABLE + " (" +
                ItemTable.COL_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                ItemTable.COL_ITEMNAME + " TEXT, " +
                ItemTable.COL_COUNT + " INTEGER," +
                ItemTable.COL_DESC + " TEXT DEFAULT 'No Description'," +
                ItemTable.COL_LASTUSER + " TEXT DEFAULT 'No User'," +
                ItemTable.COL_LASTDATE + " TEXT DEFAULT 'No Date')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE " + ItemTable.TABLE + " ADD COLUMN " + ItemTable.COL_DESC + " TEXT DEFAULT 'No Description'");
        db.execSQL("ALTER TABLE " + ItemTable.TABLE + " ADD COLUMN " + ItemTable.COL_LASTUSER + " TEXT DEFAULT 'No User'");
        db.execSQL("ALTER TABLE " + ItemTable.TABLE + " ADD COLUMN " + ItemTable.COL_LASTDATE + " TEXT DEFAULT 'No Date'");
    }

    public long addItem(String item, String lastUser) {
        // -1 is item not found.
        if (item.isEmpty())
            return -1;

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ItemTable.COL_ITEMNAME, item);
        values.put(ItemTable.COL_COUNT, 0);
        values.put(ItemTable.COL_DESC, "No description");
        values.put(ItemTable.COL_LASTUSER, lastUser);
        values.put(ItemTable.COL_LASTDATE, CurrentDate());

        return db.insert(ItemTable.TABLE, null, values);
    }

    public List<Item> getAllItems() {
        List<Item> items = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * " + " FROM " + ItemTable.TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        // Gets all items, puts each item data in a Item Object than in a LinkedList.
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.set_id(cursor.getLong(0));
                item.setName(cursor.getString(1));
                item.setCount(cursor.getInt(2));
                item.setDesc(cursor.getString(3));
                item.setLastUser(cursor.getString(4));
                item.setLastDate(cursor.getString(5));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public Item getByID(long _ID) {
        Item item;
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * " + " FROM " + ItemTable.TABLE + " WHERE _id = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {Long.toString(_ID)});

        if (cursor.moveToFirst()) {
            item = new Item();
            item.set_id(cursor.getLong(0));
            item.setName(cursor.getString(1));
            item.setCount(cursor.getInt(2));
            item.setDesc(cursor.getString(3));
            item.setLastUser(cursor.getString(4));
            item.setLastDate(cursor.getString(5));
        }
        else
            item = null; // Item not found.
        cursor.close();
        return item;
    }

    public boolean deleteByID(long id) {
        SQLiteDatabase db = getWritableDatabase();
        //String sql = "SELECT * " + " FROM " + ItemTable.TABLE + " WHERE _id = ?";
        int deleted = db.delete(ItemTable.TABLE, ItemTable.COL_ID + "= ?",
                new String[] {Long.toString(id)});
        return deleted >= 1;
    }

    public boolean UpdateCountByID(long id, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemTable.COL_COUNT, count);
        values.put(ItemTable.COL_LASTUSER, ActiveUser);
        values.put(ItemTable.COL_LASTDATE, CurrentDate());
        int changed = db.update(ItemTable.TABLE, values, "_id = ?",
                new String[] { Long.toString(id) });
        return changed >= 1;
    }
    public boolean UpdateDescByID(long id, String desc) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemTable.COL_DESC, desc);
        values.put(ItemTable.COL_LASTUSER, ActiveUser);
        values.put(ItemTable.COL_LASTDATE, CurrentDate());
        int changed = db.update(ItemTable.TABLE, values, "_id = ?",
                new String[] { Long.toString(id) });
        return changed >= 1;
    }
    private String CurrentDate() {
        // https://www.javatpoint.com/java-get-current-date
        // https://www.geeksforgeeks.org/dateformat-getdatetimeinstance-method-in-java-with-examples/
        DateFormat DFormat = DateFormat.getDateTimeInstance();
        Date date = new Date();
        return DFormat.format(date);
    }
}
