package com.yingjun.dubbox.services;

import com.yingjun.dubbox.api.RcmdService;
import com.yingjun.dubbox.entity.RecommendApps;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
* @author mikemyzhao
 * http://localhost:8080/rcmd/getRcmdList/{uid}
* */

@Service("rcmdService")
@Path("/rcmd")//这个路径也要和UserService保持一致
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RcmdServiceImpl implements RcmdService{

    @GET
    @Path("/getRcmdList/{uid}/")//和RcmdService的url对照上
    //和path里面的方法对应上
    public RecommendApps getRcmdList(@PathParam("uid") String uid) {

        //获取数据库连接
        Jedis jedis = new Jedis("node001",6379);

        //从历史用户下载表来获取最近下载
        String downloadListString = jedis.hget("rcmd_user_history",uid);
        String[] downloadList = downloadListString.split(",");
        System.out.println(uid+"downloadList:"+downloadList);

        //获取应用ID列表
        Set<String> appList = jedis.hkeys("rcmd_item_list");

        //存储总的特征分值
        Map<String,Double> scores = new HashMap();

        //去掉已经下载的应用
        for(int i=0; i < downloadList.length;i++){
            System.out.println(Integer.toString(i));
            System.out.print(downloadList[i]);
            if(appList.contains(downloadList[i])) {
                appList.remove(downloadList[i]);
            }else {
                System.out.println("not in");
            }
        }

        //分别计算所有应用的总权重
        for(String appId : appList){
            //计算关联权重
            double relativeFeatureScore = this.getRelativeFeatureScore(appId,downloadList,jedis);
            updateScoresMap(scores,appId,relativeFeatureScore);

            //计算单个权重
            double basicFeatureScore = this.getBasicFeatureScore(appId,jedis);
            updateScoresMap(scores,appId,basicFeatureScore);
        }

        //这里将map.entrySet()转成list
        List<Map.Entry<String,Double>> list = new ArrayList(scores.entrySet());
        //通过比较器
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        //打印分值
        for(Map.Entry<String,Double> mapping : list){
            System.out.println(mapping.getKey() + ":" + mapping.getValue());
        }

        //取到前10个appID返回
        List<String> result = new ArrayList();
        int count = 0 ;
        for(Map.Entry<String,Double>mapping : list){
            count++;
            result.add(mapping.getKey());
            if(count == 10){
                break;
            }
        }
        RecommendApps res = new RecommendApps(result.toString());
        return res;
    }

    private double getRelativeFeatureScore(String appId, String[] downloadList,Jedis jedis){
        double score = 0.0;
        for(String downloadAppId : downloadList){
            //Item.id*Item.id
            //构成关联特征
            String feature = "Itemid.Item.id@" + appId+ "*" + downloadAppId;
            String rcmd_features_score = jedis.hget("rcmd_features_score",feature);
            if(rcmd_features_score != null){
                score += Double.valueOf(rcmd_features_score);
            }
            String featurex = "Item.id*Item.id@" + downloadAppId + "*" + appId;
            String rcmd_features_scorex = jedis.hget("rcmd_features_score",featurex);
            if(rcmd_features_scorex!=null){
                score += Double.valueOf(rcmd_features_scorex);
            }
        }
        return score;
    }

    private double getBasicFeatureScore(String appId, Jedis jedis){
        double basicScore = 0.0;
        String[] basicFeatureNames = {"Item.id ", "Item.name ", "Item.author", "Item.sversion", "Item.ischarge"
                , "Item.dgner", "Item.font", "Item.icount", "Item.icount_dscrt", "Item.stars", "Item.price"
                , "Item.fsize", "Item.fsize_dscrt", "Item.comNum", "Item.comNum_dscrt", "Item.screen", "Item.downNum"
                , "Item.downNum_dscrt"
        };
        String rcmd_item_list = jedis.hget("rcmd_item_list",appId);
        String[] basicFeatures = rcmd_item_list.split("\t");
        for(int i=0; i < basicFeatureNames.length; i++){
            String rcmd_features_score = jedis.hget("rcmd_features_score",basicFeatureNames[i] + "@" + basicFeatures[i]);
            if(rcmd_features_score != null){
                basicScore += Double.valueOf(rcmd_features_score);
            }
        }

        return basicScore;
    }

    private void updateScoresMap(Map<String,Double> scores,String appName,double score){
        if(scores.get(appName) == null){
            scores.put(appName,score);
        }else{
            scores.put(appName, scores.get(appName)+score);

        }

    }
}
