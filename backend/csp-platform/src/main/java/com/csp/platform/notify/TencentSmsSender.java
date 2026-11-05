package com.csp.platform.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

/**
 * 腾讯云短信通道（sms 2021-01-11，手写 TC3-HMAC-SHA256 签名，无额外 SDK 依赖）。
 *
 * <p>
 * 配置示例：
 * <pre>
 * csp:
 *   notify:
 *     sms-enabled: true
 *     sms-provider: tencent
 *     tencent:
 *       secret-id: ${CSP_SMS_TENCENT_ID:}
 *       secret-key: ${CSP_SMS_TENCENT_KEY:}
 *       sdk-app-id: 1400000000
 *       sign-name: 某某平台
 *       template-id: 1234567
 *       template-param-count: 1
 * </pre>
 */
@Slf4j
@Component
public class TencentSmsSender implements SmsSender {

    private static final String HOST = "sms.tencentcloudapi.com";
    private static final String SERVICE = "sms";
    private static final String ACTION = "SendSms";
    private static final String VERSION = "2021-01-11";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneOffset.UTC);

    private final NotifyProperties props;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public TencentSmsSender(NotifyProperties props) {
        this.props = props;
    }

    @Override
    public String provider() {
        return "tencent";
    }

    @Override
    public void sendCode(String phone, String code) throws Exception {
        NotifyProperties.Tencent c = props.getTencent();
        if (isBlank(c.getTemplateId())) {
            throw new IllegalStateException("腾讯云短信未配置 template-id");
        }
        String[] params = new String[Math.max(1, c.getTemplateParamCount())];
        params[0] = code;
        for (int i = 1; i < params.length; i++) params[i] = "";
        doSend(phone, c.getTemplateId(), params);
    }

    @Override
    public void sendText(String phone, String text) throws Exception {
        NotifyProperties.Tencent c = props.getTencent();
        if (isBlank(c.getNoticeTemplateId())) {
            throw new IllegalStateException("腾讯云短信未配置 notice-template-id，无法发送自由文本");
        }
        doSend(phone, c.getNoticeTemplateId(), new String[]{text});
    }

    private void doSend(String phone, String templateId, String[] params) throws Exception {
        NotifyProperties.Tencent c = props.getTencent();
        if (isBlank(c.getSecretId()) || isBlank(c.getSecretKey())) {
            throw new IllegalStateException("腾讯云短信缺少 secret-id/secret-key");
        }
        if (isBlank(c.getSdkAppId()) || isBlank(c.getSignName())) {
            throw new IllegalStateException("腾讯云短信缺少 sdk-app-id/sign-name");
        }

        StringBuilder paramArr = new StringBuilder("[");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) paramArr.append(',');
            paramArr.append('"').append(escape(params[i])).append('"');
        }
        paramArr.append(']');

        String phoneE164 = phone.startsWith("+") ? phone : "+86" + phone;
        String payload = "{\"PhoneNumberSet\":[\"" + phoneE164 + "\"],"
                + "\"SmsSdkAppId\":\"" + escape(c.getSdkAppId()) + "\","
                + "\"SignName\":\"" + escape(c.getSignName()) + "\","
                + "\"TemplateId\":\"" + escape(templateId) + "\","
                + "\"TemplateParamSet\":" + paramArr + "}";

        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String date = DATE.format(Instant.now());

        // 1) 规范请求串
        String canonicalRequest = "POST\n/\n\n"
                + "content-type:application/json; charset=utf-8\n"
                + "host:" + HOST + "\n\n"
                + "content-type;host\n"
                + sha256Hex(payload);

        // 2) 待签字符串
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String stringToSign = "TC3-HMAC-SHA256\n" + timestamp + "\n"
                + credentialScope + "\n" + sha256Hex(canonicalRequest);

        // 3) 计算签名
        byte[] secretDate = hmacSha256(("TC3" + c.getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, SERVICE);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = HexFormat.of().formatHex(hmacSha256(secretSigning, stringToSign));

        String authorization = "TC3-HMAC-SHA256 Credential=" + c.getSecretId() + "/" + credentialScope
                + ", SignedHeaders=content-type;host, Signature=" + signature;

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://" + HOST + "/"))
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", authorization)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", HOST)
                .header("X-TC-Action", ACTION)
                .header("X-TC-Timestamp", timestamp)
                .header("X-TC-Version", VERSION)
                .header("X-TC-Region", c.getRegion())
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = resp.body();
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IllegalStateException("腾讯云短信 HTTP " + resp.statusCode() + ": " + body);
        }
        if (body != null && body.contains("\"Error\"")) {
            throw new IllegalStateException("腾讯云短信返回错误: " + body);
        }
        log.info("[SMS-TENCENT] 已下发 phone={} template={}", mask(phone), templateId);
    }

    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(String s) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
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