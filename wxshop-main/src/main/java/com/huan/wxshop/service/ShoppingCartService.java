package com.huan.wxshop.service;

import com.huan.api.entity.DataStatus;
import com.huan.wxshop.controller.ShoppingCartController;
import com.huan.wxshop.dao.ShoppingCartQueryMapper;
import com.huan.wxshop.entity.GoodsWithNumber;
import com.huan.wxshop.entity.PageResponse;
import com.huan.wxshop.entity.ShoppingCartData;
import com.huan.wxshop.exceptions.HttpException;
import com.huan.wxshop.generate.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class ShoppingCartService {
    private final ShoppingCartQueryMapper shoppingCartQueryMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final GoodsService goodsService;
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public ShoppingCartService(ShoppingCartQueryMapper shoppingCartQueryMapper,
                               ShoppingCartMapper shoppingCartMapper,
                               SqlSessionFactory sqlSessionFactory,
                               GoodsService goodsService) {
        this.shoppingCartQueryMapper = shoppingCartQueryMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.goodsService = goodsService;
    }

    public PageResponse<ShoppingCartData> getShoppingCardOfUser(int pageNum, int pageSize) {
        User currentUser = UserContext.getCurrentUser();
        long userId = currentUser.getId();
        int totalNum = shoppingCartQueryMapper.countHowManyShopsInUserShoppingCart(userId);
        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;

        int offset = (pageNum - 1) * pageSize;
        List<Long> shopsId = shoppingCartQueryMapper.selectShopIdsByPageAndUser(userId, pageSize, offset);
        List<ShoppingCartData> pageData = new ArrayList<>();
        for (long shopId : shopsId) {
            ShoppingCartData shoppingCart = new ShoppingCartData();
            shoppingCart.setShop(shoppingCartQueryMapper.selectShopById(shopId));
            shoppingCart.setGoods(shoppingCartQueryMapper.selectGoodsByShopIdFromCart(shopId, userId));
            pageData.add(shoppingCart);
        }
        return PageResponse.pagedData(pageNum, pageSize, totalPage, pageData);
    }

    public ShoppingCartData addToShoppingCart(ShoppingCartController.AddToShoppingCartRequest request, long userId) {
        List<Long> goodsId = request.getGoods()
                .stream()
                .map(ShoppingCartController.AddToShoppingCartItem::getGoodsId)
                .collect(Collectors.toList());

        Map<Long, Goods> idToGoodsMap = goodsService.getIdToGoodsMap(goodsId);
        if (idToGoodsMap.values().stream().map(Goods::getShopId).collect(Collectors.toSet()).size() != 1) {
            throw HttpException.badRequest("商品 ID 非法:" + goodsId);
        }
        List<ShoppingCart> shoppingCartRows = request.getGoods()
                .stream()
                .map(item -> toShoppingCartRow(item, idToGoodsMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //批量插入
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            ShoppingCartMapper mapper = sqlSession.getMapper(ShoppingCartMapper.class);
            shoppingCartRows.forEach(mapper::insertSelective);
            sqlSession.commit();
        }

        long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        return getLatestShoppingCartData(shopId, userId);
    }

    private ShoppingCart toShoppingCartRow(ShoppingCartController.AddToShoppingCartItem item,
                                           Map<Long, Goods> idToGoodsMap) {
        Goods goods = idToGoodsMap.get(item.getGoodsId());
        if (goods == null) {
            return null;
        }
        ShoppingCart result = new ShoppingCart();
        result.setGoodsId(item.getGoodsId());
        result.setNumber(item.getNumber());
        result.setUserId(UserContext.getCurrentUser().getId());
        result.setShopId(goods.getShopId());
        result.setStatus(DataStatus.OK.getStatus());
        return result;
    }

    public ShoppingCartData deleteShoppingCartByGoodsId(Long goodsId) {
        long userId = UserContext.getCurrentUser().getId();
        ShoppingCartExample example = new ShoppingCartExample();
        example.createCriteria().andGoodsIdEqualTo(goodsId).andUserIdEqualTo(userId);
        List<ShoppingCart> cartGoods = shoppingCartMapper.selectByExample(example);

        if (Objects.nonNull(cartGoods) && !cartGoods.isEmpty()) {
            ShoppingCart cartGoodsRow = cartGoods.get(0);
            cartGoodsRow.setStatus(DataStatus.DELETE_STATUS.getStatus());
            cartGoodsRow.setUpdatedAt(new Date());
            shoppingCartMapper.updateByPrimaryKey(cartGoodsRow);

            long shopId = cartGoodsRow.getShopId();
            return getLatestShoppingCartData(shopId, userId);
        }
        throw HttpException.notFound("未找到商品！");
    }

    private ShoppingCartData getLatestShoppingCartData(long shopId, long userId) {
        ShoppingCartData data = new ShoppingCartData();
        Shop shop = shoppingCartQueryMapper.selectShopById(shopId);
        List<GoodsWithNumber> goods = shoppingCartQueryMapper.selectGoodsByShopIdFromCart(shopId, userId);
        data.setGoods(goods);
        data.setShop(shop);
        return data;
    }
}
