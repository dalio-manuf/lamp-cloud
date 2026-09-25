package com.dalio.cloud.common.utils;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.StrPool;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.dalio.basic.exception.code.ExceptionCode.JWT_BASIC_INVALID;

/**
 * 工具类
 *
 * @author admin
 */
public class Base64Util {

    private static final String BASIC_PREFIX = "Basic ";

    private Base64Util() {
    }

    /**
     * authorization: base64(clientId:clientSec)
     * 解析请求头中存储的 client 信息
     * <p>
     * Basic clientId:clientSec -截取-> clientId:clientSec后调用 extractClient 解码
     *
     * @param basicHeader Basic clientId:clientSec
     * @return clientId:clientSec
     */
    public static String[] getClient(String basicHeader) {
        if (StrUtil.isEmpty(basicHeader)) {
            throw BizException.wrap("客户端参数尚未传递");
        }

        return extractClient(basicHeader);
    }

    /**
     * 解析请求头中存储的 client 信息
     * clientId:clientSec 解码
     *
     * @param client client 信息或带 Basic 前缀的 Authorization header
     * @return [clientId, clientSec]
     */
    public static String[] extractClient(String client) {
        if (StrUtil.isEmpty(client)) {
            throw BizException.wrap("客户端参数尚未传递");
        }
        String cleanClient = StrUtil.removePrefixIgnoreCase(client.trim(), BASIC_PREFIX).trim();
        String token = base64Decoder(cleanClient);
        int index = token.indexOf(StrPool.COLON);
        if (index == -1) {
            throw BizException.wrap(JWT_BASIC_INVALID);
        } else {
            return new String[]{token.substring(0, index), token.substring(index + 1)};
        }
    }

    /**
     * 使用 Base64 解码
     *
     * @param val 参数
     * @return 解码后的值
     */
    @SneakyThrows
    public static String base64Decoder(String val) {
        if (StrUtil.isEmpty(val)) {
            return StrPool.EMPTY;
        }
        byte[] decoded = Base64.getDecoder().decode(val.getBytes(StandardCharsets.UTF_8));
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
