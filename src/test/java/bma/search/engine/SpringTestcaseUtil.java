package bma.search.engine;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Spring框架的测试用例工具
 * 
 * @author 关中
 * @since 1.0
 * 
 */
public class SpringTestcaseUtil {

	public static ApplicationContext projectContext(String[] names) {
		ApplicationContextBuilder b = new ApplicationContextBuilder();
		for (int i = 0; i < names.length; i++) {
			b.project(names[i]);
		}
		return b.build();
	}

	public static class ApplicationContextBuilder {

		private List<String> urlList = new LinkedList<String>();

		public ApplicationContextBuilder project(String name) {
			File file = new File(System.getProperty("user.dir"), name);
			urlList.add(file.getPath());
			return this;
		}

		public FileSystemXmlApplicationContext build() {
			return new FileSystemXmlApplicationContext(
					urlList.toArray(new String[0]));
		}
	}
}
