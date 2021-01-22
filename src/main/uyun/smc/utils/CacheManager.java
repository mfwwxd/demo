package uyun.smc.utils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.smc.common.RouteKey;
import uyun.smc.config.SdkConfig;

/**
 * 路由缓存
 */
public final class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    private static final String CONFIG_NAME = "_SMC_CACHE_";

    private static Cache cache;
    private static Cache secondCache;

    static {
        init();
    }

    private static void init() {
        try {

            Configuration config = new Configuration();

            //router一级缓存
            net.sf.ehcache.config.CacheConfiguration cacheConfig1 = new CacheConfiguration();
            cacheConfig1.setName("routeCache");
            cacheConfig1.setMaxEntriesLocalHeap(10000);
            cacheConfig1.setEternal(false);
            cacheConfig1.setTimeToIdleSeconds(SdkConfig.CACHE_EXPIRED_TIME);
            cacheConfig1.setTimeToLiveSeconds(SdkConfig.CACHE_EXPIRED_TIME);
            cacheConfig1.persistence(new PersistenceConfiguration()
                    .strategy(PersistenceConfiguration.Strategy.NONE));
            cacheConfig1.setMemoryStoreEvictionPolicy("LRU");
            cacheConfig1.setDiskExpiryThreadIntervalSeconds(120);

            config.addCache(cacheConfig1);

            //router二级缓存
            net.sf.ehcache.config.CacheConfiguration cacheConfig2 = new CacheConfiguration();
            cacheConfig2.setName("routeSecondCache");
            cacheConfig2.setMaxEntriesLocalHeap(10000);
            cacheConfig2.setEternal(true);
            cacheConfig1.persistence(new PersistenceConfiguration()
                    .strategy(PersistenceConfiguration.Strategy.NONE));
            config.addCache(cacheConfig2);

            config.setName(CONFIG_NAME);
            net.sf.ehcache.CacheManager cacheManager = net.sf.ehcache.CacheManager.newInstance(config);

            cache = cacheManager.getCache("routeCache");
            secondCache = cacheManager.getCache("routeSecondCache");

        } catch (Exception ex) {
            throw new RuntimeException("ehcache config file exception");
        }
    }

    public static boolean addElementCache(RouteKey key, String value) {
        Element element = new Element(key, value);
        // 4. 将元素添加到缓存、二级缓存（双写）
        cache.put(element);
        secondCache.put(element);
        return true;
    }

    public static boolean delElementCache(RouteKey key) {
        return cache.remove(key);
    }

    /**
     * 常规获取路由缓存(1级缓存)
     *
     * @param key
     * @return
     */
    public static String getNormalRouteCache(RouteKey key) {
        //从一级缓存取值（短生命周期）
        Element value = cache.get(key);
        if (value == null || (cache.isKeyInCache(key) && cache.isExpired(value))) {
            return null;
        } else {
            return String.valueOf(value.getObjectValue());
        }
    }

    /**
     * 通过两层缓存机制获取路由
     * @param key
     * @return
     */
    public static String getElementCache(RouteKey key) {
        //首先从一级缓存取值（短生命周期）
        Element value = cache.get(key);
        //如果取不到，则取二级缓存取值（长生命周期，永不过期直至重启，不过数据到达指定容量仍会淘汰一部分数据）
        if (value == null || cache.isKeyInCache(key) && cache.isExpired(value)) {
            value = secondCache.get(key);
            if (value != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("##get url from secondCache##");
                }
            }
        }
        //如果仍取不到，则返回null（即遇到了新元素，外层调用业务存入新元素）
        if (value != null) {
            if (logger.isDebugEnabled()) {
//                logger.debug("##cacheKey##" + JSON.toJSONString(key));
                logger.debug("##cacheValue##" + value.getObjectValue());
            }
            return (String) value.getObjectValue();
        } else {
            return null;
        }
    }

}