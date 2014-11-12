package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.view.uiv3.DeleteShoppingListDialog;
import com.bigbasket.mobileapp.view.uiv3.EditShoppingDialog;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melnykov.fab.FloatingActionButton;

import java.util.HashMap;
import java.util.List;


public class ShoppingListFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadShoppingLists();
    }

    private void loadShoppingLists() {
        if (getActivity() == null || getBaseActivity() == null) return;
        AuthParameters authParameters = AuthParameters.getInstance(getActivity());
        if (authParameters.isAuthTokenEmpty()) {
            getBaseActivity().showAlertDialog(getActivity(), null, getString(R.string.login_required), Constants.LOGIN_REQUIRED);
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SYSTEM, "1");
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.SL_GET_LISTS,
                params, true, true, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.SL_GET_LISTS)) {
            JsonObject httpJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = httpJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    JsonArray shoppingListNamesJsonArray = httpJsonObj.get(Constants.SHOPPING_LISTS).getAsJsonArray();
                    List<ShoppingListName> shoppingListNames = ParserUtil.parseShoppingList(shoppingListNamesJsonArray);
                    renderShoppingList(shoppingListNames);
                    break;
                default:
                    // TODO : Add error handling
                    showErrorMsg("Server Error");
                    break;
            }
        } else if (url.contains(Constants.SL_EDIT_LIST)) {
            JsonObject httpJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = httpJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    Toast.makeText(getActivity(), getString(R.string.shoppingListUpdated),
                            Toast.LENGTH_LONG).show();
                    loadShoppingLists();
                    break;
                default:
                    // TODO : Improve error handling
                    showErrorMsg("Server Error");
                    break;
            }
        } else if (url.contains(Constants.SL_DELETE_LIST)) {
            JsonObject httpJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = httpJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    HashMap<Object, String> additionalCtx = httpOperationResult.getAdditionalCtx();
                    String msg = "\"" + additionalCtx.get(Constants.SHOPPING_LIST_NAME) + "\" was deleted successfully";
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    loadShoppingLists();
                    break;
                default:
                    // TODO : Add error handling
                    showErrorMsg("Server Error");
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void renderShoppingList(final List<ShoppingListName> shoppingListNames) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_fab_list_view, null);
        ListView shoppingNameListView = (ListView) base.findViewById(R.id.fabListView);
        ShoppingListNameAndOpAdapter shoppingListNameAndOpAdapter = new ShoppingListNameAndOpAdapter(shoppingListNames);
        shoppingNameListView.setAdapter(shoppingListNameAndOpAdapter);

        shoppingNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingListName shoppingListName = shoppingListNames.get(position);
                launchShoppingListSummary(shoppingListName);
            }
        });
        FloatingActionButton fabCreateShoppingList = (FloatingActionButton) base.findViewById(R.id.btnFab);
        fabCreateShoppingList.attachToListView(shoppingNameListView);
        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void launchShoppingListSummary(ShoppingListName shoppingListName) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SHOPPING_LIST_NAME, shoppingListName);
        ShoppingListSummaryFragment shoppingListSummaryFragment = new ShoppingListSummaryFragment();
        shoppingListSummaryFragment.setArguments(bundle);
        changeFragment(shoppingListSummaryFragment);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    private class ShoppingListNameAndOpAdapter extends BaseSwipeAdapter {

        private List<ShoppingListName> shoppingListNames;

        public ShoppingListNameAndOpAdapter(List<ShoppingListName> shoppingListNames) {
            this.shoppingListNames = shoppingListNames;
        }

        @Override
        public int getSwipeLayoutResourceId(int i) {
            return R.id.swipeShoppingList;
        }

        @Override
        public View generateView(int position, ViewGroup viewGroup) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.uiv3_shopping_list_row, null);
            SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
            TextView txtShoppingListName = (TextView) v.findViewById(R.id.txtShopLstName);
            txtShoppingListName.setTypeface(faceRobotoRegular);
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout swipeLayout) {

                }

                @Override
                public void onOpen(SwipeLayout swipeLayout) {

                }

                @Override
                public void onStartClose(SwipeLayout swipeLayout) {

                }

                @Override
                public void onClose(SwipeLayout swipeLayout) {

                }

                @Override
                public void onUpdate(SwipeLayout swipeLayout, int i, int i1) {

                }

                @Override
                public void onHandRelease(SwipeLayout swipeLayout, float v, float v1) {

                }
            });
            return v;
        }

        @Override
        public void fillValues(int position, View convertView) {

            final ShoppingListName shoppingListName = shoppingListNames.get(position);

            TextView txtShoppingListName = (TextView) convertView.findViewById(R.id.txtShopLstName);

            ImageView imgEditShoppingListName = (ImageView) convertView.findViewById(R.id.imgEditShopList);
            ImageView imgDeleteShoppingList = (ImageView) convertView.findViewById(R.id.imgDeleteShoppingList);
            imgEditShoppingListName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditShoppingDialog editShoppingDialog = EditShoppingDialog.newInstance(shoppingListName);
                    editShoppingDialog.setTargetFragment(getFragmentInstance(), 0);
                    editShoppingDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
                }
            });
            imgDeleteShoppingList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeleteShoppingListDialog deleteShoppingListDialog = DeleteShoppingListDialog.newInstance(shoppingListName);
                    deleteShoppingListDialog.setTargetFragment(getFragmentInstance(), 0);
                    deleteShoppingListDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
                }
            });
            txtShoppingListName.setText(shoppingListName.getName());

            SwipeLayout swipeLayout = (SwipeLayout) convertView.findViewById(getSwipeLayoutResourceId(position));
            swipeLayout.setSwipeEnabled(!shoppingListName.isSystem());
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
    }

    public ShoppingListFragment getFragmentInstance() {
        return this;
    }


    public void editShoppingListName(ShoppingListName shoppingListName, String newName) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SLUG, shoppingListName.getSlug());
        params.put("name", newName);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.SL_EDIT_LIST, params, true, false, null);
    }

    public void deleteShoppingList(ShoppingListName shoppingListName) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SLUG, shoppingListName.getSlug());
        HashMap<Object, String> additionalCtx = new HashMap<>();
        additionalCtx.put(Constants.SHOPPING_LIST_NAME, shoppingListName.getName());
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.SL_DELETE_LIST, params, true, false, additionalCtx);
    }

    @Override
    public String getTitle() {
        return "Shopping Lists";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListFragment.class.getName();
    }
}