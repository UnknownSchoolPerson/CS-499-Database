package com.snhu.cs360project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class DescDialog extends DialogFragment {
    /** @noinspection EmptyMethod*/
    interface DescDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNeutralClick(DialogFragment dialog);

    }
    DescDialogListener listener;
    public EditText input;
    // https://developer.android.com/develop/ui/views/components/dialogs
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        // Build the dialog and set up the button click handlers
        // Listeners will be made for the fragment to handle.
        builder.setMessage(R.string.change_description)
                .setPositiveButton(R.string.change, (dialog, id) -> {
                    // Send the positive button event back to the host activity
                    listener.onDialogPositiveClick(this);
                })
                .setNeutralButton(R.string.add_cancel, (dialog, id) -> {
                    // Send the neutral button event back to the host activity
                    listener.onDialogNeutralClick(this);
                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // https://stackoverflow.com/questions/13733304/callback-to-a-fragment-from-a-dialogfragment
        // This will find the DetailFragment and attach the listener to it.
        // Since only one DetailFragment should ever be open, this shouldn't cause a problem.
        // This took me to long to figure out.
        // I can think of a few ways to make this always get the caller, but this works.
        FragmentManager fragmentManager = getParentFragmentManager();
        List<Fragment> fragmentList = fragmentManager.getFragments();
        Fragment correctFrag = null;
        for (Fragment frag : fragmentList) {
            if (frag instanceof DetailFragment) {
                correctFrag = frag;
            }
        }
        if (correctFrag == null)
            throw new ClassCastException("Fragment missing.");
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (DescDialogListener) correctFrag;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Class must implement NoticeDialogListener");
        }
    }


}
