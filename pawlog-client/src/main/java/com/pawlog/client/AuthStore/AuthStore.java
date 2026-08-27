package com.pawlog.client.AuthStore;
import javax.swing.*;

import com.pawlog.client.ui.auth.Login;

import java.awt.*;


public class AuthStore {
    private static final AuthStore instance = new AuthStore();
    private String id;
    private String nickname;
    private long userId;
    private  boolean loggedIn ;//로그인 확인용
    private String birthDate;

    public boolean isLoggedIn() { return loggedIn;}//로그인 체크 확인 함수
   
    public void login(long userId, String id, String nickname) {//로그인 상태 활성화
        login(userId, id, nickname, "");
    }

    public void login(long userId, String id, String nickname, String birthDate) {
        this.userId = userId;
        this.id = id;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.loggedIn = true;
    }

    public String getId() {
    return id;
    }
    public long getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public String getBirthDate(){return birthDate;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public void logout(){//로갓
        id = null;
        nickname = null;
        userId = 0;
        birthDate = null;
        loggedIn = false;
        
    }
    private AuthStore() {

    }

    public static AuthStore getInstance() {
        return instance;
    }
}
