package freeze.in.co.ufily;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.felipecsl.gifimageview.library.GifImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import freeze.in.co.ufily.common.Ufily;
import freeze.in.co.ufily.common.UfilyConstants;

import static freeze.in.co.ufily.common.DataBaseHandler.getDBInstance;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class UfilyFunnyFacesGridView extends Fragment implements AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener,View.OnClickListener {

    private List<Ufily> mUfilyList;

    private ImageAdapter mImageAdapter;

    FrameLayout mFrameLayoutSemiTransparent;
    private GifImageView mGifImageView;


    private ImageButton mWatsAppImageAddButton;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(mFrameLayoutSemiTransparent.getVisibility() == view.VISIBLE)
        {
            return;
        }
        List<Ufily> ufilyList = getDBInstance().getAllUfily();
        Ufily ufily = ufilyList.get(position);
        Uri shareUri = Uri.fromFile(new File (ufily.getImagePath()));
        displayGif(shareUri);
        //((MainActivity)getActivity()).shareGif(shareUri);
    }

    @Override
    public void onClick(View v) {

        Log.d(UfilyConstants.APP,"UfilyRecentFragment::onClick() id:"+String.valueOf(v.getId()));
        switch (v.getId()) {
            case R.id.WatsAppImageAddButton:
                addtoWatsApp();
               break;
            case R.id.GifImageViewRecentFragment:
                toggleGifView();
                break;
        }
    }

    private void addtoWatsApp()
    {
        Intent intent = new Intent(getActivity(), AddStickerPackActivity.class);
         //TODO
        //List<StickerPack> sticker = ((MainActivity)getActivity()).getDatabaseHandler().getStickerList();
        intent.putExtra(UfilyConstants.EXTRA_PACK_IDENTIFIER,"face_smile_identifier");
        intent.putExtra(UfilyConstants.EXTRA_PACK_NAME,"Ravi");
        startActivity(intent);
    }

    private void toggleGifView()
    {
        if(mGifImageView.getVisibility() == View.VISIBLE)
        {
            mFrameLayoutSemiTransparent.setVisibility(View.INVISIBLE);
            if(mGifImageView.isAnimating()) {
                mGifImageView.stopAnimation();
            }
            mGifImageView.setVisibility(View.INVISIBLE);
        }
    }



    static class ViewHolder {
        TextView textView;
        CircleImageView circleImageView;
    }

    class ImageAdapter extends BaseAdapter {
        private Context mContext;


        // Constructor
        public ImageAdapter(Activity activity) {
            MainActivity mainactivity = (MainActivity)activity;
            mContext = activity.getApplicationContext();
        }

        public int getCount() {
            if(mUfilyList == null)
                return 0;

            return mUfilyList.size();
        }

        public Object getItem(int position) {
            return mUfilyList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            //viewholder to avoid calls to findViewById everytime.
             ViewHolder viewHolder;

            View view = convertView;
            if(view == null)
            {
                LayoutInflater inflater = (LayoutInflater)(mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
                view = inflater.inflate(R.layout.griditemlayout,null);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView)view.findViewById(R.id.gridItemImageView1);
                viewHolder.textView = (TextView)view.findViewById(R.id.gridItemtextView);
                view.setTag(viewHolder);
            }
            else
            {
                viewHolder =  (ViewHolder)view.getTag();
            }

        /*ImageView imageView = (ImageView)view.findViewById(R.id.gridItemImageView1);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(8, 8, 8, 8);
        imageView.setImageBitmap(lUfily.getImage());
        */

            Ufily lUfily  = mUfilyList.get(position);
            Uri uri = Uri.parse(lUfily.getImagePath());

            Glide.with(getActivity().getApplicationContext())
                   .load(new File(uri.getPath()))
                    .crossFade()
                    .into(
            new ViewTarget<CircleImageView, GlideDrawable>(viewHolder.circleImageView) {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    CircleImageView circleImageView = this.view;
                    circleImageView.setImageDrawable(resource.getCurrent());
                    // Set your resource on myView and/or start your animation here.
                }
            });

            //viewHolder.textView.setText(lUfily.getName());
            //TextView textView = (TextView)view.findViewById(R.id.gridItemtextView);
            //textView.setText(Integer.toString(position));
            return view;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(UfilyConstants.APP,"UfilyFamousFragment::onResume()");

        if(mGifImageView!= null && mGifImageView.getVisibility() == View.VISIBLE)
        {
            if(!mGifImageView.isAnimating()) {
                mGifImageView.startAnimation();
            }

        }
    }

    public UfilyFunnyFacesGridView() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(UfilyConstants.APP,"UfilyRecentFragment::onResume()");
        mGifImageView = null;
        mWatsAppImageAddButton = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //infalte views
        View recentUfilyView = inflater.inflate(R.layout.fragment_ufily_recent, container, false);
        GridView gridview = (GridView) recentUfilyView.findViewById(R.id.gridview);

        //fill the views and buttons or whatever
        mFrameLayoutSemiTransparent =(FrameLayout)recentUfilyView.findViewById(R.id.framelLayoutSemiTransparentRecent);
        mGifImageView = (GifImageView) recentUfilyView.findViewById(R.id.GifImageViewRecentFragment);
        mWatsAppImageAddButton = (ImageButton) recentUfilyView.findViewById(R.id.WatsAppImageAddButton);
        mGifImageView.setOnClickListener(this);
        mUfilyList = getDBInstance().getAllUfily();
        mImageAdapter = new ImageAdapter(this.getActivity());

        //setListners
        gridview.setAdapter(mImageAdapter);
        gridview.setOnItemLongClickListener(this);
        gridview.setOnItemClickListener(this);


        mWatsAppImageAddButton.setOnClickListener(this);

        return recentUfilyView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
            updateAndNotifyListView();


            if (mGifImageView != null && mGifImageView.getVisibility() == View.VISIBLE) {
                if (!mGifImageView.isAnimating()) {
                    mGifImageView.startAnimation();
                }

            }

        }


    }



    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        List<Ufily> ufilyList = getDBInstance().getAllUfily();
        Ufily ufily = ufilyList.get(position);

        getDBInstance().deleteUfily(ufily);

        updateAndNotifyListView();

        return true;
    }



    //diplays the GIfImageView
    private void displayGif(Uri result)
    {
        mFrameLayoutSemiTransparent.setVisibility(View.VISIBLE);

        Log.d(UfilyConstants.APP,"diaplaying GIF with glide:");
        try {

            mGifImageView.setBytes(readBytes(result));
            mGifImageView.setTag(result);
            //@TODO dismissing the dialog can be moved here
            mGifImageView.startAnimation();


            mGifImageView.setVisibility(View.VISIBLE);
            //mShareButon.setVisibility(View.VISIBLE);
        }
        catch (Exception e)
        {
            mFrameLayoutSemiTransparent.setVisibility(View.INVISIBLE);
            if(mGifImageView.isAnimating())
            {
                mGifImageView.stopAnimation();
            }
            Log.d(UfilyConstants.APP,"exception while GIF player"+e.getMessage());
        }




    }

    //@TODO should be done in separate thread
    public byte[] readBytes(Uri uri) throws IOException {
        // this dynamically extends to take the bytes you read
        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }


    private void updateAndNotifyListView()
    {

        //update list here for the
        mUfilyList = getDBInstance().getAllUfily();
        mImageAdapter.notifyDataSetChanged();
    }


}


