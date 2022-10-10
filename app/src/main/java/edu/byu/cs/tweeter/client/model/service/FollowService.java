package edu.byu.cs.tweeter.client.model.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.IsFollowerTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.UnfollowTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {

    public interface FollowersObserver extends ServiceObserver {
        void handleSuccess(List<User> followers, boolean morePages);
    }

    private class FollowersHandler extends BackgroundTaskHandler<FollowersObserver> {
        public FollowersHandler(FollowersObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(FollowersObserver observer, Bundle data) {
            List<User> followers = (List<User>) data.getSerializable(GetFollowingTask.ITEMS_KEY);
            boolean hasMorePages = data.getBoolean(GetFollowingTask.MORE_PAGES_KEY);

            observer.handleSuccess(followers, hasMorePages);
        }
    }

    public interface FollowCounterObserver extends ServiceObserver {
        void handleSuccess(int count);
    }

    private class FollowCountHandler extends BackgroundTaskHandler<FollowCounterObserver> {
        public FollowCountHandler(FollowCounterObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(FollowCounterObserver observer, Bundle data) {
            int count = data.getInt(GetFollowersCountTask.COUNT_KEY);
            observer.handleSuccess(count);
        }
    }

    public interface FollowUserObserver extends ServiceObserver {
        void handleSuccess();
    }

    private class FollowUserHandler extends BackgroundTaskHandler<FollowUserObserver> {
        public FollowUserHandler(FollowUserObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(FollowUserObserver observer, Bundle data) {
            observer.handleSuccess();
        }
    }

    public interface IsFollowingObserver extends ServiceObserver {
        void handleSuccess(boolean follower);
    }

    private class IsFollowingHandler extends BackgroundTaskHandler<IsFollowingObserver> {
        public IsFollowingHandler(IsFollowingObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(IsFollowingObserver observer, Bundle data) {
            boolean isFollower = data.getBoolean(IsFollowerTask.IS_FOLLOWER_KEY);
            observer.handleSuccess(isFollower);
        }
    }

    public void loadMoreItems(AuthToken currUserAuthToken, User user, int pageSize, User lastFollowee,
                              FollowersObserver getFollowingObserver) {
        GetFollowingTask getFollowingTask = new GetFollowingTask(currUserAuthToken,
                user, pageSize, lastFollowee, new FollowersHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getFollowingTask);
    }

    public void getFollower(AuthToken token, User user, int limit, User lastFollower, FollowersObserver observer) {
        GetFollowersTask getFollowersTask = new GetFollowersTask(token, user, limit, lastFollower, new FollowersHandler(observer));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(getFollowersTask);
    }

    public void followUser(AuthToken authToken, User user, FollowUserObserver followUserObserver) {
        FollowTask followTask = new FollowTask(authToken, user, new FollowUserHandler(followUserObserver));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(followTask);
    }


    public void unfollowUser(AuthToken authToken, User user, FollowUserObserver observer) {
        UnfollowTask unfollow = new UnfollowTask(authToken, user, new FollowUserHandler(observer));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(unfollow);
    }

    public void updateSelectedUserFollowingAndFollowers(User selectedUser, AuthToken token, FollowCounterObserver followersObserver, FollowCounterObserver followingObserver) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // Get count of most recently selected user's followers.
        GetFollowersCountTask followersCountTask = new GetFollowersCountTask(token,
                selectedUser, new FollowCountHandler(followersObserver));
        executor.execute(followersCountTask);

        // Get count of most recently selected user's followees (who they are following)
        GetFollowingCountTask followingCountTask = new GetFollowingCountTask(token,
                selectedUser, new FollowCountHandler(followingObserver));
        executor.execute(followingCountTask);
    }

    public void isFollower(AuthToken token, User user, User targetUser, IsFollowingObserver observer) {
        IsFollowerTask isFollowerTask = new IsFollowerTask(token, user, targetUser, new IsFollowingHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(isFollowerTask);
    }
}
