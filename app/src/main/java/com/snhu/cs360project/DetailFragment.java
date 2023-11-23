package com.snhu.cs360project;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class DetailFragment extends Fragment implements DeleteDialog.DeleteDialogListener, ChangeDialog.ChangeDialogListener{

    public static final String ARG_ITEM_ID = "item_id";
    private ItemsDataBaseHandler.Item dbItem;
    private ItemsDataBaseHandler itemsDB;
    private long itemID;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private PermSMSFileHandler permSMSFileHandler;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemID = 1;

        // Get the item ID from the fragment arguments
        Bundle args = getArguments();
        if (args != null) {
            itemID = args.getLong(ARG_ITEM_ID);
        }

        itemsDB = new ItemsDataBaseHandler(getContext());
        // Getting the item info.
        dbItem = itemsDB.getByID(itemID);

        //https://developer.android.com/guide/fragments/appbar
        setHasOptionsMenu(true);
        permSMSFileHandler = PermSMSFileHandler.getInstance(requireActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        nameTextView = rootView.findViewById(R.id.item_name);
        descriptionTextView = rootView.findViewById(R.id.item_description);
        if (dbItem != null) {
            updateThisScreen();
        }

        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Determine which menu option was chosen
        if (item.getItemId() == R.id.action_delete) {
            DeleteDialog dialog = new DeleteDialog();
            dialog.show(getParentFragmentManager(), "deleteDialog");
            return true;
        } else if (item.getItemId() == R.id.action_change_amount) {
            ChangeDialog dialog = new ChangeDialog();
            dialog.show(getParentFragmentManager(), "changeDialog");
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void deleteItem() {
        itemsDB.deleteByID(dbItem.get_id());
        // Used https://stackoverflow.com/questions/10863572/programmatically-go-back-to-the-previous-fragment-in-the-backstack
        // But found it crashed when trying to go back to a DetailFragment again.
        // I looked at how ListFragment was getting here and found Navigation has a navigateUp func.
        //getParentFragmentManager().popBackStack();
        Navigation.findNavController(requireView()).navigateUp();
        Toast.makeText(getContext(),dbItem.getName() + " Deleted", Toast.LENGTH_SHORT).show();
    }

    private void changeAmount(int amount) {
        itemsDB.UpdateCountByID(itemID, amount);
        dbItem = itemsDB.getByID(itemID);
        updateThisScreen();
    }
    public void updateThisScreen() {
        nameTextView.setText(dbItem.getName());
        descriptionTextView.setText(getString(R.string.detail_item_amount, dbItem.getCount(), dbItem.get_id()));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // These will check to see what Dialog sent the event.
        if (dialog instanceof DeleteDialog)
            deleteItem();
        else if (dialog instanceof ChangeDialog)
        {
            String userInput = ((ChangeDialog)dialog).input.getText().toString();
            if (userInput.isEmpty()) {
                Toast.makeText(getContext(), R.string.gen_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            // This should never not be an int as empty is already checked and in
            // add dialog it checks if it's possible to be an int already.
            int userInputInt = Integer.parseInt(userInput);
            changeAmount(userInputInt);
            permSMSFileHandler.checkIfAlert(requireActivity(), dbItem);

        }
    }

    // Not used, but needed for events.
    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {

    }
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

}