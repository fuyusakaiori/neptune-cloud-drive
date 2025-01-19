package com.neptune.cloud.drive.validator;

import com.neptune.cloud.drive.constant.StringConstant;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@SpringBootConfiguration
public class WebValidatorConfig {

    /**
     * 快速失败: 只要一个参数不符合条件就会直接返回校验失败
     */
    private final static String FAIL_FAST_KEY = "hibernate.validator.fail_fast";

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.setValidator(validator());
        return postProcessor;
    }

    private Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure().addProperty(FAIL_FAST_KEY, StringConstant.TRUE)
                .buildValidatorFactory();
        return validatorFactory.getValidator();
    }

}
