package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemSelectedSubredditBinding;

public class SelectedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private CustomThemeWrapper customThemeWrapper;
    private ArrayList<String> subreddits;

    public SelectedSubredditsRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper, ArrayList<String> subreddits) {
        this.customThemeWrapper = customThemeWrapper;
        if (subreddits == null) {
            this.subreddits = new ArrayList<>();
        } else {
            this.subreddits = subreddits;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_subreddit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditViewHolder) {
            ((SubredditViewHolder) holder).binding.subredditName.setText(subreddits.get(holder.getBindingAdapterPosition()));
            ((SubredditViewHolder) holder).binding.deleteImageView.setOnClickListener(view -> {
                subreddits.remove(holder.getBindingAdapterPosition());
                notifyItemRemoved(holder.getBindingAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return subreddits.size();
    }

    public void addSubreddits(ArrayList<String> newSubreddits) {
        int oldSize = subreddits.size();
        subreddits.addAll(newSubreddits);
        notifyItemRangeInserted(oldSize, newSubreddits.size());
    }

    public void addUserInSubredditType(String username) {
        subreddits.add(username);
        notifyItemInserted(subreddits.size());
    }

    public ArrayList<String> getSubreddits() {
        return subreddits;
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {
        private final ItemSelectedSubredditBinding binding;

        public SubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSelectedSubredditBinding.bind(itemView);

            binding.subredditName.setTextColor(customThemeWrapper.getPrimaryIconColor());
            binding.deleteImageView.setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
