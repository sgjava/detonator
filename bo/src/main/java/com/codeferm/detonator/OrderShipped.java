/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generate order shipped info using template. This could be used to generate email, HTML, etc. This is just an example of post
 * order creation. Obviously much more complicated stuff goes on,
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderShipped {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrderShipped.class);
    /**
     * Multi threaded executor service.
     */
    private final ExecutorService executor;
    /**
     * Orders BO.
     */
    private OrdersBo ordersBo;
    /**
     * Template to use.
     */
    private String template;
    /**
     * Output dir for shipment statements.
     */
    private String outputDir;
    /**
     * FreeMarker configuration singleton.
     */
    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);

    /**
     * Construct with template dir, OrdersBo and max threads.
     *
     * @param templateDir Template directory.
     * @param template Template to use.
     * @param outputDir Output dir for shipment statements.
     * @param ordersBo Orders BO.
     * @param maxThreads Maximum threads.
     */
    public OrderShipped(final String templateDir, final String template, final String outputDir, final OrdersBo ordersBo,
            final int maxThreads) {
        this.template = template;
        this.outputDir = outputDir;
        this.ordersBo = ordersBo;
        executor = Executors.newFixedThreadPool(maxThreads, new ThreadFactoryBuilder().setNameFormat("order-shipped-%d").build());
        try {
            configuration.setDirectoryForTemplateLoading(new File(templateDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
    }

    /**
     * Generate order shipped template. Pass in the Writer required for a particular purpose.
     *
     * @param dto Orders DTO.
     * @param writer Template output.
     */
    public void shipTemplate(final Orders dto, final Writer writer) {
        final var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        // Template model
        final Map<String, Object> model = ordersBo.orderInfo(dto.getOrderId());
        model.put("now", LocalDateTime.now().format(formatter));
        // Process DTO template
        try {
            final var temp = configuration.getTemplate(template);
            temp.process(model, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Ship order generates a plain text shipping manifest, but you would be dealing with a shipping system in the real world.
     *
     * @param dto Orders DTO.
     */
    public void shipOrder(final Orders dto) {
        final Runnable task = () -> {
            try {
                try (var out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format("%s/%d.txt",
                        outputDir, dto.getOrderId())), false), StandardCharsets.UTF_8))) {
                    shipTemplate(dto, out);
                }
            } catch (IOException e) {
                throw new RuntimeException("shipOrder", e);
            }
        };
        executor.execute(task);
    }

    /**
     * Wait for queued threads to finish.
     */
    public void shutdown() {
        // Shutdow executor service
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
