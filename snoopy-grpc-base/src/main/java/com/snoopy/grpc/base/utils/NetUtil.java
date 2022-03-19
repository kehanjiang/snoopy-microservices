package com.snoopy.grpc.base.utils;

import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 网络工具类
 *
 * @author :   kehanjiang
 * @date :   2021/12/4  16:53
 */
public class NetUtil {
    private static final Pattern IP_PATTERN = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|(\\d{1,3}(\\.\\d{1,3}){3,5}$)");

    public static final String LOCALHOST_ADDRESS = "localhost";
    public static final String LOOPBACK_ADDRESS = "127.0.0.1";


    public static boolean isIpAddress(String host) {
        return StringUtils.hasText(host) && IP_PATTERN.matcher(host).matches();
    }

    public static String getLocalIpAddress() {
        List<String> ips = getAllLocalIpAddress();
        return ips.size() > 0 ? ips.get(0) : LOOPBACK_ADDRESS;
    }


    public static List<String> getAllLocalIpAddress() {
        List<String> ips = new ArrayList<>();
        try {
            //获取本地所有网络接口
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            //遍历枚举中的每一个元素
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                Enumeration<InetAddress> enumInetAddr = ni.getInetAddresses();
                while (enumInetAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddr.nextElement();
                    // 排除loopback回环类型地址和链路本地地址
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                            && inetAddress.isSiteLocalAddress()) {
                        ips.add(inetAddress.getHostAddress());

                    }
                }
            }
        } catch (SocketException e) {
            LoggerBaseUtil.error(NetUtil.class, e.getMessage(), e);
        }
        return ips;
    }
}
