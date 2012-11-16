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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class App
{

  public static void main(String[] args)
  {
    if (args.length < 1 || args[0] == null || args[0].length() < 1) {
      System.err.println("Error: Usage: mailbucket <directoryForMail> " +
        "[<port>]");
      System.err.println("  (port defaults to 60025 if not provided)");
      System.exit(1);
    }

    File f = new File(args[0]);
    if (!f.isDirectory() || !f.canWrite()) {
      System.err.println("Error: " + args[0] + " is not a writable directory!");
      System.exit(1);
    }

    int port = 60025;
    if (args.length >= 2) {
      try {
        port = Integer.parseInt(args[1]);
      } catch (Exception e) {
        System.err.println("Invalid Port Number: " + e.getMessage());
	System.exit(1);
      }
    }
    BucketServer bs = new BucketServer(f, port);
  }
}
