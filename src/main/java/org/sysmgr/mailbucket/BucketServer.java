/*
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the copyright
 * notice below and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * Copyright (c) 2012 Joshua M. Clulow <josh@sysmgr.org>
 */
package org.sysmgr.mailbucket;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

public class BucketServer
{

  private File dir;
  private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
  private SMTPServer smtp;
  private Session s = Session.getDefaultInstance(new Properties());

  public BucketServer(File dir, int port)
  {
    smtp = new SMTPServer(new SimpleMessageListenerAdapter(new BucketMessageHandler()));
    smtp.setPort(port);
    smtp.start();
    this.dir = dir;
  }

  private class BucketMessageHandler implements SimpleMessageListener
  {

    public boolean accept(String from, String recipient)
    {
      return true;
    }

    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
    {
      Date d = new Date();
      String fn_s = sdf.format(d);
      String fn_e = ".txt";
      File dest = new File(dir, fn_s + fn_e);
      BufferedOutputStream bw = null;
      BufferedOutputStream aw = null;

      try {
        /* Parse MIME Message: */
        MimeMessage mm = new MimeMessage(s, data);

        /* Print Log Message: */
        String subject = mm.getSubject();
        System.err.println("RECEIVE FROM: " + from);
        System.err.println("          TO: " + recipient);
        System.err.println("        DATE: " + d.toString());
        System.err.println("    FILENAME: " + fn_s + fn_e);
        if (subject != null)
          System.err.println("     SUBJECT: " + subject);

        /* Attempt to find attachments: */
        if (mm.getContent() instanceof Multipart) {
          Multipart mp = (Multipart) mm.getContent();
          int mpc = mp.getCount();
          for (int ii = 0; ii < mpc; ii++) {
            BodyPart bp = mp.getBodyPart(ii);
            if ("attachment".equalsIgnoreCase(bp.getDisposition()) ||
                "inline".equalsIgnoreCase(bp.getDisposition())) {
              String fn_attach = bp.getFileName();
              if (fn_attach != null && fn_attach.length() > 0) {
                System.err.println("  ATTACHMENT: " + fn_attach + " (" +
                  bp.getContentType() + ")");

                /* Write Attachment to File */
                try {
                  File ad = new File(dir, fn_s + ".attachment." + fn_attach);
                  aw = new BufferedOutputStream(new FileOutputStream(ad, false));
                  bp.getDataHandler().writeTo(aw);
                  aw.flush();
                } catch (Exception e) {
                  System.err.println("ERROR: Could not write an attachment: " +
                    e.getMessage());
                } finally {
                  try {
                    aw.close();
                  } catch (Exception e) {}
                }
              }
            }
          }
        }
        System.err.println("------------");

        /* Write output to text file, containing entire message: */
        bw = new BufferedOutputStream(new FileOutputStream(dest, false));
        bw.write(("RECEIVE FROM: " + from + "\n").getBytes());
        bw.write(("          TO: " + recipient + "\n").getBytes());
        bw.write(("        DATE: " + d.toString() + "\n").getBytes());
        bw.write(("------------------------\n").getBytes());
        mm.writeTo(bw);
        bw.flush();
      } catch (Exception e) {
        System.err.println("ERROR: Could not parse and deliver MIME Message: " +
           e.getMessage());
      } finally {
        try { data.close(); } catch (Throwable t) {}
        try { bw.close(); } catch (Throwable t) {}
      }
    }
  }
}
