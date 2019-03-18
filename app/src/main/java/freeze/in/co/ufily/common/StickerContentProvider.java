/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package freeze.in.co.ufily.common;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static freeze.in.co.ufily.common.DataBaseHandler.getDBInstance;
import static freeze.in.co.ufily.common.DataBaseHandler.initializeDb;

public class StickerContentProvider extends ContentProvider {

    /**
     * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
     */
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website";

    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
    public static final String CONTENT_FILE_NAME = "contents.json";

    public static Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(UfilyConstants.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).build();

    /**
     * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
     */
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static final String METADATA = "metadata";
    private static final int METADATA_CODE = 1;

    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;

    static final String STICKERS = "stickers";
    private static final int STICKERS_CODE = 3;

    static final String STICKERS_ASSET = "stickers_asset";
    private static final int STICKERS_ASSET_CODE = 4;

    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;

    private List<StickerPack> stickerPackList;



    @Override
    public boolean onCreate() {

        initializeDb(getContext());

        SQLiteDatabase db = getDBInstance().getWritableDatabase();

        if(db != null)
            Log.d(UfilyConstants.APP, this.getClass()+":OnCreate()"+ " db created");


        final String authority = UfilyConstants.CONTENT_PROVIDER_AUTHORITY;
        if (!authority.startsWith(Objects.requireNonNull(getContext()).getPackageName())) {
            throw new IllegalStateException("your authority (" + authority + ") for the content provider should start with your package name: " + getContext().getPackageName());
        }

        Log.d(UfilyConstants.APP,"StickerContentProvider::onCreate()");

        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE);

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE);

        for (StickerPack stickerPack : getStickerPackList()) {
            MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier +    "/" + stickerPack.trayImageFile, STICKER_PACK_TRAY_ICON_CODE);
            for (Sticker sticker : stickerPack.getStickers()) {
                //String webpfile = sticker.imageFileName.replaceAll("png","webp");
                String stickerAssetCode = STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker.imageFileName;
                Log.d(UfilyConstants.APP,"StickerContentProvider::onCreate() sticketAssetcode:"+stickerAssetCode);
                MATCHER.addURI(authority, stickerAssetCode, STICKERS_ASSET_CODE);
            }
        }



        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);


        Log.d(UfilyConstants.APP,"StickerContentProvider::query():"+code);
        if (code == METADATA_CODE) {
            return getPackForAllStickerPacks(uri);
        } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
            return getCursorForSingleStickerPack(uri);
        } else if (code == STICKERS_CODE) {
            return getStickersForAStickerPack(uri);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
/*
    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) {

        Log.d(UfilyConstants.APP,"StickerContentProvider::openAssetFile");

        final int matchCode = MATCHER.match(uri);
        if (matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {
            return getImageAsset(uri);
        }
        return null;
    }

*/

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Log.d(UfilyConstants.APP,"StickerContentProvider::openFile():Uri:"+uri.getPath());

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new IllegalArgumentException("path segments should be 3, uri is: " + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);

        Log.d(UfilyConstants.APP,"StickerContentProvider::openFile():"+fileName);
        File privateFile = null;
        String cuppy = "tray_Cuppy.png";
        if(fileName.equals(cuppy))
        {
            String path = "/storage/emulated/0/Pictures/tray_Cuppy.png";
            Uri uri_path  = Uri.parse(path);
            Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + uri_path.getPath());
            privateFile  = new File(uri_path.getPath());
        }
        else {

            Ufily selectedUfily = getDBInstance().getUfilyByName(fileName);
            if (selectedUfily != null) {
                Uri imageUri = Uri.parse(selectedUfily.getImagePath1());
                if (imageUri != null) {
                    privateFile = new File(imageUri.getPath());
                    Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + imageUri.getPath());
                }
            }
        }

        if(privateFile == null)
        {
            throw new IllegalArgumentException("illegal file fetch"+uri);
        }

        return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }




    public String[] getStreamTypes (Uri uri,
                                    String mimeTypeFilter)
    {
        Log.d(UfilyConstants.APP,"StickerContentProvider::getStreamTypes()"+uri.getPath());
        final int matchCode = MATCHER.match(uri);

        switch (matchCode) {
            case  STICKERS_ASSET_CODE:
            case STICKER_PACK_TRAY_ICON_CODE:
            String mimeTypes[] = { "image/jpeg", "image/png", "image/gif"};
            return mimeTypes;
            default:
                Log.d(UfilyConstants.APP,"Unknown StickerContentProvider::getStreamTypes()");
                return null;
        }
    }


    @Override
    public String getType(@NonNull Uri uri) {

        Log.d(UfilyConstants.APP,"StickerContentProvider::getType()"+uri.getPath());
        final int matchCode = MATCHER.match(uri);
        switch (matchCode) {
            case METADATA_CODE:
                return "vnd.android.cursor.dir/vnd." + UfilyConstants.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK:
                return "vnd.android.cursor.item/vnd." + UfilyConstants.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case STICKERS_CODE:
                return "vnd.android.cursor.dir/vnd." + UfilyConstants.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
            case STICKERS_ASSET_CODE:
                return "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE:
                return "image/png";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private synchronized void readContentFile(@NonNull Context context) {


        /*try (InputStream contentsInputStream = context.getAssets().open(CONTENT_FILE_NAME)) {
            //stickerPackList = ContentFileParser.parseStickerPacks(contentsInputStream);*/


/*
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException(CONTENT_FILE_NAME + " file has some issues: " + e.getMessage(), e);
        }*/
    }

    public List<StickerPack> getStickerPackList() {

        //TODO provide the latest list
        //if (stickerPackList == null) {

            Log.d(UfilyConstants.APP,"Adding Sticker pack");
            //readContentFile(Objects.requireNonNull(getContext()));
            stickerPackList = new ArrayList<StickerPack>();
            StickerPack stickerpack =
                    new StickerPack("face_smile_identifier","facesmiles1","Ravi","tray_Cuppy.png","", "","", "");

            List <String> smileyList  = new ArrayList<>();
            smileyList.add(new String(":-||"));
            smileyList.add(new String(":@"));

            List<Sticker> sticketlist = new ArrayList<>();

            List<Ufily> ufilyList = getDBInstance().getAllUfily();

            for(int i=0;i<ufilyList.size();i++)
            {
                Log.d(UfilyConstants.APP,"Adding sticker item:"+String.valueOf(ufilyList.get(i).getId()));
                sticketlist.add(new Sticker(String.valueOf(ufilyList.get(i).getName()),smileyList));
            }



            //sticketlist.add(new Sticker(String.valueOf("01_Cuppy_smile.webp"),smileyList));
            //sticketlist.add(new Sticker(String.valueOf("02_Cuppy_lol.webp"),smileyList));
            //sticketlist.add(new Sticker(String.valueOf("03_Cuppy_rofl.webp"),smileyList));
            //sticketlist.add(new Sticker(String.valueOf("04_Cuppy_sad.webp"),smileyList));
            //sticketlist.add(new Sticker(String.valueOf("share_image_1547921914159.webp"),smileyList));

            stickerpack.setStickers(sticketlist);
            stickerPackList.add(stickerpack);

        //}
        return stickerPackList;
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        Log.d(UfilyConstants.APP,"StickerContentProvider::getPackForAllStickerPacks()");
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {

        Log.d(UfilyConstants.APP,"StickerContentProvider::getCursorForSingleStickerPack()"+uri.getPath());
        final String identifier = uri.getLastPathSegment();
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }

        return getStickerPackInfo(uri,  new ArrayList<StickerPack>());
    }

    @NonNull
    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {


        Log.d(UfilyConstants.APP,"StickerContentProvider::getStickerPackInfo()"+uri.getPath());
        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREENMENT_WEBSITE
                });
        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.identifier);
            builder.add(stickerPack.name);
            builder.add(stickerPack.publisher);
            builder.add(stickerPack.trayImageFile);
            builder.add(stickerPack.androidPlayStoreLink);
            builder.add(stickerPack.iosAppStoreLink);
            builder.add(stickerPack.publisherEmail);
            builder.add(stickerPack.publisherWebsite);
            builder.add(stickerPack.privacyPolicyWebsite);
            builder.add(stickerPack.licenseAgreementWebsite);
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {

        Log.d(UfilyConstants.APP,"StickerContentProvider::getStickersForAStickerPack()"+uri.getPath());

        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY});
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.imageFileName, TextUtils.join(",", sticker.emojis)});
                    Log.d(UfilyConstants.APP,"StickerContentProvider::getStickersForAStickerPack() Adding"+sticker.imageFileName);
                }
            }
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    private AssetFileDescriptor getImageAsset(Uri uri) throws IllegalArgumentException {
        AssetManager am = Objects.requireNonNull(getContext()).getAssets();
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new IllegalArgumentException("path segments should be 3, uri is: " + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);
        final String identifier = pathSegments.get(pathSegments.size() - 2);
        if (TextUtils.isEmpty(identifier)) {
            throw new IllegalArgumentException("identifier is empty, uri: " + uri);
        }
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("file name is empty, uri: " + uri);
        }
        //making sure the file that is trying to be fetched is in the list of stickers.
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                if (fileName.equals(stickerPack.trayImageFile)) {
                    return fetchFile(uri, am, fileName, identifier);
                } else {
                    for (Sticker sticker : stickerPack.getStickers()) {
                        if (fileName.equals(sticker.imageFileName)) {
                            return fetchFile(uri, am, fileName, identifier);
                        }
                    }
                }
            }
        }
        return null;
    }

    private AssetFileDescriptor fetchFile(@NonNull Uri uri, @NonNull AssetManager am, @NonNull String fileName, @NonNull String identifier) {
        Log.d(UfilyConstants.APP,"StickerContentProvider::fetchFile:"+uri.getPath());
        try {
            return am.openFd(identifier + "/" + fileName);
        } catch (IOException e) {
            Log.e(Objects.requireNonNull(getContext()).getPackageName(), "IOException when getting asset file, uri:" + uri, e);
            return null;
        }
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

           /*
        if (false) {

                String stickerpath1 = "/storage/emulated/0/Android/data/freeze.in.co.ufily/files/Pictures/01_Cuppy_smile.webp";
                String stickerpath2 = "/storage/emulated/0/Android/data/freeze.in.co.ufily/files/Pictures/02_Cuppy_lol.webp";
                String stickerpath3 = "/storage/emulated/0/Android/data/freeze.in.co.ufily/files/Pictures/03_Cuppy_rofl.webp";
                String stickerpath4 = "/storage/emulated/0/Android/data/freeze.in.co.ufily/files/Pictures/04_Cuppy_sad.webp";
                String stickerpath5 = "/storage/emulated/0/Android/data/freeze.in.co.ufily/files/Pictures/share_image_1547921914159.webp";

    String stickerpath1 = "/storage/emulated/0/Download/01_Cuppy_smile.webp";
    String stickerpath2 = "/storage/emulated/0/Download/02_Cuppy_lol.webp";
    String stickerpath3 = "/storage/emulated/0/Download/03_Cuppy_rofl.webp";
    String stickerpath4 = "/storage/emulated/0/Download/04_Cuppy_sad.webp";
    String stickerpath5 = "/storage/emulated/0/Download/share_image_1547921914159.webp";

                if (fileName.equals("01_Cuppy_smile.webp")) {
        Uri uri_path = Uri.parse(stickerpath1);
        Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + uri_path.getPath());
        privateFile = new File(uri_path.getPath());
    } else if (fileName.equals("02_Cuppy_lol.webp")) {
        Uri uri_path = Uri.parse(stickerpath2);
        Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + uri_path.getPath());
        privateFile = new File(uri_path.getPath());
    } else if (fileName.equals("03_Cuppy_rofl.webp")) {
        Uri uri_path = Uri.parse(stickerpath3);
        Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + uri_path.getPath());
        privateFile = new File(uri_path.getPath());
    } else if (fileName.equals("share_image_1547921914159.webp")) {
        Uri uri_path = Uri.parse(stickerpath5);
        Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + uri_path.getPath());
        privateFile = new File(uri_path.getPath());
    } else {
        Uri uri_path = Uri.parse(stickerpath4);
        Log.d(UfilyConstants.APP, "StickerContentProvider::openFile():" + uri_path.getPath());
        privateFile = new File(uri_path.getPath());

    }


}

     */

        throw new UnsupportedOperationException("Not supported");
    }
}
