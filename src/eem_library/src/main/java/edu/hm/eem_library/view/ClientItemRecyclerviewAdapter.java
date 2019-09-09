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

public class ClientItemRecyclerviewAdapter extends NameTabRecyclerViewAdapter {
    private final Context context;


    ClientItemRecyclerviewAdapter(SelectableSortableItemLiveData<?, SelectableSortableItem<?>> liveData, Context context, ItemListFragment.OnListFragmentPressListener listener, ItemListContent content) {
        super(liveData, context, listener, content, false);
        this.context = context;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.studentdevice_item, parent, false);
        return new StudentDeviceViewHolder(v);
    }

    public class StudentDeviceViewHolder extends NameTabRecyclerViewAdapter.NameTabViewHolder {
        final ConstraintLayout layout;
        final ConstraintSet constraintSet = new ConstraintSet();
        final TextView countNotificationDrawer;
        final int disconnectedColor = context.getColor(R.color.disconnected);

        StudentDeviceViewHolder(View view) {
            super(view);
            icon.setImageResource(R.drawable.ic_student);
            layout = view.findViewById(R.id.itemlayout);
            countNotificationDrawer = view.findViewById(R.id.count_notification_drawer);
        }

        private void lighthouse() {
            int position = getAdapterPosition();
            ((ClientItemViewModel.ClientItemLiveData) liveData).lighthouse(position);
            listener.onListFragmentPress(position);
        }

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
                countNotificationDrawer.setText(context.getString(R.string.notification_drawer_has_been_opened, device.countNotificationDrawer));
            } else {
                countNotificationDrawer.setVisibility(View.GONE);
            }
            if (device.disconnected) {
                item.setCardBackgroundColor(disconnectedColor);
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
