package com.snhu.cs360project;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.LinkedList;
import java.util.List;


public class ItemsDataBaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "items.db";
    private static final int VERSION = 1;
    public ItemsDataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    private static final class ItemTable {
        private static final String TABLE = "items";
        private static final String COL_ID = "_id";
        private static final String COL_ITEMNAME = "itemname";
        private static final String COL_COUNT = "count";
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
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // https://www.tutorialspoint.com/example-to-create-a-table-with-all-datatypes-in-mysql-using-jdbc
        db.execSQL("CREATE TABLE " + ItemTable.TABLE + " (" +
                ItemTable.COL_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                ItemTable.COL_ITEMNAME + " TEXT, " +
                ItemTable.COL_COUNT + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not fully made, should not ever happen anyway.
        db.execSQL("drop table if exists " + ItemTable.TABLE);
        onCreate(db);
    }

    public long addItem(String item) {
        // -1 is item not found.
        if (item.isEmpty())
            return -1;

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ItemTable.COL_ITEMNAME, item);
        values.put(ItemTable.COL_COUNT, 0);

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
        int changed = db.update(ItemTable.TABLE, values, "_id = ?",
                new String[] { Long.toString(id) });
        return changed >= 1;
    }
}
