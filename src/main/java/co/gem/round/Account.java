package co.gem.round;

import co.gem.round.coinop.MultiWallet;
import co.gem.round.patchboard.Client;
import co.gem.round.patchboard.Resource;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Account extends Base {

  public static final String RESOURCE_NAME = "account";

  private Wallet wallet;
  private TransactionCollection transactions;

  public Account(String url, Round round)
      throws Client.UnexpectedStatusCodeException, IOException {
    super(url, round, RESOURCE_NAME);
  }

  public Account(Resource resource, Round round) {
    super(resource, round);
  }

  public Address createAddress()
      throws IOException, Client.UnexpectedStatusCodeException {
    Resource addressResource = resource.subresource("addresses").action("create");

    return new Address(addressResource, this.round);
  }

  public TransactionCollection transactions()
      throws IOException, Client.UnexpectedStatusCodeException {
    if (this.transactions == null) {
      Resource transactionsResource = resource.subresource("transactions");
      this.transactions = new TransactionCollection(transactionsResource, this.round);
    }

    return this.transactions;
  }

  public String name() {
    return getString("name");
  }

  public long balance() {
    return getLong("balance");
  }

  public long pendingBalance() {
    return getLong("pending_balance");
  }

  public void setWallet(Wallet wallet) {
    this.wallet = wallet;
  }

  public Payment payToEmail(String passphrase, String email, long amount)
      throws IOException, Client.UnexpectedStatusCodeException {
    Recipient recipient = Recipient.recipientWithEmail(email, amount);
    return this.pay(passphrase, recipient);
  }

  public Payment payToAddress(String passphrase, String address, long amount)
      throws IOException, Client.UnexpectedStatusCodeException {
    return this.pay(passphrase, Recipient.recipientWithAddress(address, amount));
  }

  public Payment pay(String passphrase, Recipient recipient)
      throws IOException, Client.UnexpectedStatusCodeException {
    List<Recipient> recipients = Arrays.asList(new Recipient[]{recipient});
    return this.pay(passphrase, recipients);
  }

  public Payment pay(String passphrase, List<Recipient> recipients)
      throws IOException, Client.UnexpectedStatusCodeException {
    final Payment payment = this.createUnsignedPayment(recipients);
    this.wallet.unlock(passphrase, new UnlockedWalletCallback() {
      @Override
      public void execute(MultiWallet wallet) throws IOException, Client.UnexpectedStatusCodeException {
        payment.sign(wallet);
      }
    });
    return payment;
  }

  public Payment createUnsignedPayment(List<Recipient> recipients)
      throws IOException, Client.UnexpectedStatusCodeException {
    JsonArray recipientsJson = new JsonArray();
    for (Recipient recipient : recipients) {
      JsonObject payeeJson = new JsonObject();
      if (recipient.email != null) {
        payeeJson.addProperty("email", recipient.email);
      } else if (recipient.address != null) {
        payeeJson.addProperty("address", recipient.address);
      }

      JsonObject recipientJson = new JsonObject();
      recipientJson.add("payee", payeeJson);
      recipientJson.addProperty("amount", recipient.amount);

      recipientsJson.add(recipientJson);
    }

    JsonObject body = new JsonObject();
    body.add("outputs", recipientsJson);

    Resource paymentResource = resource.subresource("payments").action("create", body);

    return new Payment(paymentResource, this.round);
  }

}
