# Heartbleed Attack: Java-Based Attack Demo

## Overview

This repository features `Heartbleed.java`, a custom Java-based exploit designed to demonstrate the Heartbleed vulnerability (CVE-2014-0160) by extracting sensitive data from a vulnerable server (IP: 192.168.1.132) running Apache (port 443) and Postfix (port 465). The exploit sends malformed TLS heartbeat requests, leveraging the vulnerability in unpatched OpenSSL versions (1.0.1 to 1.0.1f) to leak up to 64KB of memory per request. Supporting Bash scripts (`apache_memload.sh` and `postfix_memload.sh`) flood the services’ memory with sensitive data, such as user credentials, mail server passwords, and SSL certificates, which the Java exploit then extracts.

This project is part of a comprehensive security demonstration documented in a detailed Word document, which outlines the attack methodology, results, and implications for both Apache and Postfix services. The work involved crafting scripts to load memory, executing the exploit from a Windows client (IP: 192.168.1.99) and analyzing the leaked data to highlight the vulnerability’s impact.

## About Heartbleed Expolit

`Heartbleed.java` is the core of this demo, implementing a streamlined attack process:
- Establishes a connection to the target server and completes a TLS handshake.
- Sends malformed heartbeat requests via the `sendHeartbeats` method, which is central to the exploit.
- Captures and displays the server’s response, revealing sensitive data in ASCII format.

The `sendHeartbeats` method, a key component, orchestrates the attack by repeatedly sending crafted heartbeat messages to the server, exploiting the Heartbleed vulnerability to extract memory contents. This method’s role is detailed in the accompanying Word document, alongside a screenshot of its implementation.

## Project Scope and Documentation

The full scope of this work includes:
- **Memory Flooding**: Developed `apache_memload.sh` to flood Apache’s memory with 50 `curl` requests, embedding sensitive data like SHA-512 password hashes via a PHP script (`memload.php`). Similarly, `postfix_memload.sh` sends 50 SMTP payloads to Postfix, embedding email credentials and passwords.
- **Exploit Execution**: Ran `Heartbleed.java` from a Windows client (IP: 192.168.1.99) to target Apache (port 443) and Postfix (port 465), successfully leaking data such as user credentials, passwords, and certificates.
- **Documentation**: Compiled a detailed Word document capturing the attack setup, execution, and results. It includes service verification (Apache and Postfix), attack descriptions, screenshots of script outputs and leaked data, an explanation of the `sendHeartbeats` method, and a conclusion with future mitigation strategies.

## Prerequisites

- **Server**:
  - Ubuntu Linux server (IP: 192.168.1.132) with:
    - Apache2 on port 443 (vulnerable OpenSSL 1.0.1c).
    - Postfix on port 465 (vulnerable OpenSSL 1.0.1c).
    - `memload.php` at `https://localhost/phpmyadmin/memload.php`.
- **Client**:
  - Windows client (IP: 192.168.1.99).
  - Java Development Kit (JDK): `sudo apt install openjdk-11-jdk`.
- **Network**:
  - Client must reach server on ports 443 and 465.

## Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Shayhha/HeartbleedAttack
   ```

2. **Compile the Exploit**:
   ```bash
   javac -d bin Heartbleed.java
   ```

## Usage

1. **Flood Server Memory** (on 192.168.1.132):
- Apache:
```bash
~/Desktop/apache_memload.sh
```

- Postfix:
```bash
~/Desktop/postfix_memload.sh
```

2. **Run the Exploit** (on 192.168.1.99):
- For Apache (port 443) edit `Heartbleed.java` to set `SERVER_PORT = POSTFIX_PORT`, recompile, then:
```bash
javac -d bin Heartbleed.java
java -cp bin Heartbleed
```

- For Postfix (port 465) edit `Heartbleed.java` to set `SERVER_PORT = POSTFIX_PORT`, recompile, then:
```bash
javac -d bin Heartbleed.java
java -cp bin Heartbleed
```

## Results

- **Apache**: Leaked user credentials, mail server passwords, and SHA-512 password hashes and SSL certificates.
- **Postfix**: Extracted email headers, shell commands, credentials, passwords, and SSL certificates.

## Ethical Use

This exploit is for educational purposes only. Do not use it to harm systems without explicit permission. Ensure compliance with legal and ethical standards.

## License

MIT License—see [LICENSE](LICENSE.txt) for details.
