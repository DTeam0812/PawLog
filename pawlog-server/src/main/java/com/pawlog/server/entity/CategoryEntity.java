package server.db.dto;

public class CategoryDTO {
	private int categoryId;
    private String name;


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
        return "CategoryDTO ["
                + "categoryId=" + categoryId
                + ", name=" + name
                + "]";
    }
}
