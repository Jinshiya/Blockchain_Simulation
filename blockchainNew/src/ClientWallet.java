
public class ClientWallet {
	
	String ip;
	private int coins;
	
	public ClientWallet(String ip, int coins){
		
		this.ip = ip;
		this.coins = coins;
	}
	
	public void update(int award) {
		
		coins = coins + award;
	}
	public int getCoins() {
		
		return coins;
	}
	
}
