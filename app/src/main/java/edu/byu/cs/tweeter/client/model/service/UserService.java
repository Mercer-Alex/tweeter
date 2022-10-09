package edu.byu.cs.tweeter.client.model.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService {

    public interface LoginObserver {
        void handleLoginSuccess(User user, AuthToken token);
        void handleLoginThrewException(Exception e);
        void handleLoginFailure(String message);
    }

    public interface GetUserObserver {
        void handleGetUserSuccess(User user);
        void handleGetUserFailed(String message);
        void handleGetUserThrewException(Exception e);
    }

    public interface RegisterObserver {
        void handleRegisterSuccess(User user, AuthToken token);
        void handleRegisterThrewException(Exception ex);
        void handleRegisterFailure(String message);
    }

    public interface LogoutObserver {
        void handleLogoutSuccess();
        void handleLogoutFailure(String message);
        void handleLogoutThrewException(Exception ex);
    }

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

    public void login(String username, String password, AuthenticationObserver loginObserver) {
        LoginTask loginTask = new LoginTask(username, password, new AuthenticationHandler(loginObserver));
        ExecutorService executorService  = Executors.newSingleThreadExecutor();
        executorService.execute(loginTask);
    }

    private class LoginHandler extends Handler {
        private LoginObserver observer;

        public LoginHandler(LoginObserver observer) {this.observer = observer;}

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(LoginTask.SUCCESS_KEY);

            if (success) {
                User loggedInUser = (User) msg.getData().getSerializable(LoginTask.USER_KEY);
                AuthToken token = (AuthToken) msg.getData().getSerializable(LoginTask.AUTH_TOKEN_KEY);

                Cache.getInstance().setCurrUser(loggedInUser);
                Cache.getInstance().setCurrUserAuthToken(token);

                observer.handleLoginSuccess(loggedInUser, token);
            } else if (msg.getData().containsKey(LoginTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(LoginTask.MESSAGE_KEY);
                observer.handleLoginFailure(message);
            } else if (msg.getData().containsKey(LoginTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(LoginTask.EXCEPTION_KEY);
                observer.handleLoginThrewException(ex);
            }
        }
    }

    public void getUser(AuthToken token, String alias, UserService.GetUserObserver observer){
        GetUserTask getUsersTask = new GetUserTask(token, alias, new UserService.GetUserHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUsersTask);
    }
    private class GetUserHandler extends Handler{
        private UserService.GetUserObserver observer;

        public GetUserHandler(UserService.GetUserObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg){
            boolean success = msg.getData().getBoolean(GetUserTask.SUCCESS_KEY);
            if (success) {
                User user = (User) msg.getData().getSerializable(GetUserTask.USER_KEY);
                observer.handleGetUserSuccess(user);
            }
            else if (msg.getData().containsKey(GetUserTask.MESSAGE_KEY)){
                String message = msg.getData().getString(GetUserTask.MESSAGE_KEY);
                observer.handleGetUserFailed(message);
            }
            else if (msg.getData().containsKey(GetUserTask.EXCEPTION_KEY)){
                Exception ex = (Exception) msg.getData().getSerializable(GetUserTask.EXCEPTION_KEY);
                observer.handleGetUserThrewException(ex);
            }
        }
    }

    public void register(String firstName, String lastName, String username, String password, String image, AuthenticationObserver registerObserver) {
        RegisterTask registerTask = new RegisterTask(firstName, lastName, username, password, image, new AuthenticationHandler(registerObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(registerTask);
    }
    private class RegisterHandler extends Handler {
        private RegisterObserver observer;
        public RegisterHandler(RegisterObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(RegisterTask.SUCCESS_KEY);
            if (success) {
                User registeredUser = (User) msg.getData().getSerializable(RegisterTask.USER_KEY);
                AuthToken authToken = (AuthToken) msg.getData().getSerializable(RegisterTask.AUTH_TOKEN_KEY);
                Cache.getInstance().setCurrUser(registeredUser);
                Cache.getInstance().setCurrUserAuthToken(authToken);

                observer.handleRegisterSuccess(registeredUser, authToken);
            } else if (msg.getData().containsKey(RegisterTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(RegisterTask.MESSAGE_KEY);
                observer.handleRegisterFailure(message);
            } else if (msg.getData().containsKey(RegisterTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(RegisterTask.EXCEPTION_KEY);
                observer.handleRegisterThrewException(ex);
            }
        }
    }

    public void logout(AuthToken token, LogoutObserver observer) {
        LogoutTask logoutTask = new LogoutTask(token, new LogoutHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(logoutTask);
    }
    private class LogoutHandler extends Handler {
        LogoutObserver observer;
        public LogoutHandler(LogoutObserver observer) {
            this.observer = observer;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(LogoutTask.SUCCESS_KEY);
            if (success) {
                observer.handleLogoutSuccess();
            } else if (msg.getData().containsKey(LogoutTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(LogoutTask.MESSAGE_KEY);
                observer.handleLogoutFailure(message);
            } else if (msg.getData().containsKey(LogoutTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(LogoutTask.EXCEPTION_KEY);
                observer.handleLogoutThrewException(ex);
            }
        }
    }
}

