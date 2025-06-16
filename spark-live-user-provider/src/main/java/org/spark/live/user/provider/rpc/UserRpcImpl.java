package org.spark.live.user.provider.rpc;

import org.apache.dubbo.config.annotation.DubboService;
import org.spark.live.user.interfaces.IUserRpc;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Override
    public String test() {
        System.out.println("UserRpcImpl.test");
        return "success";
    }
}
