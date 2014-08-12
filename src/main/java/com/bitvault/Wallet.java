package com.bitvault;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.google.gson.JsonObject;
import com.neilalexander.jnacl.EncryptedMessage;
import com.neilalexander.jnacl.PassphraseBox;

public class Wallet extends Resource {
	
	protected String resourceName;
	
	private AccountCollection accountsCollection;

	private String primarySeed;
	
	public static final String RESOURCE_NAME = "wallet";
	
	public Wallet(String url, Client client) {
		super(url, client, RESOURCE_NAME);
	}
	
	public Wallet(JsonObject resource, Client client) {
		super(resource, client, RESOURCE_NAME);
	}
	
	public String unlock(String passphrase) {
		if (this.primarySeed ==  null) {
		    JsonObject seedObject = this.resource.getAsJsonObject("primary_private_seed");
		    
		    EncryptedMessage encryptedMessage = new EncryptedMessage();
		    encryptedMessage.ciphertext = seedObject.get("ciphertext").toString();
		    encryptedMessage.salt = seedObject.get("salt").toString();
		    encryptedMessage.nonce = seedObject.get("nonce").toString();
		    encryptedMessage.iterations = Integer.parseInt(seedObject.get("iterations").toString());
		    
		    try {
				this.primarySeed = PassphraseBox.decrypt(passphrase, encryptedMessage);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
		}
		
		return this.primarySeed;
	}
	
	public AccountCollection accounts() {
		if (this.accountsCollection == null) {
			this.accountsCollection = new AccountCollection(this.url, this.client);
		}
		
		return this.accountsCollection;
	}
	
}
