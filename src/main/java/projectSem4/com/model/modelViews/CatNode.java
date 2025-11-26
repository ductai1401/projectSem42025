package projectSem4.com.model.modelViews;

import java.util.ArrayList;
import java.util.List;

import projectSem4.com.model.entities.Category;

public class CatNode {
	private Category category;
    private List<CatNode> children = new ArrayList<>();

    public CatNode(Category category) {
        this.category = category;
    }

    public Category getCategory() { return category; }
    public List<CatNode> getChildren() { return children; }
    public void setChildren(List<CatNode> children) { this.children = children; }
}
