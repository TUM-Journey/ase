package de.tum.ase.kleo.application.config.ethereum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import de.tum.ase.kleo.ethereum.AttendanceTracker;

@Configuration
public class AttendanceTrackerConfig {

    @Value("${ethereum.attendanceTracker.address}")
    private String address;

    @Bean
    AttendanceTracker attendanceTracker(Web3j web3j, Credentials credentials) {
        return AttendanceTracker.load(address, web3j, credentials,
                ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
    }
}
