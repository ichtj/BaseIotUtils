package com.wave_chtj.example.socket;

import com.chtj.base_iotutils.KLog;
import com.xuhao.didi.core.iocore.interfaces.ISendable;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Create on 2019/10/12
 * author chtj
 * desc $
 */
public class TestSendData implements ISendable {
    private String str = "";

    public TestSendData(String strInfo) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmd", 14);
            jsonObject.put("data", "{x:2,y:1}");
            //str = jsonObject.toString();
            str=strInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] parse() {
        //Build the byte array according to the server's parsing rules
        byte[] payload = str.getBytes(Charset.defaultCharset());
        //4 is package header fixed length and payload length
        ByteBuffer bb = ByteBuffer.allocate(4 + payload.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(payload.length);
        bb.put(payload);
        return bb.array();
    }
}