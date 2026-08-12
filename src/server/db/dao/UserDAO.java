package server.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import server.db.DBConnectionMgr;
import server.db.dto.UserDTO;

public class UserDAO {
	private DBConnectionMgr pool;
	
	public UserDAO() {
		pool = DBConnectionMgr.getInstance();
	}
	
	//회원가입
	public boolean insertUser(UserDTO dto) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			con = pool.getConnection();
			sql = "insert users (email, password_hash, nickname, gender, birth_date) values (?, ?, ?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, dto.getEmail());
			pstmt.setString(2, dto.getPasswordHash());
			pstmt.setString(3, dto.getNickname());
			pstmt.setString(4, dto.getGender());
			pstmt.setDate(5, dto.getBirthDate());

			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return true;
	}
	
	//로그인
	public boolean loginChk(String email, String passwordHash) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag=false;
		try {
			con = pool.getConnection();
			sql = "select count(email) from users where email = ?, password_hash = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, email);
			pstmt.setString(2, passwordHash);
			rs = pstmt.executeQuery();
			if(rs.next()&&rs.getInt(1)==1) flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}
	
}
