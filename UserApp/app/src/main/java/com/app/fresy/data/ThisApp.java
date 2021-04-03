package com.app.fresy.data;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.connection.response.RespDevice;
import com.app.fresy.connection.response.RespState;
import com.app.fresy.model.DeviceInfo;
import com.app.fresy.model.Setting;
import com.app.fresy.model.State;
import com.app.fresy.model.User;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThisApp extends Application {

    private static ThisApp mInstance;

    public static synchronized ThisApp get() {
        return mInstance;
    }

    private Setting setting;

    private int fcm_count = 0;
    private final int FCM_MAX_COUNT = 10;
    private SharedPref shared_pref;
    private User user = null;
    private List<State> states = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        shared_pref = new SharedPref(this);
        user = shared_pref.getUser();
        initStateData(null);

        // Init firebase.
        FirebaseApp.initializeApp(this);

        obtainFirebaseToken();
        subscribeTopicNotif();

    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    // user data
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        shared_pref.saveUser(user);
        this.user = user;
    }

    public void logout() {
        shared_pref.clearUser();
        this.user = null;
    }

    public boolean isLogin() {
        return user != null;
    }

    //states data
    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public State getStateByCode(String code) {
        if (states == null) return null;
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            if (code.equals(s.code)) return s;
        }
        return null;
    }

    public void updateUserData(long id, final LoadUserListener listener) {
        API api = RestAdapter.createAPI();
        Call<User> callbackUser = api.user(id);
        callbackUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User resp = response.body();
                if (resp == null) {
                    listener.onFailed();
                } else {
                    setUser(resp);
                    listener.onSuccess(resp);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                listener.onFailed();
            }
        });
    }

    public interface LoadUserListener {
        void onSuccess(User new_user);

        void onFailed();
    }

    public void initStateData(final LoadStateListener listener) {
        API api = RestAdapter.createAPI();
        Call<RespState> callbackState = api.state(AppConfig.COUNTRY_CODE);
        callbackState.enqueue(new Callback<RespState>() {
            @Override
            public void onResponse(Call<RespState> call, Response<RespState> response) {
                RespState resp = response.body();
                if (resp == null) {
                    if (listener != null) listener.onFailed();
                } else if (resp.states != null) {
                    setStates(resp.states);
                    if (listener != null) listener.onSuccess(resp);
                }
            }

            @Override
            public void onFailure(Call<RespState> call, Throwable t) {
                if (listener != null) listener.onFailed();
            }
        });
    }

    public interface LoadStateListener {
        void onSuccess(RespState new_user);

        void onFailed();
    }

    private void subscribeTopicNotif() {
        if (shared_pref.isSubscibeNotif()) return;
        FirebaseMessaging.getInstance().subscribeToTopic(AppConfig.NOTIFICATION_TOPIC).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                shared_pref.setSubscibeNotif(task.isSuccessful());
            }
        });
    }

    private void obtainFirebaseToken() {
        if (NetworkCheck.isConnect(this) && shared_pref.isNeedRegister()) {
            fcm_count++;

            Task<InstanceIdResult> resultTask = FirebaseInstanceId.getInstance().getInstanceId();
            resultTask.addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String regId = instanceIdResult.getToken();
                    shared_pref.setFcmRegId(regId);
                    if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId);
                }
            });

            resultTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (fcm_count > FCM_MAX_COUNT) return;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            obtainFirebaseToken();
                        }
                    }, 500);
                }
            });
        }
    }

    private void sendRegistrationToServer(String token) {
        Log.d("FCM_TOKEN", token + "");
        DeviceInfo deviceInfo = Tools.getDeviceInfo(this);
        deviceInfo.regid = token;

        API api = RestAdapter.createAPI();
        Call<RespDevice> callbackCall = api.registerDevice(deviceInfo);
        callbackCall.enqueue(new Callback<RespDevice>() {
            @Override
            public void onResponse(Call<RespDevice> call, Response<RespDevice> response) {
                RespDevice resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    shared_pref.setNeedRegister(false);
                }
            }

            @Override
            public void onFailure(Call<RespDevice> call, Throwable t) {
            }
        });
    }

}
