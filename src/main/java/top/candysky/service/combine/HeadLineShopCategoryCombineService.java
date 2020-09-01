package top.candysky.service.combine;

import top.candysky.entity.dto.MainPageInfoDTO;
import top.candysky.entity.dto.Result;

public interface HeadLineShopCategoryCombineService {
    Result<MainPageInfoDTO> getMainPageInfo();
}
