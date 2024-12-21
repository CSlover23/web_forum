package com.nowcoder.community;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

	/*注意：下面代码是培训视频中因redis与elasticsearch均依赖netty导致冲突而进行的代码修正，但是
	* 这里elasticsearch7.0之后的版本不再出现该问题（培训视频中elasticsearch版本是6.x版本），因此下面代码仅供浏览*/
//	@PostConstruct
//	public void init() {
//		// 解决netty启动冲突问题
//		// see Netty4Util.setAvailableProcessors
//		System.setProperty("es.set.netty.runtime.available.processors", "false");
//	}

	public static void  main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
