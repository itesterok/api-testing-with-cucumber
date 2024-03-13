package com.apilayer.api.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class CustomResponseCode400Converter extends AbstractGenericHttpMessageConverter<String> {
    @Override
    protected void writeInternal(String string, Type type, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {

        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        return new BufferedReader(new InputStreamReader(inputMessage.getBody()))
            .lines()
            .collect(Collectors.joining("\n"));
    }
}
