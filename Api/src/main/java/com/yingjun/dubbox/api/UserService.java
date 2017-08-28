package com.yingjun.dubbox.api;

import com.yingjun.dubbox.entity.User;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author yingjun
 * 注意：如果消费端想要访问提供者的rest服务，这里需要加上JAX-RS的Annotation
 */

@Service("userService")
@Path("rcmd")//
@Produces(MediaType.APPLICATION_JSON)
public interface UserService {

    @GET
    @Path("/getUserByPhone/{phone}/")
    public User getUserByPhone(@PathParam("phone")String phone);

    @GET
    @Path("/getUserByName/{name}/")
    public User getUserByName(@PathParam("name")String name);
}
