package edu.hm.eem_library.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Objects;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ClientItemViewModel;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;
import edu.hm.eem_library.net.ClientItem;

/**
 * Subclass of {@link ItemRecyclerViewAdapter}. For more info on the {@link ItemRecyclerViewAdapter} family, check out
 * {@link ItemRecyclerViewAdapter}.
 */
public class ClientItemRecyclerviewAdapter extends NameTabRecyclerViewAdapter {
    private final Context context;


    /**
     * Constructor
     *
     * @param liveData liveData holding the data for this adapter
     * @param context  calling activity
     * @param listener listener for on item clicks
     * @param content  Indicator for the recyclerview fragment holding this adapter
     */
    ClientItemRecyclerviewAdapter(SelectableSortableItemLiveData<?, SelectableSortableItem<?>> liveData, Context context, ItemListFragment.OnListFragmentPressListener listener, ItemListContent content) {
        super(liveData, context, listener, content, false);
        this.context = context;
    }

    /**
     * Create viewholders to fill the screen
     *
     * @param parent   Android basics
     * @param viewType Android basics
     * @return the viewholder
     */
    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.studentdevice_item, parent, false);
        return new StudentDeviceViewHolder(v);
    }

    /**
     * The viewholder for this adapter
     */
    class StudentDeviceViewHolder extends NameTabRecyclerViewAdapter.NameTabViewHolder {
        final ConstraintLayout layout;
        final ConstraintSet constraintSet = new ConstraintSet();
        final TextView countNotificationDrawer;
        final int disconnectedColor = context.getColor(R.color.disconnected);

        /**
         * Constructor
         *
         * @param view inflated view of this viewholder
         */
        StudentDeviceViewHolder(View view) {
            super(view);
            icon.setImageResource(R.drawable.ic_student);
            layout = view.findViewById(R.id.itemlayout);
            countNotificationDrawer = view.findViewById(R.id.count_notification_drawer);
        }

        /**
         * Indicate, that this student is currently "lighthoused"
         */
        private void lighthouse() {
            int position = getAdapterPosition();
            ((ClientItemViewModel.ClientItemLiveData) liveData).lighthouse(position);
            listener.onListFragmentPress(position);
        }

        /**
         * Initialize this viewholder from livedata
         *
         * @param position view is at this position in list
         */
        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            ClientItem device = (ClientItem) Objects.requireNonNull(liveData.getValue()).get(position).item;
            constraintSet.clone(layout);
            if (device.lighthoused)
                constraintSet.connect(R.id.card_item, ConstraintSet.LEFT, R.id.lighthouse, ConstraintSet.RIGHT);
            else
                constraintSet.connect(R.id.card_item, ConstraintSet.LEFT, R.id.itemlayout, ConstraintSet.LEFT);
            constraintSet.applyTo(layout);
            if (device.countNotificationDrawer > 0) {
                countNotificationDrawer.setVisibility(View.VISIBLE);
                countNotificationDrawer.setText(context.getResources().getQuantityString(R.plurals.notification_drawer_has_been_opened, device.countNotificationDrawer, device.countNotificationDrawer));
            } else {
                countNotificationDrawer.setVisibility(View.GONE);
            }
            if (device.disconnected) {
                item.setCardBackgroundColor(disconnectedColor);
                countNotificationDrawer.setBackgroundColor(disconnectedColor);
                selectedCb.setText(R.string.client_disconnected);
            } else {
                selectedCb.setText(R.string.documents_checked);
            }
        }

        @Override
        void setSelected(boolean selected) {
            item.setCardBackgroundColor(selected ? colorPrimary : colorPrimaryLight);
            selectedCb.setChecked(selected);
        }

        @Override
        void setInteractions() {
            view.setOnClickListener(v -> lighthouse());
        }
    }
}
