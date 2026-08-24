package com.pawlog.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.ProgramEntity;

public class ProgramDAO {
    private DBConnectionMgr pool;

    public ProgramDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 1. 프로세스 이름으로 프로그램 조회 (감지 시 미등록 확인용)
    public ProgramEntity getProgramByProcessName(String processName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT program_id, process_name, display_name FROM programs WHERE process_name = ?";

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

                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    // 2. 프로그램 ID로 단건 조회
    public ProgramEntity getProgramById(long programId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT program_id, process_name, display_name FROM programs WHERE program_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, programId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                ProgramEntity entity = new ProgramEntity();
                entity.setProgramId(rs.getLong("program_id"));
                entity.setProcessName(rs.getString("process_name"));
                entity.setDisplayName(rs.getString("display_name"));

                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    // 3. 신규 프로그램 등록 (최초 감지 시 자동 등록용)
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

    // 4. 키워드로 프로그램 검색 (process_name 또는 display_name LIKE 검색)
    public List<ProgramEntity> searchPrograms(String keyword, int limit) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ProgramEntity> list = new ArrayList<>();

        String sql = "SELECT program_id, process_name, display_name FROM programs "
                   + "WHERE LOWER(process_name) LIKE ? OR LOWER(display_name) LIKE ? "
                   + "ORDER BY program_id ASC LIMIT ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            String searchPattern = "%" + (keyword != null ? keyword.toLowerCase().trim() : "") + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setInt(3, limit > 0 ? limit : 10);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProgramEntity entity = new ProgramEntity();
                entity.setProgramId(rs.getLong("program_id"));
                entity.setProcessName(rs.getString("process_name"));
                entity.setDisplayName(rs.getString("display_name"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }
}
