package st.old.isis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ExtractionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExtractionApplication.class, args);
	}

}
