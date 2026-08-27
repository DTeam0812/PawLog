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
        String sql = "INSERT INTO users (id, password_hash, nickname, gender, birth_date) VALUES (?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getId());
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

    // 로그인 (유저 엔티티 반환)
    public UserEntity getUserByIdAndPassword(String id, String passwordHash) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            String sql = "SELECT user_id, id, password_hash, nickname, gender, birth_date, created_at FROM users WHERE id = ? AND password_hash = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, passwordHash);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                UserEntity entity = new UserEntity();
                entity.setUserId(rs.getLong("user_id"));
                entity.setId(rs.getString("id"));
                entity.setPasswordHash(rs.getString("password_hash"));
                entity.setNickname(rs.getString("nickname"));
                entity.setGender(rs.getString("gender"));
                entity.setBirthDate(rs.getDate("birth_date"));
                entity.setCreatedAt(rs.getTimestamp("created_at"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    // 로그인
    public boolean loginChk(String id, String passwordHash) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean flag = false;

        try {
            con = pool.getConnection();
            String sql = "SELECT COUNT(id) FROM users WHERE id = ? AND password_hash = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
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

    // 아이디 중복 체크
    public boolean checkIdExists(String id) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            con = pool.getConnection();
            String sql = "SELECT COUNT(id) FROM users WHERE id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
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

    // user_id로 사용자 엔티티 조회 (기존 비밀번호 대조 및 사용자 확인용)
    public UserEntity getUserById(long userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            String sql = "SELECT user_id, id, password_hash, nickname, gender, birth_date, created_at FROM users WHERE user_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                UserEntity entity = new UserEntity();
                entity.setUserId(rs.getLong("user_id"));
                entity.setId(rs.getString("id"));
                entity.setPasswordHash(rs.getString("password_hash"));
                entity.setNickname(rs.getString("nickname"));
                entity.setGender(rs.getString("gender"));
                entity.setBirthDate(rs.getDate("birth_date"));
                entity.setCreatedAt(rs.getTimestamp("created_at"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    // 마이페이지 회원 정보 수정 (닉네임, 비밀번호 선택적 수정)
    public boolean updateUser(long userId, String nickname, String newPasswordHash) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE users SET ";
        boolean needComma = false;

        if (nickname != null && !nickname.trim().isEmpty()) {
            sql += "nickname = ?";
            needComma = true;
        }
        if (newPasswordHash != null && !newPasswordHash.trim().isEmpty()) {
            if (needComma) sql += ", ";
            sql += "password_hash = ?";
            needComma = true;
        }

        // 수정할 항목이 없는 경우 방어
        if (!needComma) {
            return false;
        }

        sql += " WHERE user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            int idx = 1;
            if (nickname != null && !nickname.trim().isEmpty()) {
                pstmt.setString(idx++, nickname.trim());
            }
            if (newPasswordHash != null && !newPasswordHash.trim().isEmpty()) {
                pstmt.setString(idx++, newPasswordHash.trim());
            }
            pstmt.setLong(idx, userId);

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

    // 로그인 아이디로 사용자 엔티티 조회
    public UserEntity getUserByLoginId(String id) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            String sql = "SELECT user_id, id, password_hash, nickname, gender, birth_date, created_at FROM users WHERE id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                UserEntity entity = new UserEntity();
                entity.setUserId(rs.getLong("user_id"));
                entity.setId(rs.getString("id"));
                entity.setPasswordHash(rs.getString("password_hash"));
                entity.setNickname(rs.getString("nickname"));
                entity.setGender(rs.getString("gender"));
                entity.setBirthDate(rs.getDate("birth_date"));
                entity.setCreatedAt(rs.getTimestamp("created_at"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }
}
