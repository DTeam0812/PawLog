package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class CategoryEntity {
    private int categoryId;
    private String name;

    @Override
    public String toString() {
        return "CategoryEntity ["
                + "categoryId=" + categoryId
                + ", name=" + name
                + "]";
    }
}
