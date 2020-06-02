package com.hs.http.client.test.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hs.http.client.test.proto.Test.TestMessageRequest;
import com.hs.http.client.test.proto.Test.TestMessageResponse;

@Controller
@RequestMapping(value = "/test")
public class HttpClientTestController {
	
	@RequestMapping(value = "/get", method = RequestMethod.POST , produces = "application/x-protobuf")
    public @ResponseBody TestMessageResponse get(@RequestBody TestMessageRequest request) {
		TestMessageResponse.Builder bd = TestMessageResponse.newBuilder();
		bd.setValue(request.getKey() + ":" + UUID.randomUUID());
        
		return bd.build();
    }
}
