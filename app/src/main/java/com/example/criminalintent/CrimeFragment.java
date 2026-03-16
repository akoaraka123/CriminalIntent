package com.example.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_READ_CONTACTS = 2;

    private Crime mCrime;
    private EditText titleField;
    private Button mDateButton;
    private CheckBox solvedCheckBox;
    private Button saveButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private Button mReportButton;
    private TextView mStatusTextView;

    private boolean mPendingPickContact;
    private String mSuspectPhoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete_crime) {
            CrimeLab.get(getActivity()).deleteCrime(mCrime);
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        titleField = view.findViewById(R.id.crime_title);
        mDateButton = view.findViewById(R.id.crime_date);
        solvedCheckBox = view.findViewById(R.id.crime_solved);
        saveButton = view.findViewById(R.id.crime_save);
        mSuspectButton = view.findViewById(R.id.crime_suspect);
        mCallSuspectButton = view.findViewById(R.id.crime_call_suspect);
        mReportButton = view.findViewById(R.id.crime_report);
        mStatusTextView = view.findViewById(R.id.crime_status);

        titleField.setText(mCrime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        solvedCheckBox.setChecked(mCrime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mCrime.setSolved(isChecked);
            updateStatusText();
        });

        mDateButton.setEnabled(true);
        updateDate();
        updateStatusText();
        mDateButton.setOnClickListener(v -> {
            if (isTablet()) {
                FragmentManager manager = getParentFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            } else {
                Intent intent = new Intent(getActivity(), DatePickerActivity.class);
                intent.putExtra(DatePickerFragment.EXTRA_DATE, mCrime.getDate());
                startActivityForResult(intent, REQUEST_DATE);
            }
        });

        saveButton.setOnClickListener(v -> {
            CrimeLab.get(getActivity()).updateCrime(mCrime);
            requireActivity().finish();
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareCompat.IntentBuilder
                        .from(getActivity())
                        .setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .setChooserTitle(getString(R.string.send_report))
                        .startChooser();
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        final Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));

        PackageManager pm = requireActivity().getPackageManager();
        if (pm.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        } else {
            mSuspectButton.setEnabled(true);
        }

        if (pm.resolveActivity(dialIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mCallSuspectButton.setEnabled(false);
        } else {
            mCallSuspectButton.setEnabled(mSuspectPhoneNumber != null);
        }

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    mPendingPickContact = true;
                    requestPermissions(new String[] { Manifest.permission.READ_CONTACTS }, REQUEST_READ_CONTACTS);
                    return;
                }

                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mCallSuspectButton.setOnClickListener(v -> {
            if (mSuspectPhoneNumber == null) {
                return;
            }

            Uri number = Uri.parse("tel:" + Uri.encode(mSuspectPhoneNumber));
            Intent intent = new Intent(Intent.ACTION_DIAL, number);
            if (pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_CONTACTS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted && mPendingPickContact) {
                mPendingPickContact = false;
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                if (requireActivity().getPackageManager().resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            } else {
                mPendingPickContact = false;
            }
        }
    }

    private String getCrimeReport() {
        String solvedString = mCrime.isSolved()
                ? getString(R.string.crime_report_solved)
                : getString(R.string.crime_report_unsolved);

        String dateString = android.text.format.DateFormat
                .format("EEE, MMM dd", mCrime.getDate())
                .toString();

        String suspect = mCrime.getSuspect() == null
                ? getString(R.string.crime_report_no_suspect)
                : getString(R.string.crime_report_suspect, mCrime.getSuspect());

        return getString(R.string.crime_report,
                mCrime.getTitle(),
                dateString,
                solvedString,
                suspect);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    private boolean isTablet() {
        return getResources().getBoolean(R.bool.is_tablet);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };

            Cursor cursor = requireActivity()
                    .getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try {
                if (cursor == null || cursor.getCount() == 0) {
                    return;
                }

                cursor.moveToFirst();
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);

                String suspect = nameIndex >= 0 ? cursor.getString(nameIndex) : null;
                String contactId = idIndex >= 0 ? cursor.getString(idIndex) : null;

                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);

                mSuspectPhoneNumber = null;
                if (contactId != null) {
                    mSuspectPhoneNumber = getPhoneNumberForContact(contactId);
                }

                Intent dialCheck = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));
                if (requireActivity().getPackageManager().resolveActivity(dialCheck, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    mCallSuspectButton.setEnabled(mSuspectPhoneNumber != null);
                }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private String getPhoneNumberForContact(String contactId) {
        Cursor cursor = null;
        try {
            cursor = requireActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[] { contactId },
                    null
            );

            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (numberIndex < 0) {
                return null;
            }

            String number = cursor.getString(numberIndex);
            return (number == null || number.trim().isEmpty()) ? null : number.trim();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private void updateStatusText() {
        String statusText = mCrime.isSolved() ? "crime close" : "status open";
        mStatusTextView.setText(statusText);
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        CrimeFragment fragment = new CrimeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        fragment.setArguments(args);
        return fragment;
    }
}
