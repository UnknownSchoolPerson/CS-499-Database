package com.snhu.cs360project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserDataBaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "users.db";
    private static final int VERSION = 1;
    private UserDataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
        private static final String COL_SALT = "salt";
    }

    public String getActiveUser() {
        return activeUser;
    }

    private String activeUser = null;
    private static UserDataBaseHandler single_instance = null;
    // Good idea from https://stackoverflow.com/a/1944842 to get a 'global'
    // https://www.geeksforgeeks.org/singleton-class-java/#
    public static synchronized UserDataBaseHandler getInstance(Context context)
    {
        if (single_instance == null)
            single_instance = new UserDataBaseHandler(context);

        return single_instance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // https://www.tutorialspoint.com/example-to-create-a-table-with-all-datatypes-in-mysql-using-jdbc
        //Log.d(TAG, "onCreate: User");
        db.execSQL("CREATE TABLE " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_USERNAME + " TEXT UNIQUE, " +
                UserTable.COL_PASSWORD + " BINARY, " +
                UserTable.COL_SALT + " BINARY)");
        //Log.d(TAG, "onCreate: User Done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + UserTable.TABLE);
        onCreate(db);
    }

    public long addUser(String user, String password) {
        if (user.isEmpty() || password.isEmpty())
            return -1;

        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT " + UserTable.COL_USERNAME +
                " FROM " + UserTable.TABLE +
                " WHERE username = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { user });
        if (cursor.getCount() >= 1) {
            cursor.close();
            return -1;
        }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, user);

        // https://www.baeldung.com/java-password-hashing
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        byte[] hashedPassword;
        hashedPassword = hashPassword(salt, password);
        values.put(UserTable.COL_PASSWORD, hashedPassword);
        values.put(UserTable.COL_SALT, salt);
        activeUser = user;
        return db.insert(UserTable.TABLE, null, values);
    }

    private byte[] hashPassword(byte[] salt, String password) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        byte[] hashedPassword;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hashedPassword = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        //Log.d(TAG, "hashed: " + Arrays.toString(hashedPassword));
        return hashedPassword;
    }

    public boolean validUser(String user, String password) {
        if (user.isEmpty() || password.isEmpty())
            return false;
        boolean valid = false;
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT " + UserTable.COL_PASSWORD + ", " + UserTable.COL_SALT +
                " FROM " + UserTable.TABLE +
                " WHERE username = ?";
        //Log.d(TAG, "validUser: " + sql);
        Cursor cursor = db.rawQuery(sql, new String[] { user });

        if (cursor.moveToFirst()) {
            byte[] passwordDB = cursor.getBlob(0);
            byte[] saltDB = cursor.getBlob(1);
            byte[] userInputHashed = hashPassword(saltDB, password);
            if (Arrays.equals(userInputHashed, passwordDB)) {
                valid = true;
                activeUser = user;
            }
        }
        cursor.close();
        return valid;
    }
}
