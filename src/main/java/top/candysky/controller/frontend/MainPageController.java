package top.candysky.controller.frontend;

import top.candysky.entity.dto.MainPageInfoDTO;
import top.candysky.entity.dto.Result;
import top.candysky.service.combine.HeadLineShopCategoryCombineService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainPageController {
    private HeadLineShopCategoryCombineService headLineShopCategoryCombineService;

    public Result<MainPageInfoDTO> getMainPageInfo(HttpServletRequest req, HttpServletResponse resp) {
      return headLineShopCategoryCombineService.getMainPageInfo();
    }
}
