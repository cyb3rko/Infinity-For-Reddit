package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentRandomBottomSheetBinding;

public class RandomBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final int RANDOM_SUBREDDIT = 0;
    public static final int RANDOM_POST = 1;
    public static final int RANDOM_NSFW_SUBREDDIT = 2;
    public static final int RANDOM_NSFW_POST = 3;

    private RandomOptionSelectionCallback activity;

    public RandomBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentRandomBottomSheetBinding binding =
                FragmentRandomBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        boolean isNSFW = getArguments().getBoolean(EXTRA_IS_NSFW, false);

        if (!isNSFW) {
            binding.randomNsfwSubredditTextView.setVisibility(View.GONE);
            binding.randomNsfwPostTextView.setVisibility(View.GONE);
        } else {
            binding.randomNsfwSubredditTextView.setOnClickListener(view -> {
                activity.randomOptionSelected(RANDOM_NSFW_SUBREDDIT);
                dismiss();
            });

            binding.randomNsfwPostTextView.setOnClickListener(view -> {
                activity.randomOptionSelected(RANDOM_NSFW_POST);
                dismiss();
            });
        }

        binding.randomSubredditTextView.setOnClickListener(view -> {
            activity.randomOptionSelected(RANDOM_SUBREDDIT);
            dismiss();
        });

        binding.randomPostTextView.setOnClickListener(view -> {
            activity.randomOptionSelected(RANDOM_POST);
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (RandomOptionSelectionCallback) context;
    }

    public interface RandomOptionSelectionCallback {
        void randomOptionSelected(int option);
    }
}