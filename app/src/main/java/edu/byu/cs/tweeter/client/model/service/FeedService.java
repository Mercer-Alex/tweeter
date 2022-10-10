package edu.byu.cs.tweeter.client.model.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFeedTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class FeedService {

    public interface FeedObserver extends ServiceObserver {
        void handleSuccess(List<Status> statuses, boolean hasMorePages);
    }

    private class FeedHandler extends BackgroundTaskHandler<FeedObserver> {
        public FeedHandler(FeedObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(FeedObserver observer, Bundle data) {
            List<Status> statuses = (List<Status>) data.getSerializable(GetFeedTask.ITEMS_KEY);
            boolean morePages = data.getBoolean(GetFeedTask.MORE_PAGES_KEY);
            observer.handleSuccess(statuses, morePages);
        }
    }

    public void getFeed(AuthToken token, User user, int limit, Status lastStatus, FeedObserver observer) {
        GetFeedTask getFeedTask = new GetFeedTask(token, user, limit, lastStatus, new FeedHandler(observer));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(getFeedTask);
    }
}
