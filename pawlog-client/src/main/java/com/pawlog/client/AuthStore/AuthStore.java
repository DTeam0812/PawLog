package com.pawlog.client.AuthStore;
import javax.swing.*;

import com.pawlog.client.ui.auth.Login;

import java.awt.*;


public class AuthStore {
    private static final AuthStore instance = new AuthStore();
    private String email ;
    private String nickname;
    private long userId;
    private  boolean loggedIn ;//로그인 확인용 

    public boolean isLoggedIn() { return loggedIn;}//로그인 체크 확인 함수
   
    public void login(long userId, String email, String nickname) {//로그인 상태 활성화
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        loggedIn = true;
    }
    public String getEmail() {
    return email;
    }
    public long getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public void logout(){//로갓
        email = null;
        nickname = null;
        userId = 0;
        loggedIn = false;
        
    }
    private AuthStore() {

    }

    public static AuthStore getInstance() {
        return instance;
    }
}
