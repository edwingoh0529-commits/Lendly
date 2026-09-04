package com.example.groupassignment2app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.model.BorrowRequest;

import java.util.ArrayList;
import java.util.List;


public class BorrowRequestAdapter extends RecyclerView.Adapter<BorrowRequestAdapter.Holder> {

    public interface Listener {
        void onAccept(BorrowRequest request);
        void onReject(BorrowRequest request);
        void onMarkReturned(BorrowRequest request);
        void onRate(BorrowRequest request);
        void onMessage(BorrowRequest request);
        void onViewMeetup(BorrowRequest request);
        void onMarkPaid(BorrowRequest request);
    }

    
    public static final int AS_OWNER = 0;
    public static final int AS_BORROWER = 1;

    private List<BorrowRequest> requests = new ArrayList<>();
    private final Listener listener;
    private final int viewerRole;

    public BorrowRequestAdapter(int viewerRole, Listener listener) {
        this.viewerRole = viewerRole;
        this.listener = listener;
    }

    public void setRequests(List<BorrowRequest> requests) {
        this.requests = requests == null ? new ArrayList<>() : requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrow_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        BorrowRequest r = requests.get(position);
        boolean owner = viewerRole == AS_OWNER;

        h.itemName.setText(r.getItemName());
        h.person.setText(owner
                ? "From: " + safe(r.getBorrowerName())
                : "Owner: " + safe(r.getLenderName()));

        h.status.setText(r.getStatus());
        h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), colorFor(r.getStatus())));

        
        if ("BUY".equals(r.getType())) {
            h.dates.setText("\uD83D\uDCB0  Purchase \u2014 no return needed");
        } else {
            h.dates.setText("\uD83D\uDD01  Borrow  \u00B7  "
                    + r.getRequestDate() + "  \u2192  " + r.getReturnDate());
        }

        
        if (r.hasMeetupPoint()) {
            h.meetup.setVisibility(View.VISIBLE);
            String where = r.getMeetupLocationName();
            if (where == null || where.isEmpty()) where = "the chosen point";

            boolean settled = !BorrowRequest.PENDING.equals(r.getStatus());
            h.meetup.setText("\uD83D\uDCCD  "
                    + (settled ? "Meeting at " : "Proposed: ")
                    + where + "  \u2013  view map");
            h.meetup.setOnClickListener(v -> listener.onViewMeetup(r));
        } else {
            h.meetup.setVisibility(View.GONE);
        }

       
        h.payment.setText(r.getPaymentReceived()
                ? "\u2713  Paid by " + r.getPaymentMethod()
                : "\uD83D\uDCB3  Paying by " + r.getPaymentMethod() + " on handover");
        h.payment.setTextColor(ContextCompat.getColor(h.itemView.getContext(),
                r.getPaymentReceived() ? R.color.status_accepted : R.color.grey_text));

        if (r.getMessage() != null && !r.getMessage().isEmpty()) {
            h.message.setVisibility(View.VISIBLE);
            h.message.setText("\u201C" + r.getMessage() + "\u201D");
        } else {
            h.message.setVisibility(View.GONE);
        }

        
        h.accept.setVisibility(View.GONE);
        h.reject.setVisibility(View.GONE);
        h.markReturned.setVisibility(View.GONE);
        h.markPaid.setVisibility(View.GONE);
        h.rate.setVisibility(View.GONE);
        h.message2.setVisibility(View.GONE);

        String status = r.getStatus();
        boolean buying = "BUY".equals(r.getType());

        
        h.message2.setVisibility(View.VISIBLE);
        h.message2.setText(owner
                ? (buying ? "Message buyer" : "Message borrower")
                : (buying ? "Message seller" : "Message owner"));

        if (BorrowRequest.PENDING.equals(status)) {
            if (owner) {
                
                h.accept.setVisibility(View.VISIBLE);
                h.reject.setVisibility(View.VISIBLE);
                h.accept.setText(buying ? "Confirm sale" : "Accept");
            }
        } else if (BorrowRequest.ACCEPTED.equals(status) || BorrowRequest.OVERDUE.equals(status)) {
            if (owner) {
                h.markReturned.setVisibility(View.VISIBLE);
                
                if (!r.getPaymentReceived()) h.markPaid.setVisibility(View.VISIBLE);
            }
        } else if (BorrowRequest.RETURNED.equals(status) || BorrowRequest.PURCHASED.equals(status)) {
            if (owner && !r.getPaymentReceived()) {
                h.markPaid.setVisibility(View.VISIBLE);
            }
            boolean alreadyRated = owner ? r.getLenderRated() : r.getBorrowerRated();
            if (!alreadyRated) {
                h.rate.setVisibility(View.VISIBLE);
                h.rate.setText(owner
                        ? (buying ? "Rate buyer" : "Rate borrower")
                        : (buying ? "Rate seller" : "Rate owner"));
            }
        } else if (BorrowRequest.REJECTED.equals(status)) {
            
            h.message2.setVisibility(View.GONE);
        }

       
        h.primaryRow.setVisibility(
                anyVisible(h.accept, h.reject, h.markReturned, h.markPaid)
                        ? View.VISIBLE : View.GONE);
        h.secondaryRow.setVisibility(
                anyVisible(h.rate, h.message2) ? View.VISIBLE : View.GONE);

        h.accept.setOnClickListener(v -> listener.onAccept(r));
        h.reject.setOnClickListener(v -> listener.onReject(r));
        h.markReturned.setOnClickListener(v -> listener.onMarkReturned(r));
        h.markPaid.setOnClickListener(v -> listener.onMarkPaid(r));
        h.rate.setOnClickListener(v -> listener.onRate(r));
        h.message2.setOnClickListener(v -> listener.onMessage(r));
    }

    
    private boolean anyVisible(View... views) {
        for (View v : views) {
            if (v.getVisibility() == View.VISIBLE) return true;
        }
        return false;
    }

    private int colorFor(String status) {
        if (BorrowRequest.PENDING.equals(status)) return R.color.status_pending;
        if (BorrowRequest.ACCEPTED.equals(status)) return R.color.status_accepted;
        if (BorrowRequest.OVERDUE.equals(status)) return R.color.status_overdue;
        if (BorrowRequest.REJECTED.equals(status)) return R.color.status_rejected;
        return R.color.status_done;
    }

    private String safe(String s) { return s == null || s.isEmpty() ? "a student" : s; }

    @Override
    public int getItemCount() { return requests.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView itemName, person, status, dates, message, meetup, payment;
        final Button accept, reject, markReturned, markPaid, rate, message2;
        final View primaryRow, secondaryRow;

        Holder(@NonNull View v) {
            super(v);
            itemName = v.findViewById(R.id.txtRequestItem);
            person = v.findViewById(R.id.txtRequestPerson);
            status = v.findViewById(R.id.txtRequestStatus);
            dates = v.findViewById(R.id.txtRequestDates);
            message = v.findViewById(R.id.txtRequestMessage);
            meetup = v.findViewById(R.id.txtRequestMeetup);
            payment = v.findViewById(R.id.txtRequestPayment);
            accept = v.findViewById(R.id.btnAccept);
            reject = v.findViewById(R.id.btnReject);
            markReturned = v.findViewById(R.id.btnMarkReturned);
            markPaid = v.findViewById(R.id.btnMarkPaid);
            rate = v.findViewById(R.id.btnRate);
            message2 = v.findViewById(R.id.btnMessage);
            primaryRow = v.findViewById(R.id.layoutPrimaryActions);
            secondaryRow = v.findViewById(R.id.layoutSecondaryActions);
        }
    }
}