package src;

public class Player {
    //protected TCPConnection con;
    protected String usr;
    protected String pwd;
    protected int score;

    public Player(String usr, String pwd) {
        //this.con = con;
        this.usr = usr;
        this.pwd = pwd;
        this.score = 0;
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

    public void updateScore() { score++; }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetScore() {
        this.score= 0;
    }
}
