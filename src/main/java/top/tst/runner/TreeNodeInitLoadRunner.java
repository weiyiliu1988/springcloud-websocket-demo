package top.tst.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TreeNodeInitLoadRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(TreeNodeInitLoadRunner.class);

	@Override
	public void run(String... args) throws Exception {
		logger.warn("[---服务启动数据初始化至Redis开始---]");
		logger.warn("[---服务启动数据初始化至Redis结束---]");
	}

}
