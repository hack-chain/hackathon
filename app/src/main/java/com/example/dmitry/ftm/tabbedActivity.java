package com.example.dmitry.ftm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

        private AlertDialog dialog;

        SharedPreferences preferences;
        public static final String MY_SHARED_PREFERENCES = "MySharedPrefs";

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

            getdata task = new getdata();
            task._view = moneyView;
            task.execute(getArguments().getString(PUBLIC_KEY));

            ListView listview =(ListView)rootView.findViewById(R.id.list);
            arrayList = new ArrayList<String> ();
            arrayList.add(getArguments().getString(PUBLIC_KEY));

            adapter = new MySimpleArrayAdapter(getActivity(), arrayList);

            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    publicKey.setText(selectedItem);

                    Bitmap identicon = Identicon.create(selectedItem);
                    imageView.setImageBitmap(identicon);

                    getdata task = new getdata();
                    task._view = moneyView;
                    task.execute(selectedItem);
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

        public class MySimpleArrayAdapter extends ArrayAdapter<String> {
            private final Context context;
            private final ArrayList<String> values;

            public MySimpleArrayAdapter(Context context, ArrayList<String> values) {
                super(context, R.layout.rowlayout, values);
                this.context = context;
                this.values = values;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

                TextView publicKeyView = (TextView) rowView.findViewById(R.id.publicKey);
                TextView money = (TextView) rowView.findViewById(R.id.money);
                publicKeyView.setText(values.get(position));

                // Change the icon for Windows and iPhone
                String s = values.get(position);

                publicKeyView.setTypeface(publicKeyView.getTypeface(), Typeface.BOLD);

                getdata task = new getdata();
                task._view = money;
                task.execute(s);

                return rowView;
            }
        }

        protected void showInputDialog() {

            // get prompts.xml view
            layoutInflater = LayoutInflater.from(getActivity());
            promptView = layoutInflater.inflate(R.layout.input_dialog, null);

            dialog = new AlertDialog.Builder(getActivity()).setView(promptView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();


            publicKeyView = (AutoCompleteTextView) promptView.findViewById(R.id.publicKeyDialog);
            privateKeyView = (EditText) promptView.findViewById(R.id.privateKeyDialog);

            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }

        public class getdata extends AsyncTask<String, String, String> {

            HttpURLConnection urlConnection;
            TextView _view;
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

                    _view.setText("Balance: "+ balance);
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
                publicKeyView.setError(Html.fromHtml("<font color='#ffffff'>This field is required</font>"));
                focusView = publicKeyView;
                cancel = true;
            } else if (!isEmailValid(publicKey)) {
                publicKeyView.setError(Html.fromHtml("<font color='#ffffff'>Invalid public ke</font>"));
                focusView = publicKeyView;
                cancel = true;
            }

            if (TextUtils.isEmpty(privateKey)){
                privateKeyView.setError(Html.fromHtml("<font color='#ffffff'>This field is required</font>"));
                focusView = privateKeyView;
                cancel = true;
            } else if(!isPasswordValid(privateKey)) {
                privateKeyView.setError(Html.fromHtml("<font color='#ffffff'>Invalid private key</font>"));
                focusView = privateKeyView;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                arrayList.add(publicKeyView.getText().toString());
                adapter.notifyDataSetChanged();
                dialog.cancel();
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

    public static class SecondFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static String POPUP_CONSTANT = "mPopup";
        private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
        String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> adapter;
        ListView listView;
        FloatingActionButton fabView;

        private LayoutInflater layoutInflater;
        private View promptView;
        private AutoCompleteTextView publicKeyView;
        private EditText privateKeyView;

        private AlertDialog dialog;

        private ArrayList<String> arrayList;

        public SecondFragment() {
        }

        public static SecondFragment newInstance(int sectionNumber) {
            SecondFragment fragment = new SecondFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_second, container, false);

            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, days);
            listView = (ListView) rootView.findViewById(R.id.lvDays);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    listView.setItemChecked(position, true);
                    showPopup(view);
                }
            });

            fabView = (FloatingActionButton) rootView.findViewById(R.id.fab);

            fabView.setOnClickListener(new View.OnClickListener() {
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
            promptView = layoutInflater.inflate(R.layout.second_dialog, null);

            dialog = new AlertDialog.Builder(getActivity()).setView(promptView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();


            publicKeyView = (AutoCompleteTextView) promptView.findViewById(R.id.publicKeyDialog);

            Spinner dropdown = (Spinner) promptView.findViewById(R.id.spinner1);

            Bundle bundle=getArguments();
            arrayList = bundle.getStringArrayList("LIST");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrayList);
            dropdown.setAdapter(adapter);

            privateKeyView = (EditText) promptView.findViewById(R.id.privateKeyDialog);

            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }

        public void showPopup(View view) {
            PopupMenu popup = new PopupMenu(getActivity(), view);
            try {
                // Reflection apis to enforce show icon
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().equals(POPUP_CONSTANT)) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String selectedItem = adapter.getItem(listView.getCheckedItemPosition());
            switch (item.getItemId()) {
                case R.id.pmnuDelete:
                    Toast.makeText(this.getContext(), "You clicked delete on Item : " + selectedItem, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.pmnuEdit:
                    Toast.makeText(this.getContext(), "You clicked edit on Item : " + selectedItem, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.pmnuShare:
                    Toast.makeText(this.getContext(), "You clicked share on Item : " + selectedItem, Toast.LENGTH_SHORT).show();
                    break;
            }

            return false;
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
        public static ThirdFragment newInstance(int sectionNumber) {
            ThirdFragment fragment = new ThirdFragment();
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
                default:
                    return ThirdFragment.newInstance(7);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
