package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowerPresenter implements UserService.GetUserObserver, FollowService.FollowerObserver {

    private User user;
    private AuthToken token;

    private FollowerView view;
    private FollowService service;

    private boolean isLoading;
    private boolean hasMorePages;
    private User lastFollower;

    public FollowerPresenter(User user, AuthToken token, FollowerView view) {
        this.user = user;
        this.token = token;
        this.view = view;
        isLoading = false;
        hasMorePages = true;
        lastFollower = null;
        service = new FollowService();
    }

    public interface FollowerView {
        void addItems(List<User> followers);
        void displayMessage(String message);
        void navigateToUser(User user);
        void setLoadingFooter(boolean loading);
    }

    @Override
    public void handleGetFollowerSuccess(List<User> followers, boolean morePages) {
        isLoading = false;
        view.setLoadingFooter(false);
        if (followers.size() > 0) {
            lastFollower = followers.get(followers.size()-1);
        }
        else {
            lastFollower = null;
        }
        view.addItems(followers);
    }

    @Override
    public void handleGetFollowerFailure(String message) {
        isLoading = false;
        view.setLoadingFooter(false);
        view.displayMessage("Failed to get followers: " + message);
    }

    @Override
    public void handleGetFollowerThrewException(Exception ex) {
        isLoading = false;
        view.setLoadingFooter(false);
        view.displayMessage("Failed to get followers because of exception: " + ex.getMessage());
    }

    @Override
    public void handleGetUserSuccess(User user) {
        view.displayMessage("Getting user's profile...");
        view.navigateToUser(user);
    }

    @Override
    public void handleGetUserFailed(String message) {
        view.displayMessage("Failed to get user's profile: " + message);
    }

    @Override
    public void handleGetUserThrewException(Exception ex) {
        view.displayMessage("Failed to get user's profile because of exception: " + ex.getMessage());
    }

    public User getUser() {
        return  user;
    }

    public AuthToken getToken() {
        return  token;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isHasMorePages() {
        return hasMorePages;
    }

    public void loadMoreItems() {
        if (!isLoading) {
            isLoading = true;
            view.setLoadingFooter(true);
            new FollowService().getFollower(token, user, 10, lastFollower, this);
        }
    }
    public void selectUser(String clickable) {
        new UserService().getUser(token, clickable, this);
    }
}