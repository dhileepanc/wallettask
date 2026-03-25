package com.app.wallettask.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.wallettask.R;
import com.app.wallettask.data.Transaction;
import com.app.wallettask.databinding.ItemTransactionBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions;
;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        

        holder.binding.transactionType.setText(transaction.getType());
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM. dd", java.util.Locale.getDefault());
        holder.binding.transactionDate.setText(sdf.format(new java.util.Date(transaction.getTimestamp())));

        if ("Transfer".equals(transaction.getMethod())) {
            // Transfer transactions: description already contains "Sent to <name>" or "Received from <name>"
            if ("Income".equals(transaction.getType())) {
                holder.binding.transactionIcon.setImageResource(R.drawable.ic_income);
                holder.binding.transactionIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50"));
                holder.binding.transactionDescription.setText(transaction.getDescription() + " (" + transaction.getAmount() + " $)");
            } else {
                holder.binding.transactionIcon.setImageResource(R.drawable.ic_expend);
                holder.binding.transactionIcon.setColorFilter(android.graphics.Color.parseColor("#F44336"));
                holder.binding.transactionDescription.setText(transaction.getDescription() + " (" + transaction.getAmount() + " $)");
            }
        } else if ("Income".equals(transaction.getType())) {
            holder.binding.transactionIcon.setImageResource(R.drawable.ic_income);
            holder.binding.transactionIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50"));

            holder.binding.transactionDescription.setText("Received "+transaction.getAmount()+" $"+" in "+transaction.getDescription());
        } else {
            holder.binding.transactionIcon.setImageResource(R.drawable.ic_expend);
            holder.binding.transactionIcon.setColorFilter(android.graphics.Color.parseColor("#F44336"));
            holder.binding.transactionDescription.setText("Spending "+transaction.getAmount()+" $"+" on "+transaction.getDescription());

        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTransactionBinding binding;
        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
