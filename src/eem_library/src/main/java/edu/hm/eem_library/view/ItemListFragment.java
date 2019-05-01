package edu.hm.eem_library.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableMapLiveData;
import edu.hm.eem_library.model.StringMapViewModel;
import edu.hm.eem_library.model.ExamViewModel;
import edu.hm.eem_library.model.ExamListViewModel;
import edu.hm.eem_library.model.ItemViewModel;

enum ItemListContent
{
    EXAM(0), EXAMDOCUMENT(1), DEVICE(2);
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
            if(content.ordinal()>ItemListContent.EXAMDOCUMENT.ordinal()){
                adapter = new ItemRecyclerViewAdapter((SortableMapLiveData<String, ?>) model.getLivedata());
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
            case DEVICE:
                model = ViewModelProviders.of(getActivity()).get(StringMapViewModel.class);
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
        model.getLivedata().observe(getViewLifecycleOwner(), new Observer<String[]>() {
            @Override
            public void onChanged(@Nullable String[] selectableItemList) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnListFragmentPressListener {
        void onListFragmentPress(int index);
        void onListFragmentLongPress();
    }
}
