package it.polimi.tiw.utils;

import javax.servlet.ServletContext;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * This class is used to initialize Thymeleaf.
 * It sets the template mode, the template resolver, and the template engine.
 */
public abstract class ThymeleafInitializer {
	public static TemplateEngine initialize(ServletContext servletContext) {
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		
		TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setPrefix("WEB-INF/");
		templateResolver.setSuffix(".html");
		templateResolver.setCacheable(false); // TODO: remove this
		return templateEngine;
	}
}
