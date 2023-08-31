/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  DataInitializer.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.paramservice.domain.*;
import com.ailegorreta.paramservice.service.EventService;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Initialize default dates and some rates form demo purpose
 *
 * @project param-service
 * @autho rlh
 * @date August 2023
 */
@Component
class DataInitializer implements ApplicationRunner, HasLogger {


    final SystemDateRepository systemDateRepository;
    final SystemRateRepository systemRateRepository;
    final DocumentTypeRepository documentTypeRepository;
    final EventService eventService;

    public DataInitializer(SystemDateRepository systemDateRepository,
                           SystemRateRepository systemRateRepository,
                           DocumentTypeRepository documentTypeRepository,
                           EventService eventService) {
        this.systemDateRepository = systemDateRepository;
        this.systemRateRepository = systemRateRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.eventService = eventService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (this.systemDateRepository.findAll().size() == 0) {
            getLogger().info("Base de datos vacía, se inicializan algunos parámetros...");
            systemDateRepository.deleteAll();
            systemRateRepository.deleteAll();
            documentTypeRepository.deleteAll();
            getLogger().info("se borran todos los registros");

            List<SystemDate> dates = new ArrayList<>();
            var today = LocalDate.now();

            dates.add(SystemDate.builder().name(DayType.HOY).day(today).id(UUID.randomUUID()).build());
            dates.add(SystemDate.builder().name(DayType.MANANA).day(today.plusDays(1)).build());
            dates.add(SystemDate.builder().name(DayType.AYER).day(today.minusDays(1)).build());
            dates.add(SystemDate.builder().name(DayType.FESTIVO).day(today.plusDays(10)).build());
            dates.add(SystemDate.builder().name(DayType.FESTIVO).day(today.plusDays(20)).build());
            systemDateRepository.saveAll(dates);

            List<SystemRate> rates = new ArrayList<>();

            rates.add(SystemRate.builder().name("TIIF").rate(new BigDecimal(4.56)).build());
            rates.add(SystemRate.builder().name("MXN-DLR").rate(new BigDecimal( 20.15)).build());
            rates.add(SystemRate.builder().name("MXN-YEN").rate(new BigDecimal( 0.0013)).build());
            systemRateRepository.saveAll(rates);

            List<DocumentType> documents = new ArrayList<>();

            documents.add(DocumentType.builder().name("Licencia").expiration("3m").build());
            documents.add(DocumentType.builder().name("Credencial elector").expiration("6m").build());
            documents.add(DocumentType.builder().name("Pasaporte").expiration("12m").build());
            documents.add(DocumentType.builder().name("Comprobante domicilio").expiration("3m").build());
            documents.add(DocumentType.builder().name("Visa").expiration("12m").build());
            documents.add(DocumentType.builder().name("Pasaporte extranjero").expiration("6m").build());
            documents.add(DocumentType.builder().name("Contrato").expiration("NA").build());
            documentTypeRepository.saveAll(documents);

            systemDateRepository.findAll().forEach(p -> getLogger().info("dates: {}", p));
            systemRateRepository.findAll().forEach(p -> getLogger().info("rates: {}", p));
            documentTypeRepository.findAll().forEach(p -> getLogger().info("documents: {}", p));
            getLogger().info("done data initialization...");
            try {
                eventService.sendEvent("NA", "NA", "INICIALIZA DB ParamDB", dates);
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().warn("No se pudo enviar el mensaje de INICIALIZA DB ParamDB");
            }
        }
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }

}
