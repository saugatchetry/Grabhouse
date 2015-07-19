package saugat.chetry.com.grabhouse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by a511863 on 13/07/15.
 */
public class MyPhotoAdapter extends BaseAdapter {

    private Activity callingActivity;
    private ArrayList<String> filePaths = new ArrayList<String>();
    private ArrayList<String> savedAddresses = new ArrayList<String>();
    ArrayList<Bitmap> myImages = new ArrayList<Bitmap>();
    private int imageWidth;
    int position = 0;

    ImageView iv_photo;
    TextView tv_address;


    public MyPhotoAdapter(Activity activity,ArrayList<String> paths,int width,ArrayList<String> addresses)
    {
        this.callingActivity = activity;
        this.filePaths = paths;
        this.imageWidth = width;
        this.savedAddresses = addresses;
    }



    @Override
    public int getCount() {
        return filePaths.size();
    }

    @Override
    public Object getItem(int i) {
        return this.filePaths.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        position = i;
        View row = view;

        if(row == null)
        {
            LayoutInflater inflater = (LayoutInflater) callingActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_photo, viewGroup, false);
        }

        iv_photo = (ImageView) row.findViewById(R.id.photoView);
        tv_address = (TextView) row.findViewById(R.id.tv_address);
        tv_address.setText(savedAddresses.get(i));
        AsyncImageLoader loader = new AsyncImageLoader(iv_photo,i);
        loader.execute(filePaths);
        return row;
    }

    class AsyncImageLoader extends AsyncTask<ArrayList<String>,Bitmap,Void>{


        ProgressDialog dialog;
        private final WeakReference<ImageView> imageViewReference;
        private int counter;

        public AsyncImageLoader(ImageView imageView,int index)
        {
            imageViewReference = new WeakReference<ImageView>(imageView);
            counter = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(callingActivity,"Loading Images","Please Wait ....");
        }

        @Override
        protected Void doInBackground(ArrayList<String>... arrays) {
            //Log.d("Test","Total Images :- "+arrays[0].size());
            Log.d("Test","Position = "+position);
            //for(int i = 0; i< arrays[0].size(); i++)
            //{
                Bitmap map = decodeFile(arrays[0].get(counter).toString(), 150, 150);
                myImages.add(map);
                publishProgress(map);
            //}

            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... bitmap) {

            //iv_photo.setImageBitmap(bitmap[0]);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap[0]);
                }
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            dialog.dismiss();
            /*for(int i = 0; i < myImages.size(); i ++)
            {
                iv_photo.setImageBitmap(myImages.get(i));
            }*/
        }


        public Bitmap decodeFile(String filePath, int WIDTH, int HIGHT) {
            try {

                File f = new File(filePath);

                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                final int REQUIRED_WIDTH = WIDTH;
                final int REQUIRED_HIGHT = HIGHT;
                int scale = 1;
                while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
                        && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                    scale *= 2;

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
