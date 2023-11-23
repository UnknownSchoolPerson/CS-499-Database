package com.snhu.cs360project;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ListFragment extends Fragment implements  AddDialog.AddDialogListener, LowInvDialog.LowInvDialogListener {
    ItemsDataBaseHandler itemsDB;
    View rootView;
    private PermSMSFileHandler permSMSFileHandler;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        itemsDB = new ItemsDataBaseHandler(getContext());
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        this.rootView = rootView;
        fillRecycler(rootView);

        // https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        FloatingActionButton fab = rootView.findViewById(R.id.add_fab);
        fab.setOnClickListener(view -> {
            AddDialog dialog = new AddDialog();
            dialog.show(getParentFragmentManager(), "addDialog");
        });
        //https://developer.android.com/guide/fragments/appbar
        setHasOptionsMenu(true);
        permSMSFileHandler = PermSMSFileHandler.getInstance(requireActivity());
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Determine which menu option was chosen
        if (item.getItemId() == R.id.action_notifications) {
            if (permSMSFileHandler.checkSms(requireActivity())) {
                Toast.makeText(getContext(), R.string.notifications_good, Toast.LENGTH_SHORT).show();
                LowInvDialog dialog = new LowInvDialog();
                dialog.show(getParentFragmentManager(), "LowInvDialog");
            } else {
                Toast.makeText(getContext(), R.string.notifications_bad, Toast.LENGTH_SHORT).show();
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    private void fillRecycler(View rootView) {
        View.OnClickListener onClickListener = itemView -> {

            // Create fragment arguments containing the selected item ID
            long selectedItemId = (long) itemView.getTag();
            Bundle args = new Bundle();
            args.putLong(DetailFragment.ARG_ITEM_ID, selectedItemId);

            // Replace list with details
            Navigation.findNavController(itemView).navigate(R.id.show_item_detail, args);
        };
        RecyclerView recyclerView = rootView.findViewById(R.id.item_list);
        List<ItemsDataBaseHandler.Item> items = itemsDB.getAllItems();
        recyclerView.setAdapter(new ItemAdapter(items, onClickListener));
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
    }
    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {

        private final List<ItemsDataBaseHandler.Item> mItems;
        private final View.OnClickListener mOnClickListener;

        public ItemAdapter(List<ItemsDataBaseHandler.Item> items, View.OnClickListener onClickListener) {
            mItems = items;
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ItemHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            ItemsDataBaseHandler.Item item = mItems.get(position);
            holder.bind(item);
            holder.itemView.setTag(item.get_id());
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private final TextView mNameTextView;

        public ItemHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            mNameTextView = itemView.findViewById(R.id.item_name);
        }

        public void bind(ItemsDataBaseHandler.Item item) {
            mNameTextView.setText(getString(R.string.list_item, item.getName(), item.getCount()));
        }
    }
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof AddDialog) {
            String userInput = ((AddDialog) dialog).input.getText().toString();
            if (userInput.isEmpty()) {
                Toast.makeText(getContext(), R.string.gen_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            itemsDB.addItem(userInput);
            fillRecycler(rootView);
        } else if (dialog instanceof LowInvDialog)
        {
            String userInput = ((LowInvDialog)dialog).input.getText().toString();
            if (!userInput.isEmpty()) {
                permSMSFileHandler.setLowInv(requireActivity(), Integer.parseInt(userInput), itemsDB);
                return;
            }
            permSMSFileHandler.checkIfAlert(requireActivity(), itemsDB.getAllItems());
        }
    }
    public void onDialogNeutralClick(DialogFragment dialog) {
    }
}
