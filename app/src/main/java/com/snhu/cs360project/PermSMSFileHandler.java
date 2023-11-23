package com.snhu.cs360project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermSMSFileHandler {
    private static final int REQUEST_SEND_SMS_AND_PUSH_PERMISSION = 0;
    private static PermSMSFileHandler instance;
    private int lowInvAmount;
    private PermSMSFileHandler(Activity activity) {
        lowInvAmount = getLowInv(activity);
    }
    public static synchronized PermSMSFileHandler getInstance(Activity activity) {
        if (instance == null)
            instance = new PermSMSFileHandler(activity);
        return instance;
    }
    public int getLowInvAmount() {
        return lowInvAmount;
    }
    private int getLowInv(Activity activity) {
        //https://www.tutorialspoint.com/how-to-make-a-txt-file-in-internal-storage-in-android
        //https://www.baeldung.com/java-filereader
        int lowInv = 5;
        File file = new File(activity.getFilesDir(), "lowInvSave");
        if (!file.exists())
            try {
                //file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.append('5');
                writer.flush();
                writer.close();
                //Toast.makeText(getActivity(), "Saved your text", Toast.LENGTH_LONG).show();
            } catch (Exception ignored) { }
        else {
            try {
                FileReader reader = new FileReader(file);
                StringBuilder content = new StringBuilder();
                int nextChar;
                while ((nextChar = reader.read()) != -1) {
                    content.append((char) nextChar);
                }
                lowInv = Integer.parseInt(content.toString());
                //Toast.makeText(getActivity(), Integer.toString(lowInv), Toast.LENGTH_LONG).show();
                reader.close();
            } catch (Exception ignored) { }
        }
        return lowInv;
    }
    public void checkIfAlert(Activity activity, @NonNull ItemsDataBaseHandler.Item item) {
        StringBuilder sb = new StringBuilder();
        String lowInvText = "LOW INVENTORY!";
        sb.append(lowInvText);
        if (item.getCount() <= lowInvAmount) {
            sb.append('\n');
            sb.append(item.getName());
            sb.append(" Has ");
            sb.append(item.getCount());
            sendSms(activity, sb.toString());
        }
    }
    public void checkIfAlert(Activity activity, @NonNull List<ItemsDataBaseHandler.Item> items) {
        StringBuilder sb = new StringBuilder();
        String lowInvText = "LOW INVENTORY!";
        sb.append(lowInvText);
        for (ItemsDataBaseHandler.Item item : items) {
            if (item.getCount() <= lowInvAmount) {
                sb.append('\n');
                sb.append(item.getName());
                sb.append(" Has ");
                sb.append(item.getCount());
            }
        }
        if (sb.length() != lowInvText.length())
            sendSms(activity, sb.toString());
    }
    boolean setLowInv(@NonNull Activity activity, int lowInv, ItemsDataBaseHandler itemsDB) {
        File file = new File(activity.getFilesDir(), "lowInvSave");
        try {
            // If the file exist, than it will be deleted.
            if (file.exists() && !file.delete()) {
                return false;
            }
            FileWriter writer = new FileWriter(file);
            writer.append(Integer.toString(lowInv));
            writer.flush();
            writer.close();
        } catch (Exception ignored) {
            return false;
        }
        lowInvAmount = lowInv;
        checkIfAlert(activity, itemsDB.getAllItems());
        return true;
    }
    public boolean sendSms (Activity activity, String text) {
        // https://www.tutorialspoint.com/android/android_sending_sms.htm
        if (!checkSms(activity))
            return false;
        SmsManager smsManager = SmsManager.getDefault();
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        // Checked with checkSms
        @SuppressLint({"MissingPermission", "HardwareIds"}) String userNum = telephonyManager.getLine1Number();
        if (userNum == null)
            return false;
        smsManager.sendTextMessage(userNum, null, text, null, null);
        return true;
    }
    public boolean checkSms(Activity activity) {
        // Checks if has SMS and Notifications permissions.
        if (ActivityCompat.checkSelfPermission(activity,
                android.Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity,
                        android.Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity,
                        android.Manifest.permission.READ_PHONE_NUMBERS) !=
                        PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity,
                        android.Manifest.permission.READ_SMS) !=
                        PackageManager.PERMISSION_GRANTED){
            // Checks if on API version that can request notifications at runtime.
            // If not only ask for SMS
            List<String> permissions = new ArrayList<>(Arrays.asList(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    new String[]{android.Manifest.permission.SEND_SMS, android.Manifest.permission.POST_NOTIFICATIONS, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_SMS} :
                    new String[]{android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_SMS}));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                permissions.add(Manifest.permission.READ_PHONE_NUMBERS);
            // https://www.geeksforgeeks.org/convert-list-to-array-in-java/
            String[] permissionsArr = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity,
                    permissionsArr,
                    REQUEST_SEND_SMS_AND_PUSH_PERMISSION);
            return false;
        } else {
            return true;
        }
    }
}
