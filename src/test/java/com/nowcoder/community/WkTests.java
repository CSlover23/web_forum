package com.nowcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "F:/work/wkhtmltopdf/bin/wkhtmltopdf https://wkhtmltopdf.org/downloads.html F:/work/data/wkhtmltopdf/wk-pdfs/1.pdf";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
