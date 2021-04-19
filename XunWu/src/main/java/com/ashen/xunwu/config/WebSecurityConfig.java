package com.ashen.xunwu.config;

import com.ashen.xunwu.security.AuthFilter;
import com.ashen.xunwu.security.AuthProvider;
import com.ashen.xunwu.security.LoginAuthFailHandler;
import com.ashen.xunwu.security.LoginUrlEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * HTTP权限控制
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 添加拦截器
        http.addFilterBefore(authFilter(), UsernamePasswordAuthenticationFilter.class);

        // 设置资源访问权限
        http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() //登录入口静态资源放权
                .antMatchers("/static/**").permitAll()
                .antMatchers("/user/login").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/api/user/**").hasAnyRole("ADMIN", "USER")
                .and()
                .formLogin()                        //指定支持基于表单的身份验证
                .loginProcessingUrl("/login")       //指定用于验证凭据的URL
                .failureHandler(authFailHandler())  //指定登录验证失败处理器
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logout/page")   //登出后跳转页面
                .deleteCookies("JSESSIONID")        //登出后删除cookie中的JSESSIONID 
                .invalidateHttpSession(true)        //登出后使会话失效
                .and()
                .exceptionHandling()                        //允许配置异常处理
                .authenticationEntryPoint(urlEntryPoint())  //指定登录入口控制器
                .accessDeniedPage("/403");                  //指定拒绝访问的URL
        
        // 关闭csrf防御
        http.csrf().disable();
        // 使用同源策略
        http.headers().frameOptions().sameOrigin();
    }

    /**
     * 自定义认证策略
     */
    @Autowired
    public void configGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // 内存中设置用户和角色
        // auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN").and();
        
        // 走自定义身份认证类
        auth.authenticationProvider(authProvider()).eraseCredentials(true);  //擦除密码
    }

    /**
     * 自定义身份认证类
     */
    @Bean
    public AuthProvider authProvider() {
        return new AuthProvider();
    }

    /**
     * 登录入口控制器
     */
    @Bean
    public LoginUrlEntryPoint urlEntryPoint() {
        return new LoginUrlEntryPoint("/user/login");
    }

    /**
     * 登录验证失败处理器
     */
    @Bean
    public LoginAuthFailHandler authFailHandler() {
        return new LoginAuthFailHandler(urlEntryPoint());
    }

    /**
     * 认证管理器
     * @return
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        AuthenticationManager authenticationManager = null;
        try {
            authenticationManager = super.authenticationManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authenticationManager;
    }

    /**
     * 身份验证拦截器
     */
    @Bean
    public AuthFilter authFilter() {
        AuthFilter authFilter = new AuthFilter();
        authFilter.setAuthenticationManager(authenticationManager());   // 设置认证管理器
        authFilter.setAuthenticationFailureHandler(authFailHandler());  // 设置认证失败处理器
        return authFilter;
    }
    
}
