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
 *  SystemDateService.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.service;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.paramservice.domain.DayType;
import com.ailegorreta.paramservice.domain.SystemDate;
import com.ailegorreta.paramservice.domain.SystemDateRepository;
import com.ailegorreta.paramservice.gql.types.SystemDateInput;
import com.ailegorreta.resourceserver.utils.UserContextHolder;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Mutation service for System Dates
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
@Service
@RequiredArgsConstructor
public class SystemDateService implements HasLogger {
    final SystemDateRepository systemDateRepository;
    final EventService          eventService;

    public SystemDate addSystemDate(SystemDateInput systemDateInput)  {
        if (systemDateInput.getName() != DayType.FESTIVO) {
            // validate uniqueness
            var systemDate = systemDateRepository.findSystemDateByName(systemDateInput.getName());

            Validate.isTrue(systemDate == null,"Las fecha del sistema YA existe en el catálogo");
        }
        var systemDate = new SystemDate(null, systemDateInput.getName(), systemDateInput.getDay());

        eventService.sendEvent(UserContextHolder.getContext().getCorrelationId(), systemDateInput.getUserModify(),
                "ANADE_FECHA_SISTEMA", systemDate);
        getLogger().info("Add a new system date " + systemDate.getName());

        return systemDateRepository.save(systemDate);
    }

    public SystemDate updateSystemDate(SystemDateInput systemDateInput)  {
        // Validate that the systemDate already exists
        var systemDate = systemDateRepository.findById(systemDateInput.getId());

        Validate.isTrue(systemDate.isPresent(), "La fecha del sistema ya NO existe en el catálogo.");

        systemDate.get().setName(systemDateInput.getName());
        systemDate.get().setDay(systemDateInput.getDay());

        eventService.sendEvent(UserContextHolder.getContext().getCorrelationId(), systemDateInput.getUserModify(),
                "MODIFICA_FECHA_SISTEMA", systemDate.get());
        getLogger().info("Update a new system date " + systemDate.get().getName());

        return systemDateRepository.save(systemDate.get());
    }

    public SystemDate deleteSystemDate(UUID id, String user) {
        // Validate that the systemDate still exists
        var systemDate = systemDateRepository.findById(id);

        Validate.isTrue(systemDate.isPresent(), "La fecha del sistema ya NO existe en el catálogo.");

        systemDateRepository.deleteById(id);

        eventService.sendEvent(UserContextHolder.getContext().getCorrelationId(),user,
                "ELIMINA_FECHA_SISTEMA", systemDate.get());
        getLogger().info("Delete a new system date " + systemDate.get().getName() + " user:" + user);

        return systemDate.get();
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }
}
