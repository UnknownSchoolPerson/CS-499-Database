package com.snhu.cs360project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

/** @noinspection ALL*/
public class LowInvDialog extends DialogFragment {
    interface LowInvDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        /** @noinspection EmptyMethod*/
        void onDialogNeutralClick(DialogFragment dialog);

    }
    LowInvDialogListener listener;
    public EditText input;
    // https://developer.android.com/develop/ui/views/components/dialogs
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        input = new EditText(getContext());
        // https://stackoverflow.com/questions/9133937/android-emulator-insert-negative-number
        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String userInput = s.toString();
                // Nothing is find to display and allow in text.
                if (s.length() == 0)
                    return;
                // Just a negative symbol is fine.
                // Allowing negatives is valid for this app
                if (s.charAt(0) == '-')
                    return;
                // This will try to see if the user input is 32-bit number (or one at all).
                try {
                    Integer.parseInt(userInput);
                } catch (Exception e) {
                    // If not, remove last input.
                    // This should never happen with an empty string, thus is always safe to use.
                    Toast.makeText(getContext(), R.string.gen_invalid, Toast.LENGTH_SHORT).show();
                    String safeText = userInput.substring(0, userInput.length() - 1);
                    input.setText(safeText);
                    input.setSelection(safeText.length());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        builder.setView(input);
        // Build the dialog and set up the button click handlers
        // Listeners will be made for the fragment to handle.
        builder.setMessage(R.string.change_low_inv_alert)
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
            if (frag instanceof LowInvDialogListener) {
                correctFrag = frag;
            }
        }
        if (correctFrag == null)
            throw new ClassCastException("Fragment missing.");
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (LowInvDialogListener) correctFrag;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Class must implement NoticeDialogListener");
        }
    }


}
