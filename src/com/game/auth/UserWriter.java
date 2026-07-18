package com.game.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;
/**
 * This class handles the data writing into the file
 * It saves the data into the file in JSON format
 * @author: Boya Liu, Maneet Singh,
 * @email: boya.liu@ucalgary.ca, maneet.singh1@ucalgary.ca,
 */

public class UserWriter {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void WriteUser(String filename, List<User> users) throws IOException{
        mapper.writeValue(new File(filename), users);
    }
}
