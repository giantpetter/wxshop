package com.huan.wxshop.service;

import com.huan.wxshop.dao.ShopDao;
import com.huan.wxshop.entity.PageResponse;
import com.huan.wxshop.exceptions.HttpException;
import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.generate.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ShopService {
    private final ShopDao shopDao;

    @Autowired
    public ShopService(ShopDao shopDao) {
        this.shopDao = shopDao;
    }


    public Shop queryShopById(Long shopId) {
        return shopDao.selectShopById(shopId);
    }

    public Shop updateShopById(Shop shop) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw HttpException.notAuthorized("Unauthorized");
        }
        Shop oldShop = shopDao.selectShopById(shop.getId());
        if (oldShop == null) {
            throw HttpException.notFound("Not Found");
        }
        if (!Objects.equals(currentUser.getId(), oldShop.getOwnerUserId())) {
            throw HttpException.notShopOwner("Forbidden");
        }
        shop.setUpdatedAt(new Date());
        shopDao.updateShop(shop);
        return shop;

    }

    public Shop createShop(Shop shop) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw HttpException.notAuthorized("Unauthorized");
        }
        shop.setOwnerUserId(UserContext.getCurrentUser().getId());
        return shopDao.insertShop(shop);
    }

    public Shop deleteShopById(Long shopId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw HttpException.notAuthorized("Unauthorized");
        }
        Shop shopToBeDeleted = shopDao.selectShopById(shopId);
        if (shopToBeDeleted == null) {
            throw HttpException.notFound("Not Found");
        }
        if (!Objects.equals(currentUser.getId(), shopToBeDeleted.getOwnerUserId())) {
            throw HttpException.notShopOwner("Forbidden");
        }

        return shopDao.deleteShop(shopToBeDeleted);

    }

    public PageResponse<Shop> getShopsByPage(int pageNum, int pageSize) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw HttpException.notAuthorized("Unauthorized");
        }
        int totalShops = shopDao.selectCountShopsByUser(currentUser.getId());
        int totalPages = totalShops % pageSize == 0 ? totalShops / pageSize : totalShops / pageSize + 1;
        List<Shop> data = shopDao.selectAllShopsByUser(currentUser.getId(), pageNum, pageSize);
        return PageResponse.pagedData(pageNum, pageSize, totalPages, data);
    }

    public Shop getShopByShopId(Long shopId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw HttpException.notAuthorized("Unauthorized");
        }
        Shop shop = shopDao.selectShopById(shopId);
        if (shop == null) {
            throw HttpException.notFound("Not Found");
        }
        return shop;
    }
}
