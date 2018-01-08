package com.example.sonia.asystentgotowania.allrecipeview;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.example.sonia.asystentgotowania.databaseforrecipes.RecipeEntity;
import com.squareup.picasso.Picasso;

import com.example.sonia.asystentgotowania.R;
import com.example.sonia.asystentgotowania.databaseforrecipes.DataBaseSingleton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.webkit.WebViewDatabase.getInstance;


public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<File> mPictureFiles;

    public ImageAdapter(Context c) {
        mContext = c;
        mPictureFiles = new ArrayList<File>();
        new QueryDatabase().execute();
    }

    public int getCount() {
        Log.d("IMAGE ADAPTER", "shelf size " + mPictureFiles.size());
        return mPictureFiles.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext).load(mPictureFiles.get(position))
                .placeholder(R.drawable.default_picture_r)
                .error(R.drawable.default_picture_r)
                .noFade().resize(200, 200).centerCrop()
                .into(imageView);

        return imageView;
    }

    private ArrayList<File> getAllSavedRecipes(List<RecipeEntity> recipeList){
        //Może przechowywać jako stringi zamiast files, wtedy z doklejonym jpg
        ArrayList<File> pictureFiles = new ArrayList<File>();

        for (int i=0 ; i< recipeList.size(); i++){
            String title = recipeList.get(i).getTitle();
            File file = new File(Environment.getExternalStorageDirectory() + title);
            pictureFiles.add(file);
        }

        return(pictureFiles);

    }

    private class QueryDatabase extends AsyncTask<Void, Void, List<RecipeEntity>> {
        @Override
        protected List<RecipeEntity> doInBackground(Void... params) {
            List<RecipeEntity> recipeList = DataBaseSingleton.getInstance(mContext).getAllRecipes();
            return recipeList;
        }

        @Override
        protected void onPostExecute(List<RecipeEntity> recipeList) {
            mPictureFiles = getAllSavedRecipes(recipeList);
        }
    }

    // references to our images
//    private Integer[] mThumbIds = {
//            R.drawable.gulab_pion, R.drawable.gulab_pion,
//            R.drawable.gulab_pion, R.drawable.gulab_pion,
//            R.drawable.default_picture_r, R.drawable.default_picture_r,
//            R.drawable.default_picture_r, R.drawable.default_picture_r,
//            R.drawable.gulab_pion, R.drawable.gulab_pion,
//            R.drawable.gulab_pion, R.drawable.gulab_pion,
//            R.drawable.ic_play_r, R.drawable.ic_play_r,
//            R.drawable.ic_play_r, R.drawable.ic_play_r,
//    };
}