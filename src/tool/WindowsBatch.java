package tool;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;


public class WindowsBatch {
	
	private static final String CMD = "cmd.exe /c ";

	public static void perform(String command, Consumer<String> consumer) {
		String result = _execBatch(command);
		consumer.accept(result);
	}
	
	public static String perform(String command) {
		return _execBatch(command);
	}
	
	private static void streamWrite(OutputStream outputStream, InputStream... inputStream) throws IOException {
		for (InputStream is : inputStream) {
			int len = 0;
			byte[] buff = new byte[8192];
			while ((len = is.read(buff)) != -1) {
				outputStream.write(buff, 0, len);
			}
		}
	}

	private static String _execBatch(String command) {
		command = CMD + command;
		Process process = null;
		String result = "";
		try {
			process = Runtime.getRuntime().exec(command);
//			int exitValue = process.waitFor();
//			System.out.println(exitValue);
			try (ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
					InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
					InputStream processInStream = new BufferedInputStream(process.getInputStream())) {
				streamWrite(resultOutStream, errorInStream, processInStream);
				result = new String(resultOutStream.toByteArray(), "gbk");
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = e.getMessage();
		} finally {
			if (process != null) process.destroy();
		}
		return result;
	}
	
	public static void main(String[] args) {
		perform("ping google.com /n 1", System.out::println);
	}
}