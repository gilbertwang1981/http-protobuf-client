package com.hs.http.client.mock;

import org.apache.http.HttpResponse;

import com.google.protobuf.Message;
import com.hs.http.client.annotation.ServiceClient;
import com.hs.http.client.annotation.ServiceRpc;

@ServiceClient(service = "test-service" , desc = "测试服务")
public class HttpClientService {
	@ServiceRpc(desc = "测试方法", path = "/test/get")
    public HttpResponse get(Message request) {
        return null;
    }
}
