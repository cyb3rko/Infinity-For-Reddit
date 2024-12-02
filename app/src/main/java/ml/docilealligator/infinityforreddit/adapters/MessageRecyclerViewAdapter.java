package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPrivateMessagesActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemMessageBinding;
import ml.docilealligator.infinityforreddit.events.ChangeInboxCountEvent;
import ml.docilealligator.infinityforreddit.markdown.RedditHeadingPlugin;
import ml.docilealligator.infinityforreddit.markdown.SpoilerAwareMovementMethod;
import ml.docilealligator.infinityforreddit.markdown.SpoilerParserPlugin;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptPlugin;
import ml.docilealligator.infinityforreddit.message.FetchMessage;
import ml.docilealligator.infinityforreddit.message.Message;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import retrofit2.Retrofit;

public class MessageRecyclerViewAdapter extends PagedListAdapter<Message, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message message, @NonNull Message t1) {
            return message.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message message, @NonNull Message t1) {
            return message.getBody().equals(t1.getBody());
        }
    };
    private BaseActivity mActivity;
    private Retrofit mOauthRetrofit;
    private Markwon mMarkwon;
    private String mAccessToken;
    private int mMessageType;
    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;
    private int mColorAccent;
    private int mMessageBackgroundColor;
    private int mUsernameColor;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private int mUnreadMessageBackgroundColor;
    private int mColorPrimaryLightTheme;
    private int mButtonTextColor;
    private boolean markAllMessagesAsRead = false;

    public MessageRecyclerViewAdapter(BaseActivity activity, Retrofit oauthRetrofit,
                                      CustomThemeWrapper customThemeWrapper,
                                      String accessToken, String where,
                                      RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        mActivity = activity;
        mOauthRetrofit = oauthRetrofit;
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;

        mColorAccent = customThemeWrapper.getColorAccent();
        mMessageBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
        mUsernameColor = customThemeWrapper.getUsername();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        int spoilerBackgroundColor = mSecondaryTextColor | 0xFF000000;
        mUnreadMessageBackgroundColor = customThemeWrapper.getUnreadMessageBackgroundColor();
        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mButtonTextColor = customThemeWrapper.getButtonTextColor();

        // todo:https://github.com/Docile-Alligator/Infinity-For-Reddit/issues/1027
        //  add tables support and replace with MarkdownUtils#commonPostMarkwonBuilder
        mMarkwon = Markwon.builder(mActivity)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            intent.setData(uri);
                            mActivity.startActivity(intent);
                        });
                    }

                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(customThemeWrapper.getLinkColor());
                    }
                })
                .usePlugin(SuperscriptPlugin.create())
                .usePlugin(SpoilerParserPlugin.create(mSecondaryTextColor, spoilerBackgroundColor))
                .usePlugin(RedditHeadingPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(MovementMethodPlugin.create(new SpoilerAwareMovementMethod()))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
        mAccessToken = accessToken;
        if (where.equals(FetchMessage.WHERE_MESSAGES)) {
            mMessageType = FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE;
        } else {
            mMessageType = FetchMessage.MESSAGE_TYPE_INBOX;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            Message message = getItem(holder.getBindingAdapterPosition());
            if (message != null) {
                ArrayList<Message> replies = message.getReplies();
                Message displayedMessage;
                if (replies != null && !replies.isEmpty() && replies.get(replies.size() - 1) != null) {
                    displayedMessage = replies.get(replies.size() - 1);
                } else {
                    displayedMessage = message;
                }
                if (message.isNew()) {
                    if (markAllMessagesAsRead) {
                        message.setNew(false);
                    } else {
                        holder.itemView.setBackgroundColor(
                                mUnreadMessageBackgroundColor);
                    }
                }

                if (message.wasComment()) {
                    ((DataViewHolder) holder).binding.titleTextView.setText(message.getTitle());
                } else {
                    ((DataViewHolder) holder).binding.titleTextView.setVisibility(View.GONE);
                }

                ((DataViewHolder) holder).binding.authorTextView.setText(displayedMessage.getAuthor());
                String subject = displayedMessage.getSubject().substring(0, 1).toUpperCase() + displayedMessage.getSubject().substring(1);
                ((DataViewHolder) holder).binding.subjectTextView.setText(subject);
                mMarkwon.setMarkdown(((DataViewHolder) holder).binding.contentCustomMarkwonView, displayedMessage.getBody());

                holder.itemView.setOnClickListener(view -> {
                    if (mMessageType == FetchMessage.MESSAGE_TYPE_INBOX
                            && message.getContext() != null && !message.getContext().equals("")) {
                        Uri uri = Uri.parse(message.getContext());
                        Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                        intent.setData(uri);
                        mActivity.startActivity(intent);
                    } else if (mMessageType == FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE) {
                        Intent intent = new Intent(mActivity, ViewPrivateMessagesActivity.class);
                        intent.putExtra(ViewPrivateMessagesActivity.EXTRA_PRIVATE_MESSAGE_INDEX, holder.getBindingAdapterPosition());
                        intent.putExtra(ViewPrivateMessagesActivity.EXTRA_MESSAGE_POSITION, holder.getBindingAdapterPosition());
                        mActivity.startActivity(intent);
                    }

                    if (displayedMessage.isNew()) {
                        holder.itemView.setBackgroundColor(mMessageBackgroundColor);
                        message.setNew(false);

                        ReadMessage.readMessage(mOauthRetrofit, mAccessToken, message.getFullname(),
                                new ReadMessage.ReadMessageListener() {
                                    @Override
                                    public void readSuccess() {
                                        EventBus.getDefault().post(new ChangeInboxCountEvent(-1));
                                    }

                                    @Override
                                    public void readFailed() {
                                        message.setNew(true);
                                        holder.itemView.setBackgroundColor(mUnreadMessageBackgroundColor);
                                    }
                                });
                    }
                });

                ((DataViewHolder) holder).binding.authorTextView.setOnClickListener(view -> {
                    if (message.isAuthorDeleted()) {
                        return;
                    }
                    Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, message.getAuthor());
                    mActivity.startActivity(intent);
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Reached at the end
        if (hasExtraRow() && position == getItemCount() - 1) {
            if (networkState.getStatus() == NetworkState.Status.LOADING) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ERROR;
            }
        } else {
            return VIEW_TYPE_DATA;
        }
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof DataViewHolder) {
            ((DataViewHolder) holder).itemView.setBackgroundColor(mMessageBackgroundColor);
            ((DataViewHolder) holder).binding.titleTextView.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void updateMessageReply(Message newReply, int position) {
        if (position >= 0 && position < super.getItemCount()) {
            Message message = getItem(position);
            if (message != null) {
                notifyItemChanged(position);
            }
        }
    }

    public void setMarkAllMessagesAsRead(boolean markAllMessagesAsRead) {
        this.markAllMessagesAsRead = markAllMessagesAsRead;
    }

    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageBinding binding;

        DataViewHolder(View itemView) {
            super(itemView);
            binding = ItemMessageBinding.bind(itemView);
            if (mActivity.typeface != null) {
                binding.authorTextView.setTypeface(mActivity.typeface);
                binding.subjectTextView.setTypeface(mActivity.typeface);
                binding.titleTextView.setTypeface(mActivity.titleTypeface);
                binding.contentCustomMarkwonView.setTypeface(mActivity.contentTypeface);
            }
            itemView.setBackgroundColor(mMessageBackgroundColor);
            binding.authorTextView.setTextColor(mUsernameColor);
            binding.subjectTextView.setTextColor(mPrimaryTextColor);
            binding.titleTextView.setTextColor(mPrimaryTextColor);
            binding.contentCustomMarkwonView.setTextColor(mSecondaryTextColor);

            binding.contentCustomMarkwonView.setMovementMethod(LinkMovementMethod.getInstance());

            binding.contentCustomMarkwonView.setOnClickListener(view -> {
                if (binding.contentCustomMarkwonView.getSelectionStart() == -1 && binding.contentCustomMarkwonView.getSelectionEnd() == -1) {
                    itemView.performClick();
                }
            });
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(View itemView) {
            super(itemView);
            ItemFooterErrorBinding binding = ItemFooterErrorBinding.bind(itemView);
            if (mActivity.typeface != null) {
                binding.errorTextView.setTypeface(mActivity.typeface);
                binding.retryButton.setTypeface(mActivity.typeface);
            }
            binding.errorTextView.setText(R.string.load_comments_failed);
            binding.errorTextView.setTextColor(mSecondaryTextColor);
            binding.retryButton.setOnClickListener(view -> mRetryLoadingMoreCallback.retryLoadingMore());
            binding.retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            binding.retryButton.setTextColor(mButtonTextColor);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemFooterLoadingBinding binding = ItemFooterLoadingBinding.bind(itemView);
            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }
}
