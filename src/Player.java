package src;

public class Player {
    //protected TCPConnection con;
    protected String usr;
    protected String pwd;
    protected int points;

    public Player(String usr, String pwd) {
        //this.con = con;
        this.usr = usr;
        this.pwd = pwd;
        this.points = 0;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getPoints() {
        return points;
    }

    public void incPoints() {
        this.points++;
    }

    public void resetPoints() {
        this.points = 0;
    }
}
