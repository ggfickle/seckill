package org.apache.rocketmq.order.common.service.impl;

import org.apache.rocketmq.order.common.dao.SecKillProductMapper;
import org.apache.rocketmq.order.common.dao.dataobject.SecKillProductDO;
import org.apache.rocketmq.order.common.dao.dataobject.SecKillProductDobj;
import org.apache.rocketmq.order.common.service.SecKillProductService;
import org.apache.rocketmq.order.common.util.LogExceptionWapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2019/6/14 14:53
 * @className SecKillProductServiceImpl
 * @desc 秒杀商品服务实现
 */
@Service(value = "secKillProductService")
public class SecKillProductServiceImpl implements SecKillProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecKillProductServiceImpl.class);

    @Autowired
    SecKillProductMapper secKillProductMapper;

    /**
     * 获取秒杀商品列表
     * @return
     */
    @Override
    public List<SecKillProductDobj> querySecKillProductList() {
        return secKillProductMapper.querySecKillProductList();
    }

    /**
     * 根据商品id查询产品明细
     * @param prodId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SecKillProductDobj querySecKillProductByProdId(String prodId) {
        SecKillProductDO productDO = new SecKillProductDO();
        productDO.setProdId(prodId);
        return secKillProductMapper.querySecKillProductByProdId(productDO);
    }

    /**
     * 减库存,基于乐观锁
     * @param prodId
     * @return
     */
    @Override
    public boolean decreaseProdStock(String prodId) {
        AtomicBoolean retryFlag= new AtomicBoolean(true);
        AtomicInteger retryTimes = new AtomicInteger(1);
        int currentProdStock = 0;
        while (retryFlag.get()) {
            SecKillProductDobj productDobj = querySecKillProductByProdId(prodId);
            // 取库存
            currentProdStock = productDobj.getProdStock();
            // 取版本号
            int beforeVersion = productDobj.getVersion();
            SecKillProductDO productDO = new SecKillProductDO();
            productDO.setProdId(prodId).setVersion(beforeVersion);
            int updateCount = 0;
            try {
                // 更新操作
                updateCount = secKillProductMapper.decreaseProdStock(productDO);
                retryFlag.set(false);
            } catch (Exception e) {
                LOGGER.error("[decreaseProdStock]prodId={},商品减库存[异常],事务回滚,重试中,e={}", prodId,
                        LogExceptionWapper.getStackTrace(e));
            }
            if (updateCount != 1) {
                LOGGER.error("[decreaseProdStock]prodId={},商品减库存[失败],事务回滚, 重试中", prodId);
            }
            if (retryTimes.get() > 3) {
                LOGGER.info("超过重试次数,商品减库存[失败]");
                break;
            }
            retryTimes.incrementAndGet();
        }
        LOGGER.info("[decreaseProdStock]当前减库存[成功],prodId={},剩余库存={}", prodId, currentProdStock-1);
        return true;
    }
}
