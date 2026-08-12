package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.UserEntity;

public class UserDAO {
    private DBConnectionMgr pool;

    public UserDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 회원가입
    public boolean insertUser(UserEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO users (email, password_hash, nickname, gender, birth_date) VALUES (?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getEmail());
            pstmt.setString(2, entity.getPasswordHash());
            pstmt.setString(3, entity.getNickname());
            pstmt.setString(4, entity.getGender());
            pstmt.setDate(5, entity.getBirthDate());

            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 로그인
    public boolean loginChk(String email, String passwordHash) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean flag = false;

        try {
            con = pool.getConnection();
            String sql = "SELECT COUNT(email) FROM users WHERE email = ? AND password_hash = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 1) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return flag;
    }

    public static void main(String[] args) {
        new UserDAO();
    }
}
