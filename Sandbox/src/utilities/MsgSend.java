/*
 * @(#)MsgSend.java 1.17 03/04/22
 *
 * Copyright 1997-2003 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
package utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * Demo app that shows how to construct and send an RFC822 (singlepart) message.
 * 
 * XXX - allow more than one recipient on the command line
 * 
 * @author Max Spivak
 * @author Bill Shannon
 * @author John Fruncek
 */

public class MsgSend {

    private static final String USAGE_TEXT = "Usage: MsgSend [[-L store-url] | [-T prot] [-H host] [-U user] [-P passwd]]"
            + "\n\t[-s subject] [-o from-address] [-r reply-to-address] [-c cc-addresses] [-b bcc-addresses]"
            + "\n\t[-f record-mailbox] [-M transport-host] [-f attachment-filename] [-X msg-text] [-d] [address]";

    private InternetAddress[] dis_list, reply_to_list;

    private List attachments;

    public static void main(String[] argv) {
        new MsgSend(argv);
    }

    public MsgSend(String[] argv) {
        doit(argv);
    }

    public MsgSend(String[] argv, String[] dis_list) {
        try {
            this.dis_list = makeInternetAddressArray(dis_list);
        }
        catch (AddressException e) {
            System.err.println("Bad email address format at pos " + e.getPos() + " in '" + e.getRef() + "'");
        }
        doit(argv);
    }

    public MsgSend(String[] argv, String[] dis_list, String[] reply_to_list) {
        try {
            this.dis_list = makeInternetAddressArray(dis_list);
            this.reply_to_list = makeInternetAddressArray(reply_to_list);
        }
        catch (AddressException e) {
            System.err.println("Bad email address format at pos " + e.getPos() + " in '" + e.getRef() + "'");
        }
        doit(argv);
    }

    public MsgSend(String[] argv, String[] dis_list, String[] reply_to_list, List attachments) {
        try {
            this.attachments = attachments;
            this.dis_list = makeInternetAddressArray(dis_list);
            this.reply_to_list = makeInternetAddressArray(reply_to_list);
        }
        catch (AddressException e) {
            System.err.println("Bad email address format at pos " + e.getPos() + " in '" + e.getRef() + "'");
        }
        doit(argv);
    }

    private InternetAddress[] makeInternetAddressArray(String[] names) throws AddressException {
        InternetAddress[] retval = null;
        if (names != null) {
            retval = new InternetAddress[names.length];
            for (int i = 0; i < names.length; i++) {
                retval[i] = new InternetAddress(names[i]);
            }
        }
        return retval;
    }

    private void doit(String[] argv) {
        String to = null, subject = null, from = null, replyTo = null, cc = null, bcc = null, url = null;
        String mailhost = null;
        String mailer = "MsgSend";
        String protocol = null, host = null, user = null, password = null, record = null;
        String filename = null, msg_text = null, inline_filename = null;
        boolean debug = false;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int optind;

        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-T")) {
                protocol = argv[++optind];
            }
            else if (argv[optind].equals("-X")) {
                msg_text = argv[++optind];
            }
            else if (argv[optind].equals("-H")) {
                host = argv[++optind];
            }
            else if (argv[optind].equals("-U")) {
                user = argv[++optind];
            }
            else if (argv[optind].equals("-P")) {
                password = argv[++optind];
            }
            else if (argv[optind].equals("-M")) {
                mailhost = argv[++optind];
            }
            else if (argv[optind].equals("-f")) {
                filename = argv[++optind];
            }
            else if (argv[optind].equals("-i")) {
                inline_filename = argv[++optind];
            }
            else if (argv[optind].equals("-s")) {
                subject = argv[++optind];
            }
            else if (argv[optind].equals("-o")) { // originator (from)
                from = argv[++optind];
            }
            else if (argv[optind].equals("-r")) { // reply-to
                replyTo = argv[++optind];
            }
            else if (argv[optind].equals("-c")) {
                cc = argv[++optind];
            }
            else if (argv[optind].equals("-b")) {
                bcc = argv[++optind];
            }
            else if (argv[optind].equals("-L")) {
                url = argv[++optind];
            }
            else if (argv[optind].equals("-d")) {
                debug = true;
            }
            else if (argv[optind].equals("--")) {
                optind++;
                break;
            }
            else if (argv[optind].startsWith("-")) {
                System.err.println(USAGE_TEXT);
                System.exit(1);
            }
            else {
                break;
            }
        }

        try {
            if (optind < argv.length) {
                // XXX - concatenate all remaining arguments
                to = argv[optind];
                System.out.println("To: " + to);
            }
            else {
                System.out.print("To: ");
                System.out.flush();
                to = in.readLine();
            }
            if (subject == null) {
                System.out.print("Subject: ");
                System.out.flush();
                subject = in.readLine();
            }
            else {
                System.out.println("Subject: " + subject);
            }

            Properties props = System.getProperties();
            // XXX - could use Session.getTransport() and Transport.connect()
            // XXX - assume we're using SMTP
            if (mailhost != null) props.put("mail.smtp.host", mailhost);

            // Get a Session object
            Session session = Session.getInstance(props, null);
            if (debug) session.setDebug(true);

            // construct the message
            Message msg = new MimeMessage(session);

            if (from != null)
                msg.setFrom(new InternetAddress(from));
            else
                msg.setFrom();

            if (reply_to_list == null && replyTo != null) {
                reply_to_list = new InternetAddress[1];
                reply_to_list[0] = new InternetAddress(replyTo);
                msg.setReplyTo(reply_to_list);
            }
            else
                msg.setReplyTo(reply_to_list);

            if (dis_list == null) {
                dis_list = new InternetAddress[1];
                dis_list[0] = new InternetAddress(to);
            }

            msg.setRecipients(Message.RecipientType.TO, dis_list);
            if (cc != null) msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            if (bcc != null) msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));

            // in-line file contents if specified
            if (inline_filename != null) {
                msg_text = readFile(inline_filename);
            }

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(msg_text);

            // create the Multipart and add the text part
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);

            // create additional message part(s)

            // attach the file or files to the message
            if (filename != null) {
                MimeBodyPart mbp = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(filename);
                mbp.setDataHandler(new DataHandler(fds));
                mbp.setFileName(fds.getName());
                mp.addBodyPart(mbp);
                mbp1.setText(msg_text + "\n\nAttachment: " + filename);
                System.out.println("Added attachment: " + filename);
            }

            if (attachments != null) {
                Iterator i = attachments.iterator();
                StringBuffer list = null;
                while (i.hasNext()) {
                    String name = (String) i.next();
                    MimeBodyPart mbp = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(name);
                    mbp.setDataHandler(new DataHandler(fds));
                    mbp.setFileName(fds.getName());
                    mp.addBodyPart(mbp);
                    if (list == null) {
                        list = new StringBuffer(name);
                    }
                    else {
                        list.append(", " + name);
                    }
                    System.out.println("Added attachment: " + name);
                    mbp1.setText(msg_text + "\nAttachment(s): " + list);
                }
            }

            // add the Multipart to the message
            msg.setContent(mp);

            msg.setSubject(subject);

            // jgfrun collect(in, msg);

            msg.setHeader("X-Mailer", mailer);
            msg.setSentDate(new Date());

            // send the thing off
            Transport.send(msg);

            System.out.println("Mail was sent successfully.");

            // Keep a copy, if requested.
            if (record != null) {

                // Get a Store object
                Store store = null;
                if (url != null) {
                    URLName urln = new URLName(url);
                    store = session.getStore(urln);
                    store.connect();
                }
                else {
                    if (protocol != null)
                        store = session.getStore(protocol);
                    else
                        store = session.getStore();

                    // Connect
                    if (host != null || user != null || password != null)
                        store.connect(host, user, password);
                    else
                        store.connect();
                }

                // Get record Folder. Create if it does not exist.
                Folder folder = store.getFolder(record);
                if (folder == null) {
                    System.err.println("Can't get record folder.");
                    System.exit(1);
                }
                if (!folder.exists()) folder.create(Folder.HOLDS_MESSAGES);

                Message[] msgs = new Message[1];
                msgs[0] = msg;
                folder.appendMessages(msgs);

                System.out.println("Mail was recorded successfully.");
            }

        }
        catch (Exception e) {
            System.err.println("Could not MsgSend.doit");
            e.printStackTrace();
        }
    } // doit

    private String readFile(String inline_filename) throws FileNotFoundException, IOException {
        String msg_text;
        BufferedReader br = new BufferedReader(new FileReader(inline_filename));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    public void collect(BufferedReader in, Message msg) throws MessagingException, IOException {

        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }

        // If the desired charset is known, you can use
        // setText(text, charset)
        msg.setText(sb.toString());
    }
}
