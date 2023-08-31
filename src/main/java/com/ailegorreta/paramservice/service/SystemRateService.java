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
 *  SystemRateService.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.service;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.paramservice.domain.SystemRate;
import com.ailegorreta.paramservice.domain.SystemRateRepository;
import com.ailegorreta.paramservice.gql.types.SystemRateInput;
import com.ailegorreta.resourceserver.utils.UserContext;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Mutation service for System Rates
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
@Service
@RequiredArgsConstructor
public class SystemRateService implements HasLogger {
    final SystemRateRepository systemRateRepository;
    final EventService          eventService;

    public SystemRate addSystemRate(SystemRateInput systemRateInput)  {
        // validate uniqueness
        var systemRate = systemRateRepository.findSystemRateByName(systemRateInput.getName());

        Validate.isTrue(systemRate == null,"Las tasa del sistema YA existe en el catálogo");

        systemRate = new SystemRate(null, systemRateInput.getName(), systemRateInput.getRate());

        eventService.sendEvent(UserContext.getCorrelationId(),
                systemRateInput.getUserModify(),"ANADE_VARIABLE_SISTEMA", systemRate);
        getLogger().info("Add a new system variable " + systemRate.getName());

        return systemRateRepository.save(systemRate);
    }

    public SystemRate updateSystemRate(SystemRateInput systemRateInput)  {
        // Validate that the systemRate already exists
        var systemRate = systemRateRepository.findById(systemRateInput.getId());

        Validate.isTrue(systemRate.isPresent(), "La tasa en el sistema ya NO existe en el catálogo.");

        systemRate.get().setName(systemRateInput.getName());
        systemRate.get().setRate(systemRateInput.getRate());

        eventService.sendEvent(UserContext.getCorrelationId(), systemRateInput.getUserModify(),
                "MODIFICA_VARIABLE_SISTEMA", systemRate.get());
        getLogger().info("Update a new system variable " + systemRate.get().getName());

        return systemRateRepository.save(systemRate.get());
    }

    public SystemRate deleteSystemRate(UUID id, String user) {
        // Validate that the systemRate still exists
        var systemRate = systemRateRepository.findById(id);

        Validate.isTrue(systemRate.isPresent(), "La tasa en el sistema ya NO existe en el catálogo.");

        systemRateRepository.deleteById(id);

        eventService.sendEvent(UserContext.getCorrelationId(),
                user,"BORRA_VARIABLE_SISTEMA", systemRate.get());
        getLogger().info("Delete a new system variable " + systemRate.get().getName() + " user:" + user);

        return systemRate.get();
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }

}
