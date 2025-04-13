<?php
// Sensitive data
$email1 = "shay@shay.local";
$email2 = "user@user.local";
$password = "mailpassword";
$secret = "LeakThis: Secret42";

// Generate a large string with sensitive data
$data = str_repeat("$email1:$password:$email2:$secret:", 10000);

// Simulate MySQL query for virtual users
$conn = @mysqli_connect("127.0.0.1", "mailuser", "mailpassword", "mailserver");
if ($conn) {
    $result = $conn->query("SELECT email, password FROM virtual_users");
    while ($row = $result->fetch_assoc()) {
        $data .= $row['email'] . ":" . $row['password'] . ":";
    }
    $conn->close();
}

// Keep data in memory and output
echo $data;
?>