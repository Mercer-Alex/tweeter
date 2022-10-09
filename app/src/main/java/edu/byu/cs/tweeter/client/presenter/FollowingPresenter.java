package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter implements UserService.GetUserObserver, FollowService.GetFollowingObserver {
    private static final int PAGE_SIZE = 10;

    private FollowingView followView;
    private FollowService service;

    private User lastFollowee;
    private boolean hasMorePages;
    private boolean isLoading = false;

    public interface FollowingView {
        void displaymessage(String msg);
        void setLoadingFooter(boolean loading);
        void addFollowees(List<User> followees);
    }

    public FollowingPresenter(FollowingView view) {
        this.followView = view;
        service = new FollowService();
        hasMorePages = true;
        lastFollowee = null;
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
        service.loadMoreItems(Cache.getInstance().getCurrUserAuthToken(), Cache.getInstance().getCurrUser(), PAGE_SIZE, lastFollowee, this);
    }

    @Override
    public void addFollowers(List<User> followers, boolean hasMorePages) {
        isLoading = false;
        lastFollowee = (followers.size() > 0) ? followers.get(followers.size() - 1) : null;
        followView.addFollowees(followers);
        followView.setLoadingFooter(false);
        FollowingPresenter.this.hasMorePages = hasMorePages;
    }

    @Override
    public void displayExceptionMessage(Exception ex) {
        isLoading = false;
        followView.displaymessage("Failed to get following because of exception: " + ex.getMessage());
        followView.setLoadingFooter(false);
    }

    @Override
    public void displayErrorMessage(String msg) {
        followView.displaymessage("Failed to get following: " + msg);
        isLoading = false;
        followView.setLoadingFooter(false);
    }

    @Override
    public void handleGetUserSuccess(User user) {

    }

    @Override
    public void handleGetUserFailed(String message) {

    }

    @Override
    public void handleGetUserThrewException(Exception e) {

    }
}
