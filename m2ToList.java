import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class m2ToList {
	String directory = "-";
	BufferedWriter bw = null;
	FileWriter fw = null;
	private static final String FILENAME = "E:\\test\\filename.txt";
	private static final String DOCFOLDER = "Libs";

	public m2ToList() {
		try {

			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);

			File folder = new File("C:/Users/xanAdmin/.m2/repository");
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				bw.write("Parent ##" + listOfFiles[i].getPath() + " \n");
				if (listOfFiles[i].isFile()) {
					System.out.println();
				} else if (listOfFiles[i].isDirectory()) {
					// Create File Document
					File doc = new File(DOCFOLDER + File.separator + listOfFiles[i].getName() + ".xan");
					doc.createNewFile();
					findName(listOfFiles[i], doc);
				}
				bw.write(" \n");
			}
			bw.close();
			fw.close();
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findName(File folder, File doc) {

		try {
			FileWriter fw = new FileWriter(doc, true);
			BufferedWriter bw = new BufferedWriter(fw);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					// Filter and Unzip
					if (listOfFiles[i].getName().toLowerCase().endsWith(".jar")) {
						// bw.write("Jar File -> " +
						// listOfFiles[i].getAbsolutePath() + " \n");
						String fileZip = listOfFiles[i].getAbsolutePath();
						byte[] buffer = new byte[1024];
						ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
						ZipEntry zipEntry = zis.getNextEntry();
						while (zipEntry != null) {
							String fileName = zipEntry.getName();
							System.out.println("Class ---- > " + fileName);
							if (fileName.endsWith(".class"))
								bw.write(fileName + "\n");
							zipEntry = zis.getNextEntry();
						}
						zis.closeEntry();
						zis.close();
					}
				} else if (listOfFiles[i].isDirectory()) {
					// sbw.write("Libs -> " + listOfFiles[i].getName() + " \n");
					findName(listOfFiles[i], doc);
				}
			}
			bw.close();
			fw.close();
			System.out.println(" Added :" + folder.getPath());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new m2ToList();
	}
}
