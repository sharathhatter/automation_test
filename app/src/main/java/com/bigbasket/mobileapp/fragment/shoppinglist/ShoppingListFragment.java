package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListNamesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ShoppingListFragment extends BaseFragment implements ShoppingListNamesAware {

    private ArrayList<ShoppingListName> mShoppingListNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

        new ShoppingListNamesTask<>(this, true).startTask();
    }

    private void renderShoppingList() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_fab_list_view, contentView, false);
        ListView shoppingNameListView = (ListView) base.findViewById(R.id.fabListView);
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

        FloatingActionButton fabCreateShoppingList = (FloatingActionButton) base.findViewById(R.id.btnFab);
        fabCreateShoppingList.attachToListView(shoppingNameListView);
        fabCreateShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateShoppingListDialog();
            }
        });
        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void showCreateShoppingListDialog() {
        View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_editable_dialog, null);
        final EditText editTextDialog = (EditText) base.findViewById(R.id.editTextDialog);
        editTextDialog.setHint(getString(R.string.shoppingListNameDialogTextHint));
        editTextDialog.setTypeface(faceRobotoRegular);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.createShoppingList)
                .setView(base)
                .setPositiveButton(R.string.createList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createShoppingList(editTextDialog.getText() != null ? editTextDialog.getText().toString() : "");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
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

    public void createShoppingList(final String shoppingListName) {
        if (TextUtils.isEmpty(shoppingListName)) {
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
                                oldBaseApiResponse.message, true);
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
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void launchShoppingListSummary(ShoppingListName shoppingListName) {
        Intent intent = new Intent(getActivity(), ShoppingListSummaryActivity.class);
        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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

    }

    private void showEditShoppingListDialog(final ShoppingListName shoppingListName) {
        if (shoppingListName.isSystem()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().showAlertDialog(null, getString(R.string.isSystemShoppingListMsg));
            }
            return;
        }
        View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_editable_dialog, null);
        final EditText editTextDialog = (EditText) base.findViewById(R.id.editTextDialog);
        editTextDialog.setHint(getString(R.string.shoppingListNameDialogTextHint));
        editTextDialog.setTypeface(faceRobotoRegular);
        editTextDialog.setText(shoppingListName.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.changeShoppingListName)
                .setView(base)
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editShoppingListName(shoppingListName, editTextDialog.getText().toString().trim());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void showDeleteShoppingListDialog(final ShoppingListName shoppingListName) {
        if (shoppingListName.isSystem()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().showAlertDialog(null, getString(R.string.isSystemShoppingListMsg));
            }
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.deleteQuestion)
                .setMessage(R.string.deleteShoppingListText)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteShoppingList(shoppingListName);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
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

    private class ShoppingListAdapter extends BaseAdapter {
        private int VIEW_TYPE_HEADER = 0;
        private int VIEW_TYPE_ITEM = 1;
        private List<Object> shoppingListNames;

        public ShoppingListAdapter(List<Object> shoppingListNames) {
            this.shoppingListNames = shoppingListNames;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == VIEW_TYPE_ITEM) {
                final ShoppingListName shoppingListName = (ShoppingListName)
                        shoppingListNames.get(position);
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

                TextView txtShopLstDesc = shoppingListViewHolder.getTxtShopLstDesc();
                if (TextUtils.isEmpty(shoppingListName.getDescription())) {
                    txtShopLstDesc.setVisibility(View.GONE);
                } else {
                    txtShopLstDesc.setVisibility(View.VISIBLE);
                    txtShopLstDesc.setText(shoppingListName.getDescription());
                }

                if (shoppingListName.isSystem()) {
                    ImageView imgShoppingListAdditionalAction = shoppingListViewHolder.getImgShoppingListAdditionalAction();
                    if (imgShoppingListAdditionalAction != null) {
                        imgShoppingListAdditionalAction.setVisibility(View.GONE);
                    } else {
                        ImageView imgEditShopList = shoppingListViewHolder.getImgEditShopList();
                        ImageView imgDeleteShoppingList = shoppingListViewHolder.getImgDeleteShoppingList();
                        imgEditShopList.setVisibility(View.GONE);
                        imgDeleteShoppingList.setVisibility(View.GONE);
                    }
                } else {
                    ImageView imgShoppingListAdditionalAction = shoppingListViewHolder.getImgShoppingListAdditionalAction();
                    if (imgShoppingListAdditionalAction != null) {
                        imgShoppingListAdditionalAction.setVisibility(View.VISIBLE);
                        imgShoppingListAdditionalAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                                MenuInflater menuInflater = popupMenu.getMenuInflater();
                                menuInflater.inflate(R.menu.shopping_list_item_menu, popupMenu.getMenu());
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
                        imgEditShopList.setVisibility(View.VISIBLE);
                        imgEditShopList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showEditShoppingListDialog(shoppingListName);
                            }
                        });

                        ImageView imgDeleteShoppingList = shoppingListViewHolder.getImgDeleteShoppingList();
                        imgDeleteShoppingList.setVisibility(View.VISIBLE);
                        imgDeleteShoppingList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDeleteShoppingListDialog(shoppingListName);
                            }
                        });
                    }
                }
            } else {
                String headerText = shoppingListNames.get(position).toString();
                SlotSelectionFragment.SlotHeaderViewHolder holder;
                if (convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.uiv3_list_title, parent, false);
                    holder = new SlotSelectionFragment.SlotHeaderViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (SlotSelectionFragment.SlotHeaderViewHolder) convertView.getTag();
                }
                int padding = (int) getResources().getDimension(R.dimen.padding_small);
                TextView txtHeaderMsg = holder.getTxtHeaderMsg();
                txtHeaderMsg.setPadding(txtHeaderMsg.getPaddingLeft(), padding,
                        txtHeaderMsg.getPaddingRight(), padding);
                txtHeaderMsg.setBackgroundColor(Color.TRANSPARENT);
                txtHeaderMsg.setText(headerText);
            }
            return convertView;
        }

        private class ShoppingListViewHolder {
            private View itemView;
            private TextView txtShopLstName;
            private ImageView imgEditShopList;
            private ImageView imgDeleteShoppingList;
            private ImageView imgShoppingListAdditionalAction;
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

            public TextView getTxtShopLstDesc() {
                if (txtShopLstDesc == null) {
                    txtShopLstDesc = (TextView) itemView.findViewById(R.id.txtShopLstDesc);
                }
                return txtShopLstDesc;
            }
        }
    }
}