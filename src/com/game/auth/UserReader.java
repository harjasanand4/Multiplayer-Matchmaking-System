package com.game.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class handles the data reading from existing file
 * It read the JSON data saved in a file create database with that
 * @author: Boya Liu, Maneet Singh,
 * @email: boya.liu@ucalgary.ca, maneet.singh1@ucalgary.ca,
 */
public class UserReader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void loadUserFromFile(String filename, DatabaseStub database) throws IOException{
        List<User> users = mapper.readValue(new File(filename), new TypeReference<List<User>>() {});
        for(User user : users){
            database.addUser(user);
        }
    }
}
