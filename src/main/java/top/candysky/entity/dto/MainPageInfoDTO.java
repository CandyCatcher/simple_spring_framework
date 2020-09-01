package top.candysky.entity.dto;

import lombok.Data;
import top.candysky.entity.bo.HeadLine;
import top.candysky.entity.bo.ShopCategory;

import java.util.List;

@Data
public class MainPageInfoDTO {
    private List<HeadLine> headLineList;
    private List<ShopCategory> shopCategoryList;
}
