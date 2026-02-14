package unics.api;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		System.out.println("URL=" + System.getenv("DB_URL"));
		System.out.println("USER=" + System.getenv("DB_USER"));
		System.out.println("PASS=" + System.getenv("DB_PASSWORD"));
		SpringApplication.run(ApiApplication.class, args);
		
	}

}
