<img align="right" width="120" height="120" title="Burst Logo" src="https://raw.githubusercontent.com/burst-apps-team/Marketing_Resources/master/BURST_LOGO/PNG/icon_blue.png" />

# Burstcoin Reference Software (Burstcoin Wallet)
[![Build Status](https://travis-ci.com/burst-apps-team/burstcoin.svg?branch=develop)](https://travis-ci.com/burst-apps-team/burstcoin)
[![GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE.txt)
[![Get Support at https://discord.gg/ms6eagX](https://img.shields.io/badge/join-discord-blue.svg)](https://discord.gg/ms6eagX)

The world's first HDD-mined cryptocurrency using an energy efficient
and fair Proof-of-Capacity (PoC) consensus algorithm.

This wallet version is developed and maintained by the Burst Apps Team (BAT). The three supported database servers are:

- MariaDB (recommended)
- H2 (embedded, easier install)
- SQLite (embedded, easier install)

## Network Features

- Proof of Capacity - ASIC proof / Energy efficient mining
- No ICO/Airdrops/Premine
- Turing-complete smart contracts, via [Automated Transactions (ATs)](https://ciyam.org/at/at.html)
- Asset Exchange, Digital Goods Store, Crowdfunds (via ATs), and Alias system

## Network Specification

- 4 minute block time
- Total Supply: [2,158,812,800 BURST](https://burstwiki.org/en/block-reward/)
- Block reward starts at 10,000/block
- Block Reward Decreases at 5% each month

## BRS Features

- Decentralized Peer-to-Peer network with spam protection
- Built in Java - runs anywhere, from a Raspberry Pi to a Phone
- Fast sync with multithreaded CPU or, optionally, an OpenCL GPU
- HTTP and gRPC API for clients to interact with network

# Installation

## Prerequisites (All Platforms)

**NOTE: `burst.sh` is now deprecated and will not be included with the next release.**

### Java 8 (Required)

You need Java 8 installed. To check if it is, run `java -version`. You should get an output similar to the following:

```text
java version "1.8.0_181"
Java(TM) SE Runtime Environment (build 1.8.0_181-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.181-b13, mixed mode)
```

The important part is that the Java version starts with `1.8` (Java 8)

If you do not have Java 8 installed, download it from [Oracle's Website](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### MariaDB (Optional)

[Download and install MariaDB](https://mariadb.com/downloads/mariadb-tx)

The MariaDb installation will ask to setup a password for the root user. 
Add this password to the `brs.properties` file you will create when installing BRS:

```properties
DB.Url=jdbc:mariadb://localhost:3306/databasename
DB.Username=root
DB.Password=YOUR_PASSWORD
```

Please note that you will need to create a database and grant all priveleges to the user you specify, and substitute `databasename` in the above example with the name of your database.

## Installation

You can manually install using the following steps, or by using the pre-packaged options below.

### Manually installing - All Platforms

Grab the latest release (Or, if you prefer, compile yourself using the instructions below)

In the conf directory, copy `brs-default.properties` into a new file named `brs.properties` and modify this file to suit your needs (See "Configuration" section below)

To run BRS, run `java -jar burst.jar`. On MacOS and Windows this will create a tray icon to show that BRS is running. To disable this, instead run `java -jar burst.jar --headless`.

### Installation Packages

#### Windows

[QBundle](https://github.com/burst-apps-team/qbundle) is a tool which will automatically download any required files and tools and manage BRS for you. This is recommended for users who do not want to learn how to setup BRS.

#### MacOS

BRS can be installed using a [Homebrew formula](https://github.com/burst-apps-team/burstcoin-packages/tree/master/homebrew/burstcoind).

A number of other Homebrew formulas written by [Nixops](https://github.com/nixops) are also available for plotters and miners.

#### Linux

##### Debian

A `.deb` package is available [here](https://github.com/burst-apps-team/burstcoin-packages/releases/tag/v2.3.0).

##### Docker

[Docker repository](https://hub.docker.com/r/burstappsteam/burstcoin)

`latest` : Latest version of BRS with H2 database

`mariadb` : Latest version of BRS with MariaDB database

`2-h2` / `2-mariadb` - Version 2.X.X (latest) with corresponding database

`2.4-h2` / `2.4-mariadb` - Version 2.4.X (latest) with corresponding database

`2.4.2-h2` / `2.4.2-mariadb` - Version 2.4.2 with corresponding database

`edge-testnet` - The latest snapshot development build

Docker Compose for use with MariaDB database

```yaml
version: '3'

services:
  burstcoin:
    image: burstappsteam/burstcoin:2-mariadb
    restart: always
    depends_on:
     - mariadb
    ports:
     - 8123:8123
     - 8125:8125
     - 8121:8121
  mariadb:
    image: mariadb:10
    environment:
     - MYSQL_ROOT_PASSWORD=burst
     - MYSQL_DATABASE=burst
    command: mysqld --character_set_server=utf8mb4
    volumes:
     - ./burst_db:/var/lib/mysql
```

Docker command for use with H2 database

```shell script
docker run -p 8123:8123 -p 8125:8125 -p 8121:8121 -v "$(pwd)"/burst_db:/db -d burstappsteam/burstcoin:2-h2
```

## Configuration

### Running on mainnet (unless you are developing or running on testnet, you will probably want this)

Add the following to your `conf/brs.properties` (as a minimum):

```properties
DB.Url=jdbc:mariadb://localhost:3306/brs_master
DB.Username=brs_user
DB.Password=yourpassword
```

Once you have done this, look through the existing properties if there is anything you want to change.

### Testnet

Please see the [Wiki article](https://burstwiki.org/en/testnet/) for details on how to setup a Testnet node.

# Building

## Build options

1. Enable Protobuf Compiler: `./gradlew -DrunProtoc=true ...`

2. Enable jOOQ generator: `./gradlew -DrunJooq=true ...`

3. Enable headless build (if you do not have JavaFX and get errors in `BurstGUI.kt` when compiling, this skips building the BurstGUI launcher): `./gradlew -Dheadless=true ...`

## Building the latest stable release

Run these commands (`master` is always the latest stable release):

```shell script
git fetch --all --tags --prune
git checkout origin/master
./gradlew buildPackage
```

**If you get errors relating to `BurstGUI.kt`, try running a headless build as shown above.**

Your package will now be available in `dist/burstcoin.zip`

## Building the latest development version

Run these commands (`develop` is always the latest development version):

```shell script
git fetch --all --tags --prune
git checkout origin/develop
./gradlew buildPackage
```

**If you get errors relating to `BurstGUI.kt`, try running a headless build as shown above.**

Your package will now be available in `dist/burstcoin.zip`.

**Please note that development builds will refuse to run on Mainnet.**

## Running tests

```shell script
./gradlew clean test
```

**If you get errors relating to `BurstGUI.kt`, try running a headless build as shown above.**

# Developers

Main Developer: [Harry1453](https://github.com/harry1453). Donation address: [BURST-W5YR-ZZQC-KUBJ-G78KB](https://explore.burstcoin.network/?action=account&account=16484518239061020631)

For more information, see [Credits](doc/Credits.md)

# Further Documentation

* [Version History](doc/History.md)

* [Credits](doc/Credits.md)

* [References/Links](doc/References.md)
