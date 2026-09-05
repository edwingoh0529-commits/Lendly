//awesome_sauce
package com.example.groupassignment2app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.Review;


public class RatingReviewActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "requestId";
    public static final String EXTRA_ITEM_NAME = "itemName";
    public static final String EXTRA_TARGET_ID = "targetId";
    public static final String EXTRA_TARGET_NAME = "targetName";
    public static final String EXTRA_I_AM_BORROWER = "iAmBorrower";

    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmit;
    private TextView txtHeading, txtHint;

    private String requestId, itemName, targetId, targetName;
    private boolean iAmBorrower;

    private final Repo repo = Repo.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_review);
        
        InsetUtil.padTop(findViewById(R.id.headerBar));

        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtReviewComment);
        btnSubmit = findViewById(R.id.btnSubmitReview);
        txtHeading = findViewById(R.id.txtRatingHeading);
        txtHint = findViewById(R.id.txtRatingHint);

        findViewById(R.id.btnRatingBack).setOnClickListener(v -> finish());

        requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        itemName = getIntent().getStringExtra(EXTRA_ITEM_NAME);
        targetId = getIntent().getStringExtra(EXTRA_TARGET_ID);
        targetName = getIntent().getStringExtra(EXTRA_TARGET_NAME);
        iAmBorrower = getIntent().getBooleanExtra(EXTRA_I_AM_BORROWER, true);

        txtHeading.setText("How was " + targetName + "?");
        txtHint.setText(iAmBorrower
                ? "Was the item as described, and was pickup smooth?"
                : "Was the item returned on time and in good condition?");

        ratingBar.setOnRatingBarChangeListener((bar, value, fromUser) ->
                btnSubmit.setEnabled(value > 0));
        btnSubmit.setEnabled(false);

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        float rating = ratingBar.getRating();
        if (rating <= 0) {
            Toast.makeText(this, "Please choose a star rating", Toast.LENGTH_SHORT).show();
            return;
        }
        if (targetId == null) {
            Toast.makeText(this, "Could not identify who to rate", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review(requestId, itemName, repo.uid(), repo.currentName(),
                targetId, rating, edtComment.getText().toString().trim());

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Saving...");

        repo.submitReview(review)
                .addOnSuccessListener(v ->
                        repo.markRated(requestId, iAmBorrower).addOnCompleteListener(t -> {
                            Toast.makeText(this, "Thanks for the review!", Toast.LENGTH_SHORT).show();
                            finish();
                        }))
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit review");
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
