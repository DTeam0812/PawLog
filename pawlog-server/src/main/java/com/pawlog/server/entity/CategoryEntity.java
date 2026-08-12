package com.pawlog.server.entity;

public class CategoryEntity {
    private int categoryId;
    private String name;

    public CategoryEntity() {
    }

    public CategoryEntity(int categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CategoryEntity ["
                + "categoryId=" + categoryId
                + ", name=" + name
                + "]";
    }
}
