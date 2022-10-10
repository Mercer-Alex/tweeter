package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter {
    private static final int PAGE_SIZE = 10;

    private FollowingView followView;
    private FollowService service;

    private User lastFollowee;
    private boolean hasMorePages;
    private boolean isLoading = false;

    private AuthToken token;

    public interface FollowingView {
        void displayMessage(String msg);
        void setLoadingFooter(boolean loading);
        void addFollowees(List<User> followees);
    }

    public FollowingPresenter(FollowingView view, AuthToken token) {
        this.followView = view;
        service = new FollowService();
        hasMorePages = true;
        lastFollowee = null;
        this.token = token;
    }

    public boolean isMorePages() {
        return hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void loadMoreItems() {
        isLoading = true;
        followView.setLoadingFooter(true);
        service.loadMoreItems(Cache.getInstance().getCurrUserAuthToken(), Cache.getInstance().getCurrUser(), PAGE_SIZE, lastFollowee, new FollowingObserver());
    }

    private class GetUserObserver implements UserService.GetUserObserver {
        @Override
        public void handleSuccess(User user) {
            followView.displayMessage("Getting user's profile...");
        }

        @Override
        public void handleFailure(String message) {
            followView.displayMessage("Failed to get user's profile: " + message);
        }

        @Override
        public void handleException(Exception exception) {
            followView.displayMessage("Failed to get user's profile because of exception: " + exception.getMessage());
        }
    }

    private class FollowingObserver implements FollowService.FollowersObserver {
        @Override
        public void handleSuccess(List<User> followers, boolean hasMorePages) {
            isLoading = false;
            lastFollowee = (followers.size() > 0) ? followers.get(followers.size() - 1) : null;
            followView.addFollowees(followers);
            followView.setLoadingFooter(false);
            FollowingPresenter.this.hasMorePages = hasMorePages;
        }

        @Override
        public void handleException(Exception ex) {
            isLoading = false;
            followView.displayMessage("Failed to get following because of exception: " + ex.getMessage());
            followView.setLoadingFooter(false);
        }

        @Override
        public void handleFailure(String msg) {
            followView.displayMessage("Failed to get following: " + msg);
            isLoading = false;
            followView.setLoadingFooter(false);
        }
    }
}
