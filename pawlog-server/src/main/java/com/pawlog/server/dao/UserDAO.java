package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.UsageLogEntity;
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

    // 이메일 중복 체크
    public boolean checkEmailExists(String email) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            con = pool.getConnection();
            String sql = "SELECT COUNT(email) FROM users WHERE email = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return exists;
    }

    // 마이페이지 회원 정보 수정 (생년월일, 성별, 비밀번호)
    public boolean updateUser(UserEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE users SET gender = ?, birth_date = ?, password_hash = ? WHERE user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getGender());
            pstmt.setDate(2, entity.getBirthDate());
            pstmt.setString(3, entity.getPasswordHash());
            pstmt.setLong(4, entity.getUserId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 회원 탈퇴
    public boolean deleteUser(long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM users WHERE user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 사용자 닉네임 반환
    public String getNicknameByUserId(long userId){
        Connection con = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                String sql = null;
                String nickname = "";
                try {
                    con = pool.getConnection();
                    sql = "SELECT nickname FROM users WHERE user_id = ?";
                    pstmt = con.prepareStatement(sql);
                    pstmt.setLong(1, userId);
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        nickname = rs.getString("nickname");
                    }
                    return nickname;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    pool.freeConnection(con, pstmt, rs);
                }
                return "";
    }


//    public static void main(String[] args) {
//        new UserDAO();
//    }
}
