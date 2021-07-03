package com.chtj.base_framework.network;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.os.Build;
import android.util.Log;

import com.chtj.base_framework.FBaseTools;
import com.chtj.base_framework.FCmdTools;
import com.chtj.base_framework.FCommonTools;
import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.IpConfigInfo;

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
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 24) {
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) FBaseTools.getContext().getSystemService("ethernet");
            mEthManager.setEthernetEnabled(true);
        } else {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            ethernetManager.setEnabled(true);
        }
    }

    /**
     * 关闭以太网
     */
    public static void closeEth() {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 24) {
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) FBaseTools.getContext().getSystemService("ethernet");
            mEthManager.setEthernetEnabled(false);
        } else {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            ethernetManager.setEnabled(false);
        }
    }


    /**
     * 获取ip模式
     */
    public static String getIpMode(Context context) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 24) {
            String ipMode = "NONE";
            android.net.EthernetManager mEthManager = (android.net.EthernetManager) context.getSystemService("ethernet");
            boolean useDhcp = (mEthManager.getConfiguration().ipAssignment == IpConfiguration.IpAssignment.DHCP) ? true : false;
            boolean useStatic = (mEthManager.getConfiguration().ipAssignment == IpConfiguration.IpAssignment.STATIC) ? true : false;
            boolean usePppoe = (mEthManager.getConfiguration().ipAssignment == IpConfiguration.IpAssignment.PPPOE) ? true : false;
            boolean useUnassigned = (mEthManager.getConfiguration().ipAssignment == IpConfiguration.IpAssignment.UNASSIGNED) ? true : false;
            if (useDhcp) {
                ipMode = "DHCP";
            } else if (useStatic) {
                ipMode = "STATIC";
            } else if (usePppoe) {
                ipMode = "PPPOE";
            } else if (useUnassigned) {
                ipMode = "UNASSIGNED";
            } else {
                ipMode = "NONE";
            }
            return ipMode;
        } else {
            android.net.ethernet.EthernetManager ethernetManager = EthernetManager.getInstance();
            boolean isDhcp = ethernetManager.isDhcp();
            return isDhcp ? "DHCP" : "STATIC";
        }
    }

    /**
     * 设置静态IP参数
     *
     * @param ipConfigInfo
     */
    public static CommonValue setStaticIp(IpConfigInfo ipConfigInfo) {
        if (checkIp(ipConfigInfo)) {
            int sdk = Build.VERSION.SDK_INT;
            if (sdk >= 24) {
                return setEthStaticRk(ipConfigInfo);
            } else {
                return setEthStaticFc(ipConfigInfo);
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
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 24) {
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


    /**
     * 判断网线拔插状态
     * 通过命令cat /sys/class/net/eth0/carrier，如果插有网线的话，读取到的值是1，否则为0
     *
     * @return 是否插入网线
     */
    public static boolean isCablePluggedIn() {
        FCmdTools.CommandResult fResult = FCmdTools.execCommand("cat /sys/class/net/eth0/carrier", true);
        if (fResult.result == 0 && fResult.successMsg.trim().equals("1")) {  //有网线插入时返回1，拔出时返回0
            return true;
        }
        return false;
    }

}
