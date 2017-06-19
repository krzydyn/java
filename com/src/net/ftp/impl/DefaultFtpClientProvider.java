package net.ftp.impl;

import net.ftp.FtpClient;
import net.ftp.FtpClientProvider;

public class DefaultFtpClientProvider extends FtpClientProvider {

	@Override
	public FtpClient createFtpClient() {
		return net.ftp.impl.FtpClient.create();
	}

}
