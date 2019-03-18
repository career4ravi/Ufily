/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package freeze.in.co.ufily;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import freeze.in.co.ufily.common.UfilyConstants;
import freeze.in.co.ufily.common.WhitelistCheck;

public  class AddStickerPackActivity extends BaseActivity {
    public static final int ADD_PACK = 200;
    public static final String TAG = "AddStickerPackActivity";

    /**
     * Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";

    public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
    public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
    public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
    public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";

    private String mPackName;
    private String mPackIdentifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(UfilyConstants.APP, "OnCreate()");

        Intent intent = getIntent();
        mPackName = intent.getStringExtra(UfilyConstants.EXTRA_PACK_NAME);
        mPackIdentifier = intent.getStringExtra(UfilyConstants.EXTRA_PACK_IDENTIFIER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        Log.d(UfilyConstants.APP, "OnStart()");
        addStickerPackToWhatsApp(mPackIdentifier,mPackName);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(UfilyConstants.APP, "OnResume()");
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(UfilyConstants.APP, "OnPause()");
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(UfilyConstants.APP, "OnStop()");
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(UfilyConstants.APP, "OnDestroyed()");
        // The activity is about to be destroyed.
    }


    protected void addStickerPackToWhatsApp(String identifier, String stickerPackName) {
        try {
            //if neither WhatsApp Consumer or WhatsApp Business is installed, then tell user to install the apps.
            if (!WhitelistCheck.isWhatsAppConsumerAppInstalled(getPackageManager()) && !WhitelistCheck.isWhatsAppSmbAppInstalled(getPackageManager())) {
                Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
                Log.d(UfilyConstants.APP,"watsapp is not installed1");
                return;
            }
            final boolean stickerPackWhitelistedInWhatsAppConsumer = WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(this, identifier);
            final boolean stickerPackWhitelistedInWhatsAppSmb = WhitelistCheck.isStickerPackWhitelistedInWhatsAppSmb(this, identifier);
            //if (!stickerPackWhitelistedInWhatsAppConsumer && !stickerPackWhitelistedInWhatsAppSmb) {
                //ask users which app to add the pack to.
                launchIntentToAddPackToChooser(identifier, stickerPackName);
            /*} else if (!stickerPackWhitelistedInWhatsAppConsumer) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME);
            } else if (!stickerPackWhitelistedInWhatsAppSmb) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME);
            } else {
                Log.d(UfilyConstants.APP,"watsapp is not installed2");
                Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
            }*/
        } catch (Exception e) {
            Log.e(TAG, "error adding sticker pack to WhatsApp",  e);
            Log.d(UfilyConstants.APP,"watsapp is not installed3"+e.getMessage());
            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
        }

    }

    private void launchIntentToAddPackToSpecificPackage(String identifier, String stickerPackName, String whatsappPackageName) {
        Intent intent = createIntentToAddStickerPack(identifier, stickerPackName);
        intent.setPackage(whatsappPackageName);
        try {
            startActivityForResult(intent, ADD_PACK);
        } catch (ActivityNotFoundException e) {
            Log.d(UfilyConstants.APP,"watsapp is not installed4:"+e.getMessage());
            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
        }
    }

    //Handle cases either of WhatsApp are set as default app to handle this intent. We still want users to see both options.
    private void launchIntentToAddPackToChooser(String identifier, String stickerPackName) {
        Intent intent = createIntentToAddStickerPack(identifier, stickerPackName);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)), ADD_PACK);
        } catch (ActivityNotFoundException e) {
            Log.d(UfilyConstants.APP,"watsapp is not installed5:"+e.getMessage());
            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private Intent createIntentToAddStickerPack(String identifier, String stickerPackName) {
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        intent.putExtra(EXTRA_STICKER_PACK_ID, identifier);
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, UfilyConstants.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null) {
                    final String validationError = data.getStringExtra("validation_error");

                    if (validationError != null) {
                        if (BuildConfig.DEBUG) {
                            //validation error should be shown to developer only, not users.
                            MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(getSupportFragmentManager(), "validation error");
                        }
                        Log.e(TAG, "Validation failed:" + validationError);
                    }
                } else {
                    new StickerPackNotAddedMessageFragment().show(getSupportFragmentManager(), "sticker_pack_not_added");
                }
            }
            else if(resultCode == Activity.RESULT_OK)
            {
                Log.d(UfilyConstants.APP,"AddStickerPackActivity():"+"Completed Adding sticker pack activity");
                finish();
            }
        }
    }

    public static final class StickerPackNotAddedMessageFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.add_pack_fail_prompt_update_whatsapp)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.add_pack_fail_prompt_update_play_link,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            launchWhatsAppPlayStorePage();
                        }});

            return dialogBuilder.create();
        }

        private void launchWhatsAppPlayStorePage() {
            if (getActivity() != null) {
                final PackageManager packageManager = getActivity().getPackageManager();
                final boolean whatsAppInstalled = WhitelistCheck.isPackageInstalled(WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME, packageManager);
                final boolean smbAppInstalled = WhitelistCheck.isPackageInstalled(WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME, packageManager);
                final String playPackageLinkPrefix = "http://play.google.com/store/apps/details?id=";
                if (whatsAppInstalled && smbAppInstalled) {
                    launchPlayStoreWithUri("https://play.google.com/store/apps/developer?id=WhatsApp+Inc.");
                } else if (whatsAppInstalled) {
                    launchPlayStoreWithUri(playPackageLinkPrefix + WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME);
                } else if (smbAppInstalled) {
                    launchPlayStoreWithUri(playPackageLinkPrefix + WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME);
                }
            }
        }

        private void launchPlayStoreWithUri(String uriString) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uriString));
            intent.setPackage("com.android.vending");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.cannot_find_play_store, Toast.LENGTH_LONG).show();
            }
        }
    }
}
