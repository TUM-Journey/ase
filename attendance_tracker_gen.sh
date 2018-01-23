#!/bin/sh

rm -rf .solidity/ 2> /dev/null
mkdir .solidity/

solc attendance_tracker.sol --bin --abi --optimize -o ./.solidity/
web3j solidity generate .solidity/AttendanceTracker.bin .solidity/AttendanceTracker.abi -p de.tum.ase.kleo.ethereum -o .solidity/
cp -r .solidity/de ./backend/src/main/java
cp -r .solidity/de ./android/src/main/java