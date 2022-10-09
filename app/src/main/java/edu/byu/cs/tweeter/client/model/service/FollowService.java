package edu.byu.cs.tweeter.client.model.service;

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
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {

    public interface GetFollowingObserver {
        void addFollowers(List<User> followers, boolean hasMorePages);
        void displayExceptionMessage(Exception ex);
        void displayErrorMessage(String msg);
    }

    public void loadMoreItems(AuthToken currUserAuthToken, User user, int pageSize, User lastFollowee, GetFollowingObserver getFollowingObserver) {
        GetFollowingTask getFollowingTask = new GetFollowingTask(currUserAuthToken,
                user, pageSize, lastFollowee, new GetFollowingHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getFollowingTask);
    }

    /**
     * Message handler (i.e., observer) for GetFollowingTask.
     */
    private class GetFollowingHandler extends Handler {
        private GetFollowingObserver observer;

        public GetFollowingHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowingTask.SUCCESS_KEY);
            if (success) {
                List<User> followers = (List<User>) msg.getData().getSerializable(GetFollowingTask.ITEMS_KEY);
                boolean hasMorePages = msg.getData().getBoolean(GetFollowingTask.MORE_PAGES_KEY);
                observer.addFollowers(followers, hasMorePages);


            } else if (msg.getData().containsKey(GetFollowingTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowingTask.MESSAGE_KEY);
                observer.displayErrorMessage(message);
            } else if (msg.getData().containsKey(GetFollowingTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowingTask.EXCEPTION_KEY);
                observer.displayExceptionMessage(ex);
            }
        }
    }

    public  interface FollowerObserver {
        void handleGetFollowerSuccess(List<User> followers, boolean morePages);
        void handleGetFollowerFailure(String message);
        void handleGetFollowerThrewException(Exception ex);
    }
    public void getFollower(AuthToken token, User user, int limit, User lastFollower, FollowerObserver observer) {
        GetFollowersTask getFollowersTask = new GetFollowersTask(token, user, limit, lastFollower, new GetFollowersHandler(observer));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(getFollowersTask);
    }
    /**
     * Message handler (i.e., observer) for GetFollowersTask.
     */
    private class GetFollowersHandler extends Handler {
        private FollowerObserver observer;
        public GetFollowersHandler(FollowerObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowersTask.SUCCESS_KEY);
            if (success) {
                List<User> followers = (List<User>) msg.getData().getSerializable(GetFollowersTask.ITEMS_KEY);
                boolean hasMorePages = msg.getData().getBoolean(GetFollowersTask.MORE_PAGES_KEY);
                observer.handleGetFollowerSuccess(followers, hasMorePages);
            } else if (msg.getData().containsKey(GetFollowersTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowersTask.MESSAGE_KEY);
                observer.handleGetFollowerFailure(message);
            } else if (msg.getData().containsKey(GetFollowersTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowersTask.EXCEPTION_KEY);
                observer.handleGetFollowerThrewException(ex);
            }
        }
    }

    public interface FollowUserObserver {
        void handleFollowUserSuccess();
        void handleFollowUserFailed(String msg);
        void handleFollowUserThrewException(Exception ex);
    }

    public void followUser(AuthToken authToken, User user, FollowUserObserver followUserObserver) {
        FollowTask followTask = new FollowTask(authToken, user, new FollowUserHandler(followUserObserver));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(followTask);
    }

    private class FollowUserHandler extends Handler {
        private FollowUserObserver observer;
        public FollowUserHandler(FollowUserObserver followUserObserver) {
            this.observer = followUserObserver;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowersTask.SUCCESS_KEY);
            if (success) {
                observer.handleFollowUserSuccess();
            } else if (msg.getData().containsKey(GetFollowersTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowersTask.MESSAGE_KEY);
                observer.handleFollowUserFailed(message);
            } else if (msg.getData().containsKey(GetFollowersTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowersTask.EXCEPTION_KEY);
                observer.handleFollowUserThrewException(ex);
            }
        }
    }

    public interface UnfollowObserver {
        void handleUnfollowSuccess();
        void handleUnfollowFailed(String msg);
        void handleUnFollowThrewException(Exception ex);
    }

    public void unfollowUser(AuthToken authToken, User user, UnfollowObserver observer) {
        UnfollowTask unfollow = new UnfollowTask(authToken, user, new UnfollowHandler(observer));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(unfollow);
    }

    private class UnfollowHandler extends Handler {
        UnfollowObserver observer;
        public UnfollowHandler(UnfollowObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(UnfollowTask.SUCCESS_KEY);
            if (success) {
                observer.handleUnfollowSuccess();
            } else if (msg.getData().containsKey(UnfollowTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(UnfollowTask.MESSAGE_KEY);
                observer.handleUnfollowFailed(message);
            } else if (msg.getData().containsKey(UnfollowTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(UnfollowTask.EXCEPTION_KEY);
                observer.handleUnFollowThrewException(ex);
            }
        }
    }

    public void updateSelectedUserFollowingAndFollowers(User selectedUser, AuthToken token, GetNumFollowersObserver followersObserver, GetNumFollowingObserver followingObserver) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // Get count of most recently selected user's followers.
        GetFollowersCountTask followersCountTask = new GetFollowersCountTask(token,
                selectedUser, new GetFollowersCountHandler(followersObserver));
        executor.execute(followersCountTask);

        // Get count of most recently selected user's followees (who they are following)
        GetFollowingCountTask followingCountTask = new GetFollowingCountTask(token,
                selectedUser, new GetFollowingCountHandler(followingObserver));
        executor.execute(followingCountTask);
    }

    public interface GetNumFollowersObserver {
        void handleGetNumFollowersSuccess(int num);
        void handleGetNumFollowersFailure(String message);
        void handleGetNumFollowersThrewException(Exception ex);
    }

    private class GetFollowersCountHandler extends Handler {
        private GetNumFollowersObserver observer;
        public GetFollowersCountHandler(GetNumFollowersObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowersCountTask.SUCCESS_KEY);
            if (success) {
                int count = msg.getData().getInt(GetFollowersCountTask.COUNT_KEY);
                observer.handleGetNumFollowersSuccess(count);
            } else if (msg.getData().containsKey(GetFollowersCountTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowersCountTask.MESSAGE_KEY);
                observer.handleGetNumFollowersFailure(message);
            } else if (msg.getData().containsKey(GetFollowersCountTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowersCountTask.EXCEPTION_KEY);
                observer.handleGetNumFollowersThrewException(ex);
            }
        }
    }

    public interface GetNumFollowingObserver {
        void handleGetNumFollowingSuccess(int num);
        void handleGetNumFollowingFailure(String message);
        void handleGetNumFollowingThrewException(Exception ex);
    }

    private class GetFollowingCountHandler extends Handler {
        private GetNumFollowingObserver observer;
        public GetFollowingCountHandler(GetNumFollowingObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowingCountTask.SUCCESS_KEY);
            if (success) {
                int count = msg.getData().getInt(GetFollowingCountTask.COUNT_KEY);
                observer.handleGetNumFollowingSuccess(count);
            } else if (msg.getData().containsKey(GetFollowingCountTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowingCountTask.MESSAGE_KEY);
                observer.handleGetNumFollowingFailure(message);
            } else if (msg.getData().containsKey(GetFollowingCountTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowingCountTask.EXCEPTION_KEY);
                observer.handleGetNumFollowingThrewException(ex);
            }
        }
    }

    public interface IsFollowerObserver {
        void handleIsFollowerSuccess(boolean isFollower);
        void handleIsFollowerFailure(String message);
        void handleIsFollowerThrewException(Exception ex);
    }
    public void isFollower(AuthToken token, User user, User targetUser, IsFollowerObserver observer) {
        IsFollowerTask isFollowerTask = new IsFollowerTask(token, user, targetUser, new IsFollowerHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(isFollowerTask);
    }
    private class IsFollowerHandler extends Handler {
        private IsFollowerObserver observer;
        public IsFollowerHandler(IsFollowerObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(IsFollowerTask.SUCCESS_KEY);
            if (success) {
                boolean isFollower = msg.getData().getBoolean(IsFollowerTask.IS_FOLLOWER_KEY);
                observer.handleIsFollowerSuccess(isFollower);
            } else if (msg.getData().containsKey(IsFollowerTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(IsFollowerTask.MESSAGE_KEY);
                observer.handleIsFollowerFailure(message);
            } else if (msg.getData().containsKey(IsFollowerTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(IsFollowerTask.EXCEPTION_KEY);
                observer.handleIsFollowerThrewException(ex);
            }
        }
    }

}
