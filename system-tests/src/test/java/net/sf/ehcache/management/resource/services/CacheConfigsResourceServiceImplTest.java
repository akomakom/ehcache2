package net.sf.ehcache.management.resource.services;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.http.ContentType;
import io.restassured.internal.path.xml.NodeImpl;
import io.restassured.path.xml.XmlPath;

import java.io.UnsupportedEncodingException;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

/**
 * The aim of this test is to check via HTTP that the ehcache standalone agent /tc-management-api/agents/cacheManagers/caches/config endpoint
 * works fine
 * @author Anthony Dahanne
 */
public class CacheConfigsResourceServiceImplTest extends ResourceServiceImplITHelper {
  protected static final String EXPECTED_RESOURCE_LOCATION = "/tc-management-api/agents{agentIds}/cacheManagers{cmIds}/caches{cacheIds}/configs";

  @BeforeClass
  public static void setUpCluster() throws Exception {
    setUpCluster(CacheConfigsResourceServiceImplTest.class);
  }

  @Before
  public void setUp() throws UnsupportedEncodingException {
    cacheManagerMaxBytes = getCacheManagerMaxBytes();
  }

  @Test
  /**
   * - GET the list of caches configs
   *
   * @throws Exception
   */
  public void getCacheConfigsTest() throws Exception {
    String agentsFilter = "";
    String cmsFilter = "";
    String cachesFilter = "";

    String xml =
      givenStandalone()
      .expect()
        .contentType(ContentType.JSON)
        .body("find { it.cacheManagerName == 'testCacheManagerProgrammatic' }.cacheName", is("testCache2"))
        .body("find { it.cacheManagerName == 'testCacheManager' }.cacheName", is("testCache"))
        .body("[0].agentId", equalTo("embedded"))
        .body("[1].agentId", equalTo("embedded"))
        .statusCode(200)
      .when()
        .get(EXPECTED_RESOURCE_LOCATION, agentsFilter, cmsFilter, cachesFilter)
        .jsonPath().get("find { it.cacheManagerName == 'testCacheManager' }.xml").toString();

    XmlPath xmlPath = new XmlPath(xml);
    NodeImpl cache = xmlPath.get("cache");
    assertEquals("testCache", cache.attributes().get("name"));

    //same thing but we specify only a given cacheManager
    agentsFilter = "";
    cmsFilter = ";names=testCacheManagerProgrammatic";
    cachesFilter = ";names=testCache2";

    String filteredXml = givenStandalone()
      .expect()
        .contentType(ContentType.JSON)
        .body("[0].agentId", equalTo("embedded"))
        .body("[0].cacheManagerName", equalTo("testCacheManagerProgrammatic"))
        .body("[0].cacheName", equalTo("testCache2"))
        .statusCode(200)
      .when()
        .get(EXPECTED_RESOURCE_LOCATION, agentsFilter, cmsFilter, cachesFilter)
        .jsonPath().get("[0].xml").toString();

    xmlPath = new XmlPath(filteredXml);
    cache = xmlPath.get("cache");
    assertEquals("testCache2", cache.attributes().get("name"));
  }

  @Test
  public void getCacheConfigsTest__clustered() throws Exception {
    String cmsFilter = "";
    String cachesFilter = "";
      String agentsFilter = ";ids=" + cacheManagerMaxBytesAgentId + "," + cacheManagerMaxElementsAgentId;

    String xml = givenClustered()
      .expect()
        .contentType(ContentType.JSON)
        .body("find { it.cacheManagerName == 'testCacheManagerProgrammatic' }.agentId", equalTo(cacheManagerMaxBytesAgentId))
        .body("find { it.cacheManagerName == 'testCacheManagerProgrammatic' }.cacheName", is("testCache2"))
        .body("find { it.cacheManagerName == 'testCacheManager' }.cacheName", is("testCache"))
        .body("find { it.cacheManagerName == 'testCacheManager' }.agentId", equalTo(cacheManagerMaxElementsAgentId))
        .statusCode(200)
      .when()
        .get(EXPECTED_RESOURCE_LOCATION, agentsFilter, cmsFilter, cachesFilter)
        .jsonPath().get("find { it.cacheManagerName == 'testCacheManager' }.xml").toString();

    XmlPath xmlPath = new XmlPath(xml);
    NodeImpl cache = xmlPath.get("cache");
    assertEquals("testCache", cache.attributes().get("name"));

    //same thing but we specify only a given cacheManager
    cmsFilter = ";names=testCacheManagerProgrammatic";
    cachesFilter = ";names=testCache2";

    String filteredXml = givenClustered()
      .expect()
        .contentType(ContentType.JSON)
        .body("[0].agentId", equalTo(cacheManagerMaxBytesAgentId))
        .body("[0].cacheManagerName", equalTo("testCacheManagerProgrammatic"))
        .body("[0].cacheName", equalTo("testCache2"))
        .statusCode(200)
      .when()
        .get(EXPECTED_RESOURCE_LOCATION, agentsFilter, cmsFilter, cachesFilter)
        .jsonPath().get("[0].xml").toString();

    xmlPath = new XmlPath(filteredXml);
    cache = xmlPath.get("cache");
    assertEquals("testCache2", cache.attributes().get("name"));
  }

  @After
  public void tearDown() {
    if (cacheManagerMaxBytes != null) {
      cacheManagerMaxBytes.shutdown();
    }
  }
}
