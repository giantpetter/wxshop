package com.huan.wxshop.service;

import com.huan.wxshop.dao.GoodsDao;
import com.huan.wxshop.dao.ShopDao;
import com.huan.wxshop.entity.DataStatus;
import com.huan.wxshop.entity.PageResponse;
import com.huan.wxshop.exceptions.NotAuthorizedForShopException;
import com.huan.wxshop.exceptions.ResourceNotFoundException;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.generate.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoodsServiceTest {
    @Mock
    GoodsDao goodsDao;
    @Mock
    ShopDao shopDao;
    @Mock
    Shop shop;
    @Mock
    Goods goods;

    @InjectMocks
    GoodsService goodsService;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId(1L);
        UserContext.setCurrentUser(user);

        lenient().when(shopDao.selectShopById(anyLong())).thenReturn(shop);
    }

    @AfterEach
    public void destroyUserContext() {
        UserContext.setCurrentUser(null);
    }

    @Test
    public void createGoodsSucceedIfUserIsOwner() {
        long ownerId = 1;
        when(shop.getOwnerUserId()).thenReturn(ownerId);
        when(goodsDao.insertGoods(goods)).thenReturn(goods);

        Assertions.assertEquals(goodsService.createGoods(goods), goods);
    }

    @Test
    public void createGoodsFailedIfUserIsNotOwner() {
        long ownerId = 2;
        when(shop.getOwnerUserId()).thenReturn(ownerId);
        Assertions.assertThrows(NotAuthorizedForShopException.class, () -> {
            goodsService.createGoods(goods);
        });
    }

    @Test
    public void throwRouseNotFoundExceptionWhenDeleteGoodsById() {
        long goodsId = 123;
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsDao.selectGoodsById(goodsId)).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            goodsService.deleteGoodsById(goodsId);
        });
    }

    @Test
    public void deleteGoodsByIdFailedIfUserIsNotOwner() {
        when(shop.getOwnerUserId()).thenReturn(2L);
        Assertions.assertThrows(NotAuthorizedForShopException.class, () -> {
            goodsService.deleteGoodsById(123L);
        });
    }

    @Test
    public void deleteGoodsByIdSucceed() {
        long goodsId = 123;
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsDao.selectGoodsById(goodsId)).thenReturn(goods);
        goodsService.deleteGoodsById(goodsId);
        verify(goods).setStatus(DataStatus.DELETE_STATUS.getStatus());
    }

    @Test
    public void getGoodsSucceedWithShopId() {
        int pageNum = 5, pageSize = 10;
        when(goodsDao.countGoodsByShopId(any())).thenReturn(55);
        List<Goods> mockData = Mockito.mock(List.class);
        when(goodsDao.selectGoodsByPage(pageNum, pageSize)).thenReturn(mockData);

        PageResponse<Goods> result = goodsService.getGoods(pageNum, pageSize, any());
        Assertions.assertEquals(result.getTotalPage(), 6);
        Assertions.assertEquals(result.getPageSize(), 10);
        Assertions.assertEquals(result.getPageNum(), 5);
        Assertions.assertEquals(result.getData(), mockData);
    }

    @Test
    public void updateGoodsSucceed() {
        when(goodsDao.updateGoods(goods)).thenReturn(1);
        when(shop.getOwnerUserId()).thenReturn(1L);

//        verify(goods).setUpdatedAt(new Date());
        Assertions.assertEquals(goods, goodsService.updateGoods(123L, goods));
    }

    @Test
    public void updateGoodsFailedIfNotFound() {
        when(goodsDao.updateGoods(goods)).thenReturn(0);
        when(shop.getOwnerUserId()).thenReturn(1L);
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> {
                    goodsService.updateGoods(123L, goods);
                });
    }

    @Test
    public void updateGoodsFailedIfNotOwner() {
        when(shop.getOwnerUserId()).thenReturn(0L);
        Assertions.assertThrows(NotAuthorizedForShopException.class,
                () -> {
                    goodsService.updateGoods(123L, goods);
                });
    }


}
