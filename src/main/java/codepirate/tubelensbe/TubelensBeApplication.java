package codepirate.tubelensbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TubelensBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TubelensBeApplication.class, args);
	}

}
