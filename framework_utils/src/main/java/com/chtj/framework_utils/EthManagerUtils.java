package com.chtj.framework_utils;

import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.util.Log;

import com.chtj.framework_utils.entity.DeviceType;
import com.chtj.framework_utils.entity.IpConfigParams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.regex.Pattern;

public class EthManagerUtils {
    private static final String TAG = "EthManagerUtils";
    private final static String nullIpInfo = "0.0.0.0";

    /**
     * 开启以太网
     */
    public static void openEth() {
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            ethernetManager.setEnabled(true);
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {

        }
    }

    /**
     * 关闭以太网
     */
    public static void closeEth() {
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            ethernetManager.setEnabled(false);
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) BaseSystemUtils.getContext().getSystemService("ethernet");
            mEthManager.setEthernetEnabled(true);
        }
    }


    /**
     * 获取ip模式
     */
    public static String getIpMode() {
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            boolean isDhcp = ethernetManager.isDhcp();
            return isDhcp ? "DHCP" : "STATIC";
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) BaseSystemUtils.getContext().getSystemService("ethernet");
            boolean useDhcp = (mEthManager.getConfiguration().ipAssignment == IpConfiguration.IpAssignment.DHCP) ? true : false;
            boolean useStatic = (mEthManager.getConfiguration().ipAssignment == IpConfiguration.IpAssignment.STATIC) ? true : false;
            if (useDhcp) {
                return "DHCP";
            } else if (useStatic) {
                return "STATIC";
            } else {
                return "PPPOE";
            }
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 设置静态IP参数
     *
     * @param ipConfigParams
     */
    public static void setStaticIp(IpConfigParams ipConfigParams) {
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            //设置静态IP FC5330
            setEthStaticFc(ipConfigParams);
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {
            setEthStaticRk(ipConfigParams);
        }
    }


    private static void setEthStaticRk(IpConfigParams ipConfigParams) {
        setStaticIpConfiguration(ipConfigParams);
    }

    private static void setStaticIpConfiguration(IpConfigParams ipConfigParams) {
        android.net.EthernetManager mEthManager = (android.net.EthernetManager) BaseSystemUtils.getContext().getSystemService("ethernet");
        String ipAddr = ipConfigParams.getIp();
        String gateway = ipConfigParams.getGateWay();
        String netMask = ipConfigParams.getMask();
        String dns1 = ipConfigParams.getDns1();
        String dns2 = ipConfigParams.getDns2();
        int network_prefix_length = 24;

        StaticIpConfiguration mStaticIpConfiguration = new StaticIpConfiguration();
        /*
         * get ip address, netmask,dns ,gw etc.
         */
        Inet4Address inetAddr = getIPv4Address(ipAddr);
        int prefixLength = maskStr2InetMask(netMask);
        InetAddress gatewayAddr = getIPv4Address(gateway);
        InetAddress dnsAddr = getIPv4Address(dns1);

        if (inetAddr.getAddress().toString().isEmpty() || prefixLength == 0 || gatewayAddr.toString().isEmpty()
                || dnsAddr.toString().isEmpty()) {
            Log.d(TAG, "ip,mask or dnsAddr is wrong");
            return;
        }
        String dnsStr2 = dns2;
        try {
            Class[] classes = new Class[]{InetAddress.class, int.class};
            Class<?> linkAddressClass = Class
                    .forName("android.net.LinkAddress");
            mStaticIpConfiguration.ipAddress = (LinkAddress) linkAddressClass
                    .getDeclaredConstructor(classes).newInstance(inetAddr, prefixLength);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }

        mStaticIpConfiguration.gateway = gatewayAddr;
        mStaticIpConfiguration.dnsServers.add(dnsAddr);

        if (!dnsStr2.isEmpty()) {
            mStaticIpConfiguration.dnsServers.add(getIPv4Address(dnsStr2));
        }
        IpConfiguration mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
        mEthManager.setConfiguration(mIpConfiguration);
    }

    /*
     * convert subMask string to prefix length
     */
    private static int maskStr2InetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        /*
         * check the subMask format
         */
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (pattern.matcher(maskStr).matches() == false) {
            Log.e(TAG, "subMask is error");
            return 0;
        }

        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }

    private static Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    /**
     * 设置以太网为DHCP
     */
    public static void setEthDhcp() {
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            try {
                //动态 关闭 再打开
                //先关闭以太网 再打开以太网
                EthernetManager ethernetManager = EthernetManager.getInstance();
                EthernetDevInfo mDevInfo = ethernetManager.getSavedConfig();
                String hwaddr = "00:00:00:00:00:00";
                try {
                    hwaddr = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "errMeg:" + e.getMessage());
                }
                mDevInfo.setHwaddr(hwaddr);
                mDevInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
                ethernetManager.updateDevInfo(mDevInfo);
                ethernetManager.setEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {
            android.net.EthernetManager ethernetManager = (android.net.EthernetManager) BaseSystemUtils.getContext().getSystemService("ethernet");
            ethernetManager.setConfiguration(new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, null));
        }
    }


    /**
     * 设置以太网为静态模式
     */
    private static boolean setEthStaticFc(IpConfigParams ipConfigParams) {
        try {
            EthernetManager ethernetManager = EthernetManager.getInstance();
            EthernetDevInfo mDevInfo = ethernetManager.getSavedConfig();
            String hwaddr = "00:00:00:00:00:00";
            try {
                hwaddr = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
            mDevInfo.setHwaddr(hwaddr);
            mDevInfo.setIpAddress(ipConfigParams.getIp());
            mDevInfo.setNetMask(ipConfigParams.getMask());
            mDevInfo.setDns1Addr(ipConfigParams.getDns1());
            mDevInfo.setDns2Addr(ipConfigParams.getDns2());
            mDevInfo.setGateWay(ipConfigParams.getGateWay());
            mDevInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
            ethernetManager.updateDevInfo(mDevInfo);
            ethernetManager.setEnabled(true);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
            return false;
        }
    }


    /**
     * 获取eth物理地址
     *
     * @param filePath
     * @return
     * @throws java.io.IOException
     */
    public static String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }/** Get the STB MacAddress*/


}
