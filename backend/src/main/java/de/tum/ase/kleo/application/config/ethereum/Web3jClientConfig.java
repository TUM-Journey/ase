package de.tum.ase.kleo.application.config.ethereum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;

import static org.web3j.crypto.WalletUtils.loadCredentials;

@Configuration
public class Web3jClientConfig {

    @Value("${ethereum.infura}")
    private String infuraEndpoint;

    @Value("${ethereum.wallet.password}")
    private String walletPassword;

    @Value("${ethereum.wallet.file}")
    private String walletFile;

    @Bean
    Web3j web3j() {
        return Web3j.build(new HttpService(infuraEndpoint));
    }

    @Bean
    Credentials web3jCredentials() throws IOException, CipherException {
        return loadCredentials(walletPassword, walletFile);
    }
}
