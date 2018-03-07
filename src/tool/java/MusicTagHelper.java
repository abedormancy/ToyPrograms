package tool.java;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by abedormancy@gmail.com on 2013
 */
public class MusicTagHelper {
	
	private MusicTagHelper() {
		
	}

	/**
	 * 传入一个mp3的尾端128字节数组，获得该歌曲的标题和作者
	 * 
	 * @param header
	 * @return 返回该数组信息的name和author的Music
	 */
	private static Music getID3V1(byte[] header) {
		String name = "";
		String author = "";
		if (new String(header, 0, 3).equals("TAG")) {
			try {
				name = new String(header, 3, 30, "GBK").trim();
				author = new String(header, 33, 30, "GBK").trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return new Music(name, author);

	}

	/**
	 * 传入一个mp3的头信息数组，获得该歌曲的标题和作者
	 * 
	 * @param header
	 * @return 返回该数组信息的name和author的Music
	 */
	private static Music getID3V2(byte[] header) throws IOException {
		String name = "";
		String author = "";
		// 得到标签帧
		String tagName = "";
		int index = 10;
		int fsize = 0;
		while (index < header.length - 10 - fsize && index > 0) {
			tagName = new String(header, index, 4);
			// 得到该标签内容大小
			fsize = header[index + 4] * 0x10000000 + header[index + 5] * 0x10000 + header[index + 6] * 0x100
					+ header[index + 7];
			// 跳过标签帧的头数据（10字节）
			index += 10;
			// System.out.println(tagName + " ->" + index);
			if ("TIT2".equals(tagName)) {
				name = new String(header, index, fsize).trim();
			} else if ("TPE1".equals(tagName)) {
				author = new String(header, index, fsize).trim();
			}
			if (!"".equals(name) && !"".equals(author))
				break;
			// 跳过该标签内容
			index += fsize;
		}
		return new Music(name, author);
	}

	public static Music read(File file) {
		String name = "";
		String author = "";
		try {
			RandomAccessFile rf = new RandomAccessFile(file, "r");
			FileChannel fc = rf.getChannel();
			MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, fc.size() - 128, 128);
			byte[] header = new byte[128];
			buffer.get(header);
			// 先读取ID3V1信息
			Music music = getID3V1(header);
			name = music.getName();
			author = music.getAuthor();
			// 如果读取不到标题和作者，则从ID3V2中读取
			if ("".equals(name) || "".equals(author)) {
				header = new byte[10];
				buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, 10);
				buffer.get(header);
				// 如果读取ID3V2成功
				if (new String(header, 0, 3).equals("ID3")) {
					// 标签的大小计算
					int tagSize = (header[9] & 0xff) + ((header[8] & 0xff) << 7) + ((header[7] & 0xff) << 14)
							+ ((header[6] & 0xff) << 21);
					// System.out.println("tagsize:" + tagSize);
					/*
					 * int tagSize = (header[6]&0x7F)*0x200000 +(header[7]&0x7F)*0x400
					 * +(header[8]&0x7F)*0x80 +(header[9]&0x7F);
					 */
					buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, tagSize);
					header = new byte[tagSize];
					buffer.get(header);
					music = getID3V2(header);
					if (!"".equals(music.getName()))
						name = music.getName();
					if (!"".equals(music.getAuthor()))
						author = music.getAuthor();
				}
			}
			fc.close();
			rf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Music("".equals(name) ? "unknown" : name, "".equals(author) ? "unknown" : author);
	}

	public static Music read(String file) {
		return read(new File(file));
	}

	public static Music read(byte[] mp3) {
		byte[] header = new byte[128];
		System.arraycopy(mp3, mp3.length - 128, header, 0, 128);
		return getID3V1(header);
	}

	private static FilenameFilter filter(final String afn) {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.lastIndexOf(afn) != -1;
			}
		};
	}

	public static void main(String[] args) {
		long timeBegin = System.currentTimeMillis();

		File file = new File("R:/");
		for (File f : file.listFiles(filter(".mp3"))) {
			System.out.println(f.getName() + "\t -> \t" + read(f));
		}

		System.out.println(System.currentTimeMillis() - timeBegin + "ms");
	}

	public static class Music {
		private String name;
		private String author;

		public Music(String name, String author) {
			this.name = name;
			this.author = author;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		@Override
		public String toString() {
			return author + " - " + name;
		}
	}
}
