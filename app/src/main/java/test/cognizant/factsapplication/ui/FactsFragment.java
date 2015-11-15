package test.cognizant.factsapplication.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import test.cognizant.factsapplication.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FactsFragment extends Fragment {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recylerView;

    public FactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facts, container, false);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipeRefresh);
        recylerView = (RecyclerView) view.findViewById(R.id.recycler_facts);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFacts();
            }
        });

        recylerView.setAdapter(new FactsAdapter());
        recylerView.setHasFixedSize(true);
        recylerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshFacts() {
        // Load facts

        // Loading facts complete
        onFactsLoadComplete();
    }

    private void onFactsLoadComplete () {
        // Update Adapter & Notify dataset

        // Stop refresh animation
        refreshLayout.setRefreshing(false);
    }

    private class FactsAdapter extends RecyclerView.Adapter<FactsAdapter.ViewHolder> {
        private String[] facts = {"Hello", "How", "are", "you"};

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_facts, parent, false);
            ViewHolder viewHolder = new ViewHolder(rowView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.title.setText(facts[position]);
            holder.description.setText(facts[position]);
        }

        @Override
        public int getItemCount() {
            return facts.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView title;
            private TextView description;
            private ImageView thumbnail;

            public ViewHolder(View rowView) {
                super(rowView);
                title = (TextView) rowView.findViewById(R.id.textview_title);
                description = (TextView) rowView.findViewById(R.id.textview_description);
                thumbnail = (ImageView) rowView.findViewById(R.id.imageview_thumbnail);
            }
        }
    }
}
