package org.suchushin.projects.service.security;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class SpringWebMvcInitializer extends
        AbstractAnnotationConfigDispatcherServletInitializer {

  protected Class<?>[] getRootConfigClasses() {
    return new Class[]{SecurityConfig.class};
  }

  protected Class<?>[] getServletConfigClasses() {
    return new Class[0];
  }


  protected String[] getServletMappings() {
    return new String[0];
  }
}
