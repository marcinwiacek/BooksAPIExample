package com.mwiacek.com.booksapiexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import java.lang.Boolean;

import javax.net.ssl.HttpsURLConnection;

public class BooksListListViewAdapter extends BaseAdapter {
    ArrayList<Books> mData = new ArrayList();
    String mSearchText;
    ProgressBar mProgressBar;

    // 100 = number of cached bitmaps
    private LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(100);
    // How many books are read with one JSON
    private static final int itemsPerPage = 25;
    // Prefix for Google Books API search request.
    // API key (key) needs to be generated on the https://console.developers.google.com/
    private static final String searchURLPrefix =
            "https://www.googleapis.com/books/v1/volumes?key=";

    /**
     * Task for downloading and decoding JSON with books info
     */
    private class DownloadBooksInfoTask extends AsyncTask<Object, Void, Boolean> {
        BaseAdapter mAdapter;
        Context mContext;

        DownloadBooksInfoTask(BaseAdapter adapter, Context context) {
            mAdapter = adapter;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                URL url = new URL((String) params[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(5000); // 5 seconds
                connection.connect();
                try {
                    InputStream in = new BufferedInputStream(connection.getInputStream());

                    //Deserialization with Jackson
                    Books books = new ObjectMapper().readValue(in, Books.class);

                    if (((ArrayList<Books>) params[1]).size() == 0) {
                        if (books.totalItems != 0) {
                            ((ArrayList<Books>) params[1]).add(books);
                        }
                        return true;
                    } else {
                        if (books.totalItems != 0) {
                            ((ArrayList<Books>) params[1]).add(books);
                        }
                        return false;
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (IOException ignore) {
            }
            return false;
        }

        protected void onPostExecute(Boolean firstGroup) {
            if (firstGroup) {
                int results = mData.size() == 0 ? 0 : mData.get(0).totalItems;
                // TODO: replacing hardcoded text with Resource
                Toast.makeText(mContext, "Results: " + results,
                        Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Generates cache file name for thumbnail
     */
    public static String getDiskCacheFileName(Context context, String uniqueName) {
        final String cachePath = context.getExternalCacheDir() == null ?
                context.getCacheDir().getPath() : context.getExternalCacheDir().getPath();

        return cachePath + File.separator + uniqueName;
    }

    /**
     * Task for downloading book thumbnail and displaying it in the books list
     */
    private class DownloadThumbnailTask extends AsyncTask<Object, Void, String> {
        int mPosition;
        ViewHolder mViewHolder;

        DownloadThumbnailTask(int position, ViewHolder viewHolder) {
            mViewHolder = viewHolder;
            mPosition = position;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                URL url = new URL((String) params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000); // 5 seconds
                connection.connect();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());

                    mMemoryCache.put((String) params[1], bitmap);

                    try {
                        // TODO: implementing deleting oldest cache files

                        FileOutputStream fos = new FileOutputStream(
                                getDiskCacheFileName((Context) params[2], (String) params[1]));
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (IOException e) {
                    }
                } finally {
                    connection.disconnect();
                }
                return (String) params[1];
            } catch (IOException ignore) {
            }
            return null;
        }

        protected void onPostExecute(String id) {
            if (mPosition == mViewHolder.position && id != null) {
                mViewHolder.thumbnailPicture.setImageBitmap(mMemoryCache.get(id));
            }
        }
    }

    /**
     * Method for starting books search
     */
    public void BooksListListViewAdapterSearch(String searchText, Context context,
                                               ProgressBar progressBar) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            // TODO: replacing hardcoded text with Resource
            Toast.makeText(context, "Error, do you have network?",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mData.size() != 0) {
            mData.clear();
            notifyDataSetInvalidated();
        }

        mMemoryCache.evictAll();

        mSearchText = searchText;
        mProgressBar = progressBar;

        new DownloadBooksInfoTask(this, context).execute(
                searchURLPrefix + "&q=" + searchText + "&startIndex=0&maxResults=" + itemsPerPage,
                mData);
    }

    @Override
    public int getCount() {
        return mData.size() != 0 ?
                (mData.size() - 1) * itemsPerPage + mData.get(mData.size() - 1).items.length : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int i) {
        int currentItemGroup = i / itemsPerPage;
        int currentItemOffset = i - currentItemGroup * itemsPerPage;

        if (mData.get(currentItemGroup).items.length <= currentItemOffset) {
            // error in data - server didn't provide enough number of entries in response
            return null;
        } else {
            return mData.get(currentItemGroup).items[currentItemOffset];
        }
    }

    private static class ViewHolder {
        public TextView titleText;
        public ImageView thumbnailPicture;
        public int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.book_list_item, parent, false);

            holder = new ViewHolder();
            holder.titleText = (TextView) convertView.findViewById(R.id.BookName);
            holder.thumbnailPicture = (ImageView) convertView.findViewById(R.id.BookImage);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.position = position;

        int currentItemGroup = position / itemsPerPage;
        int currentItemOffset = position - currentItemGroup * itemsPerPage;

        // Read next page if we have last entry and something to read
        if (position == getCount() - 1 && position < mData.get(0).totalItems - 1) {
            new DownloadBooksInfoTask(this, convertView.getContext()).execute(
                    searchURLPrefix +
                            "&q=" + mSearchText +
                            "&startIndex=" + (currentItemGroup + 1) * itemsPerPage +
                            "&maxResults=" + itemsPerPage,
                    mData,
                    convertView.getContext());
        }

        if (mData.get(currentItemGroup).items.length <= currentItemOffset) {
            // TODO: replacing hardcoded text with Resource
            // Example of bug: q=android, startIndex=75, maxResults=25
            Toast.makeText(convertView.getContext(), "Error in data, q='" + mSearchText +
                            "', startIndex=" + ((currentItemGroup + 1) * itemsPerPage) +
                            ", maxResults=" + itemsPerPage,
                    Toast.LENGTH_SHORT).show();
            holder.titleText.setText("");
            holder.thumbnailPicture.setImageBitmap(null);
            return convertView;
        }

        holder.titleText.setText(
                mData.get(currentItemGroup).items[currentItemOffset].volumeInfo.title);

        // Check if we have thumbnail
        if (mData.get(currentItemGroup).items[currentItemOffset]
                .volumeInfo.imageLinks == null) {
            return convertView;
        }

        // Check if we have bitmap in memory cache
        if (mMemoryCache.get(mData.get(currentItemGroup).items[currentItemOffset].id) != null) {
            holder.thumbnailPicture.setImageBitmap(
                    mMemoryCache.get(mData.get(currentItemGroup).items[currentItemOffset].id));
            return convertView;
        }

        // Check if we have bitmap in disk cache
        File file = new File(getDiskCacheFileName(
                convertView.getContext(),
                mData.get(currentItemGroup).items[currentItemOffset].id));
        if (!file.exists()) {
            holder.thumbnailPicture.setImageBitmap(null);
            new DownloadThumbnailTask(position, holder).execute(
                    mData.get(currentItemGroup).items[currentItemOffset]
                            .volumeInfo.imageLinks.smallThumbnail,
                    mData.get(currentItemGroup).items[currentItemOffset].id,
                    convertView.getContext());

            return convertView;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(getDiskCacheFileName(
                convertView.getContext(),
                mData.get(currentItemGroup).items[currentItemOffset].id));
        holder.thumbnailPicture.setImageBitmap(bitmap);
        mMemoryCache.put(mData.get(currentItemGroup).items[currentItemOffset].id, bitmap);
        return convertView;
    }
}
