package com.yingjun.dubbox.api;


import com.yingjun.dubbox.entity.RecommendApps;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @autho mikemyzhao
* 添加推荐服务
* */

@Service("rcmdService")
@Path("rcmd")//这个会被RcmdServiceImpl的配置覆盖掉，但是最好两边保持一致
@Produces(MediaType.APPLICATION_JSON)
public interface RcmdService {

    @GET
    @Path("/getRcmdList/{uid}/")//和RcmdServiceImpl中的path保持一致
    public RecommendApps getRcmdList(@PathParam("uid") String uid);

}
