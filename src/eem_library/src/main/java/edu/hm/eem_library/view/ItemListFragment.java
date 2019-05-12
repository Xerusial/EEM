package edu.hm.eem_library.view;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.DeviceViewModel;
import edu.hm.eem_library.model.HostViewModel;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableMapLiveData;
import edu.hm.eem_library.model.ExamViewModel;
import edu.hm.eem_library.model.ExamListViewModel;
import edu.hm.eem_library.model.ItemViewModel;
import edu.hm.eem_library.net.ClientDevice;

enum ItemListContent
{
    EXAM(0), EXAMDOCUMENT(1), HOST(2), DEVICE(3);
    int id;

    ItemListContent(int id) {
        this.id = id;
    }

    static ItemListContent[] values = null;

    static ItemListContent fromId(int id) {
        if(values==null) values=values();
        for (ItemListContent c : values) {
            if (c.id == id) return c;
        }
        throw new IllegalArgumentException();
    }
}

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentPressListener}
 * interface.
 */
public class ItemListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private ItemRecyclerViewAdapter adapter;
    private ItemListContent content;
    private ItemViewModel model;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemlist, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            if(content.ordinal()==ItemListContent.DEVICE.ordinal()){
                adapter = new ItemRecyclerViewAdapter((SortableMapLiveData<String, ClientDevice>) model.getLivedata());
            } else {
                adapter = new SelectableItemRecyclerViewAdapter((SelectableSortableMapLiveData<String, ?>) model.getLivedata(),
                        (OnListFragmentPressListener) context,
                        ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                        ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight));
            }
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        switch (content){
            case EXAM:
                model = ViewModelProviders.of(getActivity()).get(ExamListViewModel.class);
                break;
            case EXAMDOCUMENT:
                model = ViewModelProviders.of(getActivity()).get(ExamViewModel.class);
                break;
            case HOST:
                model = ViewModelProviders.of(getActivity()).get(HostViewModel.class);
                break;
            case DEVICE:
                model = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
                break;
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.ItemListFragment);
        if (a.hasValue(R.styleable.ItemListFragment_ListContent)) {
            content = ItemListContent.fromId(a.getInt(R.styleable.ItemListFragment_ListContent, 0));
        }

        a.recycle();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //pass lifecycle from view because if the fragment gets reattached, the old observer would
        // not be destroyed, if we would pass the lifecycle of the fragment
        model.getLivedata().observe(getViewLifecycleOwner(), new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnListFragmentPressListener {
        void onListFragmentPress(int index);
        void onListFragmentLongPress();
    }
}
