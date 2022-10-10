package edu.byu.cs.tweeter.client.model.service;

import android.os.Bundle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService {

    public interface AuthenticationObserver extends ServiceObserver {
        void handleSuccess(User user, AuthToken token);
    }

    private class AuthenticationHandler extends BackgroundTaskHandler<AuthenticationObserver> {
        public AuthenticationHandler(AuthenticationObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(AuthenticationObserver observer, Bundle data) {
            User loggedInUser = (User) data.getSerializable(LoginTask.USER_KEY);
            AuthToken token = (AuthToken) data.getSerializable(LoginTask.AUTH_TOKEN_KEY);

            Cache.getInstance().setCurrUser(loggedInUser);
            Cache.getInstance().setCurrUserAuthToken(token);

            observer.handleSuccess(loggedInUser, token);
        }
    }

    public interface GetUserObserver extends ServiceObserver {
        void handleSuccess(User user);
    }

    private class GetUserHandler extends BackgroundTaskHandler<GetUserObserver> {
        public GetUserHandler(GetUserObserver observer) {super(observer);}

        @Override
        protected void handleSuccessMessage(GetUserObserver observer, Bundle data) {
            User user = (User) data.getSerializable(GetUserTask.USER_KEY);
            observer.handleSuccess(user);
        }
    }

    public interface LogoutUserObserver extends ServiceObserver {
        void handleSuccess();
    }

    private class LogoutUserHandler extends BackgroundTaskHandler<LogoutUserObserver> {
        public LogoutUserHandler(LogoutUserObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(LogoutUserObserver observer, Bundle data) {
            observer.handleSuccess();

        }
    }

    public void getUser(AuthToken token, String alias, UserService.GetUserObserver observer){
        GetUserTask getUsersTask = new GetUserTask(token, alias, new GetUserHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUsersTask);
    }

    public void login(String username, String password, AuthenticationObserver loginObserver) {
        LoginTask loginTask = new LoginTask(username, password, new AuthenticationHandler(loginObserver));
        ExecutorService executorService  = Executors.newSingleThreadExecutor();
        executorService.execute(loginTask);
    }

    public void register(String firstName, String lastName, String username, String password,
                         String image, AuthenticationObserver registerObserver) {
        RegisterTask registerTask = new RegisterTask(firstName, lastName, username, password, image, new AuthenticationHandler(registerObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(registerTask);
    }

    public void logout(AuthToken token, LogoutUserObserver observer) {
        LogoutTask logoutTask = new LogoutTask(token, new LogoutUserHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(logoutTask);
    }
}

