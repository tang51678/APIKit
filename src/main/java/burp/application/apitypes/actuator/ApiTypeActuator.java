/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes.actuator;

import burp.BurpExtender;
import burp.CookieManager;
import burp.CustomScanIssue;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IScanIssue;
import burp.application.apitypes.ApiEndpoint;
import burp.application.apitypes.ApiType;
import burp.exceptions.ApiKitRuntimeException;
import burp.utils.CommonUtils;
import burp.utils.HttpRequestResponse;
import burp.utils.UrlScanCount;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiTypeActuator
        extends ApiType {
    public static final UrlScanCount scannedUrl = new UrlScanCount();
    private final IExtensionHelpers helpers;
    private final IBurpExtenderCallbacks callbacks;
    private final IHttpRequestResponse baseRequestResponse;

    public ApiTypeActuator(IHttpRequestResponse baseRequestResponse, Boolean isPassive) {
        this.setApiTypeName("SpringbootActuator");
        this.callbacks = BurpExtender.getCallbacks();
        this.helpers = BurpExtender.getHelpers();
        this.baseRequestResponse = baseRequestResponse;
        this.isPassive = isPassive;
    }

    public static ApiType newInstance(IHttpRequestResponse requestResponse, Boolean isPassive) {
        return new ApiTypeActuator(requestResponse, isPassive);
    }

    @Override
    public Boolean isFingerprintMatch() {
        URL url = this.helpers.analyzeRequest(this.baseRequestResponse).getUrl();
        ArrayList<String> urlList = new ArrayList<String>();
        Set<String> UrlPathList = CommonUtils.getUrlPathListWithCrossDir(url);
        Iterator<String> iterator = UrlPathList.iterator();
        while (iterator.hasNext()) {
            String tmpurl = iterator.next().toString();
            if (scannedUrl.get(tmpurl) > 0 && this.isPassive.booleanValue()) continue;
            urlList.add(tmpurl);
            if (!this.isPassive.booleanValue()) continue;
            scannedUrl.add(tmpurl);
        }
        for (String urlPath : urlList) {
            Boolean result = this.urlAddPath(urlPath + "/actuator");
            if (result != null && !result.booleanValue()) {
                this.urlAddPath(urlPath + "/%61%63%74%75%61%74%6f%72");
                this.urlAddPath(urlPath + "/actuator.json");
                this.urlAddPath(urlPath + "/actuator;.js");
            }
            if ((result = this.urlAddPath(urlPath + "/mappings")) == null || result.booleanValue()) continue;
            this.urlAddPath(urlPath + "/%6d%61%70%70%69%6e%67%73");
            this.urlAddPath(urlPath + "/mappings.json");
            this.urlAddPath(urlPath + "/mappings;.js");
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
        IHttpRequestResponse newHttpRequestResponse = CookieManager.makeHttpRequest(this.baseRequestResponse, newRequest);
        // 添加空值检查，防止NullPointerException
        if (newHttpRequestResponse == null || newHttpRequestResponse.getResponse() == null) {
            return false;
        }
        
        String urlPath = this.helpers.analyzeRequest(newHttpRequestResponse).getUrl().toString();
        if (this.helpers.analyzeResponse(newHttpRequestResponse.getResponse()).getStatusCode() == 200) {
            if (this.isPassive.booleanValue()) {
                if (scannedUrl.get(this.helpers.analyzeRequest(newHttpRequestResponse).getUrl().toString()) <= 0) {
                    scannedUrl.add(this.helpers.analyzeRequest(newHttpRequestResponse).getUrl().toString());
                } else {
                    return false;
                }
            }
            String resp = new String(CommonUtils.getHttpResponseBody(newHttpRequestResponse.getResponse()));
            if (resp == null || resp.trim().isEmpty()) {
                return false;
            }
            try {
                // 使用更健壮的 JSON 解析方式，设置 lenient 模式以处理格式不严格的 JSON
                JsonReader reader = new JsonReader(new StringReader(resp));
                reader.setLenient(true);
                JsonElement element = JsonParser.parseReader(reader);
                if (element == null || !element.isJsonObject()) {
                    return false;
                }
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject == null) {
                    return false;
                }
                
                boolean hasLinks = jsonObject.has("_links") && jsonObject.get("_links").isJsonObject();
                boolean hasFavicon = jsonObject.has("/**/favicon.ico");
                boolean hasContexts = jsonObject.has("contexts");
                
                if (!hasLinks && !hasFavicon && !hasContexts) {
                    return false;
                }

                if (hasLinks) {
                    JsonObject links = jsonObject.getAsJsonObject("_links");
                    if (links.has("mappings")) {
                        this.urlAddPath(urlPath + "/mappings");
                    }
                }

                ((HashMap) this.getApiDocuments()).put(apiDocumentUrl, newHttpRequestResponse);
                return true;
            } catch (Exception e) {
                BurpExtender.getStderr().println("Error parsing Actuator JSON response: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public ArrayList<ApiEndpoint> parseApiDocument(IHttpRequestResponse apiDocument, IHttpRequestResponse basePath, boolean isTargetScan) {
        ArrayList<ApiEndpoint> results;
        block26:
        {
            String urlRootPath = CommonUtils.getUrlRootPath(this.helpers.analyzeRequest(this.baseRequestResponse).getUrl());
            String BaseURLPath = "";
            if (basePath != null) {
                BaseURLPath = CommonUtils.getUrlWithoutFilename(this.helpers.analyzeRequest(basePath).getUrl());
            }
            String UrlWithoutFilename = CommonUtils.getUrlWithoutFilename(this.helpers.analyzeRequest(apiDocument).getUrl());
            results = new ArrayList<ApiEndpoint>();
            Pattern pattern = Pattern.compile("(\\[[^\\]]*\\])");
            Pattern patternpath = Pattern.compile("\"\\/(\\w+\\/?)+\"");
            byte[] newRequest = null;
            try {
                String body = new String(CommonUtils.getHttpResponseBody(apiDocument.getResponse()));
                JsonElement element = JsonParser.parseString(body);
                if (!element.isJsonObject()) break block26;
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.get("_links") != null) {
                    JsonObject links = jsonObject.get("_links").getAsJsonObject();
                    Set<Map.Entry<String, JsonElement>> endpoints = links.entrySet();
                    for (Map.Entry<String, JsonElement> endpoint : endpoints) {
                        String href = endpoint.getValue().getAsJsonObject().get("href").getAsString();
                        URL hrefUrl = new URL(href);
                        href = basePath == null ? CommonUtils.combineURLs(UrlWithoutFilename, CommonUtils.getUrlPath(hrefUrl)) : CommonUtils.combineURLs(BaseURLPath, CommonUtils.getUrlPath(hrefUrl));
                        String uri = hrefUrl.getPath();
                        if (href.contains("{") || href.contains("}")) continue;
                        if (isTargetScan && !this.isPassive) {
                            String bypassSuffix = BurpExtender.TargetAPI.get("BypassSuffix");
                            if (bypassSuffix != null && !bypassSuffix.isEmpty()) {
                                int queryIndex = uri.indexOf('?');
                                if (queryIndex != -1) {
                                    uri = uri.substring(0, queryIndex) + bypassSuffix + uri.substring(queryIndex);
                                } else {
                                    uri = uri + bypassSuffix;
                                }
                                queryIndex = href.indexOf('?');
                                if (queryIndex != -1) {
                                    href = href.substring(0, queryIndex) + bypassSuffix + href.substring(queryIndex);
                                } else {
                                    href = href + bypassSuffix;
                                }
                            }
                        }
                        try {
                            newRequest = this.helpers.buildHttpRequest(new URL(href));
                            newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
                            HttpRequestResponse tempRequestResponse = new HttpRequestResponse();
                            if (basePath == null) {
                                tempRequestResponse.setHttpService(apiDocument.getHttpService());
                            } else {
                                tempRequestResponse.setHttpService(basePath.getHttpService());
                            }
                            tempRequestResponse.setRequest(newRequest);
                            tempRequestResponse.sendRequest();
                            results.add(new ApiEndpoint(uri, tempRequestResponse));
                        } catch (MalformedURLException exception) {
                            throw new ApiKitRuntimeException(exception);
                        }
                    }
                    break block26;
                }
                if (jsonObject.get("/**/favicon.ico") != null) {
                    try {
                        Set<Map.Entry<String, JsonElement>> endpoints = jsonObject.entrySet();
                        for (Map.Entry<String, JsonElement> endpoint : endpoints) {
                            String href = "";
                            String path = "";
                            try {
                                Matcher matcher = pattern.matcher(endpoint.getKey());
                                if (!matcher.find()) continue;
                                path = matcher.group().split(" ")[0];
                                path = path.endsWith("]") ? path.substring(1, path.length() - 1) : path.substring(1);
                                href = basePath == null ? CommonUtils.combineURLs(UrlWithoutFilename.substring(0, UrlWithoutFilename.length() - 1), path) : CommonUtils.combineURLs(BaseURLPath.substring(0, BaseURLPath.length() - 1), path);
                                if (href.contains("{") || href.contains("}")) continue;
                                while (href.endsWith("*")) {
                                    href = href.substring(0, href.length() - 1);
                                }
                                if (isTargetScan && !this.isPassive) {
                                    String bypassSuffix = BurpExtender.TargetAPI.get("BypassSuffix");
                                    if (bypassSuffix != null && !bypassSuffix.isEmpty()) {
                                        int queryIndex = path.indexOf('?');
                                        if (queryIndex != -1) {
                                            path = path.substring(0, queryIndex) + bypassSuffix + path.substring(queryIndex);
                                        } else {
                                            path = path + bypassSuffix;
                                        }
                                        queryIndex = href.indexOf('?');
                                        if (queryIndex != -1) {
                                            href = href.substring(0, queryIndex) + bypassSuffix + href.substring(queryIndex);
                                        } else {
                                            href = href + bypassSuffix;
                                        }
                                    }
                                }
                                try {
                                    newRequest = this.helpers.buildHttpRequest(new URL(href));
                                    newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
                                    HttpRequestResponse tempRequestResponse = new HttpRequestResponse();
                                    if (basePath == null) {
                                        tempRequestResponse.setHttpService(apiDocument.getHttpService());
                                    } else {
                                        tempRequestResponse.setHttpService(basePath.getHttpService());
                                    }
                                    tempRequestResponse.setRequest(newRequest);
                                    tempRequestResponse.sendRequest();
                                    results.add(new ApiEndpoint(path, tempRequestResponse));
                                } catch (MalformedURLException exception) {
                                    throw new ApiKitRuntimeException(exception);
                                }
                            } catch (Exception exception) {
                            }
                        }
                        break block26;
                    } catch (Exception ee) {
                        BurpExtender.getStderr().println(CommonUtils.exceptionToString(ee));
                        break block26;
                    }
                }
                if (jsonObject.get("contexts") != null) {
                    Matcher matcher = patternpath.matcher(body);
                    String href = "";
                    String uri = "";
                    while (matcher.find()) {
                        String path = matcher.group(0).replace("\"", "");
                        if (path.endsWith("/")) continue;
                        href = basePath == null ? CommonUtils.combineURLs(UrlWithoutFilename.substring(0, UrlWithoutFilename.length() - 10), path) : CommonUtils.combineURLs(BaseURLPath, path);
                        uri = path;
                        if (isTargetScan && !this.isPassive) {
                            String bypassSuffix = BurpExtender.TargetAPI.get("BypassSuffix");
                            if (bypassSuffix != null && !bypassSuffix.isEmpty()) {
                                int queryIndex = uri.indexOf('?');
                                if (queryIndex != -1) {
                                    uri = uri.substring(0, queryIndex) + bypassSuffix + uri.substring(queryIndex);
                                } else {
                                    uri = uri + bypassSuffix;
                                }
                                queryIndex = href.indexOf('?');
                                if (queryIndex != -1) {
                                    href = href.substring(0, queryIndex) + bypassSuffix + href.substring(queryIndex);
                                } else {
                                    href = href + bypassSuffix;
                                }
                            }
                        }
                        try {
                            newRequest = this.helpers.buildHttpRequest(new URL(href));
                            newRequest = basePath == null ? CookieManager.getRequest(apiDocument, newRequest) : CookieManager.getRequest(basePath, newRequest);
                            HttpRequestResponse tempRequestResponse = new HttpRequestResponse();
                            if (basePath == null) {
                                tempRequestResponse.setHttpService(apiDocument.getHttpService());
                            } else {
                                tempRequestResponse.setHttpService(basePath.getHttpService());
                            }
                            tempRequestResponse.setRequest(newRequest);
                            tempRequestResponse.sendRequest();
                            results.add(new ApiEndpoint(uri, tempRequestResponse));
                        } catch (MalformedURLException exception) {
                            throw new ApiKitRuntimeException(exception);
                        }
                    }
                }
            } catch (Exception e) {
                BurpExtender.getStderr().println(CommonUtils.exceptionToString(e));
            }
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
    
    public void clearScanState() {
        scannedUrl.clear();
    }
}