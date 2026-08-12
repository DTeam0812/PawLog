package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramEntity;

public class ProgramDAO {
    private DBConnectionMgr pool;

    public ProgramDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 프로세스 이름으로 프로그램 조회
    public ProgramEntity getProgramByProcessName(String processName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT program_id, process_name, display_name, category_id FROM programs WHERE process_name = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, processName);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                ProgramEntity entity = new ProgramEntity();
                entity.setProgramId(rs.getLong("program_id"));
                entity.setProcessName(rs.getString("process_name"));
                entity.setDisplayName(rs.getString("display_name"));

                int categoryId = rs.getInt("category_id");
                if (rs.wasNull()) {
                    entity.setCategoryId(null);
                } else {
                    entity.setCategoryId(categoryId);
                }
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    // 프로그램 등록
    public boolean insertProgram(String processName, String displayName) {
        Connection con = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO programs (process_name, display_name) VALUES (?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, processName);
            pstmt.setString(2, displayName);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
}
