package test.cognizant.factsapplication.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import test.cognizant.factsapplication.R;
import test.cognizant.factsapplication.model.Fact;

/**
 * A placeholder fragment containing a simple view.
 */
public class FactsFragment extends Fragment {


    private static final String TAG = FactsFragment.class.getName();
    private static final String factUrl = "https://dl.dropboxusercontent.com/u/746330/facts.json";

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recylerView;
    private ProgressBar progressBar;

    private List<Fact> factsList = new ArrayList<>();
    private FactsAdapter factsAdapter;
    private AsyncHttpTask jsonFeedTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        factsAdapter = new FactsAdapter(getActivity(), factsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facts, container, false);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipeRefresh);
        recylerView = (RecyclerView) view.findViewById(R.id.recycler_facts);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        return view;
    }

    @Override
    public void onDestroyView() {
        refreshLayout = null;
        recylerView = null;
        progressBar = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveFactFeed();
            }
        });

        if (savedInstanceState == null) {
            retrieveFactFeed();
        }

        recylerView.setHasFixedSize(true);
        recylerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recylerView.setAdapter(factsAdapter);
        recylerView.setItemAnimator(new DefaultItemAnimator());
    }


    public void retrieveFactFeed() {
        if (jsonFeedTask == null || jsonFeedTask.getStatus() == AsyncTask.Status.FINISHED) {
            jsonFeedTask = new AsyncHttpTask();
            jsonFeedTask.execute(factUrl);
        }
    }



    public class AsyncHttpTask extends AsyncTask<String, Void, Boolean> {


        private String appTitle;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseResult(response.toString());
                    result = true; // Successful
                } else {
                    result = false; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Download complete. Let us update UI
            if (!result) {
                Snackbar.make(progressBar, "Failed to fetch data!", Snackbar.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.INVISIBLE);
            factsAdapter.notifyDataSetChanged();
            // Stop refresh animation
            refreshLayout.setRefreshing(false);

            if (!TextUtils.isEmpty(appTitle)) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(appTitle);
            }
        }

        private void parseResult(String result) {
            try {
                JSONObject response = new JSONObject(result);
                appTitle = response.optString("title");

                //
                JSONArray rows = response.optJSONArray("rows");
                factsList.clear();

                for (int i = 0; i < rows.length(); i++) {
                    JSONObject rowJsonObject = rows.optJSONObject(i);
                    Fact item = new Fact();
                    item.setTitle(rowJsonObject.optString("title"));
                    item.setDescription(rowJsonObject.optString("description"));
                    item.setImageUrl(rowJsonObject.optString("imageHref"));

                    factsList.add(item);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception while parsing json feed", e);
            }
        }
    }

    private class FactsAdapter extends RecyclerView.Adapter<FactsAdapter.ViewHolder> {
        private List<Fact> facts;
        private Context context;

        FactsAdapter(Context context, List<Fact> facts) {
            this.facts = facts;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_facts, parent, false);
            return new ViewHolder(rowView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Fact factItem = facts.get(position);
            holder.title.setText(factItem.getTitle());
            holder.description.setText(factItem.getDescription());
            //Download image using picasso library
            Picasso.with(context)
                    .load(factItem.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_download)
                    .resizeDimen(R.dimen.thumbnail_width, R.dimen.thumbnail_height)
                    .centerInside()
                    .tag(context)
                    .into(holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return facts.size();
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
