package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListNamesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.view.uiv3.CreateShoppingListDialog;
import com.bigbasket.mobileapp.view.uiv3.DeleteShoppingListDialog;
import com.bigbasket.mobileapp.view.uiv3.EditShoppingDialog;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ShoppingListFragment extends BaseFragment implements ShoppingListNamesAware {

    private ArrayList<ShoppingListName> mShoppingListNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        new ShoppingListNamesTask<>(this, false).startTask();
    }

    private void renderShoppingList() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_fab_list_view, contentView, false);
        ListView shoppingNameListView = (ListView) base.findViewById(R.id.fabListView);
        ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(mShoppingListNames);
        shoppingNameListView.setAdapter(shoppingListAdapter);

        shoppingNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingListName shoppingListName = mShoppingListNames.get(position);
                launchShoppingListSummary(shoppingListName);
            }
        });

        FloatingActionButton fabCreateShoppingList = (FloatingActionButton) base.findViewById(R.id.btnFab);
        fabCreateShoppingList.attachToListView(shoppingNameListView);
        fabCreateShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateShoppingListDialog createShoppingListDialog = new CreateShoppingListDialog();
                createShoppingListDialog.setTargetFragment(getFragmentInstance(), 0);
                createShoppingListDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
            }
        });
        contentView.removeAllViews();
        contentView.addView(base);
    }

    public void createShoppingList(final String shoppingListName) {
        if (shoppingListName.length() == 0) {
            Toast.makeText(getActivity(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.createShoppingList(shoppingListName, "1", new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        Toast.makeText(getActivity(),
                                "List \"" + shoppingListName
                                        + "\" was created successfully", Toast.LENGTH_LONG).show();
                        trackEvent(TrackingAware.SHOP_LST_CREATED, null);
                        loadShoppingLists();
                        break;
                    default:
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
    }

    private void launchShoppingListSummary(ShoppingListName shoppingListName) {
        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_SUMMARY);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
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

    }

    private class ShoppingListAdapter extends BaseAdapter {

        private List<ShoppingListName> shoppingListNames;

        public ShoppingListAdapter(List<ShoppingListName> shoppingListNames) {
            this.shoppingListNames = shoppingListNames;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final ShoppingListName shoppingListName = shoppingListNames.get(position);
            ShoppingListViewHolder shoppingListViewHolder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.uiv3_shopping_list_name_row, parent, false);
                shoppingListViewHolder = new ShoppingListViewHolder(convertView);
                convertView.setTag(shoppingListViewHolder);
            } else {
                shoppingListViewHolder = (ShoppingListViewHolder) convertView.getTag();
            }

            TextView txtShopLstName = shoppingListViewHolder.getTxtShopLstName();
            txtShopLstName.setText(shoppingListName.getName());

            ImageView imgShoppingListAdditionalAction = shoppingListViewHolder.getImgShoppingListAdditionalAction();
            if (imgShoppingListAdditionalAction != null) {
                imgShoppingListAdditionalAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                        MenuInflater menuInflater = popupMenu.getMenuInflater();
                        menuInflater.inflate(R.menu.shopping_list_menu, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.menuEditShoppingList:
                                        showEditShoppingListDialog(shoppingListName);
                                        return true;
                                    case R.id.menuDeleteShoppingList:
                                        showDeleteShoppingListDialog(shoppingListName);
                                        return true;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            } else {
                ImageView imgEditShopList = shoppingListViewHolder.getImgEditShopList();
                imgEditShopList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditShoppingListDialog(shoppingListName);
                    }
                });

                ImageView imgDeleteShoppingList = shoppingListViewHolder.getImgDeleteShoppingList();
                imgDeleteShoppingList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteShoppingListDialog(shoppingListName);
                    }
                });
            }
            return convertView;
        }

        private class ShoppingListViewHolder {
            private View itemView;
            private TextView txtShopLstName;
            private ImageView imgEditShopList;
            private ImageView imgDeleteShoppingList;
            private ImageView imgShoppingListAdditionalAction;

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

            public ImageView getImgEditShopList() {
                if (imgEditShopList == null) {
                    imgEditShopList = (ImageView) itemView.findViewById(R.id.imgEditShopList);
                }
                return imgEditShopList;
            }

            public ImageView getImgDeleteShoppingList() {
                if (imgDeleteShoppingList == null) {
                    imgDeleteShoppingList = (ImageView) itemView.findViewById(R.id.imgDeleteShoppingList);
                }
                return imgDeleteShoppingList;
            }

            public ImageView getImgShoppingListAdditionalAction() {
                if (imgShoppingListAdditionalAction == null) {
                    imgShoppingListAdditionalAction = (ImageView) itemView.findViewById(R.id.imgShoppingListAdditionalAction);
                }
                return imgShoppingListAdditionalAction;
            }
        }
    }

    public ShoppingListFragment getFragmentInstance() {
        return this;
    }

    private void showEditShoppingListDialog(ShoppingListName shoppingListName) {
        if (shoppingListName.isSystem()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().showAlertDialog(null, getString(R.string.isSystemShoppingListMsg));
            }
            return;
        }
        EditShoppingDialog editShoppingDialog = EditShoppingDialog.newInstance(shoppingListName);
        editShoppingDialog.setTargetFragment(getFragmentInstance(), 0);
        editShoppingDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
    }

    private void showDeleteShoppingListDialog(ShoppingListName shoppingListName) {
        if (shoppingListName.isSystem()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().showAlertDialog(null, getString(R.string.isSystemShoppingListMsg));
            }
            return;
        }
        DeleteShoppingListDialog deleteShoppingListDialog = DeleteShoppingListDialog.newInstance(shoppingListName);
        deleteShoppingListDialog.setTargetFragment(getFragmentInstance(), 0);
        deleteShoppingListDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
    }

    public void editShoppingListName(ShoppingListName shoppingListName, String newName) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.editShoppingList(shoppingListName.getSlug(), newName, new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        Toast.makeText(getActivity(), getString(R.string.shoppingListUpdated),
                                Toast.LENGTH_LONG).show();
                        trackEvent(TrackingAware.SHOP_LST_NAME_CHANGED, null);
                        loadShoppingLists();
                        break;
                    default:
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
    }

    public void deleteShoppingList(final ShoppingListName shoppingListName) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.deleteShoppingList(shoppingListName.getSlug(), new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        String msg = "\"" + shoppingListName.getName() + "\" was deleted successfully";
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                        trackEvent(TrackingAware.SHOP_LST_DELETED, null);
                        loadShoppingLists();
                        break;
                    default:
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
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
        return "Shopping Lists";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListFragment.class.getName();
    }
}