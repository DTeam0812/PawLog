package com.pawlog.server.dao;

import com.pawlog.server.db.DBConnectionMgr;
import com.pawlog.server.entity.CategoryEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private DBConnectionMgr pool;

    public CategoryDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    // 카테고리 전체 목록 조회 (UI 드롭다운/컴보박스용)
    public List<CategoryEntity> getAllCategories() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CategoryEntity> list = new ArrayList<>();

        String sql = "SELECT category_id, name FROM categories ORDER BY category_id ASC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                CategoryEntity entity = new CategoryEntity();
                entity.setCategoryId(rs.getInt("category_id"));
                entity.setName(rs.getString("name"));
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    // 카테고리 ID로 단건 조회
    public CategoryEntity getCategoryById(int categoryId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT category_id, name FROM categories WHERE category_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                CategoryEntity entity = new CategoryEntity();
                entity.setCategoryId(rs.getInt("category_id"));
                entity.setName(rs.getString("name"));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }


    /*
    // 카테고리 등록
    public boolean insertCategory(CategoryEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO categories (name) VALUES (?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getName());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 카테고리명 수정
    public boolean updateCategory(CategoryEntity entity) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE categories SET name = ? WHERE category_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, entity.getName());
            pstmt.setInt(2, entity.getCategoryId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    // 카테고리 삭제
    public boolean deleteCategory(int categoryId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, categoryId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

     */

}
