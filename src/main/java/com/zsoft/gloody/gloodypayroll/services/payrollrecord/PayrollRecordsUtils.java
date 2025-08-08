package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PayrollRecordsUtils {


    public static byte[] convertHtmlToDoc(String htmlContent) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(baos);
            builder.run();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
        return baos.toByteArray();
    }

    public static byte[] generateDocFromTemplate(String templateName, Object data) throws IOException {

        String processedContent = processHandlebarsTemplate(templateName, data);

        return convertHtmlToDoc(processedContent);
    }

    public static String processHandlebarsTemplate(String templateName, Object data) throws IOException {
        TemplateLoader loader = new ClassPathTemplateLoader("/data/pay-records", ".html");
        Handlebars handlebars = new Handlebars(loader);
        Template template = handlebars.compile(templateName);
        return template.apply(data);
    }
}
