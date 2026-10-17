package org.roniyaakobi.utils;

public class User {
    private String name;
    private String email;

    public User(String name, String email){
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString(){
        return "User[name=" + name + ",email=" + email + "]";
    }

    public String toSerializable(){
        return name + " " + email;
    }

    public static User deserialize(String serialized){
        String cleaned = serialized.strip().substring(4);
        cleaned = cleaned.substring(0,cleaned.length() - 1);

        String[] args = cleaned.split(",");

        String name = args[0].split("=")[1];
        String email = args[1].split("=")[1];

        return new User(name, email);
    }
}
