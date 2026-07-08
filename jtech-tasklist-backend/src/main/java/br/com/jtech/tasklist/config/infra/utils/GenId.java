package br.com.jtech.tasklist.config.infra.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
* class GenId
*
* @author: angelo.vicente
*/
@UtilityClass
public class GenId {

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static String newId(String id) {
        return (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
    }
}
