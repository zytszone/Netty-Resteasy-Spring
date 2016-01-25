package com.tay.rest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Assert;
import org.junit.Test;

import com.tay.rest.auth.UserLogin;
import com.tay.rest.pojo.Article;
import com.tay.rest.pojo.Helloworld;
import com.tay.rest.server.BizException;


public class testHomeController {

    @Test
    public void testHelloworld() {
        ResteasyClient client = new ResteasyClientBuilder().build();

        ResteasyWebTarget target = client.target(buildUrl("hello/world"));
        Response response = target.request().header("UserToken", "").get();
        Helloworld hw = response.readEntity(Helloworld.class);
        System.out.println(hw.getMessage());
        response.close();
    }

    @Test
    public void testLogin() {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl("hello/login"));
        UserLogin userLogin = new UserLogin("003", "xyz");
        Response response = target.request().post(Entity.entity(userLogin, MediaType.APPLICATION_JSON));
        String userToken = response.readEntity(String.class);
        System.out.println(userToken);
        response.close();
    }

    @Test
    public void testSinglePOST() {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl("hello/login"));
        UserLogin userLogin = new UserLogin("003", "xyz");
        Response response = target.request().post(Entity.entity(userLogin, MediaType.APPLICATION_JSON));
        String userToken = response.readEntity(String.class);
        System.out.println(userToken);
        response.close();

        Article article = new Article(2, "NAME");

        ResteasyClient client2 = new ResteasyClientBuilder().build();
        ResteasyWebTarget target2 = client2.target(buildUrl("hello/singlesave"));
        Response response2 = target2.request().header("UserToken", userToken).post(Entity.entity(article, MediaType
                .APPLICATION_JSON));
        Article rtv = response2.readEntity(Article.class);
        response2.close();
        assertNotNull(rtv);
        assertTrue(rtv.getId().equals(2));
        assertTrue(rtv.getName().equals("NAME"));
    }

    @Test
    public void testPOSTbyList() {

        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl("hello/login"));
        UserLogin userLogin = new UserLogin("002", "abc");
        Response response = target.request().post(Entity.entity(userLogin, MediaType.APPLICATION_JSON));
        String userToken = response.readEntity(String.class);
        System.out.println(userToken);
        response.close();

        Article article = new Article(2, "NAME");

        ResteasyClient client2 = new ResteasyClientBuilder().build();
        ResteasyWebTarget target2 = client2.target(buildUrl("hello/multisave?multi=true"));
        Response response2 = target2.request().header("UserToken", userToken).post(Entity.entity(Collections
                .singletonList(article), MediaType.APPLICATION_JSON));
        GenericType<List<Article>> ArticleListType = new GenericType<List<Article>>() {
        };

        List<Article> list = response2.readEntity(ArticleListType);
        assertEquals(1, list.size());
        response2.close();
    }

    @Test
    public void testPOSTbyList2() {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl("hello/login"));
        UserLogin userLogin = new UserLogin("002", "abc");
        Response response = target.request().post(Entity.entity(userLogin, MediaType.APPLICATION_JSON));
        String userToken = response.readEntity(String.class);
        System.out.println(userToken);
        response.close();

        List<Article> list = new ArrayList<Article>();
        list.add(new Article(1, "book1"));
        list.add(new Article(2, "book2"));


        ResteasyClient client2 = new ResteasyClientBuilder().build();
        ResteasyWebTarget target2 = client2.target(buildUrl("hello/multisave?multi=true"));
        Response response2 = target2.request().header("UserToken", userToken).post(Entity.entity(list, MediaType
                .APPLICATION_JSON));
        GenericType<List<Article>> ArticleListType = new GenericType<List<Article>>() {
        };

        List<Article> list2 = response2.readEntity(ArticleListType);
        assertEquals(2, list2.size());
        response2.close();
    }

    @Test
    public void testError() {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl("hello/error"));
        Response response = target.request().get();
        assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
        BizException bizExeption = response.readEntity(BizException.class);
        assertEquals("TEST", bizExeption.getCode());
        assertEquals("test exception", bizExeption.getMsg());
        response.close();
    }

    @Test
    public void testStress() {
        ResteasyClient client = new ResteasyClientBuilder().build();
        int i = 1;
        Date beforeTime = new Date();
        for (; i <= 800; i++) {
            boolean result = helloWroldRequest(client);
            Assert.assertEquals("第" + i + "失败", true, result);
        }
        Date afterTime = new Date();
        long betweenTime = afterTime.getTime() - beforeTime.getTime();
        System.out.println("压力测试：" + (i - 1) + "次请求, 耗时" + betweenTime + "毫秒");
    }

    public String buildUrl(String target) {
        return String.format("http://localhost:%d/%s/%s",
                8082, "/resteasy", target);
    }

    private boolean helloWroldRequest(ResteasyClient client) {
        try {
            ResteasyWebTarget target = client.target(buildUrl("hello/world"));
            Response response = target.request().header("UserToken", "").get();
            response.close();
            return true;
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
