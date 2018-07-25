package fr.wcs.firebasechat;

/**
 * Created by wilder on 08/05/18.
 */

public class Users {
    // Attributs ecrit comme dans la Firebase
    public String name;
    public String image;
    public String status;

    public Users(String name, String image, String status) {
        this.name = name;
        this.image = image;
        this.status = status;
    }
    public  Users(){
      // constructeur vite pour la Firebase
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
