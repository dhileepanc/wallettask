package com.app.wallettask.ui.wallet;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.wallettask.databinding.ItemWalletMethodBinding;
import java.util.List;

public class WalletMethodAdapter extends RecyclerView.Adapter<WalletMethodAdapter.ViewHolder> {
    private List<WalletMethod> methods;

    public WalletMethodAdapter(List<WalletMethod> methods) {
        this.methods = methods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemWalletMethodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WalletMethod method = methods.get(position);
        holder.binding.methodName.setText(method.name);
        holder.binding.methodDesc.setText(method.description);
        holder.binding.methodValue.setText(method.value);
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemWalletMethodBinding binding;
        ViewHolder(ItemWalletMethodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class WalletMethod {
        String name;
        String description;
        String value;
        public WalletMethod(String name, String description, String value) {
            this.name = name;
            this.description = description;
            this.value = value;
        }
    }
}
