package projectSem4.com.dto;

import java.util.List;

public class ProductCreateRequest {
	public Integer categoryId;
	public Integer shopId;
	public String productName;
	public String description;
	public String productOption; // JSON/Text
	public Integer status; // 0/1
	public List<ProductVariantDTO> variants; // KHÔNG chứa ảnh
}
