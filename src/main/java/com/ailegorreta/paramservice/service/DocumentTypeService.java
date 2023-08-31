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
 *  DocumentTypeService.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.service;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.paramservice.domain.DocumentType;
import com.ailegorreta.paramservice.domain.DocumentTypeRepository;
import com.ailegorreta.paramservice.gql.types.DocumentTypeInput;
import com.ailegorreta.resourceserver.utils.UserContext;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Mutation service for DocumentTypes
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
@Service
@RequiredArgsConstructor
public class DocumentTypeService implements HasLogger {

    final DocumentTypeRepository documentTypeRepository;
    final EventService eventService;

    public DocumentType addDocumentType(DocumentTypeInput documentTypeInput) {
        // validate uniqueness
        var documentType = documentTypeRepository.findDocumentTypeByName(documentTypeInput.getName());

        Validate.isTrue(documentType == null,"El tipo de documento YA existe en el catálogo");

        documentType = new DocumentType(null, documentTypeInput.getName(), documentTypeInput.getExpiration());

        eventService.sendEvent(UserContext.getCorrelationId(),
                               documentTypeInput.getUserModify(),"ANADE_TIPO_DOCUMENTO", documentType);
        getLogger().info("Add a new document type " + documentType.getName());

        return documentTypeRepository.save(documentType);
    }

    public DocumentType updateDocumentType(DocumentTypeInput documentTypeInput) {
        // Validate that the documentType already exists
        var documentType = documentTypeRepository.findById(documentTypeInput.getId());

        Validate.isTrue(documentType.isPresent(), "El tipo de documento ya NO existe en el catálogo.");

        documentType.get().setName(documentTypeInput.getName());
        documentType.get().setExpiration(documentTypeInput.getExpiration());

        eventService.sendEvent(UserContext.getCorrelationId(), documentTypeInput.getUserModify(),
                "MODIFICA_TIPO_DOCUMENTO", documentType.get());
        getLogger().info("Update a document type " + documentType.get().getName());

        return documentTypeRepository.save(documentType.get());
    }

    public DocumentType deleteDocumentType(UUID id, String user) {
        // Validate that the documentType still exists
        var documentType = documentTypeRepository.findById(id);

        Validate.isTrue(documentType.isPresent(), "El tipo de documento ya NO existe en el catálogo.");

        documentTypeRepository.deleteById(id);

        eventService.sendEvent(UserContext.getCorrelationId(),user,
                "ELIMINA_TIPO_DOCUMENTO", documentType.get());
        getLogger().info("Delete document type " + documentType.get().getName() + " user:" + user);

        return documentType.get();
    }


    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }
}
