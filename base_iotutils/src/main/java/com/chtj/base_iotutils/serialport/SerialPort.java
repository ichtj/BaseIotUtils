/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.chtj.base_iotutils.serialport;
import com.chtj.base_iotutils.KLog;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

	private static final String TAG = SerialPort.class.getSimpleName();
	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/** Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/** Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException("执行 /system/bin/su chmod 666 xx出现异常,请检查该串口是否授予权限");
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		KLog.d(TAG, "SerialPort:open success ");
		if (mFd == null) {
			KLog.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	/**
	 * 先检查串口是否有读写权限
	 * 没有的情况下直接授予权限读写权限
	 * @param device 串口设备文件
	 * @param baudrate 波特率
	 * @param dataBits 数据位，5 - 8
	 * @param stopBits 停止位，1 或 2
	 * @param parity 奇偶校验，0 None, 1 Odd, 2 Even
	 */
	public SerialPort(File device, int baudrate, int dataBits,int stopBits,char parity)
			throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException("执行 /system/bin/su chmod 666 出现异常,请检查该串口是否授予权限");
			}
		}

		mFd = open2(device.getAbsolutePath(), baudrate, dataBits,stopBits,parity);
		if (mFd == null) {
			KLog.e(TAG, "native open returns null");
			throw new IOException();
		}else {
			KLog.d(TAG, "native open != null");
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}



	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	/**
	 * 写入
	 * @param buff 命令，字符等
	 */
	public void write(byte[] buff)
	{

		if(mFileOutputStream!=null)
		{
			try {
				mFileOutputStream.write(buff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取数据
	 * @param buff 读到之后存放的byte[]
	 * @param lenght 长度
	 */
	public void read(byte[] buff,int lenght)
	{

		try {
			mFileInputStream.read(buff, 0, lenght);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	private native static FileDescriptor open2(String path, int baudrate,int dataBits,int stopBits,char parity);
	public native void close();
	static {
		System.loadLibrary("serialport");
	}
}
