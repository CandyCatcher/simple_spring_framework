package top.candysky.controller.superAdmin;

import top.candysky.entity.bo.ShopCategory;
import top.candysky.entity.dto.Result;
import top.candysky.service.solo.ShopCategoryService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

//@WebServlet("/")
public class ShopCategoryOperationController {
    private ShopCategoryService ShopCategoryService;
    Result<Boolean> addShopCategory(HttpServletRequest req, HttpServletResponse resp) {
        return ShopCategoryService.addShopCategory(new ShopCategory());
    }
    Result<Boolean> removeShopCategory(HttpServletRequest req, HttpServletResponse resp){
        return ShopCategoryService.removeShopCategory(1);
    }
    Result<Boolean> modifyShopCategory(HttpServletRequest req, HttpServletResponse resp) {
        return ShopCategoryService.modifyShopCategory(new ShopCategory());
    }
    Result<ShopCategory> queryShopCategoryById(HttpServletRequest req, HttpServletResponse resp) {
        return ShopCategoryService.queryShopCategoryById(1);
    }
    Result<List<ShopCategory>> queryShopCategory(HttpServletRequest req, HttpServletResponse resp) {
        return ShopCategoryService.queryShopCategory(null, 1, 10);
    }
}
