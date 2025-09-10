/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes.graphql;

import burp.BurpExtender;
import burp.CookieManager;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IScanIssue;
import burp.application.apitypes.ApiEndpoint;
import burp.application.apitypes.ApiType;
import burp.application.apitypes.graphql.GraphQLIntrospectionParser;
import burp.application.apitypes.graphql.GraphQLParseResult;
import burp.exceptions.ApiKitRuntimeException;
import burp.utils.CommonUtils;
import burp.utils.Constants;
import burp.utils.HttpRequestFormator;
import burp.utils.HttpRequestResponse;
import burp.utils.UrlScanCount;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApiTypeGraphQL
        extends ApiType {
    public static final UrlScanCount scannedUrl = new UrlScanCount();
    private final IExtensionHelpers helpers;
    private final IBurpExtenderCallbacks callbacks;
    private final IHttpRequestResponse baseRequestResponse;
    private final String graphQLIntrospectionRequestJSON = "cXVlcnkgUXVlcnkgewogICAgX19zY2hlbWEgewogICAgICBxdWVyeVR5cGUgeyBuYW1lIH0KICAgICAgbXV0YXRpb25UeXBlIHsgbmFtZSB9CiAgICAgIHN1YnNjcmlwdGlvblR5cGUgeyBuYW1lIH0KICAgICAgdHlwZXMgewogICAgICAgIC4uLkZ1bGxUeXBlCiAgICAgIH0KICAgICAgZGlyZWN0aXZlcyB7CiAgICAgICAgbmFtZQogICAgICAgIGRlc2NyaXB0aW9uCiAgICAgICAgbG9jYXRpb24sCiAgICAgICAgYXJncyB7CiAgICAgICAgICAuLi5JbnB1dFZhbHVlCiAgICAgICAgfQogICAgICB9CiAgICB9CiAgfQoKICBmcmFnbWVudCBGdWxsVHlwZSBvbiBfX1R5cGUgewogICAga2luZAogICAgbmFtZQogICAgZGVzY3JpcHRpb24KICAgIGZpZWxkcyhpbmNsdWRlRGVwcmVjYXRlZDogdHJ1ZSkgewogICAgICBuYW1lCiAgICAgIGRlc2NyaXB0aW9uCiAgICAgIGFyZ3MgewogICAgICAgIC4uLklucHV0VmFsdWUKICAgICAgfQogICAgICB0eXBlIHsKICAgICAgICAuLi5UeXBlUmVmCiAgICAgIH0KICAgICAgaXNEZXByZWNhdGVkCiAgICAgIGRlcHJlY2F0aW9uUmVhc29uCiAgICB9CiAgICBpbnB1dEZpZWxkcyB7CiAgICAgIC4uLklucHV0VmFsdWUKICAgIH0KICAgIGludGVyZmFjZXMgewogICAgICAuLi5UeXBlUmVmCiAgICB9CiAgICBlbnVtVmFsdWVzKGluY2x1ZGVEZXByZWNhdGVkOiB0cnVlKSB7CiAgICAgIG5hbWUKICAgICAgZGVzY3JpcHRpb24KICAgICAgaXNEZXByZWNhdGVkCiAgICAgIGRlcHJlY2F0aW9uUmVhc29uCiAgICB9CiAgICBwb3NzaWJsZVR5cGVzIHsKICAgICAgLi4uVHlwZVJlZgogICAgfQogIH0KCiAgZnJhZ21lbnQgSW5wdXRWYWx1ZSBvbiBfX0lucHV0VmFsdWUgewogICAgbmFtZQogICAgZGVzY3JpcHRpb24KICAgIHR5cGUgeyAuLi5UeXBlUmVmIH0KICAgIGRlZmF1bHRWYWx1ZQogIH0KCiAgZnJhZ21lbnQgVHlwZVJlZiBvbiBfX1R5cGUgewogICAga2luZAogICAgbmFtZQogICAgb2ZUeXBlIHsKICAgICAga2luZAogICAgICBuYW1lCiAgICAgIG9mVHlwZSB7CiAgICAgICAga2luZAogICAgICAgIG5hbWUKICAgICAgICBvZlR5cGUgewogICAgICAgICAga2luZAogICAgICAgICAgbmFtZQogICAgICAgICAgb2ZUeXBlIHsKICAgICAgICAgICAga2luZAogICAgICAgICAgICBuYW1lCiAgICAgICAgICAgIG9mVHlwZSB7CiAgICAgICAgICAgICAga2luZAogICAgICAgICAgICAgIG5hbWUKICAgICAgICAgICAgICBvZlR5cGUgewogICAgICAgICAgICAgICAga2luZAogICAgICAgICAgICAgICAgbmFtZQogICAgICAgICAgICAgICAgb2ZUeXBlIHsKICAgICAgICAgICAgICAgICAga2luZAogICAgICAgICAgICAgICAgICBuYW1lCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgfQogICAgICAgICAgICB9CiAgICAgICAgICB9CiAgICAgICAgfQogICAgICB9CiAgICB9CiAgfQ==";

    public ApiTypeGraphQL(IHttpRequestResponse baseRequestResponse, Boolean isPassive) {
        this.setApiTypeName("GraphQLIntrospection");
        this.callbacks = BurpExtender.getCallbacks();
        this.helpers = BurpExtender.getHelpers();
        this.baseRequestResponse = baseRequestResponse;
        this.isPassive = isPassive;
    }

    public static ApiType newInstance(IHttpRequestResponse requestResponse, Boolean isPassive) {
        return new ApiTypeGraphQL(requestResponse, isPassive);
    }

    @Override
    public Boolean isFingerprintMatch() {
        URL url = this.helpers.analyzeRequest(this.baseRequestResponse).getUrl();
        ArrayList<String> urlList = new ArrayList<String>();
        Set<String> UrlPathList = CommonUtils.getUrlPathList(url);
        Iterator<String> iterator = UrlPathList.iterator();
        while (iterator.hasNext()) {
            String tmpurl = iterator.next().toString();
            if (scannedUrl.get(tmpurl) > 0 && this.isPassive.booleanValue()) continue;
            urlList.add(tmpurl);
            if (!this.isPassive.booleanValue()) continue;
            scannedUrl.add(tmpurl);
        }
        for (String urlPath : urlList) {
            this.urlAddPath(urlPath + "/graphql");
            this.urlAddPath(urlPath + "/%67%72%61%70%68%71%6c");
            this.urlAddPath(urlPath + "/graphql.php");
            this.urlAddPath(urlPath + "/graphiql");
            this.urlAddPath(urlPath + "/%67%72%61%70%68%69%71%6c");
            this.urlAddPath(urlPath + "/graphiql.php");
        }
        return ((HashMap) this.getApiDocuments()).size() != 0;
    }

    @Override
    public Boolean urlAddPath(String apiDocumentUrl) {
        IHttpService httpService = this.baseRequestResponse.getHttpService();
        byte[] newRequest = null;
        
        // 修复：处理相对路径URL，避免MalformedURLException异常
        if (apiDocumentUrl.startsWith("/")) {
            // 如果是相对路径，基于当前请求的URL构建完整URL
            String urlorgin = this.helpers.analyzeRequest(this.baseRequestResponse).getUrl().toString();
            try {
                URL base = new URL(urlorgin);
                String protocol = base.getProtocol();
                String host = base.getHost();
                int port = base.getPort();
                String portStr = (port != -1) ? ":" + port : "";
                apiDocumentUrl = protocol + "://" + host + portStr + apiDocumentUrl;
            } catch (MalformedURLException e) {
                // 如果构建完整URL失败，记录错误并返回false
                BurpExtender.getStderr().println("Failed to construct full URL from relative path: " + apiDocumentUrl);
                return false;
            }
        }
        
        try {
            newRequest = this.helpers.buildHttpRequest(new URL(apiDocumentUrl));
        } catch (MalformedURLException exception) {
            // 修复：捕获异常但不抛出，改为记录日志并返回false
            BurpExtender.getStderr().println("Invalid URL: " + apiDocumentUrl + ", Error: " + exception.getMessage());
            return false;
        }
        List<String> headers = this.helpers.analyzeRequest(newRequest).getHeaders();
        List<String> httpFirstLine = Arrays.asList(headers.get(0).split(" "));
        httpFirstLine.set(0, "POST");
        headers.set(0, String.join((CharSequence) " ", httpFirstLine));
        headers.add("Content-Type: application/json");
        JsonObject jsonBody = new JsonObject();
        jsonBody.add("query", new JsonPrimitive(new String(this.helpers.base64Decode(this.graphQLIntrospectionRequestJSON))));
        newRequest = this.helpers.buildHttpMessage(headers, new Gson().toJson(jsonBody).getBytes());
        IHttpRequestResponse newHttpRequestResponse = CookieManager.makeHttpRequest(this.baseRequestResponse, newRequest);
        // 添加空值检查，防止NullPointerException
        if (newHttpRequestResponse == null || newHttpRequestResponse.getResponse() == null) {
            return false;
        }
        
        if (this.helpers.analyzeResponse(newHttpRequestResponse.getResponse()).getStatusCode() == 200) {
            if (this.isPassive.booleanValue()) {
                if (scannedUrl.get(this.helpers.analyzeRequest(newHttpRequestResponse).getUrl().toString()) <= 0) {
                    scannedUrl.add(this.helpers.analyzeRequest(newHttpRequestResponse).getUrl().toString());
                } else {
                    return false;
                }
            }
            String responseJSON = new String(CommonUtils.getHttpResponseBody(newHttpRequestResponse.getResponse()));
            try {
                JsonObject jsonObject;
                JsonElement element = JsonParser.parseString(responseJSON);
                // 添加更严格的空值检查
                if (element.isJsonObject() 
                    && (jsonObject = element.getAsJsonObject()).has("data") 
                    && jsonObject.get("data") != null 
                    && !jsonObject.get("data").isJsonNull() 
                    && jsonObject.get("data").getAsJsonObject().has("__schema")
                    && jsonObject.get("data").getAsJsonObject().get("__schema") != null
                    && !jsonObject.get("data").getAsJsonObject().get("__schema").isJsonNull()) {
                    ((HashMap) this.getApiDocuments()).put(apiDocumentUrl, newHttpRequestResponse);
                    return true;
                }
            } catch (Exception ignored) {
                // 处理畸形JSON异常
                System.out.println(ignored.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public ArrayList<ApiEndpoint> parseApiDocument(IHttpRequestResponse apiDocument, IHttpRequestResponse basePath, boolean isTargetScan) {
        HttpRequestResponse tempRequestResponse;
        JsonObject jsonBody;
        List<String> httpFirstLine;
        List<String> headers;
        byte[] newRequest;
        URL url;
        String responseJSON = new String(CommonUtils.getHttpResponseBody(apiDocument.getResponse()));
        GraphQLParseResult parseResult = new GraphQLIntrospectionParser().parseIntrospection(responseJSON);
        ArrayList<ApiEndpoint> results = new ArrayList<ApiEndpoint>();
        Set<Map.Entry<String, String>> endpoints = parseResult.queryParseResult.entrySet();
        for (Map.Entry<String, String> endpoint : endpoints) {
            url = basePath == null ? this.helpers.analyzeRequest(apiDocument).getUrl() : this.helpers.analyzeRequest(basePath).getUrl();
            newRequest = null;
            try {
                newRequest = this.helpers.buildHttpRequest(new URL(url.toString()));
            } catch (MalformedURLException exception) {
                throw new ApiKitRuntimeException(exception);
            }
            newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
            headers = this.helpers.analyzeRequest(newRequest).getHeaders();
            httpFirstLine = Arrays.asList(headers.get(0).split(" "));
            httpFirstLine.set(0, "POST");
            headers.set(0, String.join((CharSequence) " ", httpFirstLine));
            headers.add("Content-Type: application/json");
            jsonBody = new JsonObject();
            jsonBody.add("query", new JsonPrimitive("query" + Constants.GRAPHQL_SPACE + endpoint.getKey() + Constants.GRAPHQL_SPACE + "{" + Constants.GRAPHQL_NEW_LINE + endpoint.getValue() + Constants.GRAPHQL_NEW_LINE + "}"));
            if (isTargetScan) {
                HttpRequestFormator.TrimDupHeader(headers);
            }
            newRequest = this.helpers.buildHttpMessage(headers, new Gson().toJson(jsonBody).getBytes());
            newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
            tempRequestResponse = new HttpRequestResponse();
            if (basePath == null) {
                tempRequestResponse.setHttpService(apiDocument.getHttpService());
            } else {
                tempRequestResponse.setHttpService(basePath.getHttpService());
            }
            tempRequestResponse.setRequest(newRequest);
            tempRequestResponse.sendRequest();
            results.add(new ApiEndpoint("query:" + Constants.GRAPHQL_SPACE + endpoint.getKey(), tempRequestResponse));
        }
        endpoints = parseResult.mutationParseResult.entrySet();
        for (Map.Entry<String, String> endpoint : endpoints) {
            url = basePath == null ? this.helpers.analyzeRequest(apiDocument).getUrl() : this.helpers.analyzeRequest(basePath).getUrl();
            newRequest = null;
            try {
                newRequest = this.helpers.buildHttpRequest(new URL(url.toString()));
            } catch (MalformedURLException exception) {
                throw new ApiKitRuntimeException(exception);
            }
            newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
            headers = this.helpers.analyzeRequest(newRequest).getHeaders();
            httpFirstLine = Arrays.asList(headers.get(0).split(" "));
            httpFirstLine.set(0, "POST");
            headers.set(0, String.join((CharSequence) " ", httpFirstLine));
            headers.add("Content-Type: application/json");
            jsonBody = new JsonObject();
            jsonBody.add("query", new JsonPrimitive("mutation" + Constants.GRAPHQL_SPACE + endpoint.getKey() + Constants.GRAPHQL_SPACE + "{" + Constants.GRAPHQL_NEW_LINE + endpoint.getValue() + Constants.GRAPHQL_NEW_LINE + "}"));
            if (isTargetScan) {
                HttpRequestFormator.TrimDupHeader(headers);
            }
            newRequest = this.helpers.buildHttpMessage(headers, new Gson().toJson(jsonBody).getBytes());
            newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
            tempRequestResponse = new HttpRequestResponse();
            if (basePath == null) {
                tempRequestResponse.setHttpService(apiDocument.getHttpService());
            } else {
                tempRequestResponse.setHttpService(basePath.getHttpService());
            }
            tempRequestResponse.setRequest(newRequest);
            tempRequestResponse.sendRequest();
            results.add(new ApiEndpoint("mutation:" + Constants.GRAPHQL_SPACE + endpoint.getKey(), tempRequestResponse));
        }
        return results;
    }

    @Override
    public List<IScanIssue> exportIssues() {
        return new ArrayList<IScanIssue>();
    }

    @Override
    public String exportConsole() {
        return "";
    }
}

