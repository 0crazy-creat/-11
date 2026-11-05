package com.csp.platform.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 阿里云短信通道（dysmsapi 2017-05-25，手写 HMAC-SHA1 签名，无额外 SDK 依赖）。
 *
 * <p>
 * 配置示例：
 * <pre>
 * csp:
 *   notify:
 *     sms-enabled: true
 *     sms-provider: aliyun
 *     aliyun:
 *       access-key-id: ${CSP_SMS_ALIYUN_AK:}
 *       access-key-secret: ${CSP_SMS_ALIYUN_SK:}
 *       sign-name: 某某平台
 *       template-code: SMS_123456789
 *       template-param: code
 * </pre>
 */
@Slf4j
@Component
public class AliyunSmsSender implements SmsSender {

    private static final String VERSION = "2017-05-25";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final NotifyProperties props;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public AliyunSmsSender(NotifyProperties props) {
        this.props = props;
    }

    @Override
    public String provider() {
        return "aliyun";
    }

    @Override
    public void sendCode(String phone, String code) throws Exception {
        NotifyProperties.Aliyun c = props.getAliyun();
        if (c.getTemplateCode() == null || c.getTemplateCode().isBlank()) {
            throw new IllegalStateException("阿里云短信未配置 template-code");
        }
        String paramJson = "{\"" + c.getTemplateParam() + "\":\"" + escape(code) + "\"}";
        doSend(phone, c.getTemplateCode(), paramJson);
    }

    @Override
    public void sendText(String phone, String text) throws Exception {
        NotifyProperties.Aliyun c = props.getAliyun();
        if (c.getNoticeTemplateCode() == null || c.getNoticeTemplateCode().isBlank()) {
            throw new IllegalStateException("阿里云短信未配置 notice-template-code，无法发送自由文本");
        }
        String paramJson = "{\"" + c.getNoticeTemplateParam() + "\":\"" + escape(text) + "\"}";
        doSend(phone, c.getNoticeTemplateCode(), paramJson);
    }

    private void doSend(String phone, String templateCode, String templateParamJson) throws Exception {
        NotifyProperties.Aliyun c = props.getAliyun();
        if (isBlank(c.getAccessKeyId()) || isBlank(c.getAccessKeySecret())) {
            throw new IllegalStateException("阿里云短信缺少 access-key-id/access-key-secret");
        }
        if (isBlank(c.getSignName())) {
            throw new IllegalStateException("阿里云短信缺少 sign-name");
        }

        Map<String, String> p = new TreeMap<>();
        p.put("AccessKeyId", c.getAccessKeyId());
        p.put("Action", "SendSms");
        p.put("Format", "JSON");
        p.put("PhoneNumbers", phone);
        p.put("RegionId", "cn-hangzhou");
        p.put("SignName", c.getSignName());
        p.put("SignatureMethod", "HMAC-SHA1");
        p.put("SignatureNonce", UUID.randomUUID().toString());
        p.put("SignatureVersion", "1.0");
        p.put("TemplateCode", templateCode);
        p.put("TemplateParam", templateParamJson);
        p.put("Timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(TS));
        p.put("Version", VERSION);

        String canonical = buildCanonical(p);
        String stringToSign = "GET&%2F&" + percentEncode(canonical);
        String signature = Base64.getEncoder().encodeToString(
                hmacSha1((c.getAccessKeySecret() + "&").getBytes(StandardCharsets.UTF_8),
                        stringToSign.getBytes(StandardCharsets.UTF_8)));

        String url = "https://" + c.getEndpoint() + "/?Signature=" + percentEncode(signature)
                + "&" + canonical;

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8)).GET().build();
        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = resp.body();
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IllegalStateException("阿里云短信 HTTP " + resp.statusCode() + ": " + body);
        }
        if (body != null && !body.contains("\"Code\":\"OK\"")) {
            throw new IllegalStateException("阿里云短信返回失败: " + body);
        }
        log.info("[SMS-ALIYUN] 已下发 phone={} template={}", mask(phone), templateCode);
    }

    private String buildCanonical(Map<String, String> p) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : p.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(percentEncode(e.getKey())).append('=').append(percentEncode(e.getValue()));
        }
        return sb.toString();
    }

    /** RFC3986 编码：空格 -> %20，* -> %2A，%7E -> ~，其余同 URLEncoder。 */
    private String percentEncode(String v) {
        try {
            return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8.name())
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] hmacSha1(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        return mac.doFinal(data);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String mask(String phone) {
        return phone != null && phone.length() >= 7
                ? phone.substring(0, 3) + "****" + phone.substring(7) : "***";
    }
}