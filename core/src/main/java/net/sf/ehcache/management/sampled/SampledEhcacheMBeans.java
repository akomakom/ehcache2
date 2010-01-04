/**
 *  Copyright 2003-2009 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.management.sampled;

import java.lang.reflect.Method;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.store.StoreFactory;

/**
 * Utility class used for getting {@link ObjectName}'s for sampled MBeans
 * 
 * <p />
 * 
 * @author <a href="mailto:asanoujam@terracottatech.com">Abhishek Sanoujam</a>
 * @since 1.7
 */
public abstract class SampledEhcacheMBeans {

    /**
     * Type used for sampled cache manager mbean
     */
    public static final String SAMPLED_CACHE_MANAGER_TYPE = "SampledCacheManager";

    /**
     * Type used for sampled cache mbean
     */
    public static final String SAMPLED_CACHE_TYPE = "SampledCache";

    /**
     * Group id for all sampled mbeans registered
     */
    public static final String GROUP_ID = "net.sf.ehcache";

    /**
     * Returns an ObjectName for the passed cacheManagerName
     * 
     * @param cacheManagerName
     * @return An {@link ObjectName} using the input name of cache manager
     * @throws MalformedObjectNameException
     */
    public static ObjectName getCacheManagerObjectName(StoreFactory storeFactory, String cacheManagerName)
      throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(GROUP_ID + ":type=" + SAMPLED_CACHE_MANAGER_TYPE +
                ",name=" + cacheManagerName + getBeanNameSuffix(storeFactory));
        return objectName;
    }

    private static String getBeanNameSuffix(StoreFactory storeFactory) {
        String suffix = "";
        if (storeFactory != null) {
            String uuid = getClientUUID(storeFactory);
            if (uuid != null) {
                suffix = ",node=" + uuid;
            }
        }
        return suffix;
    }
    
    private static String getClientUUID(StoreFactory storeFactory) {
        try {
            Class c = storeFactory.getClass();
            Method m = c.getMethod("getUUID");
            if (m == null) {
                return null;
            }
            return (String)m.invoke(storeFactory);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Returns an ObjectName for the passed cacheManagerName, cacheName
     * combination
     * 
     * @param cacheManagerName
     * @param cacheName
     * @return An {@link ObjectName} representing the cache using the passed
     *         cache and the cache manager name
     * @throws MalformedObjectNameException
     */
    public static ObjectName getCacheObjectName(StoreFactory storeFactory, String cacheManagerName, String cacheName)
      throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(GROUP_ID + ":type=" + SAMPLED_CACHE_TYPE +
                "," + SAMPLED_CACHE_MANAGER_TYPE + "=" +
                cacheManagerName + ",name=" + cacheName + getBeanNameSuffix(storeFactory));
        return objectName;
    }

    /**
     * Returns an ObjectName that can be used for querying all Cache
     * ObjectName's for the passed cacheManagerName
     * 
     * @param cacheManagerName
     * @return An {@link ObjectName} which can be used for querying all Cache
     *         ObjectName's for the input cache manager name
     * @throws MalformedObjectNameException
     */
    public static ObjectName getQueryCacheManagerObjectName(StoreFactory storeFactory, String cacheManagerName)
      throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(GROUP_ID + ":*," + SAMPLED_CACHE_MANAGER_TYPE + "=" +
                cacheManagerName + getBeanNameSuffix(storeFactory));
        return objectName;
    }

    /**
     * Returns an ObjectName that can be used to query all objectNames of {@link #SAMPLED_CACHE_MANAGER_TYPE}
     * 
     * @return An {@link ObjectName} that can be used to query all ObjectName's
     *         of {@value #SAMPLED_CACHE_MANAGER_TYPE}
     * @throws MalformedObjectNameException
     */
    public static ObjectName getQueryCacheManagersObjectName(StoreFactory storeFactory) throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(GROUP_ID + ":type=" + SAMPLED_CACHE_MANAGER_TYPE + ",*" +
                getBeanNameSuffix(storeFactory));
        return objectName;
    }
}
