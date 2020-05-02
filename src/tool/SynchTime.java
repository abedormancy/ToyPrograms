package tool;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @date 2016-06-26 13:27:35
 * @author abe
 *
 */
public class SynchTime {
	public static void main(String[] args) {
		long delay = 600000l; //默认同步间隔
		
		if (args.length == 0) {
//			throw new IllegalArgumentException("请传递需要同步的服务器URL地址");
			args = new String[1];
			args[0] = "http://www.baidu.com";
			System.out.println("未传递时间服务器URL地址，系统默认使用：" +args[0]);
		}
		
		String _url = args[0];
		if (isEmpty(_url)) {
			throw new IllegalArgumentException("URL地址不能为空");
		}
		
		print("提示： 不关闭本窗口，程序将10分钟自动同步一次服务器时间\n\n");
		
		for (;;) {
			try {
				print("=--=--=--=--=--=--=--= 同步时间 =--=--=--=--=--=--=--=\n");
				print("正在获取服务器时间 -> ("+_url+")\n");
				URL url = new URL(_url);
				URLConnection conn = url.openConnection();
				long remoteTime = conn.getDate();
				long now = System.currentTimeMillis();
				
				print("获取服务器时间成功 -> ".concat(formatDate(remoteTime, "yyyy/MM/dd HH:mm:ss")).concat("\n"));
				print("　　　当前系统时间 -> ".concat(formatDate(now, "yyyy/MM/dd HH:mm:ss")).concat("\n"));
				String command = "date ".concat(formatDate(remoteTime, "yyyy/MM/dd && HH:mm:ss") )
										.replace("&&", "&& time");
				String result = execBatch(command);
				System.out.println(isEmpty(result) ? "success" : result);
				print("=--=--=--=--=--=--=--=--=-vv-=--=--=--=--=--=--=--=--=\n\n");
				Thread.sleep(delay);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static boolean isEmpty(String text) {
		return text == null || text.trim().length() == 0;
	}
	
	public static void print(String text) {
		System.out.print(text);;
	}
	
	/**
	 * 执行windows批处理命令
	 * @param command
	 * @return
	 */
	public static final String execBatch(String command) {
		String _command = "cmd.exe /c ".concat(command);
		Process process = null;
		String result = "";
		try {
			process = Runtime.getRuntime().exec(_command);
			ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
			InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
			InputStream processInStream = new BufferedInputStream(process.getInputStream());
			int len = 0;
			byte[] buff = new byte[8196];
			while ((len = errorInStream.read(buff)) != -1) {
				resultOutStream.write(buff, 0, len);
			}
			while ((len = processInStream.read(buff)) != -1) {
				resultOutStream.write(buff, 0, len);
			}
			result = new String(resultOutStream.toByteArray(), "gbk");
			errorInStream.close();
			processInStream.close();
			resultOutStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (process != null) process.destroy();
		}
		return result;
	}
	
	public static final String formatDate(Date date, String pattern) {
		return formatDate(date.getTime(), pattern);
	}
	
	public static final String formatDate(long time, String pattern) {
		SimpleDateFormat sf = null;
		try {
			sf = new SimpleDateFormat(pattern);
		} catch (Exception e) {
			sf = new SimpleDateFormat("yyyy-MM-dd");
		}
		return sf.format(time);
	}
}
