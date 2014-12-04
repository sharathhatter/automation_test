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
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListNamesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.uiv3.CreateShoppingListDialog;
import com.bigbasket.mobileapp.view.uiv3.DeleteShoppingListDialog;
import com.bigbasket.mobileapp.view.uiv3.EditShoppingDialog;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
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
            getCurrentActivity().showAlertDialog(getActivity(), null, getString(R.string.login_required), Constants.LOGIN_REQUIRED);
            return;
        }

        new ShoppingListNamesTask<>(this, false).startTask();
    }

    private void renderShoppingList() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_fab_list_view, null);
        ListView shoppingNameListView = (ListView) base.findViewById(R.id.fabListView);
        ShoppingListNameAndOpAdapter shoppingListNameAndOpAdapter = new ShoppingListNameAndOpAdapter(mShoppingListNames);
        shoppingNameListView.setAdapter(shoppingListNameAndOpAdapter);

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
                hideProgressDialog();
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        Toast.makeText(getActivity(),
                                "List \"" + shoppingListName
                                        + "\" was created successfully", Toast.LENGTH_LONG).show();
                        loadShoppingLists();
                        break;
                    default:
                        // TODO : Add error handling
                        showErrorMsg("Server Error");
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                // TODO : Add error handling
                showErrorMsg("Server Error");
            }
        });
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

    @Override
    public void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames) {
        mShoppingListNames = shoppingListNames;
        renderShoppingList();
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
                    if (shoppingListName.isSystem()) {
                        if (getCurrentActivity() != null) {
                            getCurrentActivity().showAlertDialog(getActivity(), null, getString(R.string.isSystemShoppingListMsg));
                        }
                        return;
                    }
                    EditShoppingDialog editShoppingDialog = EditShoppingDialog.newInstance(shoppingListName);
                    editShoppingDialog.setTargetFragment(getFragmentInstance(), 0);
                    editShoppingDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
                }
            });
            imgDeleteShoppingList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shoppingListName.isSystem()) {
                        if (getCurrentActivity() != null) {
                            getCurrentActivity().showAlertDialog(getActivity(), null, getString(R.string.isSystemShoppingListMsg));
                        }
                        return;
                    }
                    DeleteShoppingListDialog deleteShoppingListDialog = DeleteShoppingListDialog.newInstance(shoppingListName);
                    deleteShoppingListDialog.setTargetFragment(getFragmentInstance(), 0);
                    deleteShoppingListDialog.show(getFragmentManager(), Constants.SHOPPING_LIST_NAME);
                }
            });
            txtShoppingListName.setText(shoppingListName.getName());
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
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.editShoppingList(shoppingListName.getSlug(), newName, new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                hideProgressDialog();
                switch (oldBaseApiResponse.status) {
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
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                // TODO : Improve error handling
                showErrorMsg("Server Error");
            }
        });
    }

    public void deleteShoppingList(final ShoppingListName shoppingListName) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.deleteShoppingList(shoppingListName.getSlug(), new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                hideProgressDialog();
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        String msg = "\"" + shoppingListName.getName() + "\" was deleted successfully";
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                        loadShoppingLists();
                        break;
                    default:
                        // TODO : Add error handling
                        showErrorMsg("Server Error");
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                // TODO : Add error handling
                showErrorMsg("Server Error");
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