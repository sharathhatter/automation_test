package com.bigbasket.mobileapp.handler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.AgeValidationActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.BasketValidationActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;

import java.util.List;

public class MessageHandler extends Handler {
    private BaseActivity activity;
    private MarketPlace marketPlace;
    private BaseFragment baseFragment;
    // TODO : Replace this with better handling

    public MessageHandler(BaseActivity activity) { // Get rid of this constructor
        this.activity = activity;
    }

    public MessageHandler(BaseActivity activity, BaseFragment fragment) {
        this.baseFragment = fragment;
        this.activity = activity;
    }

    public MessageHandler(BaseActivity activity, MarketPlace marketPlace) {
        this.activity = activity;
        this.marketPlace = marketPlace;
    }

    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {

            // search results received update the UI
            case MessageCode.UNAUTHORIZED:
                new AlertDialog.Builder(activity)
                        .setTitle("BigBasket")
                        .setMessage(R.string.login_required)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                Intent i = new Intent(activity, Signin.class);
//                                activity.startActivityForResult(i, Constants.GO_TO_HOME);
//                                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

                            }

                        })
                        .create()
                        .show();
                break;

            case MessageCode.SERVER_ERROR:
                AlertDialog.Builder alertDialogBuilder6 = new AlertDialog.Builder(activity);
                alertDialogBuilder6.setTitle("BigBasket");
                alertDialogBuilder6.setMessage(R.string.server_error)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                           Intent i = new Intent(activity, HomeActivity.class);
//                                           activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                           activity.finish();

                            }

                        });
                AlertDialog alertDialog6 = alertDialogBuilder6.create();
                alertDialog6.show();
                break;


            // case network error
            case MessageCode.INTERNET_ERROR:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("BigBasket");
                alertDialogBuilder.setMessage(R.string.checkinternet)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }

                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;

            case MessageCode.GET_SHOPPINGLIST_NAMES_OK:
                List<ShoppingListName> shoppingListNames = ((ShoppingListNamesAware) baseFragment).getShoppingListNames();
                if (shoppingListNames == null || shoppingListNames.size() == 0) {
                    Toast.makeText(activity, "Create a new shopping list", Toast.LENGTH_SHORT).show();
                } else {
                    ShoppingListNamesDialog shoppingListNamesDialog =
                            new ShoppingListNamesDialog(shoppingListNames, baseFragment.getActivity(),
                                    baseFragment);
                    shoppingListNamesDialog.show(baseFragment.getFragmentManager(), Constants.SHOP_LST);
                }
                break;

            case MessageCode.ADD_TO_SHOPPINGLIST_OK:

                Toast.makeText(activity, "Product successfully added to lists", Toast.LENGTH_SHORT).show();
                break;

            case MessageCode.DELETE_FROM_SHOPPING_LIST_OK:
                Toast.makeText(activity, "Product successfully deleted from list", Toast.LENGTH_SHORT).show();
                break;

            case MessageCode.BASKET_LIMIT_REACHED:
                AlertDialog.Builder basketProduct25ReachedBuilder = new AlertDialog.Builder(activity);
                basketProduct25ReachedBuilder.setTitle("BigBasket")
                        .setMessage(R.string.invalidBasketQty)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }

                        })
                        .create()
                        .show();
                break;

            case MessageCode.BASKET_EMPTY:
                AlertDialog.Builder basketProductIdInvalidBuilder = new AlertDialog.Builder(activity);
                basketProductIdInvalidBuilder.setTitle("BigBasket")
                        .setMessage(R.string.Product_id_invalid)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }

                        })
                        .create()
                        .show();
                break;


            // service not available
            case 2:

                AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(activity);
                alertDialogBuilder1.setTitle("BigBasket");
                alertDialogBuilder1.setMessage(R.string.servicenot)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                           Intent i = new Intent(activity, HomeActivity.class);
//                                           activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                           activity.finish();
                                activity.goToHome();

                            }

                        });

                AlertDialog alertDialog1 = alertDialogBuilder1.create();
                alertDialog1.show();
                break;


            // following are error cases

            case 7:
                AlertDialog.Builder internal_error = new AlertDialog.Builder(activity);
                internal_error.setTitle("BigBasket");
                internal_error.setMessage(R.string.INTERNAL_SERVER_ERROR)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                      Intent i = new Intent(activity, HomeActivity.class);
//                                      activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                      activity.finish();
                                activity.goToHome();
                            }

                        });
                AlertDialog internal_errorDialog = internal_error.create();
                internal_errorDialog.show();
                break;

            case 8:
                AlertDialog.Builder servererr = new AlertDialog.Builder(activity);
                servererr.setTitle("BigBasket");
                servererr.setMessage(R.string.server_error)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                 Intent i = new Intent(activity, HomeActivity.class);
//                                 activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                 activity.finish();
                                activity.goToHome();
                            }

                        });
                AlertDialog servererrDialog = servererr.create();
                servererrDialog.show();
                break;
            case 9:
                AlertDialog.Builder member_error = new AlertDialog.Builder(activity);
                member_error.setTitle("BigBasket");
                member_error.setMessage(R.string.member_error)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                    Intent i = new Intent(activity, HomeActivity.class);
//                                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                    activity.finish();
                                activity.goToHome();
                            }

                        });
                AlertDialog member_errorDialog = member_error.create();
                member_errorDialog.show();
                break;
            case 10:
                AlertDialog.Builder missing_field = new AlertDialog.Builder(activity);
                missing_field.setTitle("BigBasket");
                missing_field.setMessage(R.string.missing_field)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }

                        });
                AlertDialog missing_fieldDialog = missing_field.create();
                missing_fieldDialog.show();
                break;

            case 11:
                AlertDialog.Builder login_req = new AlertDialog.Builder(activity);
                login_req.setTitle("BigBasket");
                login_req.setMessage(R.string.login_required)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                 Intent i = new Intent(activity, HomeActivity.class);
//                                 activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                 activity.finish();
                                activity.goToHome();
                            }

                        });
                AlertDialog login_reqDialog = login_req.create();
                login_reqDialog.show();
                break;


            case 13:
                AlertDialog.Builder BASKET_EMPTY = new AlertDialog.Builder(activity);
                BASKET_EMPTY.setTitle("BigBasket");
                BASKET_EMPTY.setMessage(R.string.BASKET_EMPTY)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                    /*Intent i=new Intent(BrowseByOffersActivity.this,ShopActivity.class);
                    overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
                    BrowseByOffersActivity.this.finish();*/

                            }

                        });
                AlertDialog BASKET_EMPTY_Dialog = BASKET_EMPTY.create();
                BASKET_EMPTY_Dialog.show();
                break;
            case 21:
                AlertDialog.Builder alertDialogbuild = new AlertDialog.Builder(activity);
                alertDialogbuild.setTitle("BigBasket");
                alertDialogbuild.setMessage(R.string.UNKNOWN_HOST_EXP)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                        Intent i = new Intent(activity, HomeActivity.class);
//                                        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                        activity.finish();
                                activity.goToHome();

                            }

                        });

                AlertDialog alertDialog2 = alertDialogbuild.create();
                alertDialog2.show();
                break;

            case 22:
                AlertDialog.Builder Alertdialogbuild = new AlertDialog.Builder(activity);
                Alertdialogbuild.setTitle("BigBasket");
                Alertdialogbuild.setMessage(R.string.TIMEOUT_EXP)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                        Intent i = new Intent(activity, HomeActivity.class);
//                                        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                                        activity.finish();
                                activity.goToHome();

                            }

                        });

                AlertDialog alertDialog5 = Alertdialogbuild.create();
                alertDialog5.show();
                break;
            case 23:
                AlertDialog.Builder AlertdialogBuild = new AlertDialog.Builder(activity);
                AlertdialogBuild.setTitle("BigBasket");
                AlertdialogBuild.setMessage(R.string.JSON_EXP)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }

                        });

                AlertDialog alertDialog7 = AlertdialogBuild.create();
                alertDialog7.show();
                break;
            case 24:
                AlertDialog.Builder alertdialogBuild = new AlertDialog.Builder(activity);
                alertdialogBuild.setTitle("BigBasket");
                alertdialogBuild.setMessage(R.string.GENERAL_EXP)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }

                        });

                AlertDialog alertDialog8 = alertdialogBuild.create();
                alertDialog8.show();
                break;
            case 25:
                AlertDialog.Builder alertdialogBuild12 = new AlertDialog.Builder(activity);
                alertdialogBuild12.setTitle("BigBasket");
                alertdialogBuild12.setMessage(R.string.IOEXCEPTION_EXP)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }

                        });

                AlertDialog alertDialog10 = alertdialogBuild12.create();
                alertDialog10.show();
                break;
            case MessageCode.GO_MARKET_PLACE:
                Intent intent = new Intent(activity, BasketValidationActivity.class);
                intent.putExtra(Constants.MARKET_PLACE_INTENT, marketPlace);
                activity.startActivityForResult(intent, Constants.GO_TO_HOME);
                activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;

            case MessageCode.GO_AGE_VALIDATION:
                intent = new Intent(activity, AgeValidationActivity.class);
                intent.putExtra(Constants.MARKET_PLACE_INTENT, marketPlace);
                activity.startActivityForResult(intent, Constants.GO_TO_HOME);
                activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;

            case MessageCode.CO_RESERVE_QUANTITY_CHECK_OK:
//                int qc_len = activity.getCOReserveQuantity().isQcHasErrors() ? 0 : 1;
//                intent = new Intent(activity, BBActivity.class);
//                intent.putExtra(Constants.QC_LEN, qc_len);
//                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_QC);
//                activity.startActivityForResult(intent, Constants.GO_TO_HOME);
//                activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                // TODO : Replace with better handling
                break;
//            case MessageCode.CALL_HOME_PAGE_SUMMARY:
//                new GetCartCountTask(activity, MobileApiUrl.getBaseAPIUrl() +
//                        Constants.C_SUMMARY + activity.getCartRequestParams()).execute();
//                break;
            default:
                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(activity);
                alertDialogBuilder2.setTitle("BigBasket");
                alertDialogBuilder2.setMessage("No Response")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                activity.finish();

                            }

                        });

                AlertDialog alertDialog9 = alertDialogBuilder2.create();
                alertDialog9.show();
        }
    }
}
