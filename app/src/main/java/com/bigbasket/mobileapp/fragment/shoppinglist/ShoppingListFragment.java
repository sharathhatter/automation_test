package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListNamesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.List;


public class ShoppingListFragment extends BaseFragment implements ShoppingListNamesAware {

    private ArrayList<ShoppingListName> mShoppingListNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.SL);
        if (savedInstanceState != null) {
            mShoppingListNames = savedInstanceState.getParcelableArrayList(Constants.SHOPPING_LISTS);
            if (mShoppingListNames != null) {
                renderShoppingList();
                return;
            }
        }
        loadShoppingLists();
    }

    private void loadShoppingLists() {
        if (getActivity() == null || getCurrentActivity() == null) return;
        AuthParameters authParameters = AuthParameters.getInstance(getActivity());
        if (authParameters.isAuthTokenEmpty()) {
            getCurrentActivity().showAlertDialog(null, getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN);
            return;
        }
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        new ShoppingListNamesTask<>(this, true, getPreviousScreenName()).startTask();
    }

    private void renderShoppingList() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup base = (ViewGroup) inflater.inflate(R.layout.uiv3_fab_list_view, contentView, false);
        ListView shoppingNameListView = (ListView) base.findViewById(R.id.fabListView);
        shoppingNameListView.setDivider(null);
        shoppingNameListView.setDividerHeight(0);
        shoppingNameListView.setCacheColorHint(Color.WHITE);
        RelativeLayout noDeliveryAddLayout = (RelativeLayout) base.findViewById(R.id.noDeliveryAddLayout);

        if (mShoppingListNames != null && mShoppingListNames.size() > 0) {
            noDeliveryAddLayout.setVisibility(View.GONE);
            shoppingNameListView.setVisibility(View.VISIBLE);
            final ArrayList<Object> shoppingLists = new ArrayList<>();
            ArrayList<Object> systemShoppingLists = new ArrayList<>();
            for (ShoppingListName shoppingListName : mShoppingListNames) {
                if (shoppingListName.isSystem()) {
                    systemShoppingLists.add(shoppingListName);
                } else {
                    shoppingLists.add(shoppingListName);
                }
            }
            if (shoppingLists.size() > 0) {  // There are some user lists
                shoppingLists.add(0, getString(R.string.yourList) +
                        (shoppingLists.size() > 1 ? "s" : ""));
            }
            if (systemShoppingLists.size() > 0) {
                shoppingLists.add(getString(R.string.bbShoppingLists) +
                        (systemShoppingLists.size() > 1 ? "s" : ""));
                shoppingLists.addAll(systemShoppingLists);
            }
            ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(shoppingLists);
            shoppingNameListView.setAdapter(shoppingListAdapter);

            shoppingNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object obj = shoppingLists.get(position);
                    if (obj instanceof ShoppingListName) {
                        ShoppingListName shoppingListName = (ShoppingListName) obj;
                        launchShoppingListSummary(shoppingListName);
                    }
                }
            });

        } else {
            noDeliveryAddLayout.setVisibility(View.VISIBLE);
            shoppingNameListView.setVisibility(View.GONE);
            showNoShoppingListView(base);
        }

        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void showNoShoppingListView(View base) {
        ImageView imgEmptyPage = (ImageView) base.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_shopping_list);

        TextView txtEmptyMsg1 = (TextView) base.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.nowShoppingListMsg1);
        TextView txtEmptyMsg2 = (TextView) base.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setText(R.string.nowShoppingListMsg2);
        Button btnBlankPage = (Button) base.findViewById(R.id.btnBlankPage);
        btnBlankPage.setVisibility(View.GONE);
    }

    private void launchShoppingListSummary(ShoppingListName shoppingListName) {
        Intent intent = new Intent(getActivity(), ShoppingListSummaryActivity.class);
        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
        startActivityForResult(intent, NavigationCodes.SHOPPING_LIST_CHANGED);
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames) {
        mShoppingListNames = shoppingListNames;
        renderShoppingList();
        trackEvent(TrackingAware.SHOP_LST_SHOWN, null);
    }

    @Override
    public String getSelectedProductId() {
        return null;
    }

    @Override
    public void setSelectedProductId(String selectedProductId) {

    }

    @Override
    public void postShoppingListItemDeleteOperation() {

    }

    @Override
    public void addToShoppingList(List<ShoppingListName> selectedShoppingListNames) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public void postAddToShoppingListOperation() {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public void createNewShoppingList() {
        new CreateShoppingListTask<>(this).showDialog();
    }

    @Override
    public void onNewShoppingListCreated(String listName) {
        loadShoppingLists();
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mShoppingListNames != null && mShoppingListNames.size() > 0) {
            outState.putParcelableArrayList(Constants.SHOPPING_LISTS, mShoppingListNames);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getTitle() {
        return getString(R.string.shoppingList);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SHOPPING_LIST_SCREEN;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.SHOPPING_LIST_CHANGED) {
            loadShoppingLists();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        markBasketDirty();
    }

    public static class ShoppingListHeaderViewHolder {
        private TextView txtHeaderMsg;
        private View base;

        public ShoppingListHeaderViewHolder(View base) {
            this.base = base;
        }

        public TextView getTxtHeaderMsg() {
            if (txtHeaderMsg == null) {
                txtHeaderMsg = (TextView) base.findViewById(R.id.txtHeaderMsg);
                txtHeaderMsg.setTypeface(faceRobotoRegular);
            }
            return txtHeaderMsg;
        }
    }

    private class ShoppingListAdapter extends BaseAdapter {
        private int VIEW_TYPE_HEADER = 0;
        private int VIEW_TYPE_ITEM = 1;
        private List<Object> shoppingListNames;
        private int dp8;
        private int dp16;

        public ShoppingListAdapter(List<Object> shoppingListNames) {
            this.shoppingListNames = shoppingListNames;
            this.dp8 = (int) getResources().getDimension(R.dimen.padding_small);
            this.dp16 = (int) getResources().getDimension(R.dimen.padding_normal);
        }

        @Override
        public int getItemViewType(int position) {
            return shoppingListNames.get(position) instanceof ShoppingListName ?
                    VIEW_TYPE_ITEM : VIEW_TYPE_HEADER;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getCount() {
            return shoppingListNames.size();
        }

        @Override
        public Object getItem(int position) {
            return shoppingListNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != VIEW_TYPE_HEADER && super.isEnabled(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == VIEW_TYPE_ITEM) {
                final ShoppingListName shoppingListName = (ShoppingListName)
                        shoppingListNames.get(position);
                ShoppingListViewHolder shoppingListViewHolder;
                if (convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.uiv3_shopping_list_row, parent, false);
                    shoppingListViewHolder = new ShoppingListViewHolder(convertView);
                    convertView.setTag(shoppingListViewHolder);
                } else {
                    shoppingListViewHolder = (ShoppingListViewHolder) convertView.getTag();
                }

                TextView txtShopLstName = shoppingListViewHolder.getTxtShopLstName();
                txtShopLstName.setText(shoppingListName.getName());

                TextView txtShopLstDesc = shoppingListViewHolder.getTxtShopLstDesc();
                if (TextUtils.isEmpty(shoppingListName.getDescription())) {
                    txtShopLstDesc.setVisibility(View.GONE);
                } else {
                    txtShopLstDesc.setVisibility(View.VISIBLE);
                    txtShopLstDesc.setText(shoppingListName.getDescription());
                }

                convertView.setPadding(convertView.getPaddingLeft(), position == 0 ? dp16 : dp8,
                        convertView.getPaddingRight(), position == getCount() - 1 ? dp16 : dp8);
            } else {
                String headerText = shoppingListNames.get(position).toString();
                ShoppingListHeaderViewHolder holder;
                if (convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.uiv3_list_title, parent, false);
                    holder = new ShoppingListHeaderViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ShoppingListHeaderViewHolder) convertView.getTag();
                }

                TextView txtHeaderMsg = holder.getTxtHeaderMsg();
                txtHeaderMsg.setText(headerText);
            }
            return convertView;
        }

        private class ShoppingListViewHolder {
            private View itemView;
            private TextView txtShopLstName;
            private TextView txtShopLstDesc;

            private ShoppingListViewHolder(View itemView) {
                this.itemView = itemView;
            }

            public TextView getTxtShopLstName() {
                if (txtShopLstName == null) {
                    txtShopLstName = (TextView) itemView.findViewById(R.id.txtShopLstName);
                    txtShopLstName.setTypeface(faceRobotoRegular);
                }
                return txtShopLstName;
            }

            public TextView getTxtShopLstDesc() {
                if (txtShopLstDesc == null) {
                    txtShopLstDesc = (TextView) itemView.findViewById(R.id.txtShopLstDesc);
                }
                return txtShopLstDesc;
            }
        }
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "ShoppingListFragment";
    }
}