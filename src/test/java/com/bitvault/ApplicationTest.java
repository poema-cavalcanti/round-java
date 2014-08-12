package com.bitvault;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {
	private static final String apiToken = "PfyzNzRlv6AgV3P32a87MTXSMZhuCAG7dVIZBbZS0lc";
	private static final String appKey = "h51G8o1ZNTOwkUh0waBryQ";
	
	private static Client client = new Client(appKey, apiToken);
	
	@Test public void testGetWallets() throws IOException {
		WalletCollection wallets = client.application().wallets();
		
		Assert.assertNotNull(wallets.url);
	}
}