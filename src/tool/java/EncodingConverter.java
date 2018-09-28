package tool.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * <b>Description:</b><br> 
 * 简单的gbk、utf8文件编码转换实现(Lite)
 * 转换会覆盖原文件，请自行备份
 * @author abeholder
 * @version 1.0
 * <br><b>Date:</b> Jan 18, 2016 11:56:35 PM
 */
public class EncodingConverter {

	private Path rootPath;
	// key为文件， value为探测出的编码
	private Map<Path, Charset> mapped = new HashMap<>();

	private List<Charset> charsetList = Stream.of("utf-8", "gbk").map(c -> Charset.forName(c))
			.collect(Collectors.toList());
	private Set<String> extensionSet = new HashSet<String>() {
		private static final long serialVersionUID = -5130320059601468922L;

		{
			add(".java");
			add(".txt");
			add(".xml");
			add(".html");
			add(".md");
			add(".css");
			add(".js");
			add(".jsp");
		}
	};
	private int maxLength = charsetList.stream().mapToInt(c -> c.name().length()).max().getAsInt();

	public EncodingConverter() {
		this("");
	}

	public EncodingConverter(String rootPath) {
		if (".".equals(rootPath)) rootPath = "";
		this.rootPath = Paths.get(rootPath).toAbsolutePath();
		if (!Files.isReadable(this.rootPath)) throw new IllegalArgumentException("路径不能读取.");
		if (this.rootPath.getParent() == null) throw new IllegalArgumentException("不能是根路径.");
		load();
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 打印出需要探测的文件
	 * <b>Author:</b> abeholder
	 */
	private void load() {
		try (Stream<Path> path = Files.walk(rootPath)) {
			path.filter(p -> Files.isRegularFile(p) && verifyExtension(p)).forEach(p -> {
				charsetList.stream().anyMatch(c -> {
					boolean flag = canDecode(p, c);
					if (flag) mapped.put(p, c);
					return flag;
				});
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 打印出探测成功的文件
	 * <b>Author:</b> abeholder
	 */
	public void typeFiles() {
		mapped.forEach((path, charset) -> {
			StringBuilder sb = new StringBuilder();
			sb.append("[ ").append(center(charset.name())).append(" ] ").append(path).append("\n");
			System.out.print(sb);
		});
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * @param content 简单的居中对齐实现
	 * @return
	 * <b>Author:</b> abeholder
	 */
	private String center(String content) {
		if (content.length() == maxLength) return content;
		
		boolean right = true;
		StringBuilder sb = new StringBuilder(content);
		while (sb.length() < maxLength) {
			if (right) 
				sb.append(' ');
			else
				sb.insert(0, ' ');
			right = !right;
		}
		return sb.toString();
	}

	public boolean verifyExtension(Path file) {
		String ext = getExtension(file).toLowerCase();
		return extensionSet.contains(ext);
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 获取文件扩展名
	 * @param file
	 * @return
	 * <b>Author:</b> abeholder
	 */
	public static String getExtension(Path file) {
		String fileName = String.valueOf(file);
		int index = fileName.lastIndexOf('.');
		if (index < 0) return "";
		return fileName.substring(index, fileName.length());
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 简单测试能否通过此编码读取该文件
	 * @param filepath
	 * @param charset
	 * @return
	 * <b>Author:</b> abeholder
	 */
	private static boolean canDecode(Path filepath, Charset charset) {
		try (BufferedReader br = Files.newBufferedReader(filepath, charset)) {
			br.readLine();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 按指定编码读取文件内容
	 * @param filepath
	 * @param charset
	 * @return
	 * <b>Author:</b> abeholder
	 */
	private static String read(Path filepath, Charset charset) {
		try {
			List<String> lines = Files.readAllLines(filepath, charset);
			return lines.stream().collect(Collectors.joining("\n")); // \r\n
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 按指定编码写出文件内容
	 * @param filepath
	 * @param content
	 * @param charset
	 * <b>Author:</b> abeholder
	 */
	private static void write(Path filepath, String content, Charset charset) {
		try (BufferedWriter writer = Files.newBufferedWriter(filepath, charset)) {
			writer.write(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * <b>Description:</b><br> 
	 * 将探测出编码的文件转换成指定编码
	 * @param newCharsetName
	 * <b>Author:</b> abeholder
	 */
	public void convertTo(String newCharsetName) {
		Objects.requireNonNull(newCharsetName);
		Charset charset = Charset.forName(newCharsetName);
		
		mapped.entrySet().stream().filter(e -> e.getValue() != charset).forEach(e -> {
			Path filepath = e.getKey();
			String content = read(filepath, e.getValue());
			write(filepath, content, charset);
			StringBuilder sb = new StringBuilder();
			sb.append("[ ").append(e.getValue()).append(" -> ").append(charset).append(" ] ").append(filepath);
			System.out.println(sb);
		});
	}
	
	
	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[1];
			args[0] = ".";
		} else if (args.length > 2) {
			throw new IllegalArgumentException("参数错误. [rootpath] [charsetname]");
		}
		
		long begin = System.nanoTime();
		EncodingConverter converter = new EncodingConverter(args[0]);
		switch(args.length) {
			case 1:
				converter.typeFiles();
				break;
			case 2:
				converter.convertTo(args[1]);
		}
		System.out.println("used: " + (System.nanoTime() - begin) / 1_000_000 + " ms");
	}
}
