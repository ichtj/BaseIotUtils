package com.chtj.framework.network;

import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.util.Log;

import com.chtj.framework.FBaseTools;
import com.chtj.framework.FCommonTools;
import com.chtj.framework.entity.CommonValue;
import com.chtj.framework.entity.DeviceType;
import com.chtj.framework.entity.IpConfigInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.regex.Pattern;

public class FEthTools {
    private static final String TAG = "EthManagerUtils";
    private final static String nullIpInfo = "0.0.0.0";

    /**
     * 开启以太网
     */
    public static void openEth() {
        if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_FC5330) {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            ethernetManager.setEnabled(true);
        } else if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_RK3288) {

        }
    }

    /**
     * 关闭以太网
     */
    public static void closeEth() {
        if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_FC5330) {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            ethernetManager.setEnabled(false);
        } else if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_RK3288) {
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) FBaseTools.getContext().getSystemService("ethernet");
            mEthManager.setEthernetEnabled(true);
        }
    }


    /**
     * 获取ip模式
     */
    public static String getIpMode() {
        if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_FC5330) {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            boolean isDhcp = ethernetManager.isDhcp();
            return isDhcp ? "DHCP" : "STATIC";
        } else if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_RK3288) {
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) FBaseTools.getContext().getSystemService("ethernet");
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
     * @param ipConfigInfo
     */
    public static CommonValue setStaticIp(IpConfigInfo ipConfigInfo) {
        if (checkIp(ipConfigInfo)) {
            if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_FC5330) {
                return setEthStaticFc(ipConfigInfo);
            } else if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_RK3288) {
                return setEthStaticRk(ipConfigInfo);
            } else {
                return CommonValue.ETH_OTHER_DEVICES;
            }
        } else {
            return CommonValue.ETH_IPCHECK_ERR;
        }
    }

    /**
     * 检查ip地址输入是否合法
     *
     * @param ipConfigInfo
     * @return
     */
    private static boolean checkIp(IpConfigInfo ipConfigInfo) {
        boolean ipCheck = FCommonTools.matchesIp(ipConfigInfo.getIp());
        boolean dns1Check = FCommonTools.matchesIp(ipConfigInfo.getDns1());
        boolean dns2Check = FCommonTools.matchesIp(ipConfigInfo.getDns2());
        boolean gateWaycheck = FCommonTools.matchesIp(ipConfigInfo.getGateWay());
        boolean maskCheck = FCommonTools.matchesIp(ipConfigInfo.getMask());
        return ipCheck && dns1Check && dns2Check && gateWaycheck && maskCheck;
    }

    /**
     * RK3288设置静态IP
     *
     * @param ipConfigInfo
     */
    private static CommonValue setEthStaticRk(IpConfigInfo ipConfigInfo) {
        android.net.EthernetManager mEthManager = (android.net.EthernetManager) FBaseTools.getContext().getSystemService("ethernet");
        String ipAddr = ipConfigInfo.getIp();
        String gateway = ipConfigInfo.getGateWay();
        String netMask = ipConfigInfo.getMask();
        String dns1 = ipConfigInfo.getDns1();
        String dns2 = ipConfigInfo.getDns2();
        //int network_prefix_length = 24;

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
            return CommonValue.ETH_PARAMS_ERR;
        }
        String dnsStr2 = dns2;
        try {
            Class[] classes = new Class[]{InetAddress.class, int.class};
            Class<?> linkAddressClass = Class
                    .forName("android.net.LinkAddress");
            mStaticIpConfiguration.ipAddress = (LinkAddress) linkAddressClass
                    .getDeclaredConstructor(classes).newInstance(inetAddr, prefixLength);


            mStaticIpConfiguration.gateway = gatewayAddr;
            mStaticIpConfiguration.dnsServers.add(dnsAddr);

            if (!dnsStr2.isEmpty()) {
                mStaticIpConfiguration.dnsServers.add(getIPv4Address(dnsStr2));
            }
            IpConfiguration mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
            mEthManager.setConfiguration(mIpConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
            return CommonValue.ETH_SECURITY_ERR;
        }
        return CommonValue.EXEU_COMPLETE;
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
    public static CommonValue setEthDhcp() {
        if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_FC5330) {
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
                return CommonValue.EXEU_COMPLETE;
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
                return CommonValue.ETH_SECURITY_ERR;
            }
        } else if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_RK3288) {
            try {
                android.net.EthernetManager ethernetManager = (android.net.EthernetManager) FBaseTools.getContext().getSystemService("ethernet");
                ethernetManager.setConfiguration(new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, null));
                return CommonValue.EXEU_COMPLETE;
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
                return CommonValue.ETH_SECURITY_ERR;
            }
        } else {
            return CommonValue.ETH_OTHER_DEVICES;
        }
    }


    /**
     * 设置以太网为静态模式
     */
    private static CommonValue setEthStaticFc(IpConfigInfo ipConfigInfo) {
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
            mDevInfo.setIpAddress(ipConfigInfo.getIp());
            mDevInfo.setNetMask(ipConfigInfo.getMask());
            mDevInfo.setDns1Addr(ipConfigInfo.getDns1());
            mDevInfo.setDns2Addr(ipConfigInfo.getDns2());
            mDevInfo.setGateWay(ipConfigInfo.getGateWay());
            mDevInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
            ethernetManager.updateDevInfo(mDevInfo);
            ethernetManager.setEnabled(true);
            return CommonValue.EXEU_COMPLETE;
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
            return CommonValue.ETH_SECURITY_ERR;
        }
    }


    /**
     * 获取eth物理地址
     *
     * @param filePath
     * @return
     * @throws java.io.IOException
     */
    private static String loadFileAsString(String filePath) throws java.io.IOException {
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
    }


}
