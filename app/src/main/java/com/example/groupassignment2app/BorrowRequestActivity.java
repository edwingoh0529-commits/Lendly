package com.example.groupassignment2app;//what am i doing?

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.BorrowRequest;
import com.google.firebase.database.core.Repo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class BorrowRequestActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "itemId";
    public static final String EXTRA_ITEM_NAME = "itemName";
    public static final String EXTRA_OWNER_ID = "ownerId";
    public static final String EXTRA_OWNER_NAME = "ownerName";
    public static final String EXTRA_FEE = "fee";
    public static final String EXTRA_TYPE = "type";
    
    public static final String EXTRA_PICKUP_NAME = "pickupName";
    public static final String EXTRA_PICKUP_X = "pickupX";
    public static final String EXTRA_PICKUP_Y = "pickupY";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final TimeZone MALAYSIA = TimeZone.getTimeZone("Asia/Kuala_Lumpur");

    
    private static final String[] PAYMENT_METHODS = {
            "Cash", "DuitNow", "Touch \u2018n Go eWallet", "Bank transfer"
    };

    private TextView txtItemName, txtCostSummary, txtMeetupLabel, txtMeetupHint;
    private EditText edtBorrowDate, edtReturnDate, edtMessage;
    private AutoCompleteTextView actvPayment;
    private Button btnSubmit, btnPickMeetup;

    private String meetupName;
    private Float meetupX, meetupY;

    
    private String ownerSuggestedName;

    private Calendar borrowCal, returnCal;
    private SimpleDateFormat dateFormat;

    private String itemId, itemName, ownerId, ownerName;
    private double feePerDay;

    private final Repo repo = Repo.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_request);

        
        InsetUtil.padTop(findViewById(R.id.headerBar));

        txtItemName = findViewById(R.id.txtBorrowItemName);
        txtCostSummary = findViewById(R.id.txtCostSummary);
        edtBorrowDate = findViewById(R.id.edtBorrowDate);
        edtReturnDate = findViewById(R.id.edtReturnDate);
        edtMessage = findViewById(R.id.edtBorrowMessage);
        btnSubmit = findViewById(R.id.btnSubmitRequest);
        txtMeetupLabel = findViewById(R.id.txtMeetupLabel);
        txtMeetupHint = findViewById(R.id.txtMeetupHint);
        btnPickMeetup = findViewById(R.id.btnPickMeetup);
        actvPayment = findViewById(R.id.actvPaymentMethod);

        
        actvPayment.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, PAYMENT_METHODS));
        actvPayment.setOnClickListener(v -> actvPayment.showDropDown());

        findViewById(R.id.btnBorrowBack).setOnClickListener(v -> finish());

        itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        itemName = getIntent().getStringExtra(EXTRA_ITEM_NAME);
        ownerId = getIntent().getStringExtra(EXTRA_OWNER_ID);
        ownerName = getIntent().getStringExtra(EXTRA_OWNER_NAME);
        feePerDay = getIntent().getDoubleExtra(EXTRA_FEE, 0);

        txtItemName.setText(itemName);

        
        meetupName = getIntent().getStringExtra(EXTRA_PICKUP_NAME);
        ownerSuggestedName = meetupName;
        if (getIntent().hasExtra(EXTRA_PICKUP_X)) {
            meetupX = getIntent().getFloatExtra(EXTRA_PICKUP_X, 0f);
            meetupY = getIntent().getFloatExtra(EXTRA_PICKUP_Y, 0f);
        }
        updateMeetupLabel();

        btnPickMeetup.setOnClickListener(v ->
                meetupPicker.launch(new Intent(this, MapActivity.class)));

        dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        dateFormat.setTimeZone(MALAYSIA);

        borrowCal = Calendar.getInstance(MALAYSIA);
        returnCal = Calendar.getInstance(MALAYSIA);
        returnCal.add(Calendar.DAY_OF_MONTH, 3);

        edtBorrowDate.setText(dateFormat.format(borrowCal.getTime()));
        edtReturnDate.setText(dateFormat.format(returnCal.getTime()));
        updateCost();

        edtBorrowDate.setOnClickListener(v -> pickDate(true));
        edtReturnDate.setOnClickListener(v -> pickDate(false));
        btnSubmit.setOnClickListener(v -> submit());
    }

    
    private final ActivityResultLauncher<Intent> meetupPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Intent data = result.getData();
                meetupX = data.getFloatExtra(MapActivity.EXTRA_MAP_X, 0f);
                meetupY = data.getFloatExtra(MapActivity.EXTRA_MAP_Y, 0f);
                meetupName = data.getStringExtra(MapActivity.EXTRA_LOCATION_NAME);
                updateMeetupLabel();
            });

    
    private void updateMeetupLabel() {
        if (meetupX == null) {
            txtMeetupLabel.setText("No meeting point chosen yet");
            txtMeetupHint.setText("Pick a spot so the owner knows where to find you.");
            btnPickMeetup.setText("Choose meeting point on campus map");
            return;
        }

        txtMeetupLabel.setText(meetupName == null ? "Point chosen on campus map" : meetupName);

        boolean unchanged = ownerSuggestedName != null
                && ownerSuggestedName.equals(meetupName);

        txtMeetupHint.setText(unchanged
                ? "The owner suggested this spot. Change it if it does not suit you."
                : "Your suggestion. The owner agrees to it by accepting your request.");

        btnPickMeetup.setText("Change meeting point");
    }

    private void pickDate(boolean isBorrowDate) {
        Calendar target = isBorrowDate ? borrowCal : returnCal;

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    target.set(Calendar.YEAR, year);
                    target.set(Calendar.MONTH, month);
                    target.set(Calendar.DAY_OF_MONTH, day);

                    if (isBorrowDate) {
                        edtBorrowDate.setText(dateFormat.format(borrowCal.getTime()));
                        
                        if (returnCal.before(borrowCal)) {
                            returnCal.setTime(borrowCal.getTime());
                            edtReturnDate.setText(dateFormat.format(returnCal.getTime()));
                        }
                    } else {
                        edtReturnDate.setText(dateFormat.format(returnCal.getTime()));
                    }
                    updateCost();
                },
                target.get(Calendar.YEAR),
                target.get(Calendar.MONTH),
                target.get(Calendar.DAY_OF_MONTH));

        
        Calendar min = isBorrowDate ? Calendar.getInstance(MALAYSIA) : borrowCal;
        dialog.getDatePicker().setMinDate(min.getTimeInMillis() - 1000);
        dialog.show();
    }

    
    private void updateCost() {
        long millis = returnCal.getTimeInMillis() - borrowCal.getTimeInMillis();
        long days = Math.max(1, TimeUnit.MILLISECONDS.toDays(millis));
        double total = days * feePerDay;

        txtCostSummary.setText(String.format(Locale.getDefault(),
                "%d day%s  \u00D7  RM %.2f/day  =  RM %.2f",
                days, days == 1 ? "" : "s", feePerDay, total));
    }

    private void submit() {
        if (!repo.isLoggedIn()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ownerId == null || ownerId.equals(repo.uid())) {
            Toast.makeText(this, "You cannot borrow your own item", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Checking\u2026");

        
        repo.checkAvailable(itemId, new Repo.Result<Boolean>() {
            @Override public void onSuccess(Boolean available) {
                if (!available) {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Send request");
                    new androidx.appcompat.app.AlertDialog.Builder(BorrowRequestActivity.this)
                            .setTitle("No longer available")
                            .setMessage("Somebody else got there first \u2014 this item is now "
                                    + "on loan or sold.\n\nTry searching for a similar item.")
                            .setPositiveButton("OK", (d, w) -> finish())
                            .show();
                    return;
                }
                checkNotDuplicate();
            }
            @Override public void onError(Exception e) {
                
                sendRequest();
            }
        });
    }

    
    private void checkNotDuplicate() {
        repo.hasOpenRequestFor(itemId, already -> {
            if (already) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Send request");
                new androidx.appcompat.app.AlertDialog.Builder(BorrowRequestActivity.this)
                        .setTitle("Already requested")
                        .setMessage("You have an open request for this item.\n\n"
                                + "Check the Lends tab for the owner's reply.")
                        .setPositiveButton("OK", (d, w) -> finish())
                        .show();
                return;
            }
            sendRequest();
        });
    }

    private void sendRequest() {
        BorrowRequest request = new BorrowRequest();
        request.setItemId(itemId);
        request.setItemName(itemName);
        request.setBorrowerId(repo.uid());
        request.setBorrowerName(repo.currentName());
        request.setLenderId(ownerId);
        request.setLenderName(ownerName);
        request.setStatus(BorrowRequest.PENDING);
        request.setType("BORROW");
        request.setRequestDate(edtBorrowDate.getText().toString());
        request.setReturnDate(edtReturnDate.getText().toString());
        request.setMessage(edtMessage.getText().toString().trim());
        request.setPaymentMethod(actvPayment.getText().toString());
        request.setMeetupLocationName(meetupName);
        if (meetupX != null) {
            request.setMeetupMapX(meetupX.doubleValue());
            request.setMeetupMapY(meetupY.doubleValue());
        }

        btnSubmit.setText("Sending\u2026");

        repo.createRequest(request)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Request sent. You will see the reply in the Lends tab.",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Send request");
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}