package com.iisysgroup.androidlite.login;

import com.dg.http.HttpRequest;
import com.dg.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bamitale @Itex on 1/5/2016.
 */
public class RestWrapper {
    protected String url;
    protected Map<String, String> params, headers;

    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private byte[] body;

    public RestWrapper(String url) {
        this.url = url;
        headers = new HashMap<>();
        params = new HashMap<>();
    }

    public void setParams(Map<String, String> params) {

        this.params.putAll(params);
    }

    public void addParam(String key, String param) {
        if (key != null)
            params.put(key, param);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void addHeader(String key, String header) {
        if (key != null)
            headers.put(key, header);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String processRequest(Request request) throws IOException, MalformedURLException {
        HttpRequest httpRequest = new HttpRequest(url, request.name());


        httpRequest.setChunkedStreamingModeSize();
        httpRequest.setConnectTimeout(1000 * 60);
        httpRequest.setReadTimeout(60 * 1000);


        if (body != null)
            httpRequest.setRequestBody(body);

        if (params != null)
            httpRequest.setParams(params);

        if (headers != null)
            httpRequest.setHeaders(headers);

        HttpResponse response = httpRequest.getResponse();
        String responseString = response.getResponseText();


        return responseString;
    }

    public enum Request {
        GET, POST, PUT
    }
}
