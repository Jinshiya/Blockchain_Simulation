
import java.util.ArrayList;

import java.util.Collections;
import java.net.*;  
import java.io.*;
import java.util.Date;
import java.util.Scanner;
public class NoobChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static ArrayList<Client> ClientData = new ArrayList<Client>();
	public static ArrayList<ClientWallet> ClientsWallet = new ArrayList<ClientWallet>();
	public static int difficulty = 5;	 

	public static void main(String[] args) {	
		
		int j = 0;
		int i = 0;
		int k = 0;
		int numClients = 3;
		int numChallenges = 2;
		long timestamp = 0, tcheck;
		Boolean nextChallenge = true;
		//Scanner userInput = new Scanner(System.in);
		Scanner DataInput = new Scanner(System.in);
		Scanner DifficultyInput = new Scanner(System.in);
		
		String Data;
		
		int coins;
		int award;

		System.out.println("\nServer is  Mining Genesis block ... ");
		addBlock(new Block("Hi im the first block", "0", difficulty));
		
		try {
			ServerSocket serverSocket=new ServerSocket(3333);

		while( j != numChallenges){
				
			Socket[] client = new Socket[numClients];
			ObjectOutputStream[] serverOutputChallenge = new ObjectOutputStream[numClients];
			ObjectInputStream[] serverInputResult = new ObjectInputStream[numClients];
			Thread[] t = new Thread[numClients];
			
				try {			
							
					System.out.printf("\nCreating Chaallenge No....%d...", j+1);
					System.out.println("\nEnter data for challenge block :- ");
					Data = DataInput.nextLine();
					
					System.out.println("\nProvide difficulty level( Integer )for block...");
					difficulty  = DifficultyInput.nextInt();
					
					Block challenge = new Block( Data, blockchain.get(blockchain.size()-1).hash, difficulty );
					System.out.println("\nChallenge is created...");
					
					System.out.println("\nWaiting for clients..");
					
					i = 0;
					
					 while (i != numClients)  
				        {	
						 	System.out.printf("\nWaiting for client ..%d..", i);
						 	client[i] = serverSocket.accept();
						 	
						 	String ip = (((InetSocketAddress) client[i].getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
						 	
					 	if (isExist (ClientData, ip)) {
						 		
						 		client[i].close();
						 		continue;
						 	}
						 						 
				            try 
				            {				                       
				             
				                serverOutputChallenge[i] = new ObjectOutputStream(client[i].getOutputStream());
				                serverInputResult[i] = new ObjectInputStream(client[i].getInputStream());
				     
				                // create a new thread object 
				               				        		
				    			Client C = new Client(i, ip);
				    			ClientData.add(C);
				    			ClientData.get(i).timestamp = new Date().getTime() + 50000000;
				    			
				    			if(!isWallet(ClientsWallet, ip)) {
				    				
				    				ClientWallet W = new ClientWallet(ip, 0);
				    				ClientsWallet.add(W);
				    			}
				    			
				    			serverOutputChallenge[i].writeObject(blockchain);
				    			serverOutputChallenge[i].flush();
				    			
				    			System.out.println("\nAssigning new thread for this client"); 
				    			
				        		t[i] = new ClientHandler(client[i], serverOutputChallenge[i], serverInputResult[i], challenge, difficulty, ClientData, i, timestamp); 
				        		
				        		if(i == numClients-1) {
				                	timestamp = new Date().getTime();
				                	timestamp += 2000;
				                	tcheck = 0;
				                	while(tcheck <= timestamp){
				                		
				                		tcheck = new Date().getTime();
				                	}
				                }
				        		
				        		
				                // Invoking the start() method
				        		if(i == numClients-1) {
				        			
				        			k = 0;
				        			while(k < numClients) {
				        				
				        				t[k].start();
				        				k++;
				        			}
				        		}
				                
				                if(i == numClients-1) {
				                	timestamp = new Date().getTime();
				                	timestamp += 8000;
				                	tcheck = 0;
				                	while(tcheck <= timestamp){
				                		
				                		tcheck = new Date().getTime();
				                	}
				                }
				  
				            }
						catch (Exception e) {
						    e.printStackTrace();
							}  
				             
				            
				            i++;
				        } 
					i = 0;
					while(i != numClients) {
						System.out.printf("\nClient no. %d nonce :- %d", i, ClientData.get(i).nonce);
						System.out.printf("\nClient no. %d timestamp :- %d", i, ClientData.get(i).timestamp);
						i++;
				
					}
					
					i = 0;
					
					System.out.println("\nGot clients responses..");
					System.out.println("Verifying the block..");
							
					Sort(ClientData);
					
					if( j == numChallenges-1 ) {
						
						nextChallenge = false;
									
					}else {
						
						nextChallenge = true;
					}
					
					
					while(i != numClients){			
						
						if(verifyBlock(challenge, ClientData.get(i).nonce)) {
							
							award = 10;
							System.out.println("\nBlock is added to the chain..");
							System.out.printf("\nClient %s result is accepted...", ClientData.get(i).ip);
							coins = UpdateClientsWallet(ClientsWallet, ClientData.get(i).ip, award);	
							ServerAnswer Answer = new ServerAnswer(1, nextChallenge, coins);
				
							serverOutputChallenge[ClientData.get(i).id].writeObject(Answer);
							serverOutputChallenge[ClientData.get(i).id].flush();						
							break;
							
						}else{
							
							award = 0;
							System.out.printf("\nClient %s result is INCORRECT...", ClientData.get(i).ip);
							coins = UpdateClientsWallet(ClientsWallet, ClientData.get(i).ip, award);	
							ServerAnswer Answer = new ServerAnswer(2, nextChallenge, coins);
					
							serverOutputChallenge[ClientData.get(i).id].writeObject(Answer);
							serverOutputChallenge[ClientData.get(i).id].flush();
							i++;							
						}						
					}
					
					if (i == numClients) {
						
						i = 0;
						while( i!= numClients) {
							System.out.println("closing");
							client[i].close();
						    i++;
						}
						ClientData.clear();
						continue;						
					}					
					k = i+1;
					
					while( k < numClients ) {
							
						if(ClientData.get(k).nonce == 0){
							award = 0;
							System.out.printf("\nClient %s did not send answer within given time...", ClientData.get(k).ip);
							coins = UpdateClientsWallet(ClientsWallet, ClientData.get(k).ip, award);
							ServerAnswer Answer = new ServerAnswer(4, nextChallenge, coins);
							serverOutputChallenge[ClientData.get(k).id].writeObject(Answer);
							serverOutputChallenge[ClientData.get(k).id].flush();
							k++;	
						}else {
							
							award = 0;
							System.out.printf("\nClient %s answer is not chosen for verification because of late submission...", ClientData.get(k).ip);
							coins = UpdateClientsWallet(ClientsWallet, ClientData.get(k).ip, award);
							ServerAnswer Answer = new ServerAnswer(3, nextChallenge, coins);
							serverOutputChallenge[ClientData.get(k).id].writeObject(Answer);
							serverOutputChallenge[ClientData.get(k).id].flush();
							k++;
						}
					}
					
					
					}
					catch (Exception e){ 
		                System.out.print(e); 
		            }
					ClientData.clear();
					
					System.out.println("\nBlockchain is Valid: " + isChainValid());
					
					String blockchainJson = StringUtil.getJson(blockchain);
					System.out.println("\nThe block chain: ");
					System.out.println(blockchainJson);
					
					String ClientsWalletJson = StringUtil.getJson(ClientsWallet);
					
					System.out.println("\nClients Wallet: ");
					System.out.println(ClientsWalletJson);	
					
					i = 0;
					while(i < numClients) {
					
						client[i].close();
					    i++;
					}	     			
			j++;
		}
		
		serverSocket.close();
		
		DataInput.close();
		DifficultyInput.close();
		
		}catch(Exception e) {
			e.printStackTrace();
		}	
		System.out.println("Server exitting..");	
		
	}
		
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}
			
		}
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
	public static boolean verifyBlock (Block challenge, int newnonce) {
		if(challenge.verify(newnonce, difficulty)) {
			blockchain.add(challenge);
			return true;
		}else {
			return false;
		}
	}
	
	public static void Sort(ArrayList<Client> Chain){
		
		int n = Chain.size();
		
		for (int i = 0; i < n-1; i++) {
		
			int min_idx = i;
			for (int j = i+1; j < n; j++)
				if(Chain.get(j).timestamp < Chain.get(min_idx).timestamp)
					min_idx = j;
			
			Collections.swap(Chain, min_idx, i);
		}
	}	
	
	public static Boolean isExist (ArrayList<Client> Chain, String ip) {
		
		int i = 0;
		int size = Chain.size();
		
			while ( i < size ) {
				
				if( Chain.get(i).ip.equals(ip) ) {
					
					return true; 
				}else {
					
					i++;
				}
			}	
			return false;
		}
	
	public static Boolean isWallet (ArrayList<ClientWallet> Chain, String ip) {
		
		int i = 0;
		int size = Chain.size();
		
			while ( i < size ) {
				
				if( Chain.get(i).ip.equals(ip) ) {
					
					return true; 
				}else {
					
					i++;
				}
			}	
			return false;
		}
	
	public static int UpdateClientsWallet(ArrayList<ClientWallet> Chain, String ip, int award) {
		
		int i = 0;
		int size = Chain.size();
		
			while ( i < size ) {
				
				if( Chain.get(i).ip.equals(ip) ) {
					
					 Chain.get(i).update(award);
					 return Chain.get(i).getCoins();
					 
				}else {
					
					i++;
				}
			}
		return 0;	
	}
	
}