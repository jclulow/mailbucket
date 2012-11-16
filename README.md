# mailbucket

This was a quick hack to be an open SMTP server for testing software that wants
to send mail.  It accepts SMTP connections on some port, and for each mail
message sent it writes a new file (named for the current time) with the
contents of the received message.  It will also break out MIME attachments into
separate files (e.g. PDFs, etc).

## Build

I tested this with Java 1.6.0\_26 and Apache Maven 2.2.1.  I'm not sure if it
works with Maven 3, or Java 7, etc.  To build a JAR file that contains all
required dependencies for deployment, use:

```bash
mvn assembly:assembly
```

Assuming the build is successful, you'll have a file in `target/`:

```
target/mailbucket-0.0-SNAPSHOT-jar-with-dependencies.jar
```

## Usage

```text
java -jar mailbucket*.jar    <directoryForMail> [<port>]
  (port defaults to 60025 if not provided)
```

A typical deployment of this tool uses some part of the web root of a webserver
to expose the mail files, and redirects output to a log file,  e.g.

```bash
java -jar /opt/mailbucket/mailbucket.jar \
   /var/www/htdocs/mailroot \
   10025 \
   >>/var/www/htdocs/mailroot/logfile_10025.txt
```
