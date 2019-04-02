package edu.hm.eem_host.view;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
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

import edu.hm.eem_host.R;
import edu.hm.eem_host.model.ExamHost;
import edu.hm.eem_host.model.ExamListHost;
import edu.hm.eem_host.model.Nameable;
import edu.hm.eem_host.model.SelectableItemHost;
import edu.hm.eem_host.model.SelectableItemList;

enum SelectableListContent
{
    EXAM(0), EXAMDOCUMENT(1);
    int id;

    SelectableListContent(int id) {
        this.id = id;
    }

    static SelectableListContent fromId(int id) {
        for (SelectableListContent c : values()) {
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
public class SelectableItemListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private SelectableItemRecyclerViewAdapter adapter;
    private OnListFragmentPressListener mListener;
    private SelectableListContent content;
    private SelectableItemHost model;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SelectableItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selectableitemlist, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new SelectableItemRecyclerViewAdapter(mListener, model,
                    ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                    ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight));
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnListFragmentPressListener) context;
        if(content==SelectableListContent.EXAM)
            model = ViewModelProviders.of(getActivity()).get(ExamListHost.class);
        else
            model = ViewModelProviders.of(getActivity()).get(ExamHost.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        model.getLivedata().getValue().clearSelection();
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.SelectableItemListFragment);
        if (a.hasValue(R.styleable.SelectableItemListFragment_selectableListContent)) {
            content = SelectableListContent.fromId(a.getInt(R.styleable.SelectableItemListFragment_selectableListContent, 0));
        }

        a.recycle();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //pass lifecycle from view because if the fragment gets reattached, the old observer would
        // not be destroyed, if we would pass the lifecycle of the fragment
        model.getLivedata().observe(getViewLifecycleOwner(), new Observer<SelectableItemList<Nameable>>() {
            @Override
            public void onChanged(@Nullable SelectableItemList<Nameable> selectableItemList) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnListFragmentPressListener {
        void onListFragmentPress(int index);
        void onListFragmentLongPress();
    }
}
