package com.andre.solid.solidchat.stuff;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.andre.solid.solidchat.MyApplication;
import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.events.OpenFilePickerEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lantain on 08.04.17.
 */

public class Utils {

    public static void showToastMessage(@StringRes int messageRes) {
        Toast.makeText(MyApplication.get(), messageRes, Toast.LENGTH_SHORT).show();
    }

    public static void showToastMessage(String message) {
        Toast.makeText(MyApplication.get(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showFileChooser(final Activity activity, final int requestCode) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.dialog_file_chooser_title);
            builder.setItems(R.array.dialog_file_chooser_items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (which) {
                        case 0:
                            Utils.openFilePicker(activity, activity.getString(R.string.mime_jpeg), requestCode);
                            break;
                        case 1:
                            Utils.openFilePicker(activity, activity.getString(R.string.mime_doxc), requestCode);
                            break;
                        case 2:
                            Utils.openFilePicker(activity, activity.getString(R.string.mime_doc), requestCode);
                            break;
                        case 3:
                            Utils.openFilePicker(activity, activity.getString(R.string.mime_pdf), requestCode);
                            break;
                        case 4:
                            Utils.openFilePicker(activity, "", requestCode);
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        } else {
            Utils.aggreOrDeclineDialog(activity, activity.getString(R.string.permission_request_title), activity.getString(R.string.permission_request_message_storage), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                }
            }, null);
        }
    }

    public static void showFileChooser(final Fragment fragment, final int requestCode) {
        if (ContextCompat.checkSelfPermission(fragment.getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
            builder.setTitle(R.string.dialog_file_chooser_title);
            builder.setItems(R.array.dialog_file_chooser_items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (which) {
                        case 0:
                            Utils.openFilePicker(fragment, fragment.getString(R.string.mime_jpeg), requestCode);
                            break;
                        case 1:
                            Utils.openFilePicker(fragment, fragment.getString(R.string.mime_doc), requestCode);
                            break;
                        case 2:
                            Utils.openFilePicker(fragment, fragment.getString(R.string.mime_doxc), requestCode);
                            break;
                        case 3:
                            Utils.openFilePicker(fragment, fragment.getString(R.string.mime_pdf), requestCode);
                            break;
                        case 4:
                            Utils.openFilePicker(fragment, "", requestCode);
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        } else {
            Utils.aggreOrDeclineDialog(fragment.getContext(), fragment.getString(R.string.permission_request_title), fragment.getString(R.string.permission_request_message_storage), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                }
            }, null);
        }
    }

    public static void openFilePicker(Activity activity, String mimeType, int requestCode) {
        EventBus.getDefault().post(new OpenFilePickerEvent());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        try {
            activity.startActivityForResult(
                    Intent.createChooser(intent, "Select a File"),
                    requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Utils.showToastMessage("Please install a File Manager");
        }
    }

    public static void openFilePicker(Fragment fragment, String mimeType, int requestCode) {
        EventBus.getDefault().post(new OpenFilePickerEvent());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        try {
            fragment.startActivityForResult(
                    Intent.createChooser(intent, "Select a File"),
                    requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Utils.showToastMessage("Please install a File Manager");
        }
    }

    public static void aggreOrDeclineDialog(Context context, String title, String message, DialogInterface.OnClickListener accept, DialogInterface.OnClickListener decline) {
        aggreOrDeclineDialog(context, title, message, context.getString(android.R.string.yes), context.getString(android.R.string.no), accept, decline);
    }

    public static void aggreOrDeclineDialog(Context context, String title, String message, String acceptText, String declineText, DialogInterface.OnClickListener accept, DialogInterface.OnClickListener decline) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(acceptText, accept);
        builder.setNegativeButton(declineText, decline);
        builder.show();
    }

}
