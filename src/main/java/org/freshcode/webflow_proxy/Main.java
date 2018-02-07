package org.freshcode.webflow_proxy;

import io.netty.handler.codec.http.*;
import io.netty.util.*;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.extras.*;
import org.littleshoot.proxy.impl.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting proxy server on port 8080 ... ");
        DefaultHttpProxyServer.bootstrap()
                .withPort(8080)
                .withManInTheMiddle(new SelfSignedMitmManager())
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest) {
                        return new HttpFiltersAdapter(originalRequest) {
                            @Override
                            public HttpObject proxyToClientResponse(
                                    HttpObject httpObject) {
                                if (httpObject instanceof FullHttpResponse) {

                                    FullHttpResponse resp = (FullHttpResponse) httpObject;

                                    String response = resp.content().toString(CharsetUtil.UTF_8);
                                    if(originalRequest.getUri().contains("/freshcode-post")) {
                                        System.out.println("REPLACING INVALID HTML IN " +originalRequest.getUri());
                                        response = response.replaceAll("&quot;", "'");
                                        resp.content().clear();
                                        byte[] respB = response.getBytes(CharsetUtil.UTF_8);
                                        resp.content().writeBytes(respB);
                                        if(resp.headers().contains("Content-Length")){
                                            resp.headers().set("Content-Length", respB.length);
                                        }
                                    }
                                }
                                return httpObject;
                            }
                        };
                    }

                    @Override
                    public int getMaximumResponseBufferSizeInBytes() {
                        //      MB *   kB *    B
                        return 196 * 1024 * 1024;
                    }
                })
                .start();
    }
}