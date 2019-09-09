package edu.hm.eem_library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ClientItemViewModel;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.ExamItemViewModel;
import edu.hm.eem_library.model.HostItemViewModel;
import edu.hm.eem_library.model.ItemViewModel;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;
import edu.hm.eem_library.model.StudentExamDocumentItemViewModel;
import edu.hm.eem_library.model.TeacherExamDocumentItemViewModel;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

enum ItemListContent {
    EXAM(0), STUDENTEXAMDOCUMENT(1), TEACHEREXAMDOCUMENT(2), HOST(3), DEVICE(4), EXAMDOCUMENTEXPLORER(5);
    static ItemListContent[] values = null;
    int id;

    ItemListContent(int id) {
        this.id = id;
    }

    static ItemListContent fromId(int id) {
        if (values == null) values = values();
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

    private ItemRecyclerViewAdapter adapter;
    private ItemListContent content;
    private ItemViewModel model;
    private RecyclerView recyclerView;
    private FrameLayout emptyLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemlist, container, false);
        recyclerView = view.findViewById(R.id.list);
        Context context = recyclerView.getContext();
        Fragment parentFragment = getParentFragment();
        OnListFragmentPressListener listener;
        if (parentFragment instanceof OnListFragmentPressListener) {
            listener = (OnListFragmentPressListener) parentFragment;
        } else if (context instanceof OnListFragmentPressListener) {
            listener = (OnListFragmentPressListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentPressListener");
        }
        if (content == ItemListContent.STUDENTEXAMDOCUMENT ||
                content == ItemListContent.TEACHEREXAMDOCUMENT ||
                content == ItemListContent.EXAMDOCUMENTEXPLORER) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int spans = (int) (metrics.widthPixels / metrics.density / 180);
            recyclerView.setLayoutManager(new GridLayoutManager(context, spans));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        emptyLayout = view.findViewById(R.id.empty_layout);
        TextView tw = view.findViewById(R.id.empty_list_text);
        switch (content) {
            case EXAM:
                adapter = new NameTabRecyclerViewAdapter((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) model.getLivedata(), context, listener, content, true);
                tw.setText(R.string.placeholder_exam);
                break;
            case STUDENTEXAMDOCUMENT:
                //falltrough
            case TEACHEREXAMDOCUMENT:
                adapter = new ExamDocumentRecyclerViewAdapter((SelectableSortableItemLiveData<ExamDocument, ThumbnailedExamDocument>) model.getLivedata(), context, listener, content, true);
                tw.setText(R.string.placeholder_document);
                break;
            case HOST:
                adapter = new NameTabRecyclerViewAdapter((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) model.getLivedata(), context, listener, content, false);
                tw.setText(R.string.placeholder_host);
                break;
            case DEVICE:
                adapter = new ClientItemRecyclerviewAdapter((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) model.getLivedata(), context, listener, content);
                tw.setText(R.string.placeholder_device);
                break;
            case EXAMDOCUMENTEXPLORER:
                adapter = new ExamDocumentRecyclerViewAdapter((SelectableSortableItemLiveData<ExamDocument, ThumbnailedExamDocument>) model.getLivedata(), context, listener, content, false);
                break;
        }
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        switch (content) {
            case EXAM:
                model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ExamItemViewModel.class);
                break;
            case STUDENTEXAMDOCUMENT:
            case EXAMDOCUMENTEXPLORER:
                model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(StudentExamDocumentItemViewModel.class);
                break;
            case TEACHEREXAMDOCUMENT:
                model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TeacherExamDocumentItemViewModel.class);
                break;
            case HOST:
                model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(HostItemViewModel.class);
                break;
            case DEVICE:
                model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ClientItemViewModel.class);
                break;
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ItemListFragment);
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
        model.getLivedata().observe(getViewLifecycleOwner(), o -> {
            adapter.notifyDataSetChanged();
            updateEmptyLayout();
        });
    }

    private void updateEmptyLayout() {
        recyclerView.setVisibility(adapter.getItemCount() == 0 ? View.INVISIBLE : View.VISIBLE);
        emptyLayout.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    public interface OnListFragmentPressListener {
        void onListFragmentPress(int index);
    }
}
