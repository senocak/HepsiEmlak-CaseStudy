package com.github.senocak.util.validator;

import com.github.senocak.dto.user.UpdateUserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    private static final Logger log = LoggerFactory.getLogger(PasswordMatchesValidator.class);

    @Override
    public void initialize(PasswordMatches passwordMatches) {
        log.info("PasswordMatchesValidator initialized");
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
        if (obj.getClass().equals(UpdateUserDto.class)) {
            UpdateUserDto userDto = (UpdateUserDto) obj;
            return userDto.getPassword().equals(userDto.getPassword_confirmation());
        }
        return false;
    }

}