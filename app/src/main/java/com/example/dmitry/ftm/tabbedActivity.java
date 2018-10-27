package com.example.dmitry.ftm;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

interface SendMessage {
    void sendData(String message);
}

interface SendFrom {
    void sendFrom(String from, String to, String money);
}

public class tabbedActivity extends AppCompatActivity implements SendMessage, SendFrom {
    private static final String INIT_PUBLIC_KEY = "PUBLIC_KEY";
    private String _initPublicKey;

    private static final String INIT_PRIVATE_KEY = "PRIVATE_KEY";
    private String _initPrivateKey;

    ViewPager _viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        SectionsPagerAdapter _SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        _viewPager = (ViewPager) findViewById(R.id.container);
        _viewPager.setAdapter(_SectionsPagerAdapter);

        _initPublicKey = (String) getIntent().getStringExtra(INIT_PUBLIC_KEY);
        _initPrivateKey = (String) getIntent().getStringExtra(INIT_PRIVATE_KEY);
    }

    @Override
    public void sendData(String message) {
        String tag = "android:switcher:" + R.id.container + ":" + 1;
        SecondFragment f = (SecondFragment) getSupportFragmentManager().findFragmentByTag(tag);
        f.displayReceivedData(message);
    }

    @Override
    public void sendFrom(String from, String to, String money) {
        String tag = "android:switcher:" + R.id.container + ":" + 2;
        ThirdFragment f = (ThirdFragment) getSupportFragmentManager().findFragmentByTag(tag);
        f.displayReceivedFrom(from, to, money);
    }

    public static class FirstFragment extends Fragment {
        private ArrayList<String> _arrayList;
        private ArrayAdapter<String> _adapter;

        SendMessage SM;

        public FirstFragment() {}

        public static FirstFragment newInstance(String publicKey) {
            FirstFragment firstFragment = new FirstFragment();
            Bundle args = new Bundle();
            args.putString(INIT_PUBLIC_KEY, publicKey);
            firstFragment.setArguments(args);
            return firstFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_first, container, false);

            _arrayList = new ArrayList<String> ();
            _arrayList.add(getArguments().getString(INIT_PUBLIC_KEY));

            _adapter = new CustomArrayAdapter(getActivity(), _arrayList);

            ListView listView = (ListView) rootView.findViewById(R.id.list);
            listView.setAdapter(_adapter);

            Button addButtonView = (Button) rootView.findViewById(R.id.addBtn);
            addButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInputDialog();
                }
            });

            return rootView;
        }

        public class CustomArrayAdapter extends ArrayAdapter<String> {
            private final Context _context;
            private final ArrayList<String> _values;

            CustomArrayAdapter(Context context, ArrayList<String> values) {
                super(context, R.layout.rowlayout, values);
                this._context = context;
                this._values = values;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

                TextView publicKeyView = (TextView) rowView.findViewById(R.id.publicKey);
                TextView moneyView = (TextView) rowView.findViewById(R.id.money);

                String publicKey = _values.get(position);

                publicKeyView.setText(publicKey);

                GetAndSetBalanceAsync task = new GetAndSetBalanceAsync();
                task._view = new WeakReference<TextView>(moneyView);
                task.execute(publicKey);

                ImageView image = (ImageView) rowView.findViewById(R.id.imageView);
                Bitmap identicon = Identicon.create(publicKey);
                image.setImageBitmap(identicon);

                return rowView;
            }
        }

        protected void showInputDialog() {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.input_dialog, null);

            final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(promptView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();


            final AutoCompleteTextView publicKeyView = (AutoCompleteTextView) promptView.findViewById(R.id.publicKeyDialog);
            final EditText privateKeyView = (EditText) promptView.findViewById(R.id.privateKeyDialog);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    publicKeyView.setError(null);
                    privateKeyView.setError(null);

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
                        _arrayList.add(publicKeyView.getText().toString());
                        SM.sendData(publicKeyView.getText().toString());
                        _adapter.notifyDataSetChanged();
                        dialog.cancel();
                    }
                }
            });
        }

        private boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            return true;
        }

        private boolean isPasswordValid(String password) {
            //TODO: Replace this with your own logic
            return true;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            try {
                SM = (SendMessage) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException("Error in retrieving data. Please try again");
            }
        }
    }

    public static class SecondFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
        private static final String POPUP_CONSTANT = "mPopup";
        private static final String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";

        ArrayList<Integer> _operations;
        CustomArrayAdapter _adapter;
        ListView _listView;

        Integer _operationNum;
        ArrayList<String> _froms;
        ArrayList<String> _tos;
        ArrayList<String> _mones;

        ArrayList<String> _publicKeyList;

        SendFrom SF;

        public SecondFragment() {
        }

        public static SecondFragment newInstance(String publicKey) {
            SecondFragment fragment = new SecondFragment();
            Bundle args = new Bundle();
            args.putString(INIT_PUBLIC_KEY, publicKey);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_second, container, false);

            _operations = new ArrayList<Integer>();

            _adapter = new CustomArrayAdapter(getActivity(), _operations);

            _listView = (ListView) rootView.findViewById(R.id.operations);
            _listView.setAdapter(_adapter);
            _listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    _listView.setItemChecked(position, true);
                    showPopup(view);
                }
            });

            _publicKeyList = new ArrayList<String>();
            _publicKeyList.add(getArguments().getString(INIT_PUBLIC_KEY));

            rootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInputDialog();
                }
            });

            _operationNum = 0;

            _froms = new ArrayList<String>();
            _tos = new ArrayList<String>();
            _mones = new ArrayList<String>();

            return rootView;
        }

        public class CustomArrayAdapter extends ArrayAdapter<Integer> {
            private final Context _context;
            private final ArrayList<Integer> _values;

            CustomArrayAdapter(Context context, ArrayList<Integer> values) {
                super(context, R.layout.rowlayout, values);
                this._context = context;
                this._values = values;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.row_layout_second, parent, false);

                TextView fromView = (TextView) rowView.findViewById(R.id.from);
                TextView toView = (TextView) rowView.findViewById(R.id.to);
                TextView moneyView = (TextView) rowView.findViewById(R.id.money);

                Integer num = _values.get(position);

                fromView.setText("From: " + _froms.get(num));
                toView.setText("To: "+ _tos.get(num));
                moneyView.setText("Amount: " + _mones.get(num) + " FTM");

                return rowView;
            }
        }

        protected void showInputDialog() {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.second_dialog, null);

            final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(promptView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();


            final Spinner dropdown = (Spinner) promptView.findViewById(R.id.spinner1);


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, _publicKeyList);
            dropdown.setAdapter(adapter);

            final AutoCompleteTextView publicKeyView = (AutoCompleteTextView) promptView.findViewById(R.id.publicKeyDialog);
            final EditText moneyView = (EditText) promptView.findViewById(R.id.moneyDialog);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _froms.add(getArguments().getString(INIT_PUBLIC_KEY));
                    _tos.add(publicKeyView.getText().toString());
                    _mones.add(moneyView.getText().toString());

                    _operations.add(_operationNum);
                    _operationNum += 1;
                    _adapter.notifyDataSetChanged();

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
            Integer selectedItem = _adapter.getItem(_listView.getCheckedItemPosition());
            switch (item.getItemId()) {
                case R.id.pmnuEdit:

                    SF.sendFrom(_froms.get(selectedItem), _tos.get(selectedItem), _mones.get(selectedItem));

                    break;
            }

            return false;
        }

        protected void displayReceivedData(String message) {
            _publicKeyList.add(message);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            try {
                SF = (SendFrom) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException("Error in retrieving data. Please try again");
            }
        }
    }

    public static class ThirdFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
        private static final String POPUP_CONSTANT = "mPopup";
        private static final String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";

        ArrayList<Integer> _operations;
        ThirdFragment.CustomArrayAdapter _adapter;
        ListView _listView;

        Integer _operationNum;
        ArrayList<String> _froms;
        ArrayList<String> _tos;
        ArrayList<String> _mones;

        ArrayList<String> _publicKeyList;

        public ThirdFragment() {
        }

        public static ThirdFragment newInstance(String publicKey) {
            ThirdFragment fragment = new ThirdFragment();
            Bundle args = new Bundle();
            args.putString(INIT_PUBLIC_KEY, publicKey);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_third, container, false);

            _operations = new ArrayList<Integer>();
            _adapter = new ThirdFragment.CustomArrayAdapter(getActivity(), _operations);

            _listView = (ListView) rootView.findViewById(R.id.operations);
            _listView.setAdapter(_adapter);
            _listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    _listView.setItemChecked(position, true);
                    showPopup(view);
                }
            });

            _publicKeyList = new ArrayList<String>();
            _publicKeyList.add(getArguments().getString(INIT_PUBLIC_KEY));

            _operationNum = 0;

            _froms = new ArrayList<String>();
            _tos = new ArrayList<String>();
            _mones = new ArrayList<String>();

            return rootView;
        }

        public class CustomArrayAdapter extends ArrayAdapter<Integer> {
            private final Context _context;
            private final ArrayList<Integer> _values;

            CustomArrayAdapter(Context context, ArrayList<Integer> values) {
                super(context, R.layout.rowlayout, values);
                this._context = context;
                this._values = values;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.row_layout_second, parent, false);

                TextView fromView = (TextView) rowView.findViewById(R.id.from);
                TextView toView = (TextView) rowView.findViewById(R.id.to);
                TextView moneyView = (TextView) rowView.findViewById(R.id.money);

                Integer num = _values.get(position);

                fromView.setText("From: " + _froms.get(num));
                toView.setText("To: "+ _tos.get(num));
                moneyView.setText("Amount: " + _mones.get(num) + " FTM");

                return rowView;
            }
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
            Integer selectedItem = _adapter.getItem(_listView.getCheckedItemPosition());
            switch (item.getItemId()) {
                case R.id.pmnuEdit:
                    Toast.makeText(this.getContext(), "You clicked edit on Item : " + selectedItem, Toast.LENGTH_SHORT).show();
                    break;
            }

            return false;
        }

        protected void displayReceivedFrom(String from, String to, String money) {
            _froms.add(from);
            _tos.add(to);
            _mones.add(money);

            _operations.add(_operationNum);
            _operationNum += 1;
            _adapter.notifyDataSetChanged();
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
                    return FirstFragment.newInstance(_initPublicKey);
                case 1:
                    return SecondFragment.newInstance(_initPublicKey);
                default:
                    return ThirdFragment.newInstance(_initPublicKey);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
