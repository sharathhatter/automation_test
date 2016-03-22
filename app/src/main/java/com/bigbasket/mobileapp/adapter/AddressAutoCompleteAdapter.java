package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.model.address.AreaSearchResponse;
import com.bigbasket.mobileapp.model.address.AreaSearchResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.newrelic.com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by muniraju on 29/02/16.
 */
public class AddressAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private AreaSearchResult[] searchResults;
    private AreaSearchFilter filter;

    public AddressAutoCompleteAdapter(Context context, int cityId, String type) {
        this.mContext = context;
        filter = new AreaSearchFilter(this, cityId, type);
    }

    public Context getContext() {
        return mContext;
    }

    public void setCityId(int cityId) {
        if (cityId <= 0) {
            throw new IllegalArgumentException("Invalid city id " + cityId);
        }
        filter.setCityId(cityId);
    }

    public void setType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type can not be null");
        }
        filter.setType(type);
    }

    public void setResultFields(String... fields) {
        if (fields == null) {
            filter.setFields(null);
        } else {
            Gson gson = new Gson();
            filter.setFields(gson.toJson(fields));
        }
    }

    public void setSearchResults(AreaSearchResult[] searchResults) {
        this.searchResults = searchResults;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return searchResults != null ? searchResults.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return searchResults != null ? searchResults[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TwoLineTextViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.two_line_list_item, parent, false);
            viewHolder = new TwoLineTextViewHolder();
            viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
            FontHolder fontHolder = FontHolder.getInstance(mContext);
            viewHolder.text1.setTypeface(fontHolder.getFaceRobotoRegular());
            viewHolder.text2.setTypeface(fontHolder.getFaceRobotoLight());
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TwoLineTextViewHolder) convertView.getTag();
        }
        AreaSearchResult result = searchResults[position];
        //Workaround to hide other Area
        if (result.isOtherArea()) {
            convertView.setVisibility(View.GONE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }
        viewHolder.text1.setText(result.getDisplayName());
        String line2Text = getLine2Text(filter.getType(), result);
        if (!TextUtils.isEmpty(line2Text)) {
            viewHolder.text2.setVisibility(View.VISIBLE);
            viewHolder.text2.setText(line2Text);
        } else {
            viewHolder.text2.setVisibility(View.GONE);
        }

        return convertView;
    }

    private String getLine2Text(String type, AreaSearchResult searchResult) {
        boolean hasArea = !TextUtils.isEmpty(searchResult.getArea());
        boolean hasPinCode = !TextUtils.isEmpty(searchResult.getPincode());
        switch (type) {
            case Constants.AREA_SEARCH_TYPE_APARTMENT:
                if (hasArea && hasPinCode) {
                    return mContext.getString(R.string.search_apartment_line2_format,
                            searchResult.getArea(), searchResult.getPincode());
                } else if (hasArea) {
                    return searchResult.getArea();
                } else if (hasPinCode) {
                    return searchResult.getPincode();
                }
                break;
            case Constants.AREA_SEARCH_TYPE_AREA:
                if (hasPinCode) {
                    return searchResult.getPincode();
                }
                break;
            case Constants.AREA_SEARCH_TYPE_PINCODE:
                //Will only return pincodes
        }
        return null;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private static class TwoLineTextViewHolder {
        public TextView text1;
        public TextView text2;
    }


    private static class AreaSearchFilter extends Filter {

        private AddressAutoCompleteAdapter adapter;
        private int cityId;
        private String type;
        private LruCache<SearchKey, Filter.FilterResults> cache;
        private String fields;
        private int limit = 5;

        public AreaSearchFilter(AddressAutoCompleteAdapter adapter, int cityId, String type) {
            this.adapter = adapter;
            this.cityId = cityId;
            this.type = type;
            //Cache only last 5 results
            cache = new LruCache<>(5);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getCityId() {
            return cityId;
        }

        public void setCityId(int cityId) {
            this.cityId = cityId;
        }

        public void setFields(String fields) {
            this.fields = fields;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            SearchKey key = new SearchKey(cityId, constraint);
            if (constraint == null || TextUtils.isEmpty(constraint) || constraint.length() < 2) {
                FilterResults filterResults = new FilterResults();
                filterResults.count = 0;
                filterResults.values = null;
                return filterResults;
            }
            if (cache != null) {
                FilterResults results = cache.get(key);
                if (results != null) {
                    return results;
                }
            }

            BigBasketApiService apiAdapter = BigBasketApiAdapter.getApiService(adapter.getContext());
            Call<AreaSearchResponse> apiCall = apiAdapter.searchAreaInfo(cityId, type,
                    constraint.toString(),
                    fields, limit);
            try {
                Response<AreaSearchResponse> response = apiCall.execute();
                if (response.isSuccessful() && response.body() != null
                        && response.body().status == 0
                        && response.body().getResponse() != null) {
                    FilterResults filterResults = new FilterResults();
                    AreaSearchResult[] results = response.body().getResponse().getResults();
                    filterResults.values = results;
                    if (filterResults.values != null) {
                        filterResults.count = response.body().getResponse().getResults().length;
                        if (filterResults.count > 0) {
                            if (results[filterResults.count - 1].isOtherArea()) {
                                //Remove the Other area
                                filterResults.values = new AreaSearchResult[filterResults.count - 1];
                                filterResults.count--;
                                System.arraycopy(results, 0, filterResults.values, 0,
                                        filterResults.count);
                            }
                        }
                    } else {
                        filterResults.count = 0;
                    }
                    cache.put(key, filterResults);
                    return filterResults;
                }
            } catch (IOException e) {
                //Ignore
            }
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                adapter.setSearchResults((AreaSearchResult[]) results.values);
            } else {
                adapter.notifyDataSetInvalidated();
            }
        }
    }

    private static class SearchKey {
        private int cityId;
        private CharSequence constraint;

        public SearchKey(int cityId, CharSequence constraint) {
            this.cityId = cityId;
            this.constraint = constraint;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SearchKey searchKey = (SearchKey) o;

            if (cityId != searchKey.cityId) return false;
            return constraint != null ? constraint.equals(searchKey.constraint) : searchKey.constraint == null;

        }

        @Override
        public int hashCode() {
            int result = cityId;
            result = 31 * result + (constraint != null ? constraint.hashCode() : 0);
            return result;
        }
    }
}
