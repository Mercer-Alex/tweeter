package edu.byu.cs.tweeter.client.presenter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter {

    private User user;
    private AuthToken token;
    private MainView view;

    public MainPresenter(MainView view, User user, AuthToken token) {
        this.view = view;
        this.user = user;
        this.token = token;
    }

    public interface MainView {
        void displayMessage(String message);
        void logOutUser();
        void setFollowButtonVisibility(boolean isVisible);
        void setFollowButtonEnabled(boolean isEnabled);
        void updateFollowButton(boolean isFollower);
        void updateNumFollowing(int count);
        void updateNumFollowers(int count);
    }

    public void followUser(String followButton) {
        view.setFollowButtonEnabled(false);
        if (followButton.equals("Following")) {
            new FollowService().unfollowUser(token, user, new UnfollowUserObserver());
            view.displayMessage("Unfollowing " + user.getAlias() + "...");
        } else {
            new FollowService().followUser(token, user, new FollowUserObserver());
            view.displayMessage("Following " + user.getAlias() + "...");
        }
    }

    public void updateSelectedUserFollowingAndFollowers() {
        new FollowService().updateSelectedUserFollowingAndFollowers(user, token, new FollowersCountObserver(), new FollowingCountObserver());
    }

    public void checkIfFollower() {
        if (user.compareTo(Cache.getInstance().getCurrUser()) == 0) {
            view.setFollowButtonVisibility(false);
        }
        else {
            view.setFollowButtonVisibility(true);
            new FollowService().isFollower(token, user, Cache.getInstance().getCurrUser(), new IsFollowingObserver());
        }
    }

    public void statusPosted(String post) throws ParseException {
        view.displayMessage("Posting Status...");
        new StatusService().postStatus(post, getFormattedDateTime(), parseURLs(post), parseMentions(post), new StatusObserver());
    }

    private class StatusObserver implements StatusService.StatusObserver {
        @Override
        public void handleSuccess() {
            view.displayMessage("Successfully Posted!");
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to post status: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to post status because of exception: " + ex.getMessage());
        }
    }

    private class FollowUserObserver implements FollowService.FollowUserObserver {
        @Override
        public void handleSuccess() {
            new FollowService().updateSelectedUserFollowingAndFollowers(user, token, new FollowersCountObserver(), new FollowingCountObserver());
            view.updateFollowButton(false);
            view.setFollowButtonEnabled(true);
        }

        @Override
        public void handleFailure(String msg) {
            view.displayMessage("Failed to follow: " + msg);
            view.setFollowButtonEnabled(true);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to follow because of exception: " + ex.getMessage());
            view.setFollowButtonEnabled(true);
        }
    }

    private class UnfollowUserObserver implements FollowService.FollowUserObserver {

        @Override
        public void handleSuccess() {
            new FollowService().updateSelectedUserFollowingAndFollowers(user, token, new FollowersCountObserver(), new FollowingCountObserver());
            view.updateFollowButton(false);
            view.setFollowButtonEnabled(true);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to unfollow: " + message);
            view.setFollowButtonEnabled(true);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayMessage("Failed to unfollow because of exception: " + exception.getMessage());
            view.setFollowButtonEnabled(true);
        }
    }

    private class LogoutUserObserver implements UserService.LogoutUserObserver {
        @Override
        public void handleSuccess() {
            view.logOutUser();
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to logout: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to logout because of exception: " + ex.getMessage());
        }
    }

    private class FollowingCountObserver implements FollowService.FollowCounterObserver {

        @Override
        public void handleSuccess(int count) {
            view.updateNumFollowing(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get number of followees: " + message);

        }

        @Override
        public void handleException(Exception exception) {
            view.displayMessage("Failed to get number of followees because of exception: " + exception.getMessage());

        }
    }

    private class FollowersCountObserver implements FollowService.FollowCounterObserver {

        @Override
        public void handleSuccess(int count) {
            view.updateNumFollowers(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get number of followers: " + message);

        }

        @Override
        public void handleException(Exception exception) {
            view.displayMessage("Failed to get number of followers because of exception: " + exception.getMessage());

        }
    }

    private class IsFollowingObserver implements FollowService.IsFollowingObserver {

        @Override
        public void handleSuccess(boolean follower) {
            view.updateFollowButton(follower);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to find if follower: " + message);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayMessage("Failed to find if follower because of exception: " + exception.getMessage());
        }
    }

    public int findUrlEndIndex(String word) {
        if (word.contains(".com")) {
            int index = word.indexOf(".com");
            index += 4;
            return index;
        } else if (word.contains(".org")) {
            int index = word.indexOf(".org");
            index += 4;
            return index;
        } else if (word.contains(".edu")) {
            int index = word.indexOf(".edu");
            index += 4;
            return index;
        } else if (word.contains(".net")) {
            int index = word.indexOf(".net");
            index += 4;
            return index;
        } else if (word.contains(".mil")) {
            int index = word.indexOf(".mil");
            index += 4;
            return index;
        } else {
            return word.length();
        }
    }

    public List<String> parseURLs(String post) {
        List<String> containedUrls = new ArrayList<>();
        for (String word : post.split("\\s")) {
            if (word.startsWith("http://") || word.startsWith("https://")) {

                int index = findUrlEndIndex(word);

                word = word.substring(0, index);

                containedUrls.add(word);
            }
        }
        return containedUrls;
    }

    public List<String> parseMentions(String post) {
        List<String> containedMentions = new ArrayList<>();

        for (String word : post.split("\\s")) {
            if (word.startsWith("@")) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                word = "@".concat(word);

                containedMentions.add(word);
            }
        }

        return containedMentions;
    }

    public String getFormattedDateTime() throws ParseException {
        SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat statusFormat = new SimpleDateFormat("MMM d yyyy h:mm aaa");

        return statusFormat.format(userFormat.parse(LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8)));
    }

    public void logOutUser() {
        view.displayMessage("Logging Out...");
        new UserService().logout(token, new LogoutUserObserver());
    }
}
