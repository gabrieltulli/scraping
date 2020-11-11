package tulli.com.br.scrapingtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
// We use direct @Import instead of @ComponentScan to speed up cold starts
// @ComponentScan(basePackages = "tulli.com.br.controller")
public class Application {

//	/*
//	 * Create required HandlerMapping, to avoid several default HandlerMapping
//	 * instances being created
//	 */
//	@Bean
//	public HandlerMapping handlerMapping() {
//		return new RequestMappingHandlerMapping();
//	}
//
//	/*
//	 * Create required HandlerAdapter, to avoid several default HandlerAdapter
//	 * instances being created
//	 */
//	@Bean
//	public HandlerAdapter handlerAdapter() {
//		return new RequestMappingHandlerAdapter();
//	}
//
//	/*
//	 * optimization - avoids creating default exception resolvers; not required as
//	 * the serverless container handles all exceptions
//	 *
//	 * By default, an ExceptionHandlerExceptionResolver is created which creates
//	 * many dependent object, including an expensive ObjectMapper instance.
//	 *
//	 * To enable custom @ControllerAdvice classes remove this bean.
//	 */
//	@Bean
//	public HandlerExceptionResolver handlerExceptionResolver() {
//		return new HandlerExceptionResolver() {
//
//			@Override
//			public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
//					Object handler, Exception ex) {
//				return null;
//			}
//		};
//	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}