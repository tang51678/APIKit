/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.utils;

import burp.BurpExtender;
import burp.CookieManager;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IResponseInfo;
import burp.exceptions.ApiKitRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class RedirectUtils {
    static final int maxRedirectCount = 16;
    private IHttpService currentHttpService;
    private IHttpRequestResponse currHttpRequestResponse;
    private String currUrl;
    private int redirectCount = 0;

    public RedirectUtils(IHttpRequestResponse httpRequestResponse) {
        this.currHttpRequestResponse = httpRequestResponse;
        // 添加空值检查
        if (httpRequestResponse != null && httpRequestResponse.getHttpService() != null) {
            this.currentHttpService = httpRequestResponse.getHttpService();
        }
        // 添加空值检查
        if (httpRequestResponse != null) {
            IExtensionHelpers helpers = BurpExtender.getHelpers();
            if (helpers != null && helpers.analyzeRequest(httpRequestResponse) != null) {
                this.currUrl = helpers.analyzeRequest(httpRequestResponse).getUrl().toString();
            }
        }
    }

    public static String handleRelativeRedirectedUrl(String currentUrl, String locationUrl) {
        String result = null;
        // 添加空值检查
        if (currentUrl == null || locationUrl == null) {
            throw new ApiKitRuntimeException("URL is null");
        }
        try {
            URL currentUrlObject = new URL(currentUrl);
            int port = currentUrlObject.getPort();
            if (port == -1) {
                int n = port = "https".equals(currentUrlObject.getProtocol()) ? 443 : 80;
            }
            if (locationUrl.startsWith("/")) {
                result = currentUrlObject.getProtocol() + "://" + currentUrlObject.getHost() + ":" + port + locationUrl;
            } else {
                String currentPath = currentUrlObject.getPath();
                if (!currentPath.endsWith("/")) {
                    // 添加空值检查
                    if (currentPath != null && !currentPath.isEmpty()) {
                        // 修复：避免使用Paths.get()处理可能包含非法字符的路径
                        int lastSlashIndex = currentPath.lastIndexOf('/');
                        if (lastSlashIndex >= 0) {
                            currentPath = currentPath.substring(0, lastSlashIndex + 1);
                        } else {
                            currentPath = "/";
                        }
                    } else {
                        currentPath = "/";
                    }
                }
                // 修复：避免使用Paths.get()处理可能包含非法字符的路径
                String newPath;
                if (locationUrl.equals("./")) {
                    newPath = currentPath + "/";
                } else {
                    // 手动处理路径连接，避免使用Paths.get()
                    if (currentPath.endsWith("/")) {
                        newPath = currentPath + locationUrl;
                    } else {
                        newPath = currentPath + "/" + locationUrl;
                    }
                }
                result = currentUrlObject.getProtocol() + "://" + currentUrlObject.getHost() + ":" + port + newPath;
            }
        } catch (MalformedURLException ignored) {
            throw new ApiKitRuntimeException("URL parse error");
        }
        return result;
    }

    public static IHttpService handleAbsoluteRedirectedUrl(String locationUrl) {
        IHttpService httpService = null;
        // 添加空值检查
        if (locationUrl == null) {
            throw new ApiKitRuntimeException("URL is null");
        }
        try {
            URL tempUrl = new URL(locationUrl);
            int port = tempUrl.getPort();
            if (port == -1) {
                port = "https".equals(tempUrl.getProtocol()) ? 443 : 80;
            }
            httpService = BurpExtender.getHelpers().buildHttpService(tempUrl.getHost(), port, tempUrl.getProtocol());
        } catch (MalformedURLException ignored) {
            throw new ApiKitRuntimeException("URL parse error");
        }
        return httpService;
    }

    public static boolean isRedirectedResponse(IHttpRequestResponse httpRequestResponse) {
        // 添加更全面的空值检查，防止NullPointerException
        if (httpRequestResponse == null || httpRequestResponse.getResponse() == null) {
            return false;
        }
        if (httpRequestResponse.getResponse().length != 0) {
            IExtensionHelpers helpers = BurpExtender.getHelpers();
            // 添加空值检查
            if (helpers != null) {
                try {
                    IResponseInfo responseInfo = helpers.analyzeResponse(httpRequestResponse.getResponse());
                    if (responseInfo != null) {
                        return String.valueOf(responseInfo.getStatusCode()).startsWith("30");
                    }
                } catch (Exception e) {
                    // 忽略分析异常
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public static IHttpRequestResponse getRedirectedResponse(IHttpRequestResponse httpRequestResponse) {
        // 添加空值检查
        if (httpRequestResponse == null || httpRequestResponse.getResponse() == null) {
            return null;
        }
        
        RedirectUtils redirectUtils = new RedirectUtils(httpRequestResponse);
        IExtensionHelpers helpers = BurpExtender.getHelpers();
        IResponseInfo responseInfo = helpers.analyzeResponse(httpRequestResponse.getResponse());
        List<String> headers = responseInfo.getHeaders();
        for (String header : headers) {
            String newLocation;
            if (!header.toLowerCase().startsWith("location:")) continue;
            try {
                newLocation = header;
                if (newLocation.startsWith("http://") || newLocation.startsWith("https://")) {
                    redirectUtils.currentHttpService = RedirectUtils.handleAbsoluteRedirectedUrl(newLocation);
                } else {
                    // 添加空值检查
                    if (redirectUtils.currUrl == null) {
                        return null;
                    }
                    newLocation = RedirectUtils.handleRelativeRedirectedUrl(redirectUtils.currUrl, newLocation);
                }
                redirectUtils.currUrl = newLocation;
                // 添加空值检查
                if (newLocation == null) {
                    return null;
                }
                redirectUtils.currHttpRequestResponse = CookieManager.makeHttpRequest(httpRequestResponse, helpers.buildHttpRequest(new URL(newLocation)));
                // 添加更严格的空值检查
                if (redirectUtils.currHttpRequestResponse == null || redirectUtils.currHttpRequestResponse.getResponse() == null) {
                    return redirectUtils.currHttpRequestResponse;
                }
                responseInfo = helpers.analyzeResponse(redirectUtils.currHttpRequestResponse.getResponse());
                // 添加空값检查
                if (responseInfo == null) {
                    return redirectUtils.currHttpRequestResponse;
                }
            } catch (MalformedURLException e) {
                return redirectUtils.currHttpRequestResponse;
            }
        }
        return redirectUtils.currHttpRequestResponse;
    }

    public IHttpRequestResponse getFinalHttpRequestResponse() {
        IExtensionHelpers helpers = BurpExtender.getHelpers();
        // 添加空值检查
        if (helpers == null || this.currHttpRequestResponse == null || this.currHttpRequestResponse.getResponse() == null) {
            return null;
        }
        try {
            IResponseInfo responseInfo = helpers.analyzeResponse(this.currHttpRequestResponse.getResponse());
            // 添加空값检查
            if (responseInfo == null) {
                return null;
            }
            while (String.valueOf(responseInfo.getStatusCode()).startsWith("30")) {
                ++this.redirectCount;
                if (this.redirectCount > 16) {
                    return null;
                }
                List<String> headers = responseInfo.getHeaders();
                // 添加空값检查
                if (headers == null) {
                    return null;
                }
                List locationHeader = headers.stream().filter(header -> header != null && header.toLowerCase().startsWith("location:")).collect(Collectors.toList());
                if (locationHeader.size() > 0) {
                    String newLocation = (String) locationHeader.get(0);
                    // 添加空值检查
                    if (newLocation == null) {
                        return null;
                    }
                    if ((newLocation = newLocation.substring("location:".length()).trim()).startsWith("http://") || newLocation.startsWith("https://")) {
                        this.currentHttpService = RedirectUtils.handleAbsoluteRedirectedUrl(newLocation);
                    } else {
                        // 添加空值检查
                        if (this.currUrl == null) {
                            return null;
                        }
                        newLocation = RedirectUtils.handleRelativeRedirectedUrl(this.currUrl, newLocation);
                    }
                    this.currUrl = newLocation;
                    // 添加空值检查
                    if (newLocation == null) {
                        return null;
                    }
                    this.currHttpRequestResponse = CookieManager.makeHttpRequest(this.currHttpRequestResponse, helpers.buildHttpRequest(new URL(newLocation)));
                    // 添加空값检查
                    if (this.currHttpRequestResponse == null || this.currHttpRequestResponse.getResponse() == null) {
                        return null;
                    }
                    responseInfo = helpers.analyzeResponse(this.currHttpRequestResponse.getResponse());
                    // 添加空값检查
                    if (responseInfo == null) {
                        return null;
                    }
                    continue;
                }
                return null;
            }
            return this.currHttpRequestResponse;
        } catch (Exception e) {
            return null;
        }
    }
}