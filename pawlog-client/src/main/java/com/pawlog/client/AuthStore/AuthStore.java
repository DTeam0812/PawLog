package com.pawlog.client.AuthStore;
import javax.swing.*;

import com.pawlog.client.ui.auth.Login;

import java.awt.*;


public class AuthStore {
    private static final AuthStore instance = new AuthStore();
    private String Email1 ;

    private  boolean loggedIn ;//로그인 확인용 

    public boolean isLoggedIn() { return loggedIn;}//로그인 체크 확인 함수
   
    public void login(String Email) {//로그인 상태 활성화 
        Email1 = Email;
        loggedIn = true;
    }
    public void logout(){//로갓
        loggedIn = false;
    }
    private AuthStore() {
        

    }

    public static AuthStore getInstance() {
        return instance;
    }
}