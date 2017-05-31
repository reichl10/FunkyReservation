package funky.pom16.funkyreservation.backend.data;

/**
 * Created by Sebastian Reichl on 07.06.2016.
 */
public class User {
    private String username, pwdHash, fName, lName;
    Object picture = new Object();

    public User(String name, String hash, String firstName, String lastName){
        username = name;
        pwdHash = hash;
        fName = firstName;
        lName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public Object getPicture() {
        return picture;
    }

    public void setPicture(Object newPicture) {
        picture = newPicture;
    }
}
