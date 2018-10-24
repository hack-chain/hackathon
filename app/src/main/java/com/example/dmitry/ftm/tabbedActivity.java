package com.example.dmitry.ftm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class tabbedActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        publicKey = (String) getIntent().getStringExtra("PUBLIC_KEY");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }


    public static class FirstFragment extends Fragment {
        private static final String PUBLIC_KEY = "PUBLIC KEY";

        private ImageView imageView;
        private TextView publicKey;
        private TextView moneyView;

        private ArrayList<String> arrayList;
        private ArrayAdapter<String> adapter;
        private Button addButtonView;

        private LayoutInflater layoutInflater;
        private View promptView;
        private AutoCompleteTextView publicKeyView;
        private EditText privateKeyView;

        public FirstFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static FirstFragment newInstance(String publicKey) {
            FirstFragment fragment = new FirstFragment();
            Bundle args = new Bundle();
            args.putString(PUBLIC_KEY, publicKey);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_first, container, false);

            imageView = (ImageView) rootView.findViewById(R.id.imageView);
            Bitmap identicon = Identicon.create(getArguments().getString(PUBLIC_KEY));
            imageView.setImageBitmap(identicon);

            publicKey = (TextView) rootView.findViewById(R.id.textView);
            publicKey.setText(getArguments().getString(PUBLIC_KEY));

            moneyView = (TextView) rootView.findViewById(R.id.moneyView);
            new getdata().execute(getArguments().getString(PUBLIC_KEY));

            ListView listview =(ListView)rootView.findViewById(R.id.list);
            arrayList = new ArrayList<String> ();
            arrayList.add(getArguments().getString(PUBLIC_KEY));

            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arrayList);

            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    publicKey.setText(selectedItem);

                    Bitmap identicon = Identicon.create(selectedItem);
                    imageView.setImageBitmap(identicon);

                    new getdata().execute(selectedItem);
                }
            });

            listview.setItemChecked(0,true);

            addButtonView = (Button) rootView.findViewById(R.id.addBtn);
            addButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInputDialog();
                }
            });

            return rootView;
        }

        protected void showInputDialog() {

            // get prompts.xml view
            layoutInflater = LayoutInflater.from(getActivity());
            promptView = layoutInflater.inflate(R.layout.input_dialog, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(promptView);

            publicKeyView = (AutoCompleteTextView) promptView.findViewById(R.id.publicKeyDialog);
            privateKeyView = (EditText) promptView.findViewById(R.id.privateKeyDialog);

            // setup a dialog window
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            attemptLogin();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create an alert dialog
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

        public class getdata extends AsyncTask<String, String, String> {

            HttpURLConnection urlConnection;

            @Override
            protected String doInBackground(String... args) {

                StringBuilder result = new StringBuilder();

                try {
                    String publicKey = args[0];
                    URL url = new URL("http://18.221.128.6:8080/account/" + publicKey);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }


                return result.toString();
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject mainObject = new JSONObject(result);
                    String balance = mainObject.getString("balance");

                    moneyView.setText("Balance: "+ balance);
                } catch (Exception e) {

                }
            }
        }

        private void attemptLogin() {

            publicKeyView.setError(null);
            privateKeyView.setError(null);

            // Store values at the time of the login attempt.
            String publicKey = publicKeyView.getText().toString();
            String privateKey = privateKeyView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            if (TextUtils.isEmpty(publicKey)) {
                publicKeyView.setError(getString(R.string.error_field_required));
                focusView = publicKeyView;
                cancel = true;
            } else if (!isEmailValid(publicKey)) {
                publicKeyView.setError(getString(R.string.error_invalid_public_key));
                focusView = publicKeyView;
                cancel = true;
            }

            if (TextUtils.isEmpty(privateKey)){
                privateKeyView.setError(getString(R.string.error_field_required));
                focusView = privateKeyView;
                cancel = true;
            } else if(!isPasswordValid(privateKey)) {
                privateKeyView.setError(getString(R.string.error_invalid_password));
                focusView = privateKeyView;
                cancel = true;
            }

            if (cancel) {
            } else {
                arrayList.add(publicKeyView.getText().toString());
                adapter.notifyDataSetChanged();
            }
        }

        private boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            return true;
        }

        private boolean isPasswordValid(String password) {
            //TODO: Replace this with your own logic
            return true;
        }
    }

    public static class SecondFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public SecondFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
    public static class ThirdFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public ThirdFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return FirstFragment.newInstance(publicKey);
                case 1:
                    return SecondFragment.newInstance(6);
                case 2:
                    return ThirdFragment.newInstance(7);
            }
            return PlaceholderFragment.newInstance(0);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
