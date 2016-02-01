package com.bigbasket.mobileapp.fragment.communicationhub;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.communicationhub.AskUsAdapter;
import com.bigbasket.mobileapp.moe.addon.AskUsWelcomeView;
import com.bigbasket.mobileapp.util.Constants;
import com.crashlytics.android.Crashlytics;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moengage.addon.ubox.UBoxFragment;
import com.moengage.addon.ubox.UBoxManager;
import com.moengage.addon.ubox.UBoxMessenger;
import com.moengage.addon.ubox.UBoxUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by muniraju on 15/01/16.
 */
public class AskUsFragment extends UBoxFragment implements AskUsWelcomeView.onMsgSeenListener {
    private boolean mHasCamera;
    private static final int REQUEST_GET_FROM_GALLERY = 82;
    private static final int REQUEST_TAKE_PHOTO = 10;
    private final String PHOTO_PATH = "PHOTO_PATH";
    private String mCurrentPhotoPath;
    private final String SHOW_INFO_MESSAGE = "show_info_message";
    private AskUsWelcomeView welcomeInfoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UBoxManager.getInstance().setUboxAdapter(new AskUsAdapter(getResources()));
        setHasOptionsMenu(true);
        PackageManager pm = getActivity().getPackageManager();
        mHasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        welcomeInfoView = (AskUsWelcomeView) view.findViewById(R.id.welcome_view);
        //Work around to remove the color filter added by MoE
        EditText textInputBox = (EditText) view.findViewById(com.moengage.addon.ubox.R.id.inputMsg);
        final ImageButton btnSend = (ImageButton) view.findViewById(com.moengage.addon.ubox.R.id.btnSend);
        textInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setColorFilter(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean showInfoMsg = prefs.getBoolean(SHOW_INFO_MESSAGE, true);
        if (showInfoMsg) {
            welcomeInfoView.setMsgSeenListener(this);
        }

        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 7); //TODO: 7AM is hard coded for now
        long csStartTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 22); //TODO: 10PM is hard coded for nw
        long csEndTime = cal.getTimeInMillis();

        if (!showInfoMsg && now >= csStartTime && now < csEndTime) {
            welcomeInfoView.setExpanded(false);
        } else {
            welcomeInfoView.setExpanded(true);
        }
        super.onResume();
    }

    @Override
    public void onMsgSeen() {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        prefs.edit().putBoolean(SHOW_INFO_MESSAGE, false).apply();
        welcomeInfoView.setMsgSeenListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UBoxManager.getInstance().setUboxAdapter(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_ask_me, menu);
        if (!mHasCamera) {
            menu.removeItem(R.id.newImage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
                TaskStackBuilder.create(getActivity())
                        .addNextIntentWithParentStack(upIntent).startActivities();
                return true;
            }
            if (getActivity() != null) {
                getActivity().finish();
            }
            return true;
        } else if (item.getItemId() == R.id.storedImage) {
            getImageFromGallery();
            return true;
        } else if (item.getItemId() == R.id.newImage) {
            ArrayList<String> requiredPermissions = new ArrayList<>(2);
            if (!hasPermission(Manifest.permission.CAMERA)) {
                requiredPermissions.add(Manifest.permission.CAMERA);
            }
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                    && !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (requiredPermissions.isEmpty()) {
                dispatchTakePictureIntent();
            } else {
                requestPermissions(
                        requiredPermissions.toArray(new String[requiredPermissions.size()]),
                        Constants.PERMISSION_REQUEST_CODE_CAPTURE_CAMERA);
            }
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mHelper.onSaveInstanceState(outState);
        if (null != mCurrentPhotoPath) {
            outState.putString(PHOTO_PATH, mCurrentPhotoPath);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == REQUEST_GET_FROM_GALLERY
                && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                final Uri uri = resultData.getData();
                if (null == uri) return;

                uploadImage(uri.toString());
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {

            if (resultCode == Activity.RESULT_OK) {
                uploadImage(mCurrentPhotoPath);
                mCurrentPhotoPath = null;

            } else {
                File filenew = new File(mCurrentPhotoPath);
                if (filenew.length() == 0) {
                    filenew.delete();
                }
            }
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        try {
            startActivityForResult(intent, REQUEST_GET_FROM_GALLERY);
        } catch (ActivityNotFoundException ex) {
            Crashlytics.logException(ex);
            //Ignore
        }
    }

    //Copied from com.moengage.addon.ubox.UBoxFragment.getPreburntMessage()
    UnifiedInboxMessage getPreburntMessage() {
        UnifiedInboxMessage chatItem = new UnifiedInboxMessage();
        chatItem.author = "User";
        chatItem.gtime = System.currentTimeMillis();
        chatItem.msgTtl = chatItem.gtime + 7776000000L;
        chatItem.setTimestamp(chatItem.gtime);
        return chatItem;
    }

    private void uploadImage(String imageUri) {
        if (TextUtils.isEmpty(imageUri)) {
            return;
        }
        UnifiedInboxMessage chatMsg = getPreburntMessage();
        chatMsg.messageType = UnifiedInboxMessage.MSG_TYPE_PIC;
        chatMsg.localUri = imageUri;

        Message payload = Message.obtain(null, UBoxMessenger.MSG_UPLOAD_IMAGE);
        payload.obj = chatMsg;

        sendMessage(payload);
    }

    private String getTempDirectoryPath() {
        File cache;

        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            cache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/cache/");
        }
        // Use internal storage
        else {
            cache = getContext().getCacheDir();
        }

        // Create the cache directory if it doesn't exist
        if (!cache.exists()) {
            cache.mkdirs();
        }

        return cache.getAbsolutePath();
    }

    public File getOutputImageFile() {

        String tstamp = new SimpleDateFormat(UBoxUtils.TIME_FORMAT).format(new Date());
        File file = new File(getTempDirectoryPath(), UBoxUtils.FILE_NAME_PREFIX + tstamp + UBoxUtils.FILE_NAME_EXTN);
        mCurrentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = getOutputImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                try {
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } catch (ActivityNotFoundException ex) {
                    Crashlytics.logException(ex);
                    photoFile.delete();
                }
            }
        }
    }


    /**************
     * code for Android M Support
     ******************/

    private boolean hasPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getActivity(), permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAPTURE_CAMERA:
                if (grantResults.length > 0 && permissions.length > 0) {
                    int i = 0;
                    for (String p : permissions) {
                        if (p.equals(Manifest.permission.CAMERA)
                                && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            dispatchTakePictureIntent();
                            break;
                        }
                        i++;
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
