package com.github.senocak.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    private static final Logger log = LoggerFactory.getLogger(EmailValidator.class);

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        log.info("EmailValidator initialized");
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        if (Objects.isNull(email))
            return false;
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-+]"+
                "(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*"+ "(.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
