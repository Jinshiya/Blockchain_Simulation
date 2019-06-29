import java.io.*;

public class ServerAnswer implements Serializable{
	
	public int status;
	public Boolean nextChallenge;
	public int coins;
	public ServerAnswer(int status, Boolean nextChallenge, int coins) {
		
		this.status = status;
		this.nextChallenge = nextChallenge;
		this.coins = coins;
	}

}
