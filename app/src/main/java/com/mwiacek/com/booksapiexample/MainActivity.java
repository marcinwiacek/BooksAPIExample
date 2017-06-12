package com.mwiacek.com.booksapiexample;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class MainActivity extends Activity {
    private static ViewSwitcher mViewSwitcher;

    // controls from first layout in ViewSwitcher
    private static ProgressBar mBooksListProgressBar;
    private static ListView mBooksListListView;
    private static Button mSearchButton;
    private static EditText mBooksListCriteriaEditText;
    private static BooksListListViewAdapter customAdapter;

    // controls from second layout in ViewSwitcher
    private static Button mBackButton;
    private static TextView mBookInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        mBookInfoTextView = (TextView) findViewById(R.id.bookInfoTextView);
        mBookInfoTextView.setMovementMethod(new ScrollingMovementMethod());

        mBackButton = (Button) findViewById(R.id.backButton);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewSwitcher.showPrevious();
            }
        });

        mBooksListProgressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        mBooksListCriteriaEditText = (EditText) findViewById(R.id.searchEditText);
        mBooksListCriteriaEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    mSearchButton.callOnClick();
                    return true;
                }
                return false;
            }
        });

        mBooksListListView = (ListView) findViewById(R.id.booksListListView);
        customAdapter = new BooksListListViewAdapter();
        customAdapter.BooksListListViewAdapterSearch(
                mBooksListCriteriaEditText.getText().toString(),
                getApplicationContext(),
                mBooksListProgressBar);
        mBooksListListView.setAdapter(customAdapter);
        mBooksListListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = (Book) parent.getAdapter().getItem(position);
                if (book == null) {
                    return;
                }

                // TODO: replacing hardcoded texts with Resources

                mBookInfoTextView.setText("");
                mBookInfoTextView.append("Title: " + book.volumeInfo.title + "\r\n\r\n");
                if (book.volumeInfo.authors != null) {
                    for (String author : book.volumeInfo.authors) {
                        mBookInfoTextView.append("Author: " + author + "\r\n\r\n");
                    }
                }
                if (book.volumeInfo.description != null) {
                    mBookInfoTextView.append("Description: " + book.volumeInfo.description
                            + "\r\n\r\n");
                }
                if (book.volumeInfo.publishedDate != null) {
                    mBookInfoTextView.append("Published date: "
                            + book.volumeInfo.publishedDate + "\r\n\r\n");
                }
                if (book.volumeInfo.publisher != null) {
                    mBookInfoTextView.append("Publisher: " + book.volumeInfo.publisher);
                }

                mViewSwitcher.showNext();
            }
        });

        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBooksListCriteriaEditText.getText().length() == 0) {
                    // TODO: replacing hardcoded text with Resource
                    Toast.makeText(getApplicationContext(), "I need more, be creative...",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hide keyboard
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(
                                Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(
                        getCurrentFocus().getWindowToken(), 0);

                customAdapter.BooksListListViewAdapterSearch(
                        mBooksListCriteriaEditText.getText().toString(),
                        getApplicationContext(),
                        mBooksListProgressBar);
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mViewSwitcher.getDisplayedChild() == 1) {
            mViewSwitcher.showPrevious();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
