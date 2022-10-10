package edu.byu.cs.tweeter.client.model.service;

import android.os.Bundle;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.PostStatusTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.Status;

public class StatusService {

    public interface StatusObserver extends ServiceObserver {
        void handleSuccess();
    }

    private class StatusHandler extends BackgroundTaskHandler<StatusObserver> {
        public StatusHandler(StatusObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(StatusObserver observer, Bundle data) {
            observer.handleSuccess();
        }
    }

    public void postStatus(String post, String formattedDateTime, List<String> parseURLS, List<String> parseMentions, StatusObserver observer) {
        Status newStatus = new Status(post, Cache.getInstance().getCurrUser(), formattedDateTime, parseURLS, parseMentions);
        PostStatusTask statusTask = new PostStatusTask(Cache.getInstance().getCurrUserAuthToken(),
                newStatus, new StatusHandler(observer));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(statusTask);
    }
}
