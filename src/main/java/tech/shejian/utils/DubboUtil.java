/*
 * @Description: Dubbo 泛化测试配置
 * @Date: 2021/4/20
 */
package tech.shejian.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.List;
import java.util.Map;

public class DubboUtil {
    private ConfigUtil configUtil = ConfigUtil.builder();
    private ApplicationConfig applicationConfig;
    Map<String, String> attachment;
    private String interfaceName;
    private String method;
    private List<String> argumentKey;
    private List<String> value;

    private String address;
    private String protocol ;
    private Integer timeout ;
    private String registryGroup ;
    private String applicationName ;

    // reference
    private String version ;
    private String loadBalance;
    private String group ;
    private String rpcProtocol ;
    /***
     *
     * @param configFilePath 配置文件
     * @param interfaceName 接口名称
     * @param method 接口方法
     * @param argumentKey 接口参数名
     * @param value 接口所需要传递的值
     * @param attachment 附加值，如多租户的接口，需要加上 key
     */
    public DubboUtil(String interfaceName,
                       String method,
                       List<String> argumentKey,
                       List<String> value,
                       Map<String, String> attachment) {
        this.configUtil.read("dubbo.properties");
        this.applicationConfig = new ApplicationConfig();
        this.interfaceName = interfaceName;
        this.argumentKey = argumentKey;
        this.value = value;
        this.method = method;
        this.attachment = attachment;
        // registry
        this.address = this.configUtil.get("zookeeper.address");
        this.protocol = this.configUtil.get("registry.protocol");
        this.timeout = Integer.valueOf(this.configUtil.get("registry.timeout"));
        this.registryGroup = this.configUtil.get("registry.group");
        this.applicationName = this.configUtil.get("application.name");

        // reference
        this.version = this.configUtil.get("reference.version");
        this.loadBalance = this.configUtil.get("reference.loadBalance");
        this.group = this.configUtil.get("reference.group");
        this.rpcProtocol = this.configUtil.get("reference.rpc.protocol");
    }

    public String run(){
        this.applicationConfig.setName(applicationName);
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(address);
        registryConfig.setProtocol(protocol);
        registryConfig.setGroup(registryGroup);
        registryConfig.setTimeout(timeout);

        //  消费者
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setProtocol(rpcProtocol);
        referenceConfig.setVersion(version);
        referenceConfig.setTimeout(timeout);
        referenceConfig.setAsync(false);
        referenceConfig.setLoadbalance(loadBalance);
        referenceConfig.setGroup(group);
        // 接口信息

        referenceConfig.setInterface(interfaceName);
        if(attachment != null) {
            RpcContext.getContext().setAttachments(attachment);
        }

        // 声明为泛化接口
        referenceConfig.setGeneric("true");
        ReferenceConfigCache cache = ReferenceConfigCache.getCache(address);
        GenericService genericService = (GenericService) cache.get(referenceConfig);

        String[] arumentName = new String[this.argumentKey.size()];
        Object[] val = new Object[this.value.size()];

        for(int i = 0; i <  argumentKey.size(); i++) {
            arumentName[i] = this.argumentKey.get(i);
        }

        for (int j = 0; j < this.value.size(); j++){
            String str = this.value.get(j);
            // 如果是 JSON，那么是复杂对象, 否则就是基础对象
            if(str.contains("{")){
                Map<String, Object> o = (Map<String,Object>) JSONObject.parseObject(str);
                val[j] = o;
            }else{
                val[j] = this.value.get(j);
            }
        }
        Object o = genericService.$invoke(
                this.method,
                arumentName,
                val
        );
        String result = JSONObject.toJSONString(o);
        return result;
    }
}
