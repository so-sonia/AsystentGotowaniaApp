package com.example.sonia.asystentgotowania.allrecipeview;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sonia.asystentgotowania.Constants;
import com.example.sonia.asystentgotowania.R;
import com.example.sonia.asystentgotowania.databaseforrecipes.DataBaseSingleton;
import com.example.sonia.asystentgotowania.databaseforrecipes.RecipeEntity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecipeAdapter extends BaseAdapter {
    private static final String TAG = Constants.APP_TAG.concat(RecipeAdapter.class.getSimpleName());
    private Context mContext;
    private List<RecipeEntity> mRecipeList; // ma dlugie i
    // niepotrzebne tutaj skladniki i przygotowanie, ale bedzie tak latwiej otwierac oneRecipe

    public RecipeEntity getRecipe(int position) {
        return mRecipeList.get(position);
    }

    public void removeRecipeAt(int position) {
        mRecipeList.remove(position);
        notifyDataSetChanged();

        ContextWrapper cw = new ContextWrapper(mContext);
        final File directory = cw.getExternalFilesDir("pictures");
        File myImageFile = new File(directory, mRecipeList.get(position).getPictureTitle());
        if (myImageFile.delete()) Log.d(TAG, "image on the disk deleted successfully!");
    }

    public RecipeAdapter(Context context) {
        mContext = context;
        mRecipeList = new ArrayList<>();
        new QueryDatabase().execute();
    }

    public int getCount() {
        Log.d("IMAGE ADAPTER", "shelf size " + mRecipeList.size());
        return mRecipeList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.all_recipes_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.title.setText(mRecipeList.get(position).getTitle());
        //TODO image in database, get image from mRecipeList.get(position).getImageUri()<-this method
        // doesn't exist at this moment

        ContextWrapper cw = new ContextWrapper(mContext);
        final File directory = cw.getExternalFilesDir("pictures");
        File myImageFile = new File(directory, mRecipeList.get(position).getPictureTitle());

        Picasso.with(mContext).load(myImageFile)
                .placeholder(R.drawable.default_picture_r)
                .error(R.drawable.default_picture_r)
                .into( holder.image);

        return view;
    }

//    private ArrayList<File> getAllSavedRecipes(List<RecipeEntity> recipeList){
//        ArrayList<File> pictureFiles = new ArrayList<File>();
//
//        for (int i=0 ; i< recipeList.size(); i++){
//            String title = recipeList.get(i).getTitle();
//            File file = new File(Environment.getExternalStorageDirectory() + title);
//            pictureFiles.add(file);
//        }
//
//        return(pictureFiles);
//    }

    private class QueryDatabase extends AsyncTask<Void, Void, List<RecipeEntity>> {
        @Override
        protected List<RecipeEntity> doInBackground(Void... params) {
            List<RecipeEntity> recipeList = DataBaseSingleton.getInstance(mContext).getAllRecipes();
            return recipeList;
        }

        @Override
        protected void onPostExecute(List<RecipeEntity> recipeList) {
            Log.i(TAG, "Recipes Loaded from DB");
            Log.d(TAG, "All recipies: " + writeDownAllRecipies(recipeList));
//            mRecipeList = getAllSavedRecipes(recipeList);
            mRecipeList = recipeList;
            notifyDataSetChanged();
        }
    }

    private String writeDownAllRecipies(List<RecipeEntity> recipeList) {
        String recipies = "";
        for (RecipeEntity recipeEntity : recipeList) {
            recipies = recipies.concat(" " + recipeEntity.getTitle());
        }
        return recipies;
    }


    static class ViewHolder {
        @BindView(R.id.item_title)
        TextView title;
        @BindView(R.id.item_image)
        ImageView image;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}